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

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.InetAddress;
import java.util.EventListener;

/**
 * This class provides general message passing and socket functionalities in order to send and receive data to/from MARC toolkit.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 10/19/13
 */
public abstract class MarcSocket extends Thread {
    private final EventListenerList listeners = new EventListenerList();
    protected InetAddress hostname;
    protected int inPort;
    protected int outPort;
    protected int emlInPort;
    protected MarcSocket(String hostname, int inPort, int outPort) throws IOException {
        this.hostname = InetAddress.getByName(hostname);
        this.inPort = inPort;
        this.outPort = outPort;
        this.emlInPort = 4012;
    }
    protected MarcSocket(String hostname, int inPort, int outPort, int emlInPort) throws IOException {
        this.hostname = InetAddress.getByName(hostname);
        this.inPort = inPort;
        this.outPort = outPort;
        this.emlInPort = emlInPort;
    }
    
    /**
     * abstract function to sends message to MARC toolkit
     * @param message string message to send
     */
    public abstract void sendMessage(String message);
    
    /**
     * abstract function to sends EML message to MARC toolkit
     * @param message string EML message to send
     */
    public abstract void sendEmlMessage(String message);

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
     * starts receiving data from MARC toolkit
     * @param message received message
     */
    protected void fireDataUpdate(String message) {
        for (DataListener listener : listeners.getListeners(DataListener.class)) {
            listener.dataReceived(message);
        }
    }

    /**
     * abstract function to check connectivity with MARC toolkit
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
