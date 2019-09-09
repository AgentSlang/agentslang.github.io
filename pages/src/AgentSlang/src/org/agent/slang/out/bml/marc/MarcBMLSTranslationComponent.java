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
import org.agent.slang.data.audio.AudioData;
import org.agent.slang.data.audio.PlayerEvent;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.data.LanguageUtils;
import org.ib.logger.Logger;
import org.ib.utils.FileUtils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Adrien Fontaine, adrien.fontaine@insa-rouen.fr
 * @version 1, 07/04/17 - Initial commit, Adrien Fontaine
 */

@ConfigureParams(outputChannels = {"audioPlayer.data","bmlCommand.data"}, outputDataTypes = {PlayerEvent.class, StringData.class},
        inputDataTypes = {AudioData.class, StringData.class})
public class MarcBMLSTranslationComponent extends MixedComponent {
    private static final String PROP_CACHE = "audioCache";
    private static final String DEFAULT_CACHE = "MARCAudioCache";
    //topics
    private static final String audioPlayerData = "audioPlayer.data";
    private static final String BML_COMMAND_DATA = "bmlCommand.data";
    private final static long EXECUTION_TIMEOUT = 5 * 60 * 1000;

    private File audioCache;
    private long bmlID = 0;
    private Long lastExecutionTimestamp = null;
    
    private long messageID;

    public MarcBMLSTranslationComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }
    
    /**
     * Component's setup method called at the creation of an instance. It initializes the audio cache directory.
     */
    protected void setupComponent(ComponentConfig config) {
        audioCache = FileUtils.createCacheDir(this, config.getFileProperty(PROP_CACHE, DEFAULT_CACHE, false));
        Logger.log(this, Logger.INFORM, "MARC Audio Cache Path = " + audioCache.getAbsolutePath());
        System.out.println("MARC Audio Cache Path = " + audioCache.getAbsolutePath());
    }

    public void defineReceivedData() {
        addInboundTypeChecker(StringData.class);
        addInboundTypeChecker(AudioData.class);
    }

    public void definePublishedData() {
        addOutboundTypeChecker(audioPlayerData, PlayerEvent.class);
        addOutboundTypeChecker(BML_COMMAND_DATA, StringData.class);
    }
    
    /**
     * Method when receiving data from subscribed source(s). If data is an instance of AudioData we publish the bml command made from it.
     * Else, we must deal with a StringData feedback from MARC that sets the publication of a PlayerEvent destined to the PatternMatching component.
     * @param data data received from the subscribtion
     */
    protected void handleData(GenericData data) {
    	if (data instanceof AudioData){
	        String bmlID = generateBMLID();
	        String audioPath = saveToFile(bmlID, (AudioData) data);
	        if (audioPath != null) {
	        	String message = generateBML(bmlID, audioPath);
	            publishData(BML_COMMAND_DATA, new StringData(getMessageID(), message, LanguageUtils.getLanguageCodeByLocale(Locale.US)));
	        }
    	} else if (data instanceof StringData) {
    		String message = ((StringData) data).getData();
	        Logger.log(this, Logger.INFORM, "From MARC: " + message);
	        if (message.toLowerCase().contains("bml:start")) {
	            publishData(audioPlayerData, new PlayerEvent(0, PlayerEvent.EVENT_START));
	            System.out.println("Sending player start message !");
	        } else if (message.toLowerCase().contains("bml:end")) {
	            publishData(audioPlayerData, new PlayerEvent(0, PlayerEvent.EVENT_STOP));
	            System.out.println("Sending player stop message !");
	        }
        }
    }

    private String saveToFile(String bmlID, AudioData data) {
        File audioFileData = new File(audioCache, "audio-" + bmlID + ".wav");
        try {
            AudioSystem.write(data.getAudioStream(), AudioFileFormat.Type.WAVE, audioFileData);
            return audioFileData.getAbsolutePath();
        } catch (IOException e) {
            Logger.log(this, Logger.CRITICAL, "Invalid save path = " + audioFileData.getAbsolutePath());
            return null;
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
     * Generate a unique string as an ID for a bml element.
     */
    private String generateBMLID() {
        return "bml-" + (bmlID++);
    }

    /**
     * Generate a bml command using an ID and an audio file path.
     * @param bmlID ID of the bml element
     * @param audioPath Path to the audio file received by the component
     */
    private String generateBML(String bmlID, String audioPath) {
        return "<bml><speech id=\"" + bmlID + "\" marc:file=\"" + audioPath + "\" marc:articulate=\"1.00\" /></bml>";
    }
}

