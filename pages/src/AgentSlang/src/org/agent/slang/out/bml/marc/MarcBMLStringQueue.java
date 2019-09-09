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

import org.agent.slang.out.bml.marc.socket.MarcSocket;
import org.agent.slang.out.bml.marc.socket.TCPSocket;
import org.agent.slang.out.bml.marc.socket.UDPSocket;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.data.LanguageUtils;
import org.ib.logger.Logger;
import org.ib.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.*;

/**
 * @author Adrien Fontaine, adrien.fontaine@insa-rouen.fr
 * @version 1, 07/04/17 - Initial commit, Adrien Fontaine
 */

@ConfigureParams(optionalConfigurationParams = {"MARCSocketType", "MARCHostname", "MARCInPort", "MARCOutPort"},
		outputChannels = "marcFeedback.data", outputDataTypes = StringData.class,
        inputDataTypes = StringData.class)
public class MarcBMLStringQueue extends MixedComponent implements MarcSocket.DataListener {
    private static final String PROP_SOCKET_TYPE = "MARCSocketType";
    private static final String PROP_SOCKET_HOST = "MARCHostname";
    private static final String PROP_SOCKET_IN_PORT = "MARCInPort";
    private static final String PROP_SOCKET_OUT_PORT = "MARCOutPort";

    //topics
    private static final String MARC_FEEDBACK_DATA = "marcFeedback.data";
    private final static long EXECUTION_TIMEOUT = 5 * 60 * 1000;
    private final Queue<String> bmlExecutionQueue = new LinkedList<String>();
    //MARC socket
    private MarcSocket socket;
    private long bmlID = 0;
    private Long lastExecutionTimestamp = null;
    
    private long messageID;

    public MarcBMLStringQueue(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Component's setup method called at the creation of an instance. It initializes the socket to comunicate with MARC using the configuration
     * parameters set in the configuration xml file.
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

    public void defineReceivedData() {
        addInboundTypeChecker(StringData.class);
    }
    
    public void definePublishedData() {
        addOutboundTypeChecker(MARC_FEEDBACK_DATA, StringData.class);
    }

    /**
     * Method called each time MARC sends a message through the socket. Publish the feedback of the start (or the end) of a bml command.
     */
    public void dataReceived(String message) {
    	Logger.log(this, Logger.INFORM, "Feedback : " + message);
        if (message.toLowerCase().contains("bml:end") || (message.toLowerCase().contains("bml-step") && message.toLowerCase().contains(":end"))) {
            publishData(MARC_FEEDBACK_DATA, new StringData(getMessageID(), message, LanguageUtils.getLanguageCodeByLocale(Locale.US)));
        } else if (message.toLowerCase().contains("bml:start") || (message.toLowerCase().contains("bml-step") && message.toLowerCase().contains(":start"))) {
            publishData(MARC_FEEDBACK_DATA, new StringData(getMessageID(), message, LanguageUtils.getLanguageCodeByLocale(Locale.US)));
        }
    }

    /**
     * Method when receiving data from subscribed source(s). The data received is a bml command that is immediately scheduled for execution.
     * @param data data received from the subscribtion
     */
    protected void handleData(GenericData data) {
        System.out.println("New data in BML Queue");

        if (data != null) {
            String message = ((StringData) data).getData();
            stopAndScheduleNext();
            scheduleForExecutionMessage(message);
        }
    }

    private void scheduleForExecutionMessage(String message) {
        synchronized (bmlExecutionQueue) {
            bmlExecutionQueue.offer(message);
            scheduleNextBML();
        }
    }

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

    private void stopAndScheduleNext() {
        synchronized (bmlExecutionQueue) {
            lastExecutionTimestamp = null;
            scheduleNextBML();
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
     * Close the socket used to comunicate with MARC.
     */
    public void close() {
        super.close();

        if (socket != null) {
            socket.close();
        }
    }
}
