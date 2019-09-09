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

package org.agent.slang.out.cereproc;

import com.cereproc.cerevoice_eng.*;
import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.agent.slang.data.audio.AudioData;
import org.agent.slang.data.audio.InvalidAudioDataException;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.logger.Logger;
import org.ib.utils.FileUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;

/**
 * Provides TTS ability for agentslang using CereProc TTS engine.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 9/27/13
 */

@ConfigureParams(mandatoryConfigurationParams = {"voice", "licenseFile"},
        outputChannels = "voice.data", outputDataTypes = AudioData.class,
        inputDataTypes = {StringData.class, GenericTextAnnotation.class})
public class CereProcTTSComponent extends MixedComponent {
    private static final String voiceChannel = "voice.data";
    private static final String PROP_VOICE = "voice";
    private static final String PROP_LICENSE_FILE = "licenseFile";

    static {
        System.loadLibrary("cerevoice_eng");
    }

    private SWIGTYPE_p_CPRCEN_engine cereProcEngine;
    private int chanelHandle;
    private float sampleRate;
    //    private CereProcCallback callback;
    private AudioFormat defaultFormat = null;

    public CereProcTTSComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        File licencePath = config.getFileProperty(PROP_LICENSE_FILE, true);
        FileUtils.checkReadableFile(licencePath, true);

        File voicePath = config.getFileProperty(PROP_VOICE, true);
        FileUtils.checkReadableFile(voicePath, true);

        cereProcEngine = cerevoice_eng.CPRCEN_engine_new();
        int res = cerevoice_eng.CPRCEN_engine_load_voice(cereProcEngine, licencePath.getAbsolutePath(), "", voicePath.getAbsolutePath(), CPRC_VOICE_LOAD_TYPE.CPRC_VOICE_LOAD);
        if (res == 0) {
            Logger.log(this, Logger.CRITICAL, "Invalid CereProc License or voice model provided.");
        }

        chanelHandle = cerevoice_eng.CPRCEN_engine_open_default_channel(cereProcEngine);
        if (chanelHandle == 0) {
            Logger.log(this, Logger.CRITICAL, "Unable to open the default CereProc model.");
        }

        sampleRate = Float.parseFloat(cerevoice_eng.CPRCEN_channel_get_voice_info(cereProcEngine, chanelHandle, "SAMPLE_RATE"));

//        callback = new CereProcCallback();
//        callback.SetCallback(cereProcEngine, chanelHandle);
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
        if (data != null) {
            if (data instanceof StringData) {
                buildAudioStream(data.getId(), ((StringData) data).getData());
            } else if (data instanceof GenericTextAnnotation) {
                buildAudioStream(data.getId(), ((GenericTextAnnotation) data).getTranscription());
            }
        }
    }

    /**
     * Building Auido stream based on received text
     * @param id Audio data ID
     * @param transcription text to be converted into speech
     */
    private synchronized void buildAudioStream(long id, String transcription) {
        transcription = transcription + "\n";
        byte[] transcriptionBytes = transcription.getBytes(Charset.forName("UTF-8"));
        processBuffer(id, transcription, cerevoice_eng.CPRCEN_engine_channel_speak(cereProcEngine, chanelHandle, transcription, transcriptionBytes.length, 1));
        cerevoice_eng.CPRCEN_engine_clear_callback(cereProcEngine, chanelHandle);
//        cerevoice_eng.CPRCEN_engine_channel_speak(cereProcEngine, chanelHandle, "", 0, 1);
    }

    private synchronized void sendTranscription(long id, String transcription, byte[] buffer, String phonemes) {
        try {
        	//System.out.println("\n\n\n\n\n\nphonemes:\n"+phonemes);
            AudioFormat format = getAudioFormat();
            AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(buffer), format, buffer.length / format.getFrameSize());
            AudioData result = new AudioData(id, transcription);
            result.buildAudioData(ais);
            publishData(voiceChannel, result);

            Logger.log(this, Logger.INFORM, "CereProc Phonemes: " + phonemes);
        } catch (InvalidAudioDataException e) {
            Logger.log(this, Logger.CRITICAL, "CereProc exception while sending transcription.", e);
        }
    }

    public void processBuffer(long id, String transcription, SWIGTYPE_p_CPRC_abuf cbuffer) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbM = new StringBuilder();
        for (int i = 0; i < cerevoice_eng.CPRC_abuf_trans_sz(cbuffer); i++) {
            SWIGTYPE_p_CPRC_abuf_trans trans = cerevoice_eng.CPRC_abuf_get_trans(cbuffer, i);
            CPRC_ABUF_TRANS_TYPE transtype = cerevoice_eng.CPRC_abuf_trans_type(trans);
            float start = cerevoice_eng.CPRC_abuf_trans_start(trans);
            float end = cerevoice_eng.CPRC_abuf_trans_end(trans);
            String name = cerevoice_eng.CPRC_abuf_trans_name(trans);

            if (transtype == CPRC_ABUF_TRANS_TYPE.CPRC_ABUF_TRANS_PHONE) {
                sb.append(String.format("%.3f %.3f %s ", start, end, name));
            } else if (transtype == CPRC_ABUF_TRANS_TYPE.CPRC_ABUF_TRANS_MARK) {
                sbM.append(String.format("%.3f %.3f %s ", start, end, name));
            }
        }

        Logger.log(this, Logger.INFORM, "Phrase marks = " + sbM.toString());

        int bufferSize = cerevoice_eng.CPRC_abuf_wav_sz(cbuffer);
        byte[] b = new byte[bufferSize * 2];
        for (int i = 0; i < bufferSize; i++) {
            short sample = cerevoice_eng.CPRC_abuf_wav(cbuffer, i);
            // The sample is written in Big Endian to the buffer
            b[i * 2] = (byte) ((sample & 0xff00) >> 8);
            b[i * 2 + 1] = (byte) (sample & 0x00ff);
        }

        sendTranscription(id, transcription, b, sb.toString());
    }

    /**
     * Gets audio data format
     * @return audio format
     */
    private AudioFormat getAudioFormat() {
        if (defaultFormat == null || defaultFormat.getSampleRate() != sampleRate) {
            int sampleSizeInBits = 16;
            int channels = 1;
            boolean signed = true;
            boolean bigEndian = true;
            defaultFormat = new AudioFormat(
                    sampleRate,
                    sampleSizeInBits,
                    channels,
                    signed,
                    bigEndian);
        }
        return defaultFormat;
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

    /**
     * Closes and stops CereProc engine.
     */
    public void close() {
        super.close();

//        callback.ClearCallback(cereProcEngine, chanelHandle);
        cerevoice_eng.CPRCEN_engine_channel_close(cereProcEngine, chanelHandle);
        cerevoice_eng.CPRCEN_engine_delete(cereProcEngine);
    }

//    private class CereProcCallback extends TtsEngineCallback {
//        public void Callback(SWIGTYPE_p_CPRC_abuf cbuffer) {
//            processBuffer(cbuffer);
//        }
//    }
}
