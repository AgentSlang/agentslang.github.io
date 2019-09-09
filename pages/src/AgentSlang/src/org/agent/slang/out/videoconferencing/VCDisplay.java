/*
 * Copyright (c) Ovidiu Serban, ovidiu@roboslang.org
 *               web:http://ovidiu.roboslang.org/
 *               Sami Boukortt <sami.boukortt@insa-rouen.fr>
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
 * The CECILL-B license file should be included along with this project. If not,
 * it can be obtained at  <http://www.cecill.info/>.
 *
 * The use of this project makes it mandatory to cite the authors in
 * any scientific publication or technical reports. For websites or
 * research projects the AgentSlang website and logo need to be linked
 * in a visible area.
 */

package org.agent.slang.out.videoconferencing;

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.SourceComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.logger.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import static org.agent.slang.in.videoconferencing.VCStreamer.startWithExecutableExtension;

/**
 * This component can display what
 * {@link org.agent.slang.in.videoconferencing.VCStreamer} streams.
 * <p>
 * It requires exactly one parameter: <code>basePort</code>, which should be
 * set to the port given in the <code>target</code> parameter of
 * {@link org.agent.slang.in.videoconferencing.VCStreamer}.
 * <p>
 * As mentioned in the documentation for that component, make sure to allocate
 * a few more ports.
 * 
 * OS Compatibility: Windows and Linux
 * 
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 4/16/15
 */
@ConfigureParams(mandatoryConfigurationParams = {"basePort"})
public class VCDisplay extends SourceComponent {
    private long messageID = 0;
    private int basePort;

    private Process gstreamer = null;
    private String saveTo;

    public VCDisplay(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    @Override
    protected void setupComponent(ComponentConfig config) {
        basePort = Integer.parseInt(config.getProperty("basePort"));
        saveTo = config.getProperty("saveTo");
        start();
    }

    /**
     * Checking type of output data.
     */
    @Override
    public void definePublishedData() {}

    /**
     * Closes and stops displaying video. 
     */
    @Override
    public void close() {
        super.close();
        stop();
    }

    /**
     * Starts receiving video from another machine.
     */
    private void start() {
        try {
        	ProcessBuilder pb = new ProcessBuilder("gst-launch-1.0",
                    "rtpbin", "name=rtpbin", "do-lost=true",

                    "udpsrc", "caps=application/x-rtp, media=(string)audio, clock-rate=(int)48000, encoding-name=(string)X-GST-OPUS-DRAFT-SPITTKA-00", "port=" + basePort, "!", "rtpbin.recv_rtp_sink_0",
                    "rtpbin.", "!", "rtpopusdepay", "!", "queue", "!", "decodebin", "!", "audioconvert", "!", "audioresample", "!", "autoaudiosink",
                    "udpsrc", "port=" + (basePort + 1), "!", "rtpbin.recv_rtcp_sink_0",
                    "rtpbin.send_rtcp_src_0", "!", "udpsink", "port=" + (basePort + 5), "sync=false", "async=false",

                    "udpsrc", "caps=application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264", "port=" + (basePort + 2), "!", "rtpbin.recv_rtp_sink_1",
                    "rtpbin.", "!", "rtph264depay", "!", "queue", "!", "decodebin", "!", "videoconvert", "!", "videoscale", "!", "autovideosink",
                    "udpsrc", "port=" + (basePort + 3), "!", "rtpbin.recv_rtcp_sink_1",
                    "rtpbin.send_rtcp_src_1", "!", "udpsink", "port=" + (basePort + 7), "sync=false", "async=false"
                ).inheritIO(); 
        	
            
            if (saveTo != null) 
                pb.command().addAll(Arrays.asList(
                    "matroskamux", "name=outputfile", "!", "filesink", "location=" + saveTo,
                    "video.", "!", "queue", "!", "outputfile.",
                    "audio.", "!", "queue", "!", "outputfile."
                ));
            
            gstreamer = startWithExecutableExtension(pb);
        }
        catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Logger.log(this, Logger.CRITICAL, sw.toString());
        }
    }

    /**
     * Stops receiving video from another machine.
     */
    private void stop() {
        if (gstreamer != null) {
            gstreamer.destroy();
        }
    }
}
