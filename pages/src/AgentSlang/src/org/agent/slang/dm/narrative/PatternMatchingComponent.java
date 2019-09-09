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

package org.agent.slang.dm.narrative;

import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.agent.slang.data.audio.PlayerEvent;
import org.agent.slang.dm.narrative.data.StateChangeData;
import org.agent.slang.dm.narrative.data.commands.CommandData;
import org.agent.slang.dm.narrative.data.patterns.PatternsStates;
import org.agent.slang.dm.narrative.data.story.StoryStates;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.LivingComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.data.SystemEvent;
import org.ib.logger.Logger;
import org.syn.n.bad.annotation.TextToken;
import org.syn.n.bad.dictionary.Dictionary;
import org.syn.n.bad.dictionary.DictionaryException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * This component is used to match a proper pattern for received input. As well as making decision about what to do with that matched pattern.
 * OS Compatibility: Windows and Linux 
 * @author William Boisseleau, william.boisseleau@insa-rouen.fr
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @author sahba ZOJAJI, sahba.zojaji@insa-rouen.fr
 * @version 1, 07/23/13 - Initial commit, William Boisseleau
 *          2, 12/03/13 - AgentSlang 1.1 integration, Ovidiu Serban
 *          3, 25/11/2016 - Set timer off for WOZ Experiment, Sahba ZOJAJI
 */

@ConfigureParams(mandatoryConfigurationParams = {"modelPath", "dictionaryConfig"},
        outputChannels = {"response.data", "command.data", "stateChange.data"}, outputDataTypes = {StringData.class, CommandData.class, StateChangeData.class},
        inputDataTypes = {GenericTextAnnotation.class, PlayerEvent.class, SystemEvent.class, StateChangeData.class, StringData.class})
public class PatternMatchingComponent extends LivingComponent {
    /**
     * define model path
     */
	private static final String PROP_MODEL_PATH = "modelPath";
    
	/**
     * define dictionary
     */
	private static final String PROP_DICTIONARY = "dictionaryConfig";

    private static final String RESPONSE_DATA = "response.data";
    private static final String COMMAND_DATA = "command.data";
    private static final String STATE_CHANGE_DATA = "stateChange.data";
    //Sahba modification starts, Version 3
    private static final String PROP_WOZ_TIMER_CAN_BE_STARTED = "timerCanBeStarted";
    private static final String PROP_DEF_COUNTER_THRESHOLD = "defTimer";
    private static final String PROP_YESNO_COUNTER_THRESHOLD = "yesNoTimer";
    private static final String PROP_QSN_COUNTER_THRESHOLD = "QuestionTimer";
    private static final String PROP_WAIT_LATENCY_THRESHOLD = "WaitLatency";
    private static final String PROP_JOINT_ATTENTION_THRESHOLD = "JointAttentionTimer";
    private boolean wozTimerCanBeStarted = true;
    //Sahba modification ends, Version 3
    private int jointAttentionTimer = 0;
    private int waitLatency = 10;
    private int counterThreshold = 4;
    private int defCounterThreshold = 3;
    private int yesNOCounterThreshold = 1;
    private int qsnCounterThreshold = 4;

    private int counterSeconds;
    private int waitingSeconds;

    private PatternsStates patternsStatesOutOfContext;
    private ArrayList<PatternsStates> patternsStatesStepI;
    private int stepsNumber;
    private int currentStep;
    private int newStep;
    private boolean firstStateChangeSent;
    private boolean timerCanBeStarted;
    private boolean scenarioOver;
    private StoryStates storyStates;

    private File modelPath;
    private boolean isChildSpeaking;
    private String story;
    private boolean isOutContext_repeated;

    
    public PatternMatchingComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        this.modelPath = config.getFileProperty(PROP_MODEL_PATH, "data", true);
        try {
            Dictionary.setupDictionary(config.getFileProperty(PROP_DICTIONARY));
        } catch (DictionaryException e) {
            throw new IllegalStateException(e);
        }
        
