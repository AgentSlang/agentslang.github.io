package org.agent.slang.inout.python;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;



/**
 * This class provides a UDP socket and message passing mechanism to transfer data to/from python
 * OS Compatibility: Windows and Linux
 * @author Naser ghanad
 * @version 1, 05/03/2019
 
 * naser
 //send data
 //System.out.println("comment : "+modifiedSentence );
				//sendMessage(modifiedSentence+"newwwwwwwwwwww");
 */
public class PyUDPSocket extends pySocket {
    private DatagramSocket inSocket;
    private DatagramSocket outSocket;
	
    public PyUDPSocket(String hostname, int inPort, int outPort, DataListener listener) throws IOException {
        super(hostname, inPort, outPort);

        System.out.println("Tobii Eye Tracker Socket: "+ hostname+ " "+inPort + "  " +outPort);
        
        
        inSocket = new DatagramSocket();
        outSocket = new DatagramSocket(outPort);
        System.out.println("Py Socket : successfull allocation");
		
		
        addDataListener(listener);
        start();
    }

    /**
     * sends message to python
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
     * runs receiving python information procedure
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
