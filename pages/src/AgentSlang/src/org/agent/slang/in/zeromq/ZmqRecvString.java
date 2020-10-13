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

package org.agent.slang.in.zeromq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

import org.ib.component.base.LivingComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.component.annotations.ConfigureParams;
import org.ib.data.LanguageUtils;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.logger.Logger;

import java.util.Locale;

/**
 * This class provides general message passing and socket functionalities in order to receive string data using a ZeroMQ socket.
 * @author Mael Bouabdelli, mael.bouabdelli@insa-rouen.fr
 * @version 1, 11/13/19
 */

@ConfigureParams( mandatoryConfigurationParams = {"inPort", "inTopicName"},
                  outputChannels = "text.data",
                  outputDataTypes = StringData.class)
public class ZmqRecvString extends LivingComponent
{
    // Attributes
    private static final String PROP_IN_PORT = "inPort";
    private static final String PROP_IN_TOPIC_NAME = "inTopicName";
    private String inPort;
    private String inTopicName;

    private static final String textChannel = "text.data";

    protected ZContext context;
    protected Socket subscriber;
    private long messageID = 0;

    // Constructors - Destructors
    public ZmqRecvString(String outboundPort, ComponentConfig config)
    {
        super(outboundPort, config);
    }

    // Methods

    /**
    * Setting up the component based on input configuration.
    * @param config component configuration.
    */
    protected void setupComponent(ComponentConfig config)
    {
        // Fetch parameters
        inPort = config.getProperty(PROP_IN_PORT);
        inTopicName = config.getProperty(PROP_IN_TOPIC_NAME);

        // Init jeromq
        context = new ZContext();
        subscriber = context.createSocket(ZMQ.SUB);
        subscriber.connect(String.format("tcp://localhost:%s", inPort));
        subscriber.subscribe(inTopicName.getBytes(ZMQ.CHARSET));
    }

    public boolean act()
    {
        // Read envelope with address
        // recvStr is a blocking function (waits for a message to be received) if the ZMQ.NOBLOCK flag isn't used
        String messageReceived = subscriber.recvStr(ZMQ.NOBLOCK);

        // If a message is received, we can process and transfer it
        if (messageReceived != null)
        {
            String[] messageSplit = messageReceived.split(" ", 2);
            String topic = messageSplit[0];
            String message = messageSplit[1];

            Logger.log(this, Logger.INFORM, String.format("from topic %s : %s", topic, message));
            sendMessage(message);
        }

        return true;
    }

    /**
     * Sends text message.
     * @param message text message
     */
    protected void sendMessage(String message) {
        publishData(textChannel, new StringData(getMessageID(), message, LanguageUtils.getLanguageCodeByLocale(Locale.US)));
    }

        /**
     * Checking type of input data 
     */
    public void defineReceivedData() {}

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(textChannel, StringData.class);
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {}

    /**
     * gets message id
     * @return message id
     */
    private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }
}