        //Sahba modification starts, Version 3
        if (config.hasProperty(PROP_WOZ_TIMER_CAN_BE_STARTED)) {
            try {
            	wozTimerCanBeStarted = Boolean.parseBoolean(config.getProperty(PROP_WOZ_TIMER_CAN_BE_STARTED));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid timerCanBeStarted provided", e);
            }
        }
        //Sahba modification ends, Version 3

        if (config.hasProperty(PROP_DEF_COUNTER_THRESHOLD)) {
            try {
            	defCounterThreshold = Integer.parseInt(config.getProperty(PROP_DEF_COUNTER_THRESHOLD));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid defTimer provided", e);
            }
        }
        
        if (config.hasProperty(PROP_YESNO_COUNTER_THRESHOLD)) {
            try {
            	yesNOCounterThreshold = Integer.parseInt(config.getProperty(PROP_YESNO_COUNTER_THRESHOLD));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid yesNoTimer provided", e);
            }
        }
        
        if (config.hasProperty(PROP_QSN_COUNTER_THRESHOLD)) {
            try {
            	qsnCounterThreshold = Integer.parseInt(config.getProperty(PROP_QSN_COUNTER_THRESHOLD));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid QuestionTimer provided", e);
            }
        }
        
        if (config.hasProperty(PROP_WAIT_LATENCY_THRESHOLD)) {
            try {
            	waitLatency = Integer.parseInt(config.getProperty(PROP_WAIT_LATENCY_THRESHOLD));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid WaitLatency provided", e);
            }
        }
        
        if (config.hasProperty(PROP_JOINT_ATTENTION_THRESHOLD)) {
            try {
            	jointAttentionTimer = Integer.parseInt(config.getProperty(PROP_JOINT_ATTENTION_THRESHOLD));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid JointAttentionTimer provided", e);
            }
        }
        
        this.storyStates = new StoryStates();
        this.storyStates.init(new File(modelPath, "story/Story.xml"));
        
        this.counterSeconds = 0;
        this.waitingSeconds = 0;
        this.currentStep = 1;
        this.newStep = -1;
        this.firstStateChangeSent = false;
        this.scenarioOver = false;
        this.timerCanBeStarted = false;
        this.stepsNumber = countSteps(new File(modelPath, "steps"));
        this.patternsStatesOutOfContext = new PatternsStates();
        this.patternsStatesStepI = new ArrayList<PatternsStates>();
        
        this.isChildSpeaking = false;
        this.isOutContext_repeated = false;
        story = null;

