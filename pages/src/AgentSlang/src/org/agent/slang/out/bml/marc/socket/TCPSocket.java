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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class provides a TCP socket and message passing mechanism to transfer data to/from MARC toolkit.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 10/19/13
 */
public class TCPSocket extends MarcSocket {
    private Socket inSocket;
    private Socket emlInSocket;
    private ServerSocket outSocket;
    private PrintWriter inWriter;
    private PrintWriter emlInWriter;
    private boolean running = true;

    public TCPSocket(String hostname, int inPort, int outPort, DataListener listener) throws IOException {
        super(hostname, inPort, outPort);

        inSocket = new Socket(this.hostname, inPort);
        outSocket = new ServerSocket(outPort);
        inWriter = new PrintWriter(inSocket.getOutputStream(), true);

        addDataListener(listener);
        start();
    }

    public TCPSocket(String hostname, int inPort, int outPort, int emlInPort, DataListener listener) throws IOException {
        super(hostname, inPort, outPort, emlInPort);

        inSocket = new Socket(this.hostname, inPort);
        outSocket = new ServerSocket(outPort);
        emlInSocket = new Socket(this.hostname, emlInPort);

        inWriter = new PrintWriter(inSocket.getOutputStream(), true);
        emlInWriter = new PrintWriter(emlInSocket.getOutputStream(), true);

        addDataListener(listener);
        start();
    }

    /**
     * sends message to MARC toolkit
     * @param message string message to send
     */
    public void sendMessage(String message) {
        if (inWriter != null && inSocket != null && inSocket.isConnected()) {
            inWriter.println(message);
            System.out.println("Socket message sent: " + message);
        } else {
            System.out.println("Failed to send socket message: " + message);
        }
    }

    /**
     * sends EML message to MARC toolkit
     * @param message string message to send
     */
    public void sendEmlMessage(String message) {
    	System.out.println("\n\n****************\nSendEML msg TCP :::" +message);
        if (emlInWriter != null && emlInSocket != null && emlInSocket.isConnected()) {
            emlInWriter.println(message);
            System.out.println("Eml Socket message sent: " + message);
        } else {
            System.out.println("Failed to send Eml socket message: " + message);
        }
    }    
    
    /**
     * checks connectivity with MARC
     * @return boolean
     */
    public boolean isConnected() {
        return inSocket != null && inSocket.isConnected() ;
    }

    /**
     * runs procedure of receiving information from MARC toolkit.
     */
    public void run() {
        while (running) {
            try {
                Socket client = outSocket.accept();
                System.out.println("Accepted connection from: " + client.getRemoteSocketAddress().toString());
                BufferedReader outReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                try {
                    while ((line = outReader.readLine()) != null) {
                        fireDataUpdate(line);
                    }
                    outReader.close();
                    client.close();
                } catch (IOException e) {
                    //-- ignore for now
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * closes input, output and EML sockets.
     */
    public void close() {
        running = false;

        if (inWriter != null) {
            inWriter.close();
        }

        if (inSocket != null && inSocket.isConnected()) {
            try {
                inSocket.close();
            } catch (IOException e) {
                //-- ignore
            }
        }
        if (emlInWriter != null) {
            emlInWriter.close();
        }

        if (emlInSocket != null && emlInSocket.isConnected()) {
            try {
                emlInSocket.close();
            } catch (IOException e) {
                //-- ignore
            }
        }
        if (outSocket != null) {
            try {
                outSocket.close();
            } catch (IOException e) {
                //-- ignore
            }
        }
    }
}
