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

import javax.swing.event.EventListenerList;


import java.io.IOException;
import java.net.InetAddress;
import java.util.EventListener;

/**
 * This class provides general message passing and socket functionalities in order to send and receive data to/from ASR application.
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 10/19/13
 */
public abstract class SpeechRecognizerSocket extends Thread {
    private final EventListenerList listeners = new EventListenerList();
    protected InetAddress hostname;
    protected int inPort;
    protected int outPort;

    protected SpeechRecognizerSocket(String hostname, int inPort, int outPort) throws IOException {
        this.hostname = InetAddress.getByName(hostname);
        this.inPort = inPort;
        this.outPort = outPort;
    }

    /**
     * abstract function to sends message to ASR application
     * @param message string message to send
     */
    public abstract void sendMessage(String message);

    /**
     * Enables data listening functionality
     * @param listener data listener object
     */
    public void addDataListener(DataListener listener) {
        listeners.add(DataListener.class, listener);
    }

    /**
     * disables data listening functionality
     * @param listener data listening object
     */
    public void removeDataListener(DataListener listener) {
        listeners.remove(DataListener.class, listener);
    }

    /**
     * starts receiving data from external ASR application
     * @param message received message
     */
    protected void fireDataUpdate(String message) {
    	System.out.println("Speech msg : " +message);
        for (DataListener listener : listeners.getListeners(DataListener.class)) {
            listener.dataReceived(message);
        }
    }

    /**
     * abstract function to check connectivity with external application
     * @return boolean
     */
    public abstract boolean isConnected();

    /**
     * abstract function in order to run a procedure
     */
    public abstract void run();

    /**
     * abstract function in order to close the socket
     */
    public abstract void close();

    public interface DataListener extends EventListener {
        public void dataReceived(String message);
    }
}
