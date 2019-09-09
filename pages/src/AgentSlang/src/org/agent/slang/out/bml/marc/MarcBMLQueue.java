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

import org.agent.slang.data.audio.AudioData;
import org.agent.slang.data.audio.PlayerEvent;
import org.agent.slang.data.simple.BmlData;
import org.agent.slang.out.bml.marc.socket.MarcSocket;
import org.agent.slang.out.bml.marc.socket.TCPSocket;
import org.agent.slang.out.bml.marc.socket.UDPSocket;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.base.SinkComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.logger.Logger;
import org.ib.utils.FileUtils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Provides a BML queue for agentslang for sending BML to MARC toolkit.
 * OS Compatibility: Windows and Linux
 */
@ConfigureParams(optionalConfigurationParams = {"MARCSocketType", "MARCHostname", "MARCInPort", "MARCOutPort"},
        inputDataTypes = BmlData.class)
public class MarcBMLQueue extends SinkComponent implements MarcSocket.DataListener {
    private static final String PROP_SOCKET_TYPE = "MARCSocketType";
    private static final String PROP_SOCKET_HOST = "MARCHostname";
    private static final String PROP_SOCKET_IN_PORT = "MARCInPort";
    private static final String PROP_SOCKET_OUT_PORT = "MARCOutPort";

    //topics
    private final static long EXECUTION_TIMEOUT = 5 * 60 * 1000;
    private final Queue<String> bmlExecutionQueue = new LinkedList<String>();
    //MARC socket
    private MarcSocket socket;
    private long bmlID = 0;
    private Long lastExecutionTimestamp = null;

    public MarcBMLQueue(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        String hostname = "localhost";
        int intPort = 4111;
        int outPort = 4112;

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

        Logger.log(this, Logger.INFORM, String.format("Using MARC Connections Properties hostname='%s' inPort=%d outPort=%d", hostname, intPort, outPort));
        System.out.println(String.format("Using MARC Connections Properties hostname='%s' inPort=%d outPort=%d", hostname, intPort, outPort));

        if ("tcp".equals(config.getProperty(PROP_SOCKET_TYPE))) {
            try {
                socket = new TCPSocket(hostname, intPort, outPort, this);
            } catch (IOException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid properties for socket", e);
            }
        } else {
            try {
                socket = new UDPSocket(hostname, intPort, outPort, this);
            } catch (IOException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid properties for socket", e);
            }
        }

    }

    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
        addInboundTypeChecker(StringData.class);
    }

    /**
     * Processes received data from MARC toolkit.
     * @param message received message from MARC toolkit
     */
    public void dataReceived(String message) {
        Logger.log(this, Logger.INFORM, "From MARC: " + message);
        if (message.toLowerCase().contains("bml:start")) {
            System.out.println("MARC started event message !");
        } else if (message.toLowerCase().contains("bml:end")) {
            System.out.println("MARC started event message !");
            stopAndScheduleNext();
        }
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
        System.out.println("New data in BML Queue");
        System.out.println(data.toString());
        String bmlID = generateBMLID();
        BmlData bmlCmd = (BmlData) data;

        String bmlString = bmlCmd.getData();

        if (bmlString != null) {
            String message = generateBML(bmlID, bmlString);
            scheduleForExecutionMessage(message);
        }
    }

    /**
     * Schedules for sending BML messages to MARC toolkit in to be executed there.
     * @param message BML string message
     */
    private void scheduleForExecutionMessage(String message) {
        synchronized (bmlExecutionQueue) {
            bmlExecutionQueue.offer(message);
            scheduleNextBML();
        }
    }

    /**
     * Schedules next BML to be sent to MARC toolkit.
     */
    private void scheduleNextBML() {
        synchronized (bmlExecutionQueue) {
            if (lastExecutionTimestamp == null || System.currentTimeMillis() - lastExecutionTimestamp > EXECUTION_TIMEOUT) {
                String message = bmlExecutionQueue.poll();
                if (message != null) {
                    if (socket != null) {
                        Logger.log(this, Logger.INFORM, "MARC Sending message: " + message);
                        socket.sendMessage(message);
                        lastExecutionTimestamp = System.currentTimeMillis();
                    } else {
                        Logger.log(this, Logger.INFORM, "Marc Sending message failed: " + message);
                    }
                } else {
                    lastExecutionTimestamp = null;
                }
            }
        }
    }

    /**
     * Stops current process and schedules for next BML to be sent to MARC toolkit.
     */
    private void stopAndScheduleNext() {
        synchronized (bmlExecutionQueue) {
            lastExecutionTimestamp = null;
            scheduleNextBML();
        }
    }

    /**
     * Generates BML ID
     * @return BML ID
     */
    private String generateBMLID() {
        return "bmlGesture-" + (bmlID++);
    }

    /**
     * Generates BML message
     * @param bmlID BML ID
     * @param cmd BML Commnad
     * @return BML message
     */
    private String generateBML(String bmlID, String cmd) {

        return "<bml><posture id=\"" + bmlID + "\" stance=\"" + cmd + "\" /></bml>";
    }

    /**
     * Closes socket for communicating with MARC toolkit. 
     */
    public void close() {
        super.close();

        if (socket != null) {
            socket.close();
        }
    }
}
