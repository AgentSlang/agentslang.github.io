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

package org.agent.slang.in.proxy.socket;

import org.ib.component.base.Publisher;
import org.ib.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/24/13
 */
public class TCPDataSocket extends DataSocket {
    private ServerSocket socket;

    public TCPDataSocket(Publisher source, int port, DataListener listener) {
        super(source);
        try {
            socket = new ServerSocket(port);
            addDataListener(listener);
            start();
        } catch (IOException e) {
            Logger.log(source, Logger.CRITICAL, "Socket build error ...", e);
        }
    }

    public void run() {
        try {
            Socket clientSocket = socket.accept();
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            super.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        super.close();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                //-- ignore
            }
        }
    }
}
