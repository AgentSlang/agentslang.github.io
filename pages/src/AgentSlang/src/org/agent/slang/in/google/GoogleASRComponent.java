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

package org.agent.slang.in.google;

import org.agent.slang.data.audio.PlayerEvent;
import org.agent.slang.dm.narrative.data.StateChangeData;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.websockets.*;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import org.ib.logger.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JOptionPane;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Convert Speech to Text with using Chrome API. 
 * Modifyed by naser ghannad.
 * This component enables the use of Google's Speech Recognition engine.
 * <p>
 * Using the Google&nbsp;Speech&nbsp;API directly is no longer an ideal solution
 * because it is limited to 50&nbsp;requests/day (even with an API key).
 * However, the implementation of the
 * <a href="https://dvcs.w3.org/hg/speech-api/raw-file/9a0075d25326/speechapi.html">
 * Web&nbsp;Speech&nbsp;API</a> in Google&nbsp;Chrome is exempt from this limit,
 * hence the dirty workaround that this component implements.
 * <p>
 * The component works by setting up a WebSocket server and having a Web page
 * open in Chrome connect to it and send it the results of the
 * Web&nbsp;Speech&nbsp;API. Unfortunately, for Chrome to authorize the Web page
 * to access the microphone and remember the authorization, the page has to be
 * served via an HTTPS server. The page, in turn, needs to access the WebSocket
 * server via TLS too. It is thus necessary to create a self-signed certificate
 * and tell Chrome to accept it as valid. The process is described below.
 *
 * <h3>Setting up the component</h3>
 *
 * <h4>Creating the certificate</h4>
 *
 * The following command can be used to create the certificate. When asked for
 * the certificate information, enter <code>localhost</code> for the
 * Common Name/CN; the other values do not matter as much.
 *
 * <pre><code>
 * openssl req -new -newkey rsa:4096 -days 5000 -nodes -x509 -sha512 -out cert.crt -keyout cert.key
 * </code></pre>
 *
 * The resulting files must then be converted into a PKCS #12 file. This can be
 * done by issuing the following command and entering an empty password (adjust
 * the output path as needed):
 *
 * <pre><code>
 * openssl pkcs12 -export -in cert.crt -inkey cert.key -out $PATH_TO_AGENTSLANG/data/org/agent/slang/in/google/cert.p12
 * </code></pre>
 *
 * The resulting <code>.p12</code> file can actually be installed anywhere, but
 * this location is where the default configuration files point to.
 *
 * <h4>Making Chrome accept the certificate</h4>
 *
 * <h5>Under Microsoft Windows</h5>
 *
 * If the component is used as-is, Chrome will complain that the certificate is
 * invalid. To solve this problem, open Chrome's settings, go to Advanced
 * &rarr; HTTPS/SSL &rarr; Manage certificates, import the <code>cert.crt
 * </code> file previously created and choose "Trusted Root Certification
 * Authorities" as the destination.
 * <p>
 * Finally, restart Chrome (this is a very important part of the process).
 *
 * <h5>Under GNU/Linux</h5>
 *
 * Open Chrome, go to its Settings, then Advanced &rarr; HTTPS/SSL &rarr;
 * Manage Certificates, go to the "Authorities" tab and import <code>cert.crt
 * </code>.
 *
 * <h3>Usage</h3>
 *
 * <h4>Configuration</h4>
 *
 * The <code>certificate</code> parameter of the component must point to a
 * valid <code>*.p12</code> file (as created in the previous section). In
 * addition, the <code>language</code> parameter must contain the language
 * code corresponding to the language that should be used for recognition.
 *
 * <h4>Running</h4>
 *
 * Once AgentSlang is running with a configuration file that makes use of this
 * component, the URL <a href="https://localhost:8149/"><code>
 * https://localhost:8149/</code></a> should be opened in Chrome (this should
 * happen automatically) and permission to use the microphone should be granted,
 * if Chrome asks.
 *
 * <h3>Communication protocol</h3>
 *
 * This section describes the exact protocol used over the WebSocket.
 * <p>
 * Note that the AgentSlang component being the WebSocket server, it must be
 * started before the Web browser.
 *
 * <h4>Initialization</h4>
 *
 * Once the connection between the browser and the AgentSlang component is
 * established, the component sends the browser the language code that should be
 * used for speech recognition.
 * <p>
 * (Technically, the component could send the language code at any moment, even
 * though it currently does not.)
 *
 * <h4>Recognition loop</h4>
 *
 * After it has received the language code, the browser starts speech
 * recognition. Once a result is available, it is sent through the WebSocket.
 * <p>
 * After each iteration, recognition is started again, allowing for continuous
 * recognition. (The <code>SpeechRecognition</code> object natively supports a
 * continuous mode, but it seems slower than using the non-continuous mode
 * repeatedly.)
 * <p>
 * At any moment, the AgentSlang component can send either <code>stop</code> or
 * <code>start</code> to stop or resume recognition. (It is currently used for
 * suspending recognition while the agent speaks, so that it does not hear
 * itself.)
 *
 * <h4>End</h4>
 *
 * If the AgentSlang component is closed, the Web browser automatically stops
 * the recognition engine. If the AgentSlang component is restarted, the Web
 * page must be reloaded.
 * <p>
 * Conversely, if the Web page is closed while the AgentSlang component is still
 * active, it can simply be reopened.
 *
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 2/14/13
 * @version 2, 4/14/15
 */
