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
import org.agent.slang.data.simple.BmlData;
import org.agent.slang.dm.narrative.data.StateChangeData;
import org.agent.slang.dm.narrative.data.commands.CommandData;
import org.agent.slang.dm.narrative.data.gui.MousePositionData;
import org.agent.slang.dm.narrative.data.story.StoryStates;
import org.agent.slang.dm.narrative.gui.MousePositionListener;
import org.agent.slang.dm.narrative.gui.SlideFrame;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.LivingComponent;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import org.ib.logger.Logger;
import org.ib.utils.FileUtils;
import org.syn.n.bad.annotation.TextToken;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;//*
import java.io.File;
import java.io.FileWriter;//*
import java.text.SimpleDateFormat;//*
import java.util.ArrayList;
import java.util.Calendar;//*
import java.util.List;

/**
 * This component is used to handle different commands inside agentslang platform.
 * OS Compatibility: Windows and Linux
 * @author William Boisseleau, william.boisseleau@insa-rouen.fr
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @author Sahba ZOJAJI, sahba.zojaji@insa-rouen.fr
 * @author Mukesh BARANGE, mukesh.barange@insa-rouen.fr
 * @version 1, 07/23/13 - Initial commit, William Boisseleau
 *          2, 12/03/13 - AgentSlang 1.1 integration, Ovidiu Serban
 *          3, 07/16/15 - Publish the cursor position, Sami Boukortt
 *          4, 16/11/2016 - Fixing the bug of fixed slide picture and Sending not necessary XML elements, Sahba ZOJAJI
 *          5, - Add Posture and Gesture BML and EML, Mukesh BARANGE
 *          6, 20/01/2017 - Logging story when it played, Sahba ZOJAJI
 */

@ConfigureParams(mandatoryConfigurationParams = "modelPath",
        outputChannels = {"command.data", "bml.data", "mouse.data"},
        outputDataTypes = {StringData.class, BmlData.class, MousePositionData.class},
        inputDataTypes = {StateChangeData.class, CommandData.class, MousePositionData.class})
public class HandleCommandComponent extends MixedComponent {
    private static final String PROP_MODEL_PATH = "modelPath";

    private static final String COMMAND_DATA = "command.data";
    private static final String BML_DATA = "bml.data";
    private static final String MOUSE_DATA = "mouse.data";
    private static final String PROP_SLIDE_SHOW = "slideDisplay";

    private long messageID;

    private StoryStates storyStates;
    private ArrayList<String> launchesLists;
    private SlideFrame slideFrame;
    private Robot robot;
    
    private int counterThreshold = 4;
    private int counterSeconds;
    private boolean displaySlides;


    public HandleCommandComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        File modelPath = config.getFileProperty(PROP_MODEL_PATH, "data", true);
        FileUtils.checkReadableFile(modelPath, true);

        this.storyStates = new StoryStates();
        this.storyStates.init(new File(modelPath, "story/Story.xml"));

        this.launchesLists = new ArrayList<String>();

        displaySlides = true;
        if (config.hasProperty(PROP_SLIDE_SHOW)) {
            try {
            	displaySlides = Boolean.parseBoolean(config.getProperty(PROP_SLIDE_SHOW));
            } catch (Exception e) {
                Logger.log(this, Logger.CRITICAL, "Invalid PROP_SLIDE_SHOW provided", e);
            }
        }
        
        this.slideFrame = new SlideFrame(modelPath);
        this.slideFrame.setVisible(displaySlides);
        this.slideFrame.setLocationRelativeTo(null);
        this.slideFrame.addMousePositionListener(new MousePositionListener() {
            @Override
            public void onMouseMoved(int x, int y) {
                publishData(MOUSE_DATA, new MousePositionData(getMessageID(), x, y));
            }
        });

