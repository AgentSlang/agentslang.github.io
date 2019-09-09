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
import java.util.HashMap;
import java.util.Map;

/**
 * This component enables agentslang to communicate with terminal and managing its input and output through terminal (reading and writing files).
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/19/13
 */

@ConfigureParams(optionalConfigurationParams = {"inputFilename", "outputFilename", "audioCache", "inputMultiline"},
        outputChannels = "terminal.data", outputDataTypes = StringData.class,
        inputDataTypes = GenericData.class)
public class TerminalIOComponent extends MixedComponent implements AudioPlayer.AudioPlayerListener {
    private static final String terminalChannel = "terminal.data";
    private static final String PROP_INPUT_FILE = "inputFilename";
    private static final String PROP_INPUT_MULTILINE = "inputMultiline";
    private static final String PROP_OUTPUT_FILE = "outputFilename";
    private static final String PROP_AUDIO_CACHE = "audioCache";

    private Thread terminalThread;
    private boolean running = true;
    private int languageCode = LanguageUtils.getLanguageCodeByLocale(LanguageUtils.getLanguage("en-US"));

    private File inputFilename;
    private boolean inputMultiline;
    private File outputFilename;
    private PrintWriter out;
    private File audioCache;

    private Map<Long, String> filenameMapping = new HashMap<Long, String>();
    private AudioPlayer audioPlayer = new AudioPlayer();

    private long messageID = 0;

    public TerminalIOComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        inputFilename = config.getFileProperty(PROP_INPUT_FILE);
        outputFilename = config.getFileProperty(PROP_OUTPUT_FILE);

        inputMultiline = "true".equals(config.getProperty(PROP_INPUT_MULTILINE, "false").trim().toLowerCase());

        audioCache = FileUtils.createCacheDir(this, config.getProperty(PROP_AUDIO_CACHE, ""));

        audioPlayer.addStatusListener(this);

        if (outputFilename != null) {
            try {
                out = new PrintWriter(new FileWriter(outputFilename), true);
            } catch (FileNotFoundException e) {
                System.err.println("File Not Found: " + outputFilename.getAbsolutePath());
                System.exit(1);
            } catch (IOException e) {
                System.err.println("File Exception: " + outputFilename.getAbsolutePath());
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            out = new PrintWriter(System.out, true);
        }

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
                    System.out.println("\tOutput: " + (outputFilename == null ? "terminal" : outputFilename.getAbsolutePath()));
                    System.out.println("\tMulti-line input: " + inputMultiline);
                    StringBuilder sb = new StringBuilder();
                    while (running && ((line = reader.readLine()) != null)) {
                        if (line.startsWith("#")) {
                            if (line.contains("publishFile=")) {
                                long messageID = getMessageID();
                                filenameMapping.put(messageID, line.substring(line.indexOf('=') + 1).trim());
                                publishData(terminalChannel, new StringData(messageID, sb.toString(), languageCode));
                                sb = new StringBuilder();
                            }
                            continue;
                        }

                        if (inputMultiline) {
                            sb.append(line);//.append("\n");
                        } else {
                            publishData(terminalChannel, new StringData(getMessageID(), line, languageCode));
                        }
                    }
                    if (inputMultiline && sb.length() > 0) {
                        publishData(terminalChannel, new StringData(getMessageID(), sb.toString(), languageCode));
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
     * Saves audio data into a file.
     * @param fileID file ID
     * @param data audio data to be saved
     * @return created file 
     */
    private String saveToFile(long fileID, AudioData data) {
        File audioFileData;
        if (filenameMapping.containsKey(fileID)) {
            audioFileData = new File(audioCache, filenameMapping.get(fileID));
        } else {
            audioFileData = new File(audioCache, "audio-" + fileID + ".wav");
        }
        try {
            AudioSystem.write(data.getAudioStream(), AudioFileFormat.Type.WAVE, audioFileData);
            return audioFileData.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Invalid save path = " + audioFileData.getAbsolutePath());
            return null;
        }
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
        if (data instanceof AudioData) {
            //save the data
            String filename = saveToFile(data.getId(), (AudioData) data);
            out.println("Audio Data saved: " + filename);
            audioPlayer.playAudio(filename, ((AudioData) data).getAudioStream());
        } else {
            out.println(data.toString());
        }
    }

    /**
     * checks the status of audio data player when updated (event: start, stop)
     */
    public void audioPlayerStatusUpdated(String audioLabel, AudioPlayer.AudioPlayerEvent event) {
        if (event == AudioPlayer.AudioPlayerEvent.start) {
            out.println("Playing audio data: " + audioLabel);
        } else if (event == AudioPlayer.AudioPlayerEvent.stop) {
            out.println("Stopping audio player ...");
        }
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
        addOutboundTypeChecker(terminalChannel, StringData.class);
    }

    /**
     * Closes terminal, output writer and audio player and stop running process.
     */
    public void close() {
        super.close();
        running = false;
        terminalThread.interrupt();
        out.close();
        audioPlayer.killPlayer();
    }
}
