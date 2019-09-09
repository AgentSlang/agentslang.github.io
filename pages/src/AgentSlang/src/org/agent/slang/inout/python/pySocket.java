package org.agent.slang.inout.python;

import javax.swing.event.EventListenerList;


import java.io.IOException;
import java.net.InetAddress;
import java.util.EventListener;

/**
 * This class provides general message passing and socket functionalities in order to send and receive data to/from other application.
 * OS Compatibility: Windows and Linux
 * @author Naser ghannad
 * @version 1, 05/03/2019
 */
public abstract class pySocket extends Thread {
    private final EventListenerList listeners = new EventListenerList();
    protected InetAddress hostname;
    protected int inPort;
    protected int outPort;

    protected pySocket(String hostname, int inPort, int outPort) throws IOException {
        this.hostname = InetAddress.getByName(hostname);
        this.inPort = inPort;
        this.outPort = outPort;
    }

    /**
     * abstract function to sends message to python 
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
     * starts receiving data from external python 
     * @param message received message
     */
    protected void fireDataUpdate(String message) {
    	System.out.println("Py message : " +message);
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
