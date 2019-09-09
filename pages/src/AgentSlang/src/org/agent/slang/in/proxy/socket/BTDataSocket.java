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

import com.intel.bluetooth.BlueCoveConfigProperties;
import com.intel.bluetooth.BlueCoveImpl;
import org.ib.component.base.Publisher;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/24/13
 */
public class BTDataSocket extends DataSocket {
    private UUID uuid;
    private StreamConnection connection;

    public BTDataSocket(Publisher source, String uuid, DataListener listener) {
        super(source);
        this.uuid = new UUID(uuid, false);
        addDataListener(listener);
        start();
    }

    public void run() {
        try {
            BlueCoveImpl.setConfigProperty(BlueCoveConfigProperties.PROPERTY_DEBUG_LOG4J, "false");
            if (LocalDevice.isPowerOn()) {
                LocalDevice localDevice = LocalDevice.getLocalDevice();
                System.out.println("[BTDataSocket] Address: " + localDevice.getBluetoothAddress());
                System.out.println("[BTDataSocket] Name: " + localDevice.getFriendlyName());

                String connectionString = "btspp://localhost:" + uuid + ";name=Agent Slang Voice Proxy";
                StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);

                System.out.println("[BTDataSocket] UUID:" + uuid.toString());
                System.out.println("[BTDataSocket] Connection string:" + connectionString);
                System.out.println("[BTDataSocket] Server Started. Waiting for clients to connect...");

                connection = streamConnNotifier.acceptAndOpen();
                RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);

                System.out.println("[BTDataSocket] Remote device address: " + dev.getBluetoothAddress());
                System.out.println("[BTDataSocket] Remote device name: " + dev.getFriendlyName(true));
                reader = new BufferedReader(new InputStreamReader(connection.openInputStream()));
                connection.close();
                super.run();
            } else {
                System.err.println("[BTDataSocket] Invalid BT configuration, the device is not turned on ...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                //-- ignore
            }
        }
    }
}
