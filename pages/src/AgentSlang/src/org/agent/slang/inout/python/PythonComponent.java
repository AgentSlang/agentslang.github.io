package org.agent.slang.inout.python;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import org.ib.logger.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Provides Connection to python
 * OS Compatibility: Windows
 * @author Naser Ghannad
 * @version 1, 02/03/2019 - Initial commit, Naser Ghannad
 */
@ConfigureParams(outputChannels = {"tobiiEyeTracking.data"},
outputDataTypes = {StringData.class}
,inputDataTypes = {GenericData.class})
public class PythonComponent extends MixedComponent implements pySocket.DataListener {
	
	private static final String TOBIIEYETRACKING_DATA = "tobiiEyeTracking.data";
	private static final String PROP_SOCKET_HOST = "TobiiHostname";
    private static final String PROP_SOCKET_IN_PORT = "TobiiInPort";
    private static final String PROP_SOCKET_OUT_PORT = "TobiiOutPort";
	
	private long messageID;
	private pySocket socket;
	
	protected PythonComponent(String outboundPort, ComponentConfig config) {
		super(outboundPort, config);
	}

	/**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
	@Override
	protected void setupComponent(ComponentConfig config) {
		String hostname = "localhost";
        int intPort = 5011;
        int outPort = 5012;
        messageID = 0;

        if (config.hasProperty(PROP_SOCKET_HOST)) {
            hostname = config.getProperty(PROP_SOCKET_HOST);
        }

        if (config.hasProperty(PROP_SOCKET_IN_PORT)) {
            try {
                intPort = Integer.parseInt(config.getProperty(PROP_SOCKET_IN_PORT));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid inPort provided", e);
            }
        }

        if (config.hasProperty(PROP_SOCKET_OUT_PORT)) {
            try {
                outPort = Integer.parseInt(config.getProperty(PROP_SOCKET_OUT_PORT));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid outPort provided", e);
            }
        }

        Logger.log(this, Logger.INFORM, String.format("Using Tobii Eye Tracker Connections Properties hostname='%s' inPort=%d outPort=%d", hostname, intPort, outPort));
        System.out.println(String.format("Connections Properties hostname='%s' inPort=%d outPort=%d", hostname, intPort, outPort));

        try {
        	socket = new PyUDPSocket(hostname, intPort, outPort, this);
        } catch (IOException e) {
            Logger.log(this, Logger.CRITICAL, "Tobii Eye Tracker Invalid properties for socket", e);
            System.out.println("Invalid properties for socket" + e.toString());
       
        }
	}

	/**
     * Managing input and output data in the class.  Data Come from other components.
	 * @author Naser Ghannad
     * @param data input data         
     */
	@Override
	protected void handleData(GenericData data) {
		// TODO Auto-generated method stub		
		System.out.println("data received  py  : " + data.toString());
		socket.sendMessage(data.toString());
	}
	
	/**
	 * Gets message id 
	 * @return message id
	 */
	private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }

	/**
	 * handles received data from python
	 * @param message received message from python          
	 */
	@Override 
	public void dataReceived(String message) {
		System.out.println("data recived" + message);
        Logger.log(this, Logger.INFORM, "From py : " + message);
        publishData(TOBIIEYETRACKING_DATA, new StringData(getMessageID(), message, LanguageUtils.getLanguageCodeByLocale(Locale.FRANCE)));
        /*
        try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document document = builder.parse(new InputSource(new StringReader(message)));

	        XPathFactory xpf = XPathFactory.newInstance();
	        XPath xpath = xpf.newXPath();

	        Number leftEyeValidity = (Number) xpath.evaluate("/tobii/lefteye/validity", document, XPathConstants.NUMBER);
	        Number rightEyeValidity = (Number) xpath.evaluate("/tobii/righteye/validity", document, XPathConstants.NUMBER);	        
	        
	        if (leftEyeValidity.intValue() == 0 && rightEyeValidity.intValue() == 0)
	        {
	        	String leftGazepoint2dX = (String) xpath.evaluate("/tobii/lefteye/gazepoint2d/x", document, XPathConstants.STRING);
	        	leftGazepoint2dX = leftGazepoint2dX.replace(",", ".");
	        	double leftGazepoint2Dx = Double.parseDouble(leftGazepoint2dX);
	        	
	        	String leftGazepoint2dY = (String) xpath.evaluate("/tobii/lefteye/gazepoint2d/y", document, XPathConstants.STRING);
	        	leftGazepoint2dY = leftGazepoint2dY.replace(",", ".");
	        	double leftGazepoint2Dy = Double.parseDouble(leftGazepoint2dY);
	        	
	        	String rightGazepoint2dX = (String) xpath.evaluate("/tobii/righteye/gazepoint2d/x", document, XPathConstants.STRING);
	        	rightGazepoint2dX = rightGazepoint2dX.replace(",", ".");
	        	double rightGazepoint2Dx = Double.parseDouble(rightGazepoint2dX);
	        	
	        	String rightGazepoint2dY = (String) xpath.evaluate("/tobii/righteye/gazepoint2d/y", document, XPathConstants.STRING);
	        	rightGazepoint2dY = rightGazepoint2dY.replace(",", ".");
	        	double rightGazepoint2Dy = Double.parseDouble(rightGazepoint2dY);
		        
		        double gazepoint2dX = (leftGazepoint2Dx + rightGazepoint2Dx) / 2;
		        double gazepoint2dY = (leftGazepoint2Dy + rightGazepoint2Dy) / 2;
		        
	        	if (gazepoint2dX < 0.5 && gazepoint2dY < 0.5)
        			System.out.println("Gaze at Zone 1");  //top left
	        	else if (gazepoint2dX > 0.5 && gazepoint2dY < 0.5)
	        		System.out.println("Gaze at Zone 2!"); //top right
	        	else  if (gazepoint2dX < 0.5 && gazepoint2dY > 0.5)
	        		System.out.println("Gaze at Zone 3!"); //bottom left
	        	else  if (gazepoint2dX > 0.5 && gazepoint2dY > 0.5)
	        		System.out.println("Gaze at Zone 4!"); //bottom right
	        }
        }
        catch (Exception e) 
        {
        	Logger.log(this, Logger.CRITICAL, "Tobii Eye Tracker Invalid XML Output! ", e);
            System.out.println("Tobii Eye Tracker Invalid XML Output! " + e.toString());
        }*/
	}

	/**
     * Checking type of input data                     
     */
	@Override 
	public void defineReceivedData() {
		// TODO Auto-generated method stub
		 addInboundTypeChecker(GenericData.class);
	}
	
	/**
     * Checking type of output data.                            
     */
	@Override 
	public void definePublishedData() {
		addOutboundTypeChecker(TOBIIEYETRACKING_DATA, StringData.class);
		
	}
}
