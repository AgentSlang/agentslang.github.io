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

package org.agent.slang.in.facereader;

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.SourceComponent;
import org.ib.component.model.ComponentConfig;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Component for using FaceReader.
 *
 * <h3>Basic usage</h3>
 * <h4>FaceReader configuration: enabling the API</h4>
 * FaceReader should be configured to enable the API (it is a "Data export"
 * option). All the desired affective data should be selected as well.
 *
 * <h4>Selecting the correct camera</h4>
 * If this component is used at the same time as
 * {@link org.agent.slang.in.videoconferencing.VCStreamer}, the latter will
 * grab the camera device and FaceReader will not be able to access it. You will
 * need to enable IP Camera Simulation in
 * {@link org.agent.slang.in.videoconferencing.VCStreamer} and select
 * "IP Camera" in FaceReader to work around the issue.
 * <p>
 * Otherwise, you will be able to select the camera directly.
 *
 * <h4>Connecting to FaceReader</h4>
 * The component displays a GUI where the address and port on which FaceReader
 * is listening can be entered. (The initial values can be given <i>via</i> the
 * <code>FRaddress</code> and <code>FRport</code> parameters of the component.)
 * <p>
 * Once the component has connected to FaceReader, its GUI can be used to select
 * what log types should be sent. Analysis can also be started and stopped from
 * here, and stimuli and event markers can be scored, but those actions can be
 * triggered directly from FaceReader as well.
 *
 * <h3>Data sent</h3>
 *
 * This component sends instances of the {@link ClassificationData} class,
 * which is essentially a wrapper class for {@link Classification}, on the
 * <code>facereader.data</code> channel.
 *
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 5/28/15
 */
@ConfigureParams(optionalConfigurationParams = {"FRaddress", "FRport"},
                 outputChannels = "facereader.data", outputDataTypes = ClassificationData.class)
public class FaceReaderComponent extends SourceComponent {
    private static final String facereaderChannel = "facereader.data";
    private static final String ADDRESS_PROP = "FRaddress";
    private static final String PORT_PROP = "FRport";

    private long messageID = 0;

    private GUI gui = new GUI();

    public FaceReaderComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    @Override
    protected void setupComponent(ComponentConfig config) {
        gui.addFaceReaderConnectionListener(new FaceReaderConnectionListener() {
            @Override
            public void onFaceReaderConnection(FaceReaderReceiver receiver, FaceReaderSender sender) {
                receiver.addClassificationListener(new ClassificationListener() {
                    @Override
                    public void onClassification(Classification classification) {
                        publishData(facereaderChannel, new ClassificationData(getMessageID(), classification));
                    }
                });
            }
        });

        gui.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        start();
    }

    @Override
    public void definePublishedData() {
        addOutboundTypeChecker(facereaderChannel, ClassificationData.class);
    }

    @Override
    public void close() {
        super.close();
        stop();
    }

    private void start() {
        gui.setVisible(true);

        try {
            String address = config.getProperty(ADDRESS_PROP);
            int port = Integer.parseInt(config.getProperty(PORT_PROP));

            gui.connectTo(InetAddress.getByName(address), port);
        }
        catch (UnknownHostException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        gui.dispose();
    }

    private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }
}
