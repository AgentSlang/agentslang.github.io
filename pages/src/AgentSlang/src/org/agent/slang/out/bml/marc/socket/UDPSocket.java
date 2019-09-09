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

package org.agent.slang.out.bml.marc.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * This class provides a UDP socket and message passing mechanism to transfer data to/from MARC toolkit.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 10/19/13
 */
public class UDPSocket extends MarcSocket {
    private DatagramSocket inSocket;
    private DatagramSocket outSocket;
    private DatagramSocket emlInSocket;
    public UDPSocket(String hostname, int inPort, int outPort, DataListener listener) throws IOException {
        super(hostname, inPort, outPort);

        inSocket = new DatagramSocket();
        outSocket = new DatagramSocket(outPort);

        addDataListener(listener);
        start();
    }
    public UDPSocket(String hostname, int inPort, int outPort, int emlInPort, DataListener listener) throws IOException {
        super(hostname, inPort, outPort);

        inSocket = new DatagramSocket();
        outSocket = new DatagramSocket(outPort);

        emlInSocket = new DatagramSocket();
        
        addDataListener(listener);
        start();
    }

    /**
     * sends message to MARC toolkit
     * @param message string message to send
     */
    public void sendMessage(String message) {
        if (inSocket != null ) {
            byte[] buf = message.getBytes();
            try {
                inSocket.send(new DatagramPacket(buf, buf.length, hostname, inPort));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * sends EML message to MARC toolkit
     * @param message string message to send
     */
    public void sendEmlMessage(String message) {
        if (emlInSocket != null ) {
            byte[] buf = message.getBytes();
            try {
                emlInSocket.send(new DatagramPacket(buf, buf.length, hostname, emlInPort));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * checks connectivity with MARC
     * @return boolean
     */
    public boolean isConnected() {
        return inSocket != null ;
    }

    /**
     * runs procedure of receiving information from MARC toolkit.
     */
    public void run() {
      //  if (outSocket != null )
    	{
            StringBuilder line = new StringBuilder();
            byte[] buffer = new byte[2048];
            while (true) {
                buffer[0] = 0;
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    outSocket.receive(packet);
                } catch (IOException e) {
                    //-- ignore
                }
                String newLine = new String(buffer);
                int newLineIndex = newLine.indexOf('\n');
                if (newLineIndex > 0) {
                    line.append(newLine.substring(0, newLineIndex));
                   // System.out.println("$UDP msg Received :" + line.toString());
                    fireDataUpdate(line.toString());
                    line = new StringBuilder(newLine.substring(newLineIndex + 1));
                } else {
                   // line.append(newLine);
                }
            }
        }
    }

    /**
     * closes input, output and EML sockets.
     */
    public void close() {
        if (inSocket != null && inSocket.isConnected()) {
            inSocket.close();
        }
        if (emlInSocket != null && emlInSocket.isConnected()) {
            emlInSocket.close();
        }
        if (outSocket != null && outSocket.isConnected()) {
            outSocket.close();
        }
    }
}
