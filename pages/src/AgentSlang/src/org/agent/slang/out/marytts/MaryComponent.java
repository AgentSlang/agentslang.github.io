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

package org.agent.slang.out.marytts;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.agent.slang.data.audio.AudioData;
import org.agent.slang.data.audio.InvalidAudioDataException;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.logger.Logger;

import java.util.Locale;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/8/12
 */
@ConfigureParams(optionalConfigurationParams = "locale",
        outputChannels = "voice.data", outputDataTypes = AudioData.class,
        inputDataTypes = {StringData.class, GenericTextAnnotation.class})
public class MaryComponent extends MixedComponent {
    private static final String voiceChannel = "voice.data";
    private static final String PROP_LOCALE = "locale";
    private MaryInterface maryInterface;

    public MaryComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    protected void setupComponent(ComponentConfig config) {
        try {
            maryInterface = new LocalMaryInterface();

            Locale locale;
            if (config.hasProperty(PROP_LOCALE)) {
                String[] parts = config.getProperty(PROP_LOCALE).split("_");
                if (parts.length > 1) {
                    locale = new Locale(parts[0], parts[1]);
                } else {
                    locale = new Locale(parts[0]);
                }
            } else {
                locale = Locale.US;
            }
            maryInterface.setLocale(locale);

            Logger.log(this, Logger.INFORM, "I currently have " + maryInterface.getAvailableVoices() + " voices in "
                    + maryInterface.getAvailableLocales() + " languages available.");
            Logger.log(this, Logger.INFORM, "Out of these, " + maryInterface.getAvailableVoices(locale) + " are for locale " + locale + ".");

            //test the generation
            try {
                if (AudioConsumer.consume(maryInterface.generateAudio("This is just a test"), true)) {
                    Logger.log(this, Logger.INFORM, "MaryTTS is up and ready to go ...");
                } else {
                    Logger.log(this, Logger.CRITICAL, "Something went wrong with MaryTTS. Check the config and try again ...");
                }
            } catch (SynthesisException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid synthesis ", e);
            }
        } catch (MaryConfigurationException e) {
            Logger.log(this, Logger.CRITICAL, "Invalid Mary configuration", e);
        }
    }

    protected void handleData(GenericData data) {
        if (data != null) {
            AudioData result = null;
            if (data instanceof StringData) {
                result = new AudioData(data.getId(), ((StringData) data).getData());
                try {
                    result.buildAudioData(maryInterface.generateAudio(result.getTextTranscription()));
                } catch (InvalidAudioDataException e) {
                    Logger.log(this, Logger.CRITICAL, "Invalid audio data provided ", e);
                } catch (SynthesisException e) {
                    Logger.log(this, Logger.CRITICAL, "Invalid synthesis ", e);
                }
            } else if (data instanceof GenericTextAnnotation) {
                result = new AudioData(data.getId(), ((GenericTextAnnotation) data).getTranscription());
                try {
                    result.buildAudioData(maryInterface.generateAudio(result.getTextTranscription()));
                } catch (InvalidAudioDataException e) {
                    Logger.log(this, Logger.CRITICAL, "Invalid audio data provided ", e);
                } catch (SynthesisException e) {
                    Logger.log(this, Logger.CRITICAL, "Invalid synthesis ", e);
                }
            }
            if (result != null) {
                publishData(voiceChannel, result);
            }
        }
    }

    public void defineReceivedData() {
        addInboundTypeChecker(StringData.class);
        addInboundTypeChecker(GenericTextAnnotation.class);
    }

    public void definePublishedData() {
        addOutboundTypeChecker(voiceChannel, AudioData.class);
    }
}
