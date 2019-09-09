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

package org.agent.slang.in.SpeechRecognizer;

import org.agent.slang.data.audio.AudioData;
import org.agent.slang.data.audio.PlayerEvent;
//Sahba modification starts, Version 2
import org.agent.slang.dm.narrative.data.StateChangeData;
//Sahba modification ends, Version 2
import org.agent.slang.dm.narrative.data.patterns.PatternsStates;
import org.agent.slang.in.SpeechRecognizer.SpeechRecognizerSocket.DataListener;
import org.agent.slang.out.bml.marc.socket.MarcSocket;
import org.agent.slang.out.bml.marc.socket.TCPSocket;
import org.agent.slang.out.bml.marc.socket.UDPSocket;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import org.ib.logger.Logger;
import org.ib.utils.FileUtils;

import marytts.util.string.StringUtils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;


/**
 * This class provides agentslang with receiving speech related information and text from an external application that we developed using Microsoft Speech API via a message passing environment. 
 * OS Compatibility: Windows
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @author sahba ZOJAJI, sahba.zojaji@insa-rouen.fr
 * @version 1, 10/19/13
 * 			2, 17/11/2016 - Customize Audio Path and adding the ability of playing user defined audio files plus machine generated voices, Sahba ZOJAJI
 */

@ConfigureParams(optionalConfigurationParams = {"SpeechRecognizerHostname", "SpeechRecognizerInPort", "SpeechRecognizerOutPort"},
        outputChannels = {"voice.data"},
        outputDataTypes = {StringData.class},
        inputDataTypes = {PlayerEvent.class})
public class SpeechRecognitionComponent extends MixedComponent implements SpeechRecognizerSocket.DataListener {
    private static final String PROP_SOCKET_HOST = "SpeechRecognizerHostname";
    private static final String PROP_SOCKET_IN_PORT = "SpeechRecognizerInPort";
    private static final String PROP_SOCKET_OUT_PORT = "SpeechRecognizerOutPort";

    //topics
    private static final String SpeechRecognitionData = "voice.data";
    private SpeechRecognizerSocket socket;
    private long messageID;
    boolean isSpeaking;

    public SpeechRecognitionComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        String hostname = "localhost";
        int intPort = 5011;
        int outPort = 5012;
        messageID = 0;
        isSpeaking = false;

        if (config.hasProperty(PROP_SOCKET_HOST)) {
            hostname = config.getProperty(PROP_SOCKET_HOST);
        }

        if (config.hasProperty(PROP_SOCKET_IN_PORT)) {
            try {
                intPort = Integer.parseInt(config.getProperty(PROP_SOCKET_IN_PORT));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid inPort provided", e);
            }
        }

        if (config.hasProperty(PROP_SOCKET_OUT_PORT)) {
            try {
                outPort = Integer.parseInt(config.getProperty(PROP_SOCKET_OUT_PORT));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid outPort provided", e);
            }
        }

        Logger.log(this, Logger.INFORM, String.format("Using Speech Recognizer Connections Properties hostname='%s' inPort=%d outPort=%d", hostname, intPort, outPort));
        System.out.println(String.format("Using Speech Recognizer Connections Properties hostname='%s' inPort=%d outPort=%d", hostname, intPort, outPort));

            try {
                socket = new SpeechUDPSocket(hostname, intPort, outPort, this);
            } catch (IOException e) {
                Logger.log(this, Logger.CRITICAL, "Speech Recogniser Invalid properties for socket", e);
                System.out.println("Speech Recogniser Invalid properties for socket" + e.toString());
           
            }
    }

    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
//        addInboundTypeChecker(StringData.class);
        addInboundTypeChecker(PlayerEvent.class);
    }

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(SpeechRecognitionData, StringData.class);
    }

    /**
     * processes received data from External Automatic Speech Recognition application
     */
    public void dataReceived(String message) {
    	if(!isSpeaking || message.equals("SpeechStarted") || message.equals("SpeechEnd"))
    	{
        Logger.log(this, Logger.INFORM, "From speech Recognizer : " + message);
        publishData(SpeechRecognitionData, new StringData(getMessageID(), message, LanguageUtils.getLanguageCodeByLocale(Locale.FRANCE)));
    	}
    }
    
    /**
     * Gets message id
     * @return message id
     */
    private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
    	 if (data instanceof PlayerEvent) {
        	if (PlayerEvent.EVENT_START == ((PlayerEvent) data).getEvent()) {
        		System.out.println("SpeechRecoComp : EVENT_START " +((PlayerEvent) data).getEvent() );
        		isSpeaking = true;
        	//	socket.sendMessage("PLAYER_EVENT_START");
        	}
        	else if (PlayerEvent.EVENT_STOP == ((PlayerEvent) data).getEvent()) {
        		System.out.println("SpeechRecoComp : EVENT_STOP " +((PlayerEvent) data).getEvent() );
        		isSpeaking = false;
        	//	socket.sendMessage("PLAYER_EVENT_STOP");
            }
        } 
    }

    /**
     * closes communication socket
     */
    public void close() {
        super.close();

        if (socket != null) {
            socket.close();
        }
    }
}
