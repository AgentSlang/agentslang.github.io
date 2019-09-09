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

package org.agent.slang.data.audio;

import org.ib.data.IdentifiableData;
import org.ib.data.LanguageDependentData;
import org.ib.data.LanguageUtils;
import org.ib.data.TypeIdentification;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * A data type store audio data characteristics.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/14/12
 */

@TypeIdentification(typeID = 6)
public class AudioData implements IdentifiableData, LanguageDependentData {
    private long id;
    private int language;
    private String textTranscription;

    //-- audio data
    private byte[] audioData;
    private long audioLength;
    // -- audio format
    private String encodingName;
    private float sampleRate;
    private int sampleSizeInBits;
    private int channels;
    private int frameSize;
    private float frameRate;
    private boolean bigEndian;

    public AudioData() {
    }

    /**
     * AudioData data type constructor 
     * @param id id of input data
     * @param textTranscription transcription text related to input audio data
     */
    public AudioData(long id, String textTranscription) {
        this.id = id;
        this.textTranscription = textTranscription;
    }

    /**
     * Building audio data based on input stream
     */
    public void buildAudioData(AudioInputStream inputStream) throws InvalidAudioDataException {
        audioLength = inputStream.getFrameLength();
        encodingName = inputStream.getFormat().getEncoding().toString();
        sampleRate = inputStream.getFormat().getSampleRate();
        sampleSizeInBits = inputStream.getFormat().getSampleSizeInBits();
        channels = inputStream.getFormat().getChannels();
        frameSize = inputStream.getFormat().getFrameSize();
        frameRate = inputStream.getFormat().getFrameRate();
        bigEndian = inputStream.getFormat().isBigEndian();

        audioData = new byte[(int) (frameSize * audioLength)];
        try {
            int position = 0;
            byte[] buffer = new byte[1024];

            int currentSize;
            while ((currentSize = inputStream.read(buffer)) > 0) {
                System.arraycopy(buffer, 0, audioData, position, currentSize);
                position += currentSize;
            }
        } catch (IOException e) {
            throw new InvalidAudioDataException("The input stream seems invalid ...", e);
        }
    }

    public AudioInputStream getAudioStream() {
        AudioFormat format = new AudioFormat(new AudioFormat.Encoding(encodingName), sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);

        return new AudioInputStream(new ByteArrayInputStream(audioData), format, audioLength);
    }

    public long getId() {
        return id;
    }

    /**
     * gets text transcription of audio dat
     * @return text transcription of audio data
     */
    public String getTextTranscription() {
        return textTranscription;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public Locale getLocale() {
        return LanguageUtils.getLocaleByCode(language);
    }

    /**
     * setting text transcription of audio data
     * @param textTranscription text transcription of audio data
     */
    public void setTextTranscription(String textTranscription) {
        this.textTranscription = textTranscription;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    public long getAudioLength() {
        return audioLength;
    }

    public void setAudioLength(long audioLength) {
        this.audioLength = audioLength;
    }

    public String getEncodingName() {
        return encodingName;
    }

    public void setEncodingName(String encodingName) {
        this.encodingName = encodingName;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }

    public void setSampleSizeInBits(int sampleSizeInBits) {
        this.sampleSizeInBits = sampleSizeInBits;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public void setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }

    public String toString() {
        return textTranscription;
    }
}
