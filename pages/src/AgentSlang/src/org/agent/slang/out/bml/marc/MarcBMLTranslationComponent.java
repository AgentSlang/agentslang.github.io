/*
 * Copyright (c) Ovidiu Serban, ovidiu@roboslang.org
 *               web:http://ovidiu.roboslang.org/
 * All Rights Reserved. Use is subject to license terms.
 *
 * This file is part of AgentSlang Project (http://agent.roboslang.org/).
 *
 * AgentSlang is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License and CECILL-B.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * The CECILL-B license file should be a part of this project. If not,
 * it could be obtained at  <http://www.cecill.info/>.
 *
 * The usage of this project makes mandatory the authors citation in
 * any scientific publication or technical reports. For websites or
 * research projects the AgentSlang website and logo needs to be linked
 * in a visible area.
 */

package org.agent.slang.out.bml.marc;

import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.agent.slang.data.audio.AudioData;
import org.agent.slang.data.audio.PlayerEvent;
//Sahba modification starts, Version 2
import org.agent.slang.dm.narrative.data.StateChangeData;
//Sahba modification ends, Version 2

import org.agent.slang.data.simple.BmlData;
import org.agent.slang.data.simple.behavior.bml.*;
import org.agent.slang.data.simple.behavor.eml.EmotionalAppraisal;

import org.agent.slang.dm.narrative.data.patterns.PatternsStates;
import org.agent.slang.out.bml.marc.socket.MarcSocket;
import org.agent.slang.out.bml.marc.socket.TCPSocket;
import org.agent.slang.out.bml.marc.socket.UDPSocket;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.LivingComponent;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import org.ib.logger.Logger;
import org.ib.utils.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;

import marytts.util.string.StringUtils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Provides final BML and EML messages and sends them to MARC toolkit for behavior representation. 
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @author sahba ZOJAJI, sahba.zojaji@insa-rouen.fr
 * @version 1, 10/19/13
 * 			2, 17/11/2016 - Customize Audio Path and adding the ability of playing user defined audio files plus machine generated voices, Sahba ZOJAJI
 */

@ConfigureParams(optionalConfigurationParams = {"MARCSocketType", "MARCHostname", "MARCInPort", "MARCOutPort"},
        outputChannels = "audioPlayer.data", outputDataTypes = PlayerEvent.class,
        inputDataTypes = {BmlData.class,AudioData.class, StateChangeData.class, StringData.class})
//public class MarcBMLTranslationComponent extends MixedComponent implements MarcSocket.DataListener {
public class MarcBMLTranslationComponent extends LivingComponent implements MarcSocket.DataListener {
  private static final String PROP_SOCKET_TYPE = "MARCSocketType";
    private static final String PROP_SOCKET_HOST = "MARCHostname";
    private static final String PROP_SOCKET_IN_PORT = "MARCInPort";
    private static final String PROP_SOCKET_OUT_PORT = "MARCOutPort";
    private static final String PROP_SOCKET_EML_IN_PORT = "MARCEMLInPort";
    private static final String PROP_CACHE = "audioCache";
    //Sahba modification starts, Version 2
    private static final String PROP_USER_AUDIO_PATH = "userAudioPath";
    private static final String PROP_IMAGE_PATH = "imagePath";
    private String userAudioPath = null;
    private String imageDirectory = null;
    private int newState=0;
    //Sahba modification ends, Version 2
    private static final String DEFAULT_CACHE = "MARCAudioCache";
    //topics
    private static final String audioPlayerData = "audioPlayer.data";
    
    
    private static final String BML_FILE_PATH = "bmlFilePath";
    private File bmlFilePath;
    private boolean bmlExecuting;
    private boolean existStepBml;
    private Document bmlFile;
    private boolean emlExecuting;

    private int counterSeconds; 
    
    private final static long EXECUTION_TIMEOUT = 6* 1000;
    private final Queue<String> bmlExecutionQueue = new LinkedList<String>();
    private final Queue<String> emlExecutionQueue = new LinkedList<String>();

    //MARC socket
    private MarcSocket socket;
    private File audioCache;
    private long bmlID = 0;
    private Long lastExecutionTimestamp = null;
    
    private Boolean isSmiling=false;
    private boolean isIdleExecution= true;


    public MarcBMLTranslationComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        String hostname = "localhost";
        int intPort = 4100;
        int outPort = 4111;
        int emlInPort = 4012;

        if (config.hasProperty(PROP_SOCKET_HOST)) {
            hostname = config.getProperty(PROP_SOCKET_HOST);
        }

        if (config.hasProperty(PROP_SOCKET_IN_PORT)) {
            try {
                intPort = Integer.parseInt(config.getProperty(PROP_SOCKET_IN_PORT));
            } catch (NumberFormatException e) {
                //Logger.log(this, Logger.CRITICAL, "Invalid inPort provided", e);
            }
        }

