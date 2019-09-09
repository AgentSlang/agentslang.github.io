package org.agent.slang.decision;

import java.io.File;

import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.agent.slang.data.audio.PlayerEvent;
import org.agent.slang.data.simple.BmlData;
import org.agent.slang.dm.narrative.data.StateChangeData;
import org.agent.slang.dm.narrative.data.commands.CommandData;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.utils.FileUtils;

/**
 * The DecisionMakerComponent is used in order to making decisions. It is based in ACT-R cognitive architecture.
 * OS Compatibility: Windows, Linux version has been not tested
 * @author Sahba Zojaji, sahba.zojaji@insa-rouen.fr
 * @version 1, 20/05/2017
 */

@ConfigureParams(mandatoryConfigurationParams = "modelPath",
				outputChannels = {"text.data","senna.data","stateChange.data","audioPlayer.data","command.data","bml.data"},				
				outputDataTypes = {StringData.class,GenericTextAnnotation.class,StateChangeData.class,PlayerEvent.class,CommandData.class,BmlData.class},
				inputDataTypes = {StringData.class,GenericTextAnnotation.class,StateChangeData.class,PlayerEvent.class,CommandData.class,BmlData.class})
public class DecisionMakerComponent extends MixedComponent{
	private static final String PROP_MODEL_PATH = "modelPath";
	private static final String textData = "text.data";
	private static final String SENNA_OUT = "senna.data";
	private static final String STATE_CHANGE_DATA = "stateChange.data";
	private static final String audioPlayerData = "audioPlayer.data";
	private static final String COMMAND_DATA = "command.data";
	private static final String BML_DATA = "bml.data";
	private File actrModel;
	
	private int currentStep = -100000;

	public DecisionMakerComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }
	
	/**
	 * Setting up the component based on input configuration.
	 * @param config component configuration
	 */
	protected void setupComponent(ComponentConfig config) {
        actrModel = config.getFileProperty(PROP_MODEL_PATH, "data", true);
        FileUtils.checkReadableFile(actrModel, true);
        System.out.println("\nmodelPath: "+actrModel);
    }

	/**
     * Checking type of output data.
     */
	@Override
	public void definePublishedData() {
        addOutboundTypeChecker(textData, StringData.class);
        addOutboundTypeChecker(SENNA_OUT, GenericTextAnnotation.class);
        addOutboundTypeChecker(STATE_CHANGE_DATA, StateChangeData.class);
        addOutboundTypeChecker(audioPlayerData, PlayerEvent.class);
        addOutboundTypeChecker(COMMAND_DATA, CommandData.class);
        addOutboundTypeChecker(BML_DATA, BmlData.class);
	}

	/**
     * Checking type of input data 
     */
	@Override
	public void defineReceivedData() {
		addInboundTypeChecker(StringData.class);
		addInboundTypeChecker(GenericTextAnnotation.class);
		addInboundTypeChecker(StateChangeData.class);
		addInboundTypeChecker(PlayerEvent.class);
		addInboundTypeChecker(CommandData.class);
		addInboundTypeChecker(BmlData.class);
	}

	/**
     * Managing input and output data in the class.
     * @param data input data
     */
	@Override
	protected void handleData(GenericData data) {
		//System.out.println("\n*******\nZJ, Decision Making receied data:  "+data);
		//recieved data from TextComponent
		if (data instanceof StringData) {
			//System.out.println("\nZJ, Text terminal StringData\n*******\n");
			
			DecisionMakingTask dt = new DecisionMakingTask();
	        //dt.create(actrModel);
			
	        publishData(textData, data);
        }
		//recieved data from SennaComponent 
		if (data instanceof GenericTextAnnotation)
		{
			//System.out.println("\nZJ, senna GenericTextAnnotation\n*******\n");
			publishData(SENNA_OUT, data);
		}
		//recieved data from StoryGraphComponent, PatternMatchingComponent, OutOfContextComponent
		if (data instanceof StateChangeData) {
			//System.out.println("\nZJ, StoryGraphComponent, PatternMatchingComponent or OutOfContextComponent StateChangeData\n*******\n");
			int newState = ((StateChangeData) data).getNewStateNumber();
			//System.out.println("\nZJ, new state = "+newState+"     current state = "+currentStep);
            if (this.currentStep != newState) {
                this.currentStep = newState;
                publishData(STATE_CHANGE_DATA, data);
            }
        } 
		//recieved data from MarcBMLTranslationComponent
		if (data instanceof PlayerEvent) { 
			//System.out.println("\nZJ, MarcBMLTranslationComponent PlayerEvent\n*******\n");
			publishData(audioPlayerData, data);
		}		
		//recieved data from PatternMatchingComponent
		if (data instanceof CommandData) { 
			//System.out.println("\nZJ, PatternMatchingComponent CommandData\n*******\n");
			publishData(COMMAND_DATA, data);
		}
		//recieved data from HandleCommandComponent
		if (data instanceof BmlData) { 
			//System.out.println("\nZJ, HandleCommandComponent BmlData\n*******\n");
			publishData(BML_DATA, data);
		}
	}

}
