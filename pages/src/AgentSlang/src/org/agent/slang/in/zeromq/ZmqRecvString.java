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

/**
 * This class provides general message passing and socket functionalities in order to receive string data using a ZeroMQ socket.
 * @author Mael Bouabdelli, mael.bouabdelli@insa-rouen.fr
 * @version 1, 11/13/19
 */

public class ZmqRecvString
{
    // Attributes
    private String inPort;
    private String inTopicName;
    protected ZContext context;
    protected Socket subscriber;

    // Constructors - Destructors
    public ZmqRecvString(String inputPort, String inputTopicName)
    {
        // Fetch parameters
        inPort = inputPort;
        inTopicName = inputTopicName;

        // Init jeromq
        context = new ZContext();
        subscriber = context.createSocket(ZMQ.SUB);
        subscriber.connect(String.format("tcp://localhost:%s", inPort));
        subscriber.subscribe(inTopicName.getBytes(ZMQ.CHARSET));
    }

    // Methods

    public String recvMessage()
    {
        // Read envelope with address
        // recvStr is a blocking function (waits for a message to be received) if the ZMQ.NOBLOCK flag isn't used
        String messageReceived = subscriber.recvStr(ZMQ.NOBLOCK);
        return messageReceived;
    }
}
