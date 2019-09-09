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

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.agent.slang.dm.narrative.data.StateChangeData;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.LivingComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.data.LanguageUtils;
import org.ib.logger.Logger;
import org.ib.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * @author Adrien Fontaine, adrien.fontaine@insa-rouen.fr
 * @version 1, 07/04/17 - Initial commit, Adrien Fontaine
 */

@ConfigureParams(mandatoryConfigurationParams = {"bmlFilePath"},
		outputChannels = "bmlCommand.data", outputDataTypes = StringData.class,
        inputDataTypes = {StringData.class, StateChangeData.class})
public class MarcBMLAutonomousComponent extends LivingComponent {
    private static final String BML_FILE_PATH = "bmlFilePath";
    
    //topics
    private static final String BML_COMMAND_DATA = "bmlCommand.data";
    private final static long EXECUTION_TIMEOUT = 5 * 60 * 1000;

    private long bmlID = 0;
    private Long lastExecutionTimestamp = null;
    private long messageID;

    private Document bmlFile;
    private boolean test = true;
    
    private File bmlFilePath;
    private int currentStep;
    private boolean bmlExecuting;
    private boolean existStepBml;

    public MarcBMLAutonomousComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }
    
    /**
     * Component's setup method called at the creation of an instance. It uses SAXBuiler to parse the xml file containing all behaviors used.
     */
    protected void setupComponent(ComponentConfig config) {
    	this.bmlFilePath = config.getFileProperty(BML_FILE_PATH, "data", true);
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setIgnoringElementContentWhitespace(true);
            bmlFile = builder.build(new File(bmlFilePath,"autoBehaviors.xml"));

          } catch (JDOMException e) {
            e.printStackTrace(System.out);
          } catch (IOException e) {
            e.printStackTrace(System.out);
          }
        currentStep = 1;
        bmlExecuting = true;
        existStepBml = false;
    }

    public void defineReceivedData() {
        addInboundTypeChecker(StringData.class);
        addInboundTypeChecker(StateChangeData.class);
    }

    public void definePublishedData() {
        addOutboundTypeChecker(BML_COMMAND_DATA, StringData.class);
    }
    
    /**
     * Method called every heartbeat. Send a bml command randomly when no bml command is processed by MARC
     */
    public boolean act() {
    	//Send randomly a bml message
    	if(!bmlExecuting && (currentStep <= 9) && (currentStep >=0)){
    		int randomNum = ThreadLocalRandom.current().nextInt(1, 20 + 1);		//Random integer between 1 and 20
    		if((randomNum%7) == 0){
        		int index = ThreadLocalRandom.current().nextInt(1, 3 + 1);
    			Element rootElement = bmlFile.getRootElement();
            	List<Element> children = rootElement.getChildren("bml");
            	
                Iterator iterator = children.iterator();
                while (iterator.hasNext()) {
                	Element child = (Element) iterator.next();
                	
                	if (child.getAttribute("id").getValue().equals("bml-idle-"+index)){
                		publishData(BML_COMMAND_DATA, new StringData(getMessageID(), generateBML(child), LanguageUtils.getLanguageCodeByLocale(Locale.US)));
                	}
                } 
    		}
    	}

        return true;
    }

    /**
     * Method when receiving data from subscribed source(s). If data is an instance of StateChangeData then it publish a bml command associated
     *  with the current step. If data is an instance of StringData we must determine first were it came from. If it's a feeback from MARC
     *  then we set the bmlExecuting accordingly, else it's a response crafted by the dm component that we use to send the appropriate bml command
     * @param data data received from the subscribtion
     */
    protected void handleData(GenericData data) {
        if (data instanceof StateChangeData) {
        	//Send a bml message corresponding to the current step
            int newState = ((StateChangeData) data).getNewStateNumber();
            Logger.log(this, Logger.INFORM, "Step: " + newState);
            if (this.currentStep != newState){
            	this.currentStep = newState;
                
            	Element rootElement = bmlFile.getRootElement();
            	List<Element> children = rootElement.getChildren("bml");
            	
                Iterator iterator = children.iterator();
                while (iterator.hasNext()) {
                	Element child = (Element) iterator.next();
                	
                	if (child.getAttribute("id").getValue().equals("bml-step-"+(currentStep+1))){
                		publishData(BML_COMMAND_DATA, new StringData(getMessageID(), generateBML(child), LanguageUtils.getLanguageCodeByLocale(Locale.US)));
                		existStepBml = true;
                	}
                }  
                //If there's no bml corresponding we send the default step bml command
                if(!existStepBml){
                    Iterator iterator2 = children.iterator();
                    while (iterator2.hasNext()) {
                    	Element childDefault = (Element) iterator2.next();
                    	
                    	if (childDefault.getAttribute("id").getValue().equals("bml-step")){
                    		publishData(BML_COMMAND_DATA, new StringData(getMessageID(), generateBML(childDefault), LanguageUtils.getLanguageCodeByLocale(Locale.US)));
                    	}
                    }  
                }
                existStepBml = false;
            }
        } else if (data instanceof StringData){
            String message = ((StringData) data).getData();
            //Prevents from sending bml commands when we should'nt
            if (message.toLowerCase().contains("bml") && message.toLowerCase().contains(":end")) {
                bmlExecuting = false;
            }else if (message.toLowerCase().contains("bml") && message.toLowerCase().contains(":start")) {
                bmlExecuting = true;
            }
        	//Send a bml message corresponding to a lack of response or a response not understood
            if (message.toLowerCase().contains("i am listening to you") || message.toLowerCase().contains("please go on")){
            	Element rootElement = bmlFile.getRootElement();
            	List<Element> children = rootElement.getChildren("bml");
            	
                Iterator iterator = children.iterator();
                while (iterator.hasNext()) {
                	Element child = (Element) iterator.next();
                	
                	if (child.getAttribute("id").getValue().equals("bml-silence")){
                		publishData(BML_COMMAND_DATA, new StringData(getMessageID(), generateBML(child), LanguageUtils.getLanguageCodeByLocale(Locale.US)));
                	}
                } 
            } else if (message.toLowerCase().contains("could you tell it in another way") || message.toLowerCase().contains("could you rephrase it")){
            	Element rootElement = bmlFile.getRootElement();
            	List<Element> children = rootElement.getChildren("bml");

                Iterator iterator = children.iterator();
                while (iterator.hasNext()) {
                	Element child = (Element) iterator.next();
                	
                	if (child.getAttribute("id").getValue().equals("bml-not_understood")){
                		publishData(BML_COMMAND_DATA, new StringData(getMessageID(), generateBML(child), LanguageUtils.getLanguageCodeByLocale(Locale.US)));
                	}
                } 
            }
        }
    }
    
    /**
     * Generate a integer as an ID for a StringData message.
     */
    private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }

    /**
     * Recursive method that translate a BML element (JDOM) into a string
     * @param element BML Element (or sub-element) to translate into a string
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
        //Take out all carriage return lines that are not read correctly by MARC
        message = message.replaceAll("[\r\n\t]+", "");
    	
    	return message;
    }
}