@ConfigureParams(mandatoryConfigurationParams = {"language", "certificate"},
                 inputDataTypes = {PlayerEvent.class, StateChangeData.class}, 
                 outputChannels = "voice.data", outputDataTypes = StringData.class)
public class GoogleASRComponent extends MixedComponent {
    private static final String voiceChannel = "voice.data";
    private static final String LANG_PROP = "language";
    private static final String CERT_PROP = "certificate";

    private long messageID = 0;
    private String language;
    private int languageCode;
    private String pathToCertificate;

    private HttpServer server;
    private List<WebSocket> clients = new ArrayList<>();
    private Broadcaster webSocketBroadcaster = new OptimizedBroadcaster();

    public GoogleASRComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    @Override
    protected void setupComponent(ComponentConfig config) {
        language = config.getProperty(LANG_PROP);
        languageCode = LanguageUtils.getLanguageCodeByLocale(LanguageUtils.getLanguage(language));
        pathToCertificate = config.getProperty(CERT_PROP);
        start();
    }

    /**
     * Checking type of output data.
     */
    @Override
    public void definePublishedData() {
        addOutboundTypeChecker(voiceChannel, StringData.class);
    }

    /**
     * Checking type of input data 
     */
    @Override
    public void defineReceivedData() {
        addInboundTypeChecker(PlayerEvent.class);
        addInboundTypeChecker(StateChangeData.class);
    }

    /**
     * Closes and stops the connection with google ASR port.
     */
    @Override
    public void close() {
        super.close();
        stop();
    }

    /**
     * Starts listening to google ASR port and receiving data 
     */
    private void start() {
        NetworkListener listener = new NetworkListener("speech_listener", "localhost", 8149);
        listener.registerAddOn(new WebSocketAddOn());

        WebSocketEngine.getEngine().register("", "/", new WebSocketApplication() {
            @Override
            public void onConnect(WebSocket conn) {
                clients.add(conn);
                conn.send(language);
            }

            @Override
            public void onClose(WebSocket conn, DataFrame frame) {
                clients.remove(conn);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                transcriptReceived(message);
            }
        });

        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(pathToCertificate), new char[0]);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, new char[0]);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            SSLEngineConfigurator sslEngineConfigurator = new SSLEngineConfigurator(sslContext);
            sslEngineConfigurator.setClientMode(false);
            sslEngineConfigurator.setNeedClientAuth(false);
            sslEngineConfigurator.setWantClientAuth(false);

            listener.setSecure(true);
            listener.setSSLEngineConfig(sslEngineConfigurator);
        }
        catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }

        server = new HttpServer();
        server.addListener(listener);

        server.getServerConfiguration().addHttpHandler(new CLStaticHttpHandler(getClass().getClassLoader(), "/org/agent/slang/in/google/"), "/");

        try {
            server.start();

            new org.openqa.selenium.chrome.ChromeDriver().get("https://localhost:8149/");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops listening to the google ASR port.
     */
    private void stop() {
        server.shutdownNow();
    }

    /**
     * Gets message ID
     * @return message ID
     */
    private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    @Override
    protected void handleData(GenericData data) {
        if (data instanceof PlayerEvent) {
            PlayerEvent event = (PlayerEvent) data;

            // stop recognition when MARC is speaking, and restart it when appropriate
            String message = event.getEvent() == PlayerEvent.EVENT_START ? "stop" : "start";

            //webSocketBroadcaster.broadcast(clients, message);
            
            //System.out.println("ZJ GOOGLE API STOP/START: PlayerEvent.EVENT_START: "+PlayerEvent.EVENT_START);
        }
        if (data instanceof StateChangeData) {        	
            int stateNo = ((StateChangeData) data).getNewStateNumber();
            System.out.println("\nZJ GOOGLE ASR state: "+stateNo);            
            if (stateNo == 2) 
            {
            	webSocketBroadcaster.broadcast(clients, "stop");
            	String[] buttons = { "Ok" };

                int rc = JOptionPane.showOptionDialog(null, "Should we start ?", "Confirmation",
                  JOptionPane.WARNING_MESSAGE, 0, null, buttons, buttons[0]);

                //System.out.println("\nZJ GOOGLE ASR clicked button: "+rc);
                
                if (rc == 0)
                {
                	publishData(voiceChannel, new StringData(getMessageID(), "bonjour", languageCode));
                	webSocketBroadcaster.broadcast(clients, "start");
                }
            }
        }
    }

    /**
     * processes and and publishes received transcript from Google ASR 
     * @param transcript received transcript from Google ASR
     */
    private void transcriptReceived(String transcript) {
        Logger.log(this, Logger.INFORM, String.format("Google Transcript: %s", transcript));
        System.out.println("\nZJ GOOGLE ASR transcript: "+transcript);
        publishData(voiceChannel, new StringData(getMessageID(), transcript, languageCode));
    }
}
