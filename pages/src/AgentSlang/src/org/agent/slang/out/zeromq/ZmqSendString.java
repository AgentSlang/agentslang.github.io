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

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

/**
 * This class provides general message passing and socket functionalities in order to send string data using a ZeroMQ socket.
 * @author Mael Bouabdelli, mael.bouabdelli@insa-rouen.fr
 * @version 1, 11/13/19
 */
public class ZmqSendString {
    private String outPort;
    private String outTopicName;

    private ZContext context;
    private Socket publisher;

    public ZmqSendString(String outputPort, String outputTopicName) {
        // Fetch parameters
        outPort = outputPort;
        outTopicName = outputTopicName;

        // Init jeromq
        context = new ZContext();
        publisher = context.createSocket(ZMQ.PUB);
        publisher.bind(String.format("tcp://*:%s", outPort));
    }

    public boolean SendMessage(String msg) {
        String msg_to_send = String.format("%s %s", outTopicName, msg);
        publisher.send(msg_to_send);
        return true;
    }
}
