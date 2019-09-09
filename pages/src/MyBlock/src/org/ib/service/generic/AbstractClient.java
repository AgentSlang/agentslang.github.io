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

package org.ib.service.generic;

import org.ib.component.base.Closeable;
import org.ib.service.cns.CNClient;
import org.ib.utils.XMLProperties;
import org.zeromq.ZMQ;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/23/12
 */
public abstract class AbstractClient<Req, Rep> implements Closeable {
    private ZMQ.Context context;
    private ZMQ.Socket sender;

    public AbstractClient(String host, String port) {
        context = ZMQ.context(1);
        sender = context.socket(ZMQ.REQ);
        sender.connect("tcp://" + resolveRemoteHost(host) + ":" + port);
    }

    protected String resolveRemoteHost(String host) {
        CNClient client = ClientManager.getClient(ClientManager.CN);
        return client.resolveHost(host);
    }

    public void configure(XMLProperties properties) {
    }

    protected synchronized Rep request(Req request) {
        sender.send(convertData(request), 0);
        byte[] data = sender.recv(0);
        if (data != null) {
            return convertData(data);
        } else {
            return null;
        }
    }

    protected abstract Rep convertData(byte[] data);

    protected abstract byte[] convertData(Req data);

    public void close() {
        sender.close();
        context.term();
    }
}
