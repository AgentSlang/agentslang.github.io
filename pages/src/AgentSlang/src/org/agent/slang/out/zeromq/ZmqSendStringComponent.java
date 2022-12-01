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

package org.agent.slang.out.zeromq;

import org.ib.component.base.SinkComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.component.annotations.ConfigureParams;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.logger.Logger;

/**
 * This class component provides general message passing and socket functionalities in order to send string data using a ZeroMQ socket.
 * @author Mael Bouabdelli, mael.bouabdelli@insa-rouen.fr
 * @version 1, 11/13/19
 */

@ConfigureParams( mandatoryConfigurationParams = {"outPort", "outTopicName"},
                  inputDataTypes = GenericData.class)
public class ZmqSendStringComponent extends SinkComponent {
    private static final String PROP_OUT_PORT = "outPort";
    private static final String PROP_OUT_TOPIC_NAME = "outTopicName";
    private String outPort;
    private String outTopicName;

    ZmqSendString zmqSendString;

    public ZmqSendStringComponent(String port, ComponentConfig config) {
        super(port, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        // Fetch parameters
        outPort = config.getProperty(PROP_OUT_PORT);
        outTopicName = config.getProperty(PROP_OUT_TOPIC_NAME);

        // Init ZmqSendString object
        zmqSendString = new ZmqSendString(outPort, outTopicName);
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
        if (data instanceof StringData) {
            String message = ((StringData)data).getData();
           
            Logger.log(this, Logger.INFORM, String.format("To topic %s : %s", outTopicName, message));
            
            zmqSendString.SendMessage(message);
        }
    }

    /**
     * Checking type of input data
     */
    public void defineReceivedData() {
        addInboundTypeChecker(GenericData.class);
    }

    public boolean act() {
        return false;
    }
}