        try {
            robot = new Robot();
        }
        catch (AWTException ignored) {}
    }

    /**
     * Managing input and output data in the class.
     * @param data input data 
     */
    protected void handleData(GenericData data) {
        if (data instanceof StateChangeData) {
            int newState = ((StateChangeData) data).getNewStateNumber();
            List<String> commands = new ArrayList<>(storyStates.getListOfStoryStates().get(newState).getlCommands());
            commands.add("tellStory " + (newState + 1));

            data = new CommandData(data.getId(), commands);

            // explicit fallthrough
            
            /*
            try {
                //create a temporary file
                String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(
                    Calendar.getInstance().getTime());
                File logFile=new File("c:\\"+timeLog);

                BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
                writer.write (newState+" , "+timeLog);

                //Close writer
                writer.close();
            } catch(Exception e) {
                e.printStackTrace();
            }*/
        }

        BmlData bmldata = new BmlData(data.getId());
        if (data instanceof CommandData) {
           //System.out.println("BML Data : "+ data);
            List<String> dataWithoutGestures = ((CommandData) data).getData();
            for(int i=0;i<dataWithoutGestures.size();++i) 
            {     
            	//System.out.println("BML Data without gesture : "+ dataWithoutGestures.get(i));
                if(dataWithoutGestures.get(i).startsWith("bml:"))
                //Sahba modification starts, Version 4
                {
                //Sahba modification ends, Version 4
                	bmldata.setGesture(dataWithoutGestures.get(i).substring(4));
                	//Logger.log(this, Logger.INFORM, "new bml command: " + dataWithoutGestures.get(i));
                    //publishData(BML_DATA, new BmlData(data.getId(), "prefixed", dataWithoutGestures.get(i).substring(4)));
                    dataWithoutGestures.remove(i);
                //Sahba modification starts, Version 4
                }
                else if(dataWithoutGestures.get(i).startsWith("EmotionalAppraisal:"))
                    //Sahba modification starts, Version 4
                    {
                    //Sahba modification ends, Version 4
                    	bmldata.setEmotionalAppraisal(dataWithoutGestures.get(i).substring(19));
                    	//Logger.log(this, Logger.INFORM, "new bml command: " + dataWithoutGestures.get(i));
                        //publishData(BML_DATA, new BmlData(data.getId(), "prefixed", dataWithoutGestures.get(i).substring(4)));
                        dataWithoutGestures.remove(i);
                    //Sahba modification starts, Version 4
                    }
                else if(dataWithoutGestures.get(i).startsWith("displaySlide")) 
                {
                	bmldata.setSlide(dataWithoutGestures.get(i).substring(12));
                }
                else if(dataWithoutGestures.get(i).startsWith("audioFile:"))
                {
                	bmldata.setAudioFileName(dataWithoutGestures.get(i).substring(10));
                }
                else if(dataWithoutGestures.get(i).startsWith("intention:"))
                {
            		bmldata.setIntention(dataWithoutGestures.get(i).substring(10));
            		dataWithoutGestures.remove(i);
                }
                //Sahba modification ends, Version 4
            }

            if(!dataWithoutGestures.isEmpty()) {
            	//Sahba modification starts, Version 4
            	this.launchesLists.clear();
            	//Sahba modification ends, Version 4
            	this.launchesLists.addAll(dataWithoutGestures);
                int storyCheck = this.slideFrame.processChanges(launchesLists);
                if (storyCheck > -1) {
                    publishData(COMMAND_DATA, new StringData(data.getId(),
                            storyStates.getListOfStoryStates().get(storyCheck - 1).getStory(),
                            LanguageUtils.IDX_NONE));
                   
                    bmldata.setData(storyStates.getListOfStoryStates().get(storyCheck - 1).getStory());
                }
            }
            
         // Logger.log(this, Logger.INFORM, "new bml command: " + dataWithoutGestures.get(i));
          publishData(BML_DATA, bmldata);
          //System.out.println("\n\n\n%%%%%%published bml content%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        	
    		//System.out.println(bmldata.getData());
  	  
    	
    		//System.out.println("\n\n\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
  	 

        } else if (data instanceof MousePositionData && robot != null) {
            slideFrame.toFront();
            Point mousePosition = new Point(((MousePositionData) data).getX(), ((MousePositionData) data).getY());
            SwingUtilities.convertPointToScreen(mousePosition, slideFrame.getContentPane());
            robot.mouseMove(mousePosition.x, mousePosition.y);
        }
    }

    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
        addInboundTypeChecker(StateChangeData.class);
        addInboundTypeChecker(CommandData.class);
        addInboundTypeChecker(MousePositionData.class);
    }

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(COMMAND_DATA, StringData.class);
        addOutboundTypeChecker(BML_DATA, BmlData.class);
        addOutboundTypeChecker(MOUSE_DATA, MousePositionData.class);
    }

    /**
     * Getting message ID
     * @return message ID
     */
    private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }
/*
	@Override
	public boolean act() {
		if (counterSeconds > counterThreshold) {
            this.counterSeconds = 0;
            
        } else {
            System.out.println("HndlCmd counter: "+counterSeconds);
            this.counterSeconds++;
        }
		return true;
	}
	*/
}