        loadPatternsAndCommands();
    }

    /**
     * Counts the number steps in the story (model)
     * @param modelPath path of the scenario (story)
     * @return Number of steps
     */
    private int countSteps(File modelPath) {
        if (modelPath != null && modelPath.exists() && modelPath.canRead()) {
            File[] modelFiles = modelPath.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith("Step_") && name.endsWith(".xml");
                }
            });
            return modelFiles != null ? modelFiles.length : 0;
        } else {
            return 0;
        }
    }

    /**
     * Loads all patterns and related commands for each step
     */
    protected void loadPatternsAndCommands() {
        // Loading Steps_i.xml
        PatternsStates pS;
        for (int i = 0; i < stepsNumber; i++) {
            pS = new PatternsStates();
            pS.init(new File(modelPath, "steps/Step_" + (i + 1) + ".xml"));
            this.patternsStatesStepI.add(i, pS);
        }

        // Loading OutOfContext.xml
        this.patternsStatesOutOfContext.init(new File(modelPath, "outOfContext/OutOfContext.xml"));
    }

    /**
     * Managing input and output data in the class.
     * @param data input data 
     */
    protected void handleData(GenericData data) {
    	System.out.println("1.     ZJ Pattern Matching: START of handleData****"+data +"(data instanceof GenericTextAnnotation): "+(data instanceof GenericTextAnnotation)+", timerCanBeStarted: "+timerCanBeStarted+", isChildSpeaking:  "+isChildSpeaking);
    	if (data instanceof PlayerEvent) {
        	//System.out.println("2.     ZJ Pattern Matching: START of PlayerEvent: ****"+((PlayerEvent) data).getEvent());
        	//Sahba modification starts, Version 3
        	//The previous version was as follow:
        	//if (PlayerEvent.EVENT_STOP == ((PlayerEvent) data).getEvent()) {
        	//System.out.println("\n*****ZJ,Pattern Matching, PlayerEvent:      "+((PlayerEvent) data).getEvent());
        	if (PlayerEvent.EVENT_START == ((PlayerEvent) data).getEvent()) {
        		 
        		this.timerCanBeStarted = false; 
        		//System.out.println("3.     ZJ Pattern Matching: INSIDE of PlayerEvent EVENT_START**timerCanBeStarted**"+timerCanBeStarted);
                 //this.counterSeconds = 0;
        	}

        	if (PlayerEvent.EVENT_STOP == ((PlayerEvent) data).getEvent() && wozTimerCanBeStarted) {
            //Sahba modification ends, Version 3
            	//System.out.println("\n*****ZJ,Pattern Matching, PlayerEvent:      "+((PlayerEvent) data).getEvent());
        		this.timerCanBeStarted = true;
        		//System.out.println("4.     ZJ Pattern Matching: INSIDE of PlayerEvent EVENT_STOP**timerCanBeStarted**"+timerCanBeStarted);
                //this.counterSeconds = 0;
            }
        	System.out.println("5.     ZJ Pattern Matching: END of PlayerEvent** PlayerEvent **"+((PlayerEvent) data).getEvent()+",   timerCanBeStarted: "+timerCanBeStarted);
        }
    	else if (data instanceof GenericTextAnnotation) { 
        System.out.println("6.     ZJ Pattern Matching: START of GenericTextAnnotation** data **"+data);
            this.counterSeconds = 0;
            this.waitingSeconds = 0;
            List<String> lCommands = null;
            StringData response;
                    
            // In case the scenario is not over (<=> the last step has not been processed yet)
             if (!scenarioOver) {
            	   //System.out.println("\n\n\n$a: isOutContext_repeated: "+ isOutContext_repeated+"\n\n\n$$");
                   
                if (this.currentStep < (stepsNumber + 1)) {
                	//System.out.println("7.    ZJ Pattern Matching:  START of (this.currentStep < (stepsNumber + 1))***currentStep*"+this.currentStep+", stepsNumber+1: "+ stepsNumber+1);
                    // In case we use TextComponent and we do not answer, we simply call the "no answer" pattern
                    if (((GenericTextAnnotation) data).getTranscription().equals("")) {
                    	//System.out.println("8.     ZJ Pattern Matching:  START of ((GenericTextAnnotation) data).getTranscription().equals(\"\")****");
                        GenericTextAnnotation tokens = new GenericTextAnnotation(1);
                        tokens.addTextToken(new TextToken("wxcvbn")); // We consider this string wxcvbn as the "no-answer" pattern.
                        data = tokens;
                    }

                    //System.out.println("\n\n\n$$$$$$2"+((GenericTextAnnotation) data).getTranscription().trim());
                    if(!((GenericTextAnnotation) data).getTranscription().trim().equals("wxcvbn")){
                    	 //System.out.println("9.     ZJ Pattern Matching: START of !((GenericTextAnnotation) data).getTranscription().trim().equals(\"wxcvbn\")****");
                    	 isOutContext_repeated =false;
                    	 //System.out.println("\n\n\n$$$$$$***  2.3"+((GenericTextAnnotation) data).getTranscription().trim());
                         
                    }
                    else
                    {
                    	//System.out.println("10.     ZJ Pattern Matching: INSIDE EMPTY ELSE****");
                    	 //System.out.println("\n\n check if there is problem\n");
                    }
                    //System.out.println("\n\n\n$b: isOutContext_repeated: "+ isOutContext_repeated+"\n\n\n$$");
                    
                    // Now recovering the answer.
                    // First checking the Step_i
                    PatternsStates pSStepI = this.patternsStatesStepI.get(currentStep - 1);
                    response = new StringData(data.getId(),
                            pSStepI.checkMatching((GenericTextAnnotation) data, false),
                            ((GenericTextAnnotation) data).getLanguage());
                    //System.out.println("11.     ZJ,Pattern Matching, response step i:      "+response);

                    if (response.getData() != null && response.getData().trim().length() > 0) {
                    	System.out.println("12. *****ZJ,Pattern Matching, publish response:      "+response);
                        publishData(RESPONSE_DATA, response);
                    }
                    
                    
                    // Loading the Next Step number
                    if (pSStepI.getNext() != 0) {
                    	System.out.println("13.     ZJ,Pattern Matching, response publishing StateChangeData:      "+new StateChangeData(data.getId(), this.currentStep - 1));
                        this.currentStep = pSStepI.getNext();
                        publishData(STATE_CHANGE_DATA, new StateChangeData(data.getId(), this.currentStep - 1));

                        
                        // Loading the command for the next step
                        if (!pSStepI.getlCommands().isEmpty()) {
                            lCommands = pSStepI.getlCommands();
                            System.out.println("14.     ZJ,Pattern Matching, publishing lCommands:      "+lCommands);
                            
                            if (lCommands != null) {
                            	publishData(COMMAND_DATA, new CommandData(data.getId(), lCommands));

                            	}
                        }
                        isOutContext_repeated = false;
                        //System.out.println("\n\n\n$$ State cvhange");
                    }

                    if (currentStep > 1) {
                    	//System.out.println("15.     ZJ,Pattern Matching, Start of (currentStep > 1):  currentStep:    "+currentStep);
                    	System.out.println("16.     ZJ,Pattern Matching, before if OutOfContext if (!pSStepI.getWeMatchedStepI())  *pSStepI.getWeMatchedStepI()*"+pSStepI.getWeMatchedStepI()+",   currentStep: "+currentStep);
	                    //Then checking the OutOfContext, only if nothing was found in Step_i
	                    if (!pSStepI.getWeMatchedStepI()) {
	                    	//System.out.println("17.     ZJ,Pattern Matching, INSIDE if OutOfContext if (!pSStepI.getWeMatchedStepI()) *pSStepI.getWeMatchedStepI()*"+pSStepI.getWeMatchedStepI());
	                    	if(!isOutContext_repeated){
	                    		//System.out.println("18.     ZJ,Pattern Matching, Start of !isOutContext_repeated");
	                         response = new StringData(data.getId(),
	                                this.patternsStatesOutOfContext.checkMatching((GenericTextAnnotation) data, true),
	                                ((GenericTextAnnotation) data).getLanguage());
	                         
	                         //System.out.println("19.     ZJ,Pattern Matching, before publishing response"+ response);
	                         
	                         if (response.getData() != null && response.getData().trim().length() > 0) {
	                         	System.out.println("20. *****ZJ,Pattern Matching, publishing response:      "+response);
	                             publishData(RESPONSE_DATA, response);
	                         }
	                       
	                        	isOutContext_repeated = true;
	                    	}
	                        if (patternsStatesOutOfContext.getNext() != 0) {
	                        	//System.out.println("21.     ZJ,Pattern Matching, patternsStatesOutOfContext.getNext() != 0");
	                            this.currentStep = patternsStatesOutOfContext.getNext();
	                            if (!patternsStatesOutOfContext.getlCommands().isEmpty()) {
	                                lCommands = patternsStatesOutOfContext.getlCommands();    
	                                System.out.println("22.     ZJ,Pattern Matching, publishing not null lCommands"+ lCommands);
	                                
	                                if (lCommands != null) {
	                                	publishData(COMMAND_DATA, new CommandData(data.getId(), lCommands));

	                                	}
	                                }
	                        }
	                    }
                    }
                } else {
                    // In case the currentStep over passed the stepsNumber+1, the scenario is done.
                    response = new StringData(data.getId(),
                            "The Scenario is now over, thank you for your participation.",
                            ((GenericTextAnnotation) data).getLanguage());
                    
                    if (response.getData() != null && response.getData().trim().length() > 0) {
                    	System.out.println("23. *****ZJ,Pattern Matching, publishing final response:      "+response);
                        publishData(RESPONSE_DATA, response);
                    }
                    
                    scenarioOver = true;
                }
                /*
                if (lCommands != null) {
                	publishData(COMMAND_DATA, new CommandData(data.getId(), lCommands));

                }
                if (response.getData() != null && response.getData().trim().length() > 0) {
                	System.out.println("15. *****ZJ,Pattern Matching, response:      "+response);
                    publishData(RESPONSE_DATA, response);
                }*/
                //System.out.println("\n\n\n$c$ : isOutContext_repeated: "+ isOutContext_repeated);
             
            }
             //System.out.println("24.     ZJ Pattern Matching: END of GenericTextAnnotation****"+data);
        } else if (data instanceof StateChangeData) {
        	System.out.println("25.     ZJ Pattern Matching: START of StateChangeData****"+data);
            int newState = ((StateChangeData) data).getNewStateNumber() + 1;
            if (this.currentStep != newState) {
                this.currentStep = newState;
                publishData(STATE_CHANGE_DATA, data);
            }
            //System.out.println("26.     ZJ Pattern Matching: END of StateChangeData****"+data);
        }
        
        else if (data instanceof StringData) {
        	//System.out.println("27.     ZJ Pattern Matching: START of StringData****"+data);
            String rcvFromSpeechRec = ((StringData) data).getData();
            
            if (rcvFromSpeechRec.equals("SpeechStarted") || rcvFromSpeechRec.equals("ClassificationStarted")) {
            	isChildSpeaking = true;
            	//System.out.println("\n*****MMMMMMMMMMMMMMMMMMMMJ,Pattern Matching, SpeechStarted");
            }
            else if (rcvFromSpeechRec.equals("SpeechEnd") || rcvFromSpeechRec.equals("ClassificationEnded")) {
            	isChildSpeaking = false;
            	//counterThreshold = waitLatency;
            	this.counterSeconds = 0;
            	this.waitingSeconds = 0;
            	//System.out.println("\n*****ZJ,Pattern Matching, waitLatency: "+ waitLatency);          
            	//System.out.println("\n*****MMMMMMMMMMMMMMMMMMMMMJ,Pattern Matching, SpeechEnd");
            }
            //System.out.println("28.     ZJ Pattern Matching: END of StringData****"+data);
        }
        //System.out.println("29.     ZJ Pattern Matching: END of handleData****");
    }

    /**
     * setting system timer awake or asleep (on and off)
     * @param event SystemEvent
     */
    protected void handleSystemEvents(SystemEvent event) {
    	//Sahba modification starts, Version 3
    	//The previous version was as follow:
    	//if (SystemEvent.SYSTEM_WAKE == event.getEvent()) {
    	if (SystemEvent.SYSTEM_WAKE == event.getEvent() && wozTimerCanBeStarted) {
    	//Sahba modification ends, Version 3
            this.timerCanBeStarted = true;
        }
    }

    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
        addInboundTypeChecker(GenericTextAnnotation.class);
        addInboundTypeChecker(PlayerEvent.class);
        addInboundTypeChecker(SystemEvent.class);
        addInboundTypeChecker(StateChangeData.class);
        addInboundTypeChecker(StringData.class);
    }

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(RESPONSE_DATA, StringData.class);
        addOutboundTypeChecker(COMMAND_DATA, CommandData.class);
        addOutboundTypeChecker(STATE_CHANGE_DATA, StateChangeData.class);
    }

    /**
     * Controls system events and timers
     */
    public boolean act() {

	    	if (!this.firstStateChangeSent) {
	            publishData(STATE_CHANGE_DATA, new StateChangeData(0, this.currentStep - 1));
	            this.firstStateChangeSent = true;
	        }
	
	        // After around X waiting sec., we consider this as a "non answer".
	        // ie. we consider the non answer as an action
	
	        
	        //if (timerCanBeStarted) {
	    	//System.out.println("100.     ZJ Pattern Matching: START of act()** if (timerCanBeStarted && !isChildSpeaking)**timerCanBeStarted: "+timerCanBeStarted +", isChildSpeaking: "+isChildSpeaking);
	        if (timerCanBeStarted && !isChildSpeaking) {
	        	//System.out.println("101.     ZJ Pattern Matching: START of if (timerCanBeStarted && !isChildSpeaking)****");
	        	if (this.currentStep != this.newStep)
	        	{
	        		newStep = currentStep;
	        	   story = storyStates.getListOfStoryStates().get(currentStep-1).getStory();
	        		if (story != "" || !story.equals(null))
	        		{
		        		//Open Questions are specified with (??)
	        			if (story.endsWith("(??)")){
		        			//System.out.println("\n*****ZJ,Pattern Matching, qsnCounterThreshold:      ??"+qsnCounterThreshold);
		        			counterThreshold = qsnCounterThreshold;
		        		}
	        			//Yes/No Questions are specified with (?!)
		        		else if (story.endsWith("(?!)")){
		        			//System.out.println("\n*****ZJ,Pattern Matching, yesNOCounterThreshold:      ?!"+yesNOCounterThreshold);
		        			counterThreshold = yesNOCounterThreshold;
	        			}
	        			//Join Attention are specified with (!!)
		        		else if (story.endsWith("(!!)")){
		        			//System.out.println("\n*****ZJ,Pattern Matching, jointAttentionTimer:      !!"+jointAttentionTimer);
		        			counterThreshold = jointAttentionTimer;
	        			}
		        		else{
		        			//System.out.println("\n*****ZJ,Pattern Matching, defCounterThreshold:      "+defCounterThreshold);
		        			counterThreshold = defCounterThreshold;
		        		}
	        		}	
	        	}
	        	//System.out.println("102.     ZJ Pattern Matching: BEFORE Entering if (counterSeconds > counterThreshold)**counterSeconds: **"+ counterSeconds+" , counterThreshold"+counterThreshold);
	            if (counterSeconds > counterThreshold) {
	            	//System.out.println("103.     ZJ Pattern Matching: Publishing default to go next");
	                this.counterSeconds = 0;
	                this.waitingSeconds = 0;
	                GenericTextAnnotation tokens = new GenericTextAnnotation(1);
	                tokens.addTextToken(new TextToken("wxcvbn")); // "no-answer" pattern by default
	                handleData(tokens);
	                this.timerCanBeStarted = false;
	            } else {
	            	//System.out.println("104.     ZJ Pattern Matching: Counting");
	                System.out.println(counterSeconds);
	                this.counterSeconds++;
	            }
	            //System.out.println("105.     ZJ Pattern Matching: END of if (timerCanBeStarted && !isChildSpeaking)****");
	        }
            
	        //for going out of blocked steps in automatic mode
	        if (!timerCanBeStarted && wozTimerCanBeStarted)
	        {
		        if (waitingSeconds > waitLatency) {
	            	//System.out.println("105.     ZJ Pattern Matching: Publishing default to go next");
		        	System.out.println("!!**************************************************************************************!!");
		        	System.out.println("!!**ZJ, Pattern Matching: BLOCKED STEP Solved by publishing default to go to next step**!!");
		        	System.out.println("!!**************************************************************************************!!");
		        	this.waitingSeconds = 0;
	                GenericTextAnnotation tokens = new GenericTextAnnotation(1);
	                tokens.addTextToken(new TextToken("wxcvbn")); // "no-answer" pattern by default
	                handleData(tokens);
	                this.timerCanBeStarted = false;
	            } else {
	            	//System.out.println("106.     ZJ Pattern Matching: wait Counting: "+waitingSeconds);
	                this.waitingSeconds++;
	            }
	        }

        return true;
    }
}