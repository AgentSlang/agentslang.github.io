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

import org.agent.slang.data.audio.AudioData;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import org.ib.utils.FileUtils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/19/13
 */

@ConfigureParams(mandatoryConfigurationParams = {"inputFilename", "audioCache"},
        outputChannels = "text.data", outputDataTypes = StringData.class,
        inputDataTypes = GenericData.class)
public class TerminalIOComponentHack extends MixedComponent {
    private static final String terminalChannel = "text.data";
    private static final String PROP_INPUT_FILE = "inputFilename";
    private static final String PROP_AUDIO_CACHE = "audioCache";
    private Thread terminalThread;
    private boolean running = true;
    private int languageCode = LanguageUtils.getLanguageCodeByLocale(LanguageUtils.getLanguage("en-US"));
    private File inputFilename;
    private File audioCache;
    private long messageID = 0;

    public TerminalIOComponentHack(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    protected void setupComponent(ComponentConfig config) {
        inputFilename = config.getFileProperty(PROP_INPUT_FILE);

        audioCache = FileUtils.createCacheDir(this, config.getProperty(PROP_AUDIO_CACHE, ""));

        terminalThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //-- wait for the terminal to be active
                }
                BufferedReader reader = null;
                if (inputFilename == null) {
                    reader = new BufferedReader(new InputStreamReader(System.in));
                } else {
                    try {
                        reader = new BufferedReader(new FileReader(inputFilename));
                    } catch (FileNotFoundException e) {
                        System.err.println("File Not Found: " + inputFilename.getAbsolutePath());
                        System.exit(1);
                    }
                }
                try {
                    String line;
                    System.out.println("Starting TerminalIO ... using:");
                    System.out.println("\tInput: " + (inputFilename == null ? "terminal" : inputFilename.getAbsolutePath()));
                    while (running && ((line = reader.readLine()) != null)) {
                        if (line.trim().length() > 0) {
                            line = "<speak><s><prosody rate=\"x-slow\">" + line + "</prosody></s></speak>";
                            publishData(terminalChannel, new StringData(getMessageID(), line, languageCode));
                        }
                    }
                    reader.close();
                    System.out.println("TerminalIO stopped !");
                } catch (IOException e) {
                    //-- ignore
                }
            }
        });
        terminalThread.start();
    }

    private long getMessageID() {
        messageID++;
        if (messageID == 21) {
            messageID = 22;
        }
        return messageID;
    }

    private String saveToFile(long fileID, AudioData data) {
        File audioFileData = new File(audioCache, String.format("st%02d.wav", fileID));
        try {
            AudioSystem.write(data.getAudioStream(), AudioFileFormat.Type.WAVE, audioFileData);
            return audioFileData.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Invalid save path = " + audioFileData.getAbsolutePath());
            return null;
        }
    }

    protected void handleData(GenericData data) {
        if (data instanceof AudioData) {
            //save the data
            String filename = saveToFile(data.getId(), (AudioData) data);
            System.out.println("Audio Data saved: " + filename);
        } else {
            System.out.println(data.toString());
        }
    }

    public void defineReceivedData() {
        addInboundTypeChecker(GenericData.class);
    }

    public void definePublishedData() {
        addOutboundTypeChecker(terminalChannel, StringData.class);
    }

    public void close() {
        super.close();
        running = false;
        terminalThread.interrupt();
    }
}