        if (config.hasProperty(PROP_SOCKET_OUT_PORT)) {
            try {
                outPort = Integer.parseInt(config.getProperty(PROP_SOCKET_OUT_PORT));
            } catch (NumberFormatException e) {
                //Logger.log(this, Logger.CRITICAL, "Invalid outPort provided", e);
            }
        }
        if (config.hasProperty(PROP_SOCKET_EML_IN_PORT)) {
            try {
                emlInPort = Integer.parseInt(config.getProperty(PROP_SOCKET_EML_IN_PORT));
            } catch (NumberFormatException e) {
                //Logger.log(this, Logger.CRITICAL, "Invalid outPort provided", e);
            }
        }
        //Logger.log(this, Logger.INFORM, String.format("Using MARC Connections Properties hostname='%s' inPort=%d outPort=%d", hostname, intPort, outPort));
        System.out.println(String.format("Using MARC Connections Properties hostname='%s' inPort=%d outPort=%d", hostname, intPort, outPort));

        if ("tcp".equals(config.getProperty(PROP_SOCKET_TYPE))) {
            try {
            	//socket = new TCPSocket(hostname, intPort, outPort, this);
                socket = new TCPSocket(hostname, intPort, outPort, emlInPort, this);
            } catch (IOException e) {
                //Logger.log(this, Logger.CRITICAL, "Invalid properties for socket", e);
            }
        } else {
            try {
              //  socket = new UDPSocket(hostname, intPort, outPort, this);
            	socket = new UDPSocket(hostname, intPort, outPort, emlInPort, this);
            } catch (IOException e) {
                //Logger.log(this, Logger.CRITICAL, "Invalid properties for socket", e);
            }
        }
        
        //Sahba modification starts, Version 2
        if (config.hasProperty(PROP_USER_AUDIO_PATH)) {
        	userAudioPath = config.getFileProperty(PROP_USER_AUDIO_PATH).getAbsolutePath()+"\\";
        	//Logger.log(this, Logger.INFORM, "MARC Audio Path = " + userAudioPath);
        	System.out.println("MARC Audio Path = " + userAudioPath);
        }
        else
        {
        //Sahba modification ends, Version 2
        	audioCache = FileUtils.createCacheDir(this, config.getFileProperty(PROP_CACHE, DEFAULT_CACHE, false));
        	//Logger.log(this, Logger.INFORM, "MARC Audio Cache Path = " + audioCache.getAbsolutePath());
        	System.out.println("MARC Audio Cache Path = " + audioCache.getAbsolutePath());
        //Sahba modification starts, Version 2
        }
        if (config.hasProperty(PROP_IMAGE_PATH)) {
        	imageDirectory = config.getFileProperty(PROP_IMAGE_PATH).getAbsolutePath()+"\\";
        	//Logger.log(this, Logger.INFORM, "MARC SLIDE image Path = " + imageDirectory);
        	System.out.println("MARC Story SLIDE image Path = " + imageDirectory);
        }
        //Sahba modification ends, Version 2
        
        
    	this.bmlFilePath = config.getFileProperty(BML_FILE_PATH, "data", true);
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setIgnoringElementContentWhitespace(true);
            bmlFile = builder.build(new File(bmlFilePath,"autoBehaviors.xml"));
            System.out.println("MARC BML file path = " + bmlFilePath);
          } catch (JDOMException e) {
            e.printStackTrace(System.out);
          } catch (IOException e) {
            e.printStackTrace(System.out);
          }
       
        bmlExecuting = false;
        existStepBml = false;
        emlExecuting = false;
        
        
        
    }
  
    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
