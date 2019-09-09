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

package org.agent.slang.in.SpeechRecognizer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * This class provides a UDP socket and message passing based on it to transfer data to/from external ASR application.
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 10/19/13
 */
public class SpeechUDPSocket extends SpeechRecognizerSocket {
    private DatagramSocket inSocket;
    private DatagramSocket outSocket;

    public SpeechUDPSocket(String hostname, int inPort, int outPort, DataListener listener) throws IOException {
        super(hostname, inPort, outPort);

        System.out.println("Speech RecogniserDG "+ hostname+ " "+inPort + "  " +outPort);
        
        
        inSocket = new DatagramSocket();
        outSocket = new DatagramSocket(outPort);
        System.out.println("Speech Recogniser : successfull allocation");

        addDataListener(listener);
        start();
    }

    /**
     * sends message to external ASR application
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
     * checks connectivity with external ASR application
     * @return boolean
     */
    public boolean isConnected() {
        return inSocket != null && inSocket.isConnected();
    }

    /**
     * runs receiving external ASR information procedure
     */
    public void run() {
    	 byte[] receiveData = new byte[1024];
    	 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         while(true){
        	 try {
				outSocket.receive(receivePacket);
				String modifiedSentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
		         if(modifiedSentence.length()>0)
		        	  fireDataUpdate(modifiedSentence);
               modifiedSentence = null;      
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         }
    }

    /**
     * closes input and output sockets
     */
    public void close() {
        if (inSocket != null && inSocket.isConnected()) {
            inSocket.close();
        }

        if (outSocket != null && outSocket.isConnected()) {
            outSocket.close();
        }
    }
}
