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

package org.agent.slang.out.ispeech;

import com.iSpeech.ApiException;
import com.iSpeech.InvalidApiKeyException;
import com.iSpeech.TTSResult;
import com.iSpeech.iSpeechSynthesis;
import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.agent.slang.data.audio.AudioData;
import org.agent.slang.data.audio.InvalidAudioDataException;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.logger.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * This class used in combination with CereProcTTSComponent in order to provide output speech of agentslang.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/8/13
 */

@ConfigureParams(mandatoryConfigurationParams = {"voice", "apiKey"},
        outputChannels = "voice.data", outputDataTypes = AudioData.class,
        inputDataTypes = {StringData.class, GenericTextAnnotation.class})
public class ISpeechTTSComponent extends MixedComponent {
    private static final String voiceChannel = "voice.data";
    private static final String PROP_VOICE = "voice";
    private static final String PROP_API_KEY = "apiKey";
    private iSpeechSynthesis iSpeechTTS;

    public ISpeechTTSComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        try {
            iSpeechTTS = iSpeechSynthesis.getInstance(config.getProperty(PROP_API_KEY), false);
            iSpeechTTS.setOptionalCommand("format", "wav");

            if (config.hasProperty(PROP_VOICE)) {
                iSpeechTTS.setOptionalCommand("voice", config.getProperty(PROP_VOICE));
            } else {
                iSpeechTTS.setOptionalCommand("voice", "usenglishfemale");
            }
        } catch (InvalidApiKeyException e) {
            Logger.log(this, Logger.CRITICAL, "Invalid iSpeech API Key ", e);
        }
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
        if (data != null) {
            AudioData result = null;
            if (data instanceof StringData) {
                try {
                    AudioInputStream ais = buildAudioStream(((StringData) data).getData());
                    if (ais != null) {
                        result = new AudioData(data.getId(), ((StringData) data).getData());
                        result.buildAudioData(ais);
                    }
                } catch (InvalidAudioDataException e) {
                    Logger.log(this, Logger.CRITICAL, "Invalid audio data provided ", e);
                }
            } else if (data instanceof GenericTextAnnotation) {
                try {
                    AudioInputStream ais = buildAudioStream(((GenericTextAnnotation) data).getTranscription());
                    if (ais != null) {
                        result = new AudioData(data.getId(), ((GenericTextAnnotation) data).getTranscription());
                        result.buildAudioData(ais);
                    }
                } catch (InvalidAudioDataException e) {
                    Logger.log(this, Logger.CRITICAL, "Invalid audio data provided ", e);
                }
            }
            if (result != null) {
                publishData(voiceChannel, result);
            }
        }
    }
    
    /**
     * Building audio stream based on transcription text
     * @param transcription text to be converted into speech
     * @return audio stream
     */
    private AudioInputStream buildAudioStream(String transcription) {
        try {
            TTSResult result = iSpeechTTS.speak(transcription);
            DataInputStream in = result.getDataInputStream();

            return AudioSystem.getAudioInputStream(in);
        } catch (IOException | ApiException | UnsupportedAudioFileException e) {
            Logger.log(this, Logger.CRITICAL, "Invalid audio data provided ", e);
        }
        return null;
    }

    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
        addInboundTypeChecker(StringData.class);
        addInboundTypeChecker(GenericTextAnnotation.class);
    }

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(voiceChannel, AudioData.class);
    }
}
