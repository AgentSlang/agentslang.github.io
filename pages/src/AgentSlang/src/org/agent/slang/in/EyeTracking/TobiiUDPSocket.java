package org.agent.slang.in.EyeTracking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * This class provides a UDP socket and message passing mechanism to transfer data to/from tobii eye tracking application.
 * OS Compatibility: Windows and Linux
 * @author Sahba Zojaji, sahba.zojaji@insa-rouen.fr
 * @version 1, 03/11/2017
 */
public class TobiiUDPSocket extends TobiiSocket {
    private DatagramSocket inSocket;
    private DatagramSocket outSocket;

    public TobiiUDPSocket(String hostname, int inPort, int outPort, DataListener listener) throws IOException {
        super(hostname, inPort, outPort);

        System.out.println("Tobii Eye Tracker Socket: "+ hostname+ " "+inPort + "  " +outPort);
        
        
        inSocket = new DatagramSocket();
        outSocket = new DatagramSocket(outPort);
        System.out.println("Tobii Eye Tracker Socket : successfull allocation");

        addDataListener(listener);
        start();
    }

    /**
     * sends message to tobii eye tracking application
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
     * checks connectivity with external application
     * @return boolean
     */
    public boolean isConnected() {
        return inSocket != null && inSocket.isConnected();
    }

    /**
     * runs receiving eye tracking information procedure
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