//        addInboundTypeChecker(StringData.class);
        addInboundTypeChecker(AudioData.class);
        //Sahba modification starts, Version 2
        addInboundTypeChecker(StateChangeData.class);
        //Sahba modification ends, Version 2
        addInboundTypeChecker(BmlData.class);
        addInboundTypeChecker(StringData.class);
    }

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(audioPlayerData, PlayerEvent.class);
    }

    /**
     * Processes received data
     * @param message received data in terms of a text message
     */
    public void dataReceived(String message) {
    	
        try{                    
	    		Writer output;
	    		output = new BufferedWriter(new FileWriter("CommandsLogFile.txt", true));
	    		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    		Date date = new Date();
	    		
	    		output.append("\nEmlExecuting = "+ emlExecuting+ "\n<MarcMSG>"+dateFormat.format(date)+ " \n "+ message +"</MarcMSG>\n");
	    		output.close();
           }
           catch (IOException e)
	    	{
	    		System.out.println("\nError: File writer crashed!\n");
	    		//Logger.log(this, Logger.CRITICAL, "Error in MARC BML Translation Component log file management!", e); 
	    	}
        //Logger.log(this, Logger.INFORM, "From MARC: " + message);
        if (message.toLowerCase().contains("trackbml:start")) {
        	publishData(audioPlayerData, new PlayerEvent(0, PlayerEvent.EVENT_START));
           
            System.out.println("Sending player start message !");
        }
        if (message.toLowerCase().contains("trackbml:end")) {
         /*   if(emlExecuting){
            	emlExecuting = false;
            }
            else
           */ {
                 //	System.out.println("Sending player stop message !");
            publishData(audioPlayerData, new PlayerEvent(0, PlayerEvent.EVENT_STOP));
            System.out.println("Sending player stop message !");
            bmlExecuting = false;
            stopAndScheduleNext();
            }
           // scheduleNextBML();
        }
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
    	if (data instanceof BmlData)
    	{
    	//	System.out.println("\n\n received BmlData############################################Bml data:\n\n ");
    	  	
    		handleBmlData((BmlData)data);
    	}
    	
    	//Sahba modification starts, Version 2
    	if (data instanceof StateChangeData) {    		  
    		newState = ((StateChangeData) data).getNewStateNumber();
    		if (userAudioPath != null && newState < 0)
	        {
    			String bmlID = generateBMLID();
	        	String message="";
		        message = generateBML(bmlID, userAudioPath+String.valueOf(newState)+".wav");
	        	scheduleForExecutionMessage(message);
		      
	        }
    		
    	}
    	
    	if (data instanceof AudioData) { 
    	//Sahba modification ends, Version 2
    		String bmlID = generateBMLID();
    		//Sahba modification starts, Version 2
	        if (userAudioPath != null)
	        {
	        	String message="";
	        	String str = data.toString();
	        	if(str.length()>1){
		        	String strSign = str.substring(0,1);
		        	//if it is not understood by the agent 
		        	if (strSign.equals("#")){
		        		String strVoiceNo = str.substring(1,2);
		        		message = generateBML(bmlID, userAudioPath+"OUT"+strVoiceNo+".wav");
		        		//System.out.println("\n*****ZJ,MARC BML TRANSLATION, message:      "+message);
		        	}
		        	//if there is no response from the child side 
		        	else if (strSign.equals("*")){
		        		String strVoiceNo = str.substring(1,2);
		        		message = generateBML(bmlID, userAudioPath+"DEF"+strVoiceNo+".wav");
		        		//System.out.println("\n*****ZJ,MARC BML TRANSLATION, message:      "+message);
		        	}
		        }
	        	//telling the story
	        	if (message == "")
	            // message = generateBMLAudio(data.toString(), userAudioPath+newState ,"wav");
	        	message = generateBML(bmlID, userAudioPath+newState+".wav");
	        	scheduleForExecutionMessage(message);
	        	}
	        else
	        {
	        //Sahba modification ends, Version 2
		        String audioPath = saveToFile(bmlID, (AudioData) data);
		        if (audioPath != null) {
		            String message = generateBML(bmlID, audioPath);
		            scheduleForExecutionMessage(message);
		          //  appendMessageForExecution(message);
		        }
		    //Sahba modification starts, Version 2
	        }
    	}
    	//Sahba modification ends, Version 2
    	
    	if (data instanceof StringData) {
    		if (userAudioPath != null)
	        {    		
    			String bmlID = generateBMLID();
	        	String message="";
	        	String str = ((StringData)data).getData();
	        	if(str.length()>1){
		        	String strSign = str.substring(0,1);
		        	
		        	//if it is not understood by the agent 
		        	if (strSign.equals("#")){
		        		String strVoiceNo = str.substring(1,str.indexOf(" "));
		        		message = generateBML(bmlID, userAudioPath+"OUT"+strVoiceNo+".wav");
		        		//System.out.println("\n*****ZJ,MARC BML TRANSLATION, message:      "+message);
		        		scheduleForExecutionMessage(message);
		        		// appendMessageForExecution(message);
		        	}
		        	//if there is no response from the child side 
		        	else if (strSign.equals("*")){
		        		String strVoiceNo = str.substring(1,str.indexOf(" "));
		        		message = generateBML(bmlID, userAudioPath+"DEF"+strVoiceNo+".wav");
		        		//System.out.println("\n*****ZJ,MARC BML TRANSLATION, message:      "+message);
		        		scheduleForExecutionMessage(message);
		        		// appendMessageForExecution(message);
		        	}
		        	//for generating human voice for text on the edges of story graph
		        	else if (strSign.equals("_")){
		        		String strVoiceNo = str.substring(1,str.indexOf(" "));
		        		//System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n*****ZJ,MARC BML TRANSLATION, strVoiceNo:      "+strVoiceNo);
		        		message = generateBML(bmlID, userAudioPath+"EDG"+strVoiceNo+".wav");
		        		//System.out.println("\n*****ZJ,MARC BML TRANSLATION, message:      "+message);
		        		scheduleForExecutionMessage(message);
		        		// appendMessageForExecution(message);
		        	}
		        }
	        }
    	}
    }

    /**
     * Schedules for sending BML messages to MARC toolkit in to be executed there.
     * @param message BML string message
     */
    private void scheduleForExecutionMessage(String message) {
        synchronized (bmlExecutionQueue) {
            bmlExecutionQueue.offer(message);
             scheduleNextBML();
           // stopAndScheduleNext();
        }
    }
    
    /**
     * Sends message to MARC toolkit.
     * @param message BML message
     */
    private void pushMessageForExecution(String message) {
    	if (message != null) {
        	if (socket != null) {
                   	this.isSmiling = true;
		    		socket.sendMessage(message);	                        
            }
        } 
    }

    /**
     * Appends a message to BML queue for sending BML messages to MARC toolkit in to be executed there.
     * @param message BML string message
     */
    private void appendMessageForExecution(String message) {
        synchronized (bmlExecutionQueue) {
            bmlExecutionQueue.offer(message);
             scheduleNextBML();
            //stopAndScheduleNext();
        }
    }
    
    /**
     * Schedules next BML to be sent to MARC toolkit.
     */
    private void scheduleNextBML() {
        synchronized (bmlExecutionQueue) { 
            if (lastExecutionTimestamp == null || System.currentTimeMillis() - lastExecutionTimestamp > EXECUTION_TIMEOUT) {
                String message = bmlExecutionQueue.poll();
                if (message != null) {
                	System.out.println("***********ZJ,MARC BML TRANSLATION, message:      "+message);
                	if (socket != null) {
                        //Logger.log(this, Logger.INFORM, "MARC Sending message: " + message);
                        
                        try{                    
	                        Writer output;
	    		    		output = new BufferedWriter(new FileWriter("CommandsLogFile.txt", true));
	    		    		
	    		    		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    		    		Date date = new Date();
	    		    		output.append("<bmlStartTime>"+dateFormat.format(date)+"</bmlStartTime>"+message);
	    		    		output.close();
	    		    		
	    		    		this.bmlExecuting = true;
	    		    		this.isSmiling = false;
	    		    		this.isIdleExecution = false;
	    		    		socket.sendMessage(message);	                        
	                        lastExecutionTimestamp = System.currentTimeMillis();
                        }
                        catch (IOException e)
        		    	{
        		    		System.out.println("\nError: File writer crashed!\n");
        		    		//Logger.log(this, Logger.CRITICAL, "Error in MARC BML Translation Component log file management!", e); 
        		    	}
                    } else {
                        //Logger.log(this, Logger.INFORM, "Marc Sending message failed: " + message);
                    }
                } else {
                    lastExecutionTimestamp = null;
                }
            }
        }
    }
    
    /**
     * Stops current process and schedules for next BML to be sent to MARC toolkit.
     */
    private void stopAndScheduleNext() {
    	 bmlExecuting = false;
        synchronized (bmlExecutionQueue) {
            lastExecutionTimestamp = null;
            scheduleNextBML();
        }
    }

    /**
     * Schedules for sending EML messages to MARC toolkit in to be executed there.
     * @param message EML string message
     */
    private void scheduleForEMLMessage(String message) {
        synchronized (emlExecutionQueue) {
            emlExecutionQueue.offer(message);
            scheduleNextEML();
        }
    }

    /**
     * Schedules next EML to be sent to MARC toolkit.
     */
    private void scheduleNextEML() {
        synchronized (emlExecutionQueue) {
            if (lastExecutionTimestamp == null || System.currentTimeMillis() - lastExecutionTimestamp > EXECUTION_TIMEOUT) {
                String message = emlExecutionQueue.poll();
                if (message != null) {
                    if (socket != null) {
                        //Logger.log(this, Logger.INFORM, "MARC EML Sending message: " + message);
                        		    		
                        socket.sendMessage(message);	                        
	                    lastExecutionTimestamp = System.currentTimeMillis();                                      
                    } else {
                        //Logger.log(this, Logger.INFORM, "Marc Sending message failed: " + message);
                    }
                } else {
                    lastExecutionTimestamp = null;
                }
            }
        }
    }

    /**
     * Stops current process and schedules for next EML to be sent to MARC toolkit.
     */
    private void stopAndScheduleNextEml() {
        synchronized (emlExecutionQueue) {
            lastExecutionTimestamp = null;
            scheduleNextEML();
        }
    }

    /**
     * Generates BML ID
     * @return BML ID
     */
    private String generateBMLID() {
        return "bml-" + (bmlID++);
    }

    /**
     * Saves output audio data generated by TTS into a file 
     * @param bmlID BML ID
     * @param data audio data
     * @return audio file path
     */
    private String saveToFile(String bmlID, AudioData data) {
        File audioFileData = new File(audioCache, "audio-" + bmlID + ".wav");
        try {
            AudioSystem.write(data.getAudioStream(), AudioFileFormat.Type.WAVE, audioFileData);
            return audioFileData.getAbsolutePath();
        } catch (IOException e) {
            //Logger.log(this, Logger.CRITICAL, "Invalid save path = " + audioFileData.getAbsolutePath());
            return null;
        }
    }

    /**
     * Generates BML message
     * @param bmlID BML ID
     * @param audioPath audio file path
     * @return BML message
     */
    private String generateBML(String bmlID, String audioPath) {
        String bmlStringData =  "<bml id=\"" +"trackbml"+ "\">";
        if (audioPath.contains("OUT")){
        	try{
               if(bmlFile!=null)
               { 		
		        	Element rootElement = bmlFile.getRootElement();
		        	List<Element> children = rootElement.getChildren("bml");
		
		            Iterator iterator = children.iterator();
		            while (iterator.hasNext()) {
		            	Element child = (Element) iterator.next();
		            	
		            	if (child.getAttribute("id").getValue().equals("bml-not_understood")){
		            		
		            		List<Element> childNodes = child.getChildren();	
		            	
		            		Iterator childIterator = childNodes.iterator();
		                     while (childIterator.hasNext()) {
		                     	Element childElement = (Element) childIterator.next();
		                     	bmlStringData+=  generateBML(childElement);
		                     }
		            	}
		            } 
		        }
		      }
        	  catch(Exception e){
        	  }
        }  
        
        if(audioPath !="")
        {
        	bmlStringData+= "<speech id=\"" + bmlID + "\" marc:file=\"" + audioPath + "\" marc:articulate=\"1.00\" />"+
        	"marc:"+"articulate=\"" + 0.5 + "\""+ "\n";
        	//bmlStringData+= "<wait duration=\"1\" />";
        	bmlStringData+= "</bml>";
        }  	
        
        return bmlStringData; 
        }
    

    /**
     * Generates and adds BML audio playing command to a BML message
     * @param bml BML message
     * @param speechTxt text related to the audio file 
     * @param wavFilename wave file name and path
     * @param TTS_wav TTS generated files path 
     * @return BML message
     */
    private String generateBMLAudio(Bml bml, String speechTxt, String wavFilename,String TTS_wav)
    {
		SpeechPlayer speechplayer = new SpeechPlayer();
		bml= speechplayer.addPlaySpeech(bml,speechTxt,wavFilename,0.5,TTS_wav);// delay 0.5s to synchronize this event with torso movement
       return bml.GetBmlContent(); //.replace("\n",  "\t");
    }
    
    /**
     * Managing input BML messages and preparing output final BML message in the class.
     * @param data BML data
     */
    private void handleBmlData(BmlData data){
  		//System.out.println("\n\n handleBmlData############################################Bml data:\n\n " + data.getData());
    	Bml bml = new Bml(0);
  		bml.StartBmlWriter(); 
     	
  		if(data.getGesture()!=""){
  			String gestureName = data.getGesture();
  			if (!gestureName.isEmpty())
  	  		{

  	  			bml.AddFork(new BmlFork("marc",bml.id,(float)0.0f));
  				bml.addPosture(new Posture("marc",gestureName,"Rest Pose","bml_gesture_item"));
  	  			bml.EndFork();
  				bml.AddFork(new BmlFork("marc",bml.id+6,(float)1.0));
  				bml.AddFace(new Face("marc","51",1.0f,1.0f,"BOTH"));
  				bml.EndFork();
  				bml.AddFork(new BmlFork("marc",bml.id+7,(float)2.0));
  				bml.AddFace(new Face("marc","52",1.1f,1.0f,"BOTH"));
  				bml.EndFork();

  				/* Used for Autonomous Agent version 1
  	  			bml.AddFork(new BmlFork("marc",bml.id,(float)0.0f));
  				bml.addPosture(new Posture("marc",gestureName,"Rest Pose","bml_gesture_item"));
  	  			bml.EndFork();
  				bml.AddFork(new BmlFork("marc",bml.id+6,(float)1.0));
  				bml.AddFace(new Face("marc","51",1.0f,1.0f,"BOTH"));
  				bml.EndFork();
  				*/  				
  	  		}
  	  		
  	  		
  		}
  		else{
  			
  			if(data.getIntention().trim()!="JA" /*data.getEmotionalAppraisal()=="" */)
  	  		{
	  			bml.AddFork(new BmlFork("marc",bml.id,(float)0.0));
	  	  		//bml.resetPosture(); 
	  		  try{
	  			//	bml.AddFork(new BmlFork("marc",bml.id,(float)1.0));
	  				  if(bmlFile!=null)
	  				  {    
			  		    String bmlStringData ="";
			        		int index = ThreadLocalRandom.current().nextInt(1,4);
			        		//System.out.println("\n%%%%%%%%%%%%%%%%%%%%  bml-step- index "+ index +"\n");
			    			Element rootElement = bmlFile.getRootElement();
			            	List<Element> children = rootElement.getChildren("bml");
			            	
			                Iterator iterator = children.iterator();
			                while (iterator.hasNext()) {
			                	Element child = (Element) iterator.next();	 
			                	
			                	if (child.getAttribute("id").getValue().equals("bml-step-"+index)){
			                		List<Element> childNodes = child.getChildren();	
			    	            	
				            		Iterator childIterator = childNodes.iterator();
				                     while (childIterator.hasNext()) {
				                     	Element childElement = (Element) childIterator.next();
				                     	bmlStringData+=  generateBML(childElement);
				                     }
			                }
			               } 
			  			bml.AppendBmlContent(bmlStringData); 			
			  		  }
	  				  else{
	  					bml.resetPosture();  
	  				  }
	  			//	bml.EndFork();
	  			  }catch(Exception e){
	  			  }
	  			  
	  	  		resetSmilingFace(bml);
	  	  		bml.EndFork();	  	  		
	  	  		
	  	  	}
	  	  	else{	
		  			bml.AddFork(new BmlFork("marc",bml.id,(float)0.0));
		  			resetSmilingFace(bml);	 
		  			bml.EndFork();
	  	  		
	  	  	}
  			
  			
  		
  			
  			
  		
	  		}
  		
  		if(data.getSlide()!=""){
   			String slideName = data.getSlide();
  			if (!slideName.isEmpty())
  	  		{  if(imageDirectory!=null)
  	  				bml.AddLoadImage (slideName,imageDirectory);
  	  			else
  	  				bml.AddLoadImage (slideName);
  	  		}
   		}
  		if(data.getData()!=""){
  			  			
	        if (userAudioPath != null)
	        {
	        	
	        	try{           
                    Writer output;
		    		output = new BufferedWriter(new FileWriter("CommandsLogFile.txt", true));
		    		output.append("<speechText>"+data.getData()+"</speechText>");
		    		output.close();
                }
                catch (IOException e)
		    	{
		    		System.out.println("\nError: File writer crashed!\n");
		    		//Logger.log(this, Logger.CRITICAL, "Error in MARC BML Translation Component log file management!", e); 
		    	}
	        	
	        	String message="";
	        	String str = data.getData();
	        	if(str.length()>1){
		        	String strSign = str.substring(0,1);
		        	//if it is not understood by the agent 
		        	if (strSign.equals("#")){
		        		String strVoiceNo = str.substring(1,2);
		        		message = generateBMLAudio(bml, data.getData(), userAudioPath+"DEF"+strVoiceNo, "wav");
					}
		        	//if there is no response from the child side 
		        	else if (strSign.equals("*")){
		        		String strVoiceNo = str.substring(1,2);
		        		message = generateBMLAudio( bml, data.getData(), userAudioPath+"OUT"+strVoiceNo, "wav");
		        	}
		        }
	        	//telling the story
	        	if (message == "")
	        		if(data.getAudioFileName()!=""){
	        			message = generateBMLAudio(bml, data.getData(), userAudioPath+data.getAudioFileName().trim() ,"wav");
	        		}
	        		else
	        			message = generateBMLAudio(bml, data.getData(), userAudioPath+newState ,"wav");
   		}
	        }
  		//System.out.println("\n\n µµµµµµµµµµdata.getEmotionalAppraisal()"+ data.getEmotionalAppraisal()+"\n\n");
  		if(data.getEmotionalAppraisal()=="" ){
  			resetSmilingFace(bml);
  		}
  		//appendMessageForExecution( bml.GetBmlContent().replace("\n","\t"));
  		
  		if(data.getEmotionalAppraisal()!="" )
  		{   emlExecuting = true;
  			String appraisalString  = data.getEmotionalAppraisal();
  			String[] result = appraisalString.split(",");
  			double ex = Double.parseDouble(result[0].trim());
  			double unp = Double.parseDouble(result[1].trim());
  			double gh = Double.parseDouble(result[2].trim());
  			double cc = Double.parseDouble(result[3].trim());
  			double cp = Double.parseDouble(result[4].trim());
  			
  			EmotionalAppraisal checks=new EmotionalAppraisal(ex, unp, gh, cc, cp);
  			
  			String emlstr = generateEml(checks, "BOTH");
     		sendEml(checks,"BOTH");
  		}
  		
  		bml.EndBmlWriter();
 		
   		scheduleForExecutionMessage( bml.GetBmlContent().replace("\n","\t"));
   		
      }      
    
    /**
     * Resets MARC toolkit avatar face to a smiling face.
     * @param bmlSmilingFace BML message for a smiling face
     */
    public void resetSmilingFace(Bml bmlSmilingFace)
    {
    	bmlSmilingFace.AddFork(new BmlFork("marc",bmlSmilingFace.id++,(float)0.0));
		bmlSmilingFace.AddFace(new Face("marc","ALL","bml_clearAll",(float)0.0));
		bmlSmilingFace.EndFork();
		bmlSmilingFace.AddFork(new BmlFork("marc",bmlSmilingFace.id,(float)0.2));
		
		//**************************************************************
		// sourcils légèrement up
		bmlSmilingFace.AddFace(new Face("marc","1",0.3f,0.7f,"BOTH"));
		// Joues 
		//Bouche: Sourire
		bmlSmilingFace.AddFace(new Face("marc","12",0.8f,0.7f,"BOTH"));// Etirer le coin des lèvres (sourire): Tire les coins des lèvres de façon diagonale vers les os des joues (sourire) 
		// Joues 
		bmlSmilingFace.AddFace(new Face("marc","6",0.2f,0.7f,"BOTH"));// Lever la joue: Relève les joues
		// Définir deux catégories d'expressions de sourire: 
			// expression1: AU12+6+5+25 (bouche légérement ouverte)
			// expression2: AU12+6+5 (bouche fermée)
		int expression1AddAu25 = 1 + (int)(Math.random() * ((2 - 1) + 1));
	//	System.out.println("########################## expression "+expression1AddAu25);
		if (expression1AddAu25==1)
		{// Bouche
			bmlSmilingFace.AddFace(new Face("marc","25",0.3f,0.7f,"BOTH"));//*** Séparer les lèvres: Ouvre la bouche et sépare très légèrement les lèvres 
		}
		//bmlOneAu.AddFace(new Face("marc","26",0.1f,1.0f,"BOTH"));//Baisser la machoire: Ouvre la bouche au point où il y a un espace entre les dents
	    //**************************************************************
				
		
		bmlSmilingFace.EndFork();
		
    }
    
    /**
     * Generates EML message
     * @param checks Emotional appraisal theory values
     * @param mode Emotional appraisal theory modes
     * @return EML message
     */
    public String generateEml(EmotionalAppraisal checks, String mode){
    	String formatEml= null;
    	if(checks != null)
		{
			 formatEml = "<emotionml id:sdf>\n"+
							   "<emotion>\n"+
							   "<appraisals>\n" +
								"<expectedness value=\""+(checks.expectedness)+"\" mode=\""+mode+"\"/>\n"+
								"<unpleasantness value=\""+(checks.unpleasantness)+"\" mode=\""+mode+"\"/>\n"+
								"<goal_hindrance value=\""+(checks.goal_hindrance)+"\" mode=\""+mode+"\"/>\n"+
								"<copying_control value=\""+(checks.coping_control)+"\" mode=\""+mode+"\"/>\n"+
								"<copying_power value=\""+(checks.coping_power)+"\" mode=\""+mode+"\"/>\n"+
								"\t\t</appraisals>"+
								"\t</emotion>\n"+
								"</emotionml>\n";
		}
       return formatEml;
    }
    
    /**
     * Sends EML message to MARC toolkit
     * @param checks Emotional appraisal theory values
     * @param mode Emotional appraisal theory modes
     */
    public void sendEml(EmotionalAppraisal checks,String mode)
	{
    	//System.out.println("\n\n\n%%%%%%%%%%%%%%sendEML*********************%%%%%%%%%%%%%%%%");
    	
	//	Eml eml=new Eml();
		if(checks != null)
		{
		
			String formatEml = "<emotionml>\n"+
							   "<emotion>\n"+
							   "<appraisals>\n" +
								"<expectedness value=\""+(checks.expectedness)+"\" mode=\""+mode+"\"/>\n"+
								"<unpleasantness value=\""+(checks.unpleasantness)+"\" mode=\""+mode+"\"/>\n"+
								"<goal_hindrance value=\""+(checks.goal_hindrance)+"\" mode=\""+mode+"\"/>\n"+
								"<copying_control value=\""+(checks.coping_control)+"\" mode=\""+mode+"\"/>\n"+
								"<copying_power value=\""+(checks.coping_power)+"\" mode=\""+mode+"\"/>\n"+
								"\t\t</appraisals>"+
								"\t</emotion>\n"+
								"</emotionml>\n";
			//System.out.println("\n\n\nEML :::" +formatEml);
			if(socket != null){
				try{
					Writer output;
		    		output = new BufferedWriter(new FileWriter("CommandsLogFile.txt", true));
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		    		Date date = new Date();
		    		output.append("<emlSendTime>"+dateFormat.format(date)+"</emlSendTime><eml>"+formatEml+"</eml>");		    		
		    		output.close();
		    		
		    		socket.sendEmlMessage(formatEml.replaceAll("\n", "\t"));		    		
				}
		    	catch (IOException e)
		    	{
		    		System.out.println("\nError: File writer crashed!\n");
		    		Logger.log(this, Logger.CRITICAL, "Error in MARC BML Translation Component log file management!", e); 
		    	}
			
			}
			
			
			
			
		}
	} 
    
    /**
     * Closes socket for communicating with MARC toolkit. 
     */
    public void close() {
        super.close();

        if (socket != null) {
            socket.close();
        }
    }
    
   /**
    * Generates BML message based on different elements. 
    * @param element element
    * @return BML message
    */
   private String generateBML(Element element) {
	   String message = "<";
   	
   	//This test takes into account the namespace "marc" used by MARC on some element's and attribute's name.
   	//Its syntax isn't recognized by JDOM
   	if(element.getName() == "nao" || element.getName() == "fork" || element.getName() == "posture_channels" || element.getName() == "speech_stop" ||
   			element.getName() == "log" || element.getName() == "on_screen_message" || element.getName() == "environment" ||
   			element.getName() == "subtitles" || element.getName() == "import" || element.getName() == "restart") {
       	message += "marc:"+element.getName();	
   	} else {
     	message += element.getName();
   	}
       
       List<Attribute> attributes = element.getAttributes();
       Iterator iterator1 = attributes.iterator();
       while (iterator1.hasNext()) {
       	Attribute attributeObject = (Attribute) iterator1.next();
       	//This test takes into account the namespace "marc" used by MARC on some element's and attribute's name.
       	//Its syntax isn't recognized by JDOM
       	if((element.getName() == "speech" && (attributeObject.getName() == "volume" || attributeObject.getName() == "file" ||
       										attributeObject.getName() == "articulate" || attributeObject.getName() == "voice" ||
       										attributeObject.getName() == "synthetizer" || attributeObject.getName() == "options" ||
       										attributeObject.getName() == "f0_shift" || attributeObject.getName() == "locale" ||
       										attributeObject.getName() == "style")) ||
       		(element.getName() == "posture" && (attributeObject.getName() == "transition" || attributeObject.getName() == "loop" ||
												attributeObject.getName() == "turn_speed" || attributeObject.getName() == "transition_mode" ||
												attributeObject.getName() == "blend_duration" || attributeObject.getName() == "intensity" ||
												attributeObject.getName() == "define_as_rest_pose")) ||
       		(element.getName() == "face" && (attributeObject.getName() == "interpolate" || attributeObject.getName() == "interpolation_type" ||
												attributeObject.getName() == "intensity" || attributeObject.getName() == "animation_file")) ||
               (element.getName() == "gaze" && (attributeObject.getName() == "speed" || attributeObject.getName() == "eyes_ratio"))){
       		message += " marc:" + attributeObject.getName() + "=\"" + attributeObject.getValue() + "\"";
       	} else {
       		message += " " + attributeObject.getName() + "=\"" + attributeObject.getValue() + "\"";
       	}
       }
       
       List<Content> content = element.getContent();
       //If this element is a NO-OP command
       if(content.isEmpty()) {
       	message += "/>";
       } else {
       	message += ">";
	        Iterator iterator2 = content.iterator();
	        while (iterator2.hasNext()) {
	          Object childObject = iterator2.next();
	          if (childObject instanceof Element) {
	        	  Element childElement = (Element) childObject;
	              message += generateBML(childElement);
	            } else {
	              if (childObject instanceof Text) {
	                Text text = (Text) childObject;
	                message += text.getText();
	              }
	            }
	        }
	        message += "</" + element.getName() + ">";
       }
    //	System.out.println("\n\n\n*************\n\n"+ message +"\n************\n\n\n" );
    	 
       //Take out all carriage return lines that are not read correctly by MARC
       message = message.replaceAll("[\r\n\t]+", "");
    	return message;
  }
   
   /**
    * Acts and runs all functionalities of the class based on system beat.
    */
   public boolean act() {
	   if(!isSmiling && !bmlExecuting)
	   {
		   Bml bmlSmile = new Bml(0);
	   	   	String header ="<bml id=\"" +"smilebml"+ "\">"+"\n";
	   	  		bmlSmile.AppendBmlContent(header);
	   	  	     resetSmilingFace(bmlSmile); 
	   	  	bmlSmile.EndBmlWriter();
	   	    pushMessageForExecution(bmlSmile.GetBmlContent().replace("\n","\t"));
	   }	
	   if( !bmlExecuting && !isIdleExecution)
	   {   
		  
		   	{
		   		System.out.println("Launching Idle Animation");
		   		isIdleExecution = true;
			   Bml bmlIdle = new Bml(0);
	   	   	String header ="<bml id=\"" +"idlebml"+ "\">"+"\n";
	   	    bmlIdle.AppendBmlContent(header);
	   	    
	   	     if(bmlFile!=null){    
	  		    String bmlStringData ="";
	        		int index = ThreadLocalRandom.current().nextInt(1,3);
	        		//System.out.println("\n%%%%%%%%%%%%%%%%%%%%  bml-step- index "+ index +"\n");
	    			Element rootElement = bmlFile.getRootElement();
	            	List<Element> children = rootElement.getChildren("bml");
	            	
	                Iterator iterator = children.iterator();
	                while (iterator.hasNext()) {
	                	Element child = (Element) iterator.next();	 
	                	
	                	if (child.getAttribute("id").getValue().equals("bmlIdle-"+index)){
	                		List<Element> childNodes = child.getChildren();	
	    	            	
		            		Iterator childIterator = childNodes.iterator();
		                     while (childIterator.hasNext()) {
		                     	Element childElement = (Element) childIterator.next();
		                     	bmlStringData+=  generateBML(childElement);
		                     }
	                }
	               } 
	                bmlIdle.AppendBmlContent(bmlStringData); 			
	  		  }
	   	   	
	   	   	//bmlIdle.AppendBmlContent(header);
	   	    // bmlIdle.addPosture(new Posture("marc","idle","Rest Pose","bml_gesture_item"));
   	   	  	bmlIdle.EndBmlWriter();
	   	    pushMessageForExecution(bmlIdle.GetBmlContent().replace("\n","\t"));
		   	}
	   }	 
	    
	   
   return true;
   }

}
