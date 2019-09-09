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

package org.ib.communication;

import org.ib.component.base.Publisher;
import org.ib.data.DataHelper;
import org.ib.data.GenericData;
import org.ib.data.InvalidDataException;
import org.ib.data.SystemEvent;
import org.ib.logger.Logger;
import org.zeromq.ZMQ;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/20/12
 */
public class OutboundManager {
    private static final String protocol = "tcp";
    private String port = null;
    private String connectPattern = null;

    private ZMQ.Context context;
    private ZMQ.Socket sender;

    private Publisher component;

    public OutboundManager(Publisher component, String port) {
        this.component = component;

        context = ZMQ.context(1);
        sender = context.socket(ZMQ.PUB);

        setPort(port);
    }

    public void publishData(String topic, GenericData data) {
        try {
            if (!sender.send(DataHelper.encodeData(topic, component.getMachinePortPattern(), data))) {
                System.err.println("Failed to send message: " + data.toString());
            }
        } catch (InvalidDataException e) {
            Logger.log(component, Logger.CRITICAL, "Invalid data: " + e);
        }
    }

    public void open() {
        connectPattern = String.format("%s://*:%s", protocol, port);
        sender.bind(connectPattern);
    }

    public void close() {
        sender.close();
        context.term();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        disconnectOld();
        this.port = port;

        open();
    }

    private void disconnectOld() {
        if (connectPattern != null) {
            sender.disconnect(connectPattern);
        }
    }

}

