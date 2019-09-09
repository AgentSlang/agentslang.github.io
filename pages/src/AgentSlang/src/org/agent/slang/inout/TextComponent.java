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

package org.agent.slang.inout;

import org.agent.slang.audio.AudioPlayer;
import org.agent.slang.data.audio.AudioData;
import org.agent.slang.data.audio.PlayerEvent;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import org.ib.data.SystemEvent;

import java.util.Locale;

/**
 * This component provides a text interaction module that sends and receives text messages.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/8/12
 */
@ConfigureParams(outputChannels = {"text.data", "audioPlayer.data"},
        outputDataTypes = {StringData.class, PlayerEvent.class},
        inputDataTypes = {GenericData.class})
public class TextComponent extends MixedComponent implements AudioPlayer.AudioPlayerListener {
    private static final String textData = "text.data";
    private static final String audioPlayerData = "audioPlayer.data";

    private TextTerminal textTerminal;
    private long messageID;
    private AudioPlayer audioPlayer;

    public TextComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        textTerminal = new TextTerminal(this);
        textTerminal.setVisible(true);
        textTerminal.setLocationRelativeTo(null);

        audioPlayer = new AudioPlayer();
        audioPlayer.addStatusListener(this);
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
        if (data instanceof AudioData) {
            if (((AudioData) data).getAudioStream() != null) {
                audioPlayer.playAudio(((AudioData) data).getTextTranscription(), ((AudioData) data).getAudioStream());
            }
        } else if (data != null) {
        	//naser
        	textTerminal.handleMessage(data.toString());
        	
        	String rcvData = ((StringData) data).getData();
        	
        
            if (!rcvData.equals("SpeechStarted") && !rcvData.equals("SpeechEnd")) {
            	System.out.println("data received by text terminal : " + data.toString());
            	textTerminal.handleMessage(data.toString());
            }
            
            
        }
    }

    /**
     * Checks system working status (on or off)
     */
    protected void handleSystemEvents(SystemEvent event) {
        if (event.getEvent() == SystemEvent.SYSTEM_WAKE) {
            textTerminal.handleMessage("** System Ready ...");
        } else if (event.getEvent() == SystemEvent.SYSTEM_SHUTDOWN) {
            textTerminal.handleMessage("** System is shutting down ...");
        }
    }

    /**
     * checks the status of audio data player when updated (event: start, stop)
     */
    public void audioPlayerStatusUpdated(String audioLabel, AudioPlayer.AudioPlayerEvent event) {
        if (event == AudioPlayer.AudioPlayerEvent.start) {
            textTerminal.handleMessage("** Playing audio data: " + audioLabel);
            publishData(audioPlayerData, new PlayerEvent(0, PlayerEvent.EVENT_START));
        } else if (event == AudioPlayer.AudioPlayerEvent.stop) {
            textTerminal.handleMessage("** Stopping audio player ...");
            publishData(audioPlayerData, new PlayerEvent(0, PlayerEvent.EVENT_STOP));
        }
    }

    /**
     * Gets message ID
     * @return message ID
     */
    private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }

    /**
     * Sends text message.
     * @param message text message
     */
    protected void sendMessage(String message) {
        publishData(textData, new StringData(getMessageID(), message, LanguageUtils.getLanguageCodeByLocale(Locale.US)));
    }

    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
        addInboundTypeChecker(GenericData.class);
    }

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(textData, StringData.class);
        addOutboundTypeChecker(audioPlayerData, PlayerEvent.class);
    }
}
