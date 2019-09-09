/*
 * Copyright (c) Ovidiu Serban, ovidiu@roboslang.org
 *               web:http://ovidiu.roboslang.org/
 *               Sami Boukortt, sami.boukortt@insa-rouen.fr
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

package org.agent.slang.in.videoconferencing;

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.SourceComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.logger.Logger;
import org.ib.service.cns.CNClient;
import org.ib.service.generic.ClientManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This component enables to stream a webcam and a microphone to other machines.
 *
 * Using it requires the installation of
 * <a href="http://gstreamer.freedesktop.org/">GStreamer</a> in its <code>1.x</code>
 * series (preferably a "complete" installation).
 *
 * <h3>Basic functionality</h3>
 *
 * To stream the multimedia data to other machines, the "<code>targets</code>"
 * parameter should contain a semi-column-separated list of <code>hostname:port</code>
 * elements.
 * <p>
 * Each <code>hostname</code> should be either an IP address or a machine defined
 * in the configuration for <code>CNService</code>.
 * <p>
 * As it is currently implemented, streaming does not use just one port but six per
 * "target": not only <code>port</code> but also <code>port + {1, 2, 3, 5, 7}</code>
 * should be available on the target machine.
 * <p>
 * Each target machine should in turn run a
 * {@link org.agent.slang.out.videoconferencing.VCDisplay} component with the
 * <code>basePort</code> parameter set to the chosen port.
 *
 * <h3>IP Camera Simulation</h3>
 *
 * So as to keep the camera available to other software (most notably
 * {@link org.agent.slang.in.facereader.FaceReaderComponent FaceReader}), this
 * component is capable of simulating an IP camera.
 * <p>
 * Two parameters must be used if this feature is needed: <code>ipcamerafrom</code>,
 * which must be an available local port but otherwise bears no impact, and
 * <code>ipcamerato</code>, which defines the port on which the camera will be
 * available.
 * <p>
 * For example, if <code>ipcamerato</code> is set to 4001, the camera can be seen
 * in an MJPEG-compatible client (such as VLC, QuickTime Player, Firefox, Safari or
 * Chrome) at <a href="http://localhost:4001/"><code>http://localhost:4001/</code></a>.
 *
 * <h3>Saving to a file</h3>
 *
 * Finally, the multimedia streams can be saved to an MKV file by setting the
 * <code>saveTo</code> parameter to the desired location. The video stream will
 * be encoded using H.264 and the audio stream using
 * <a href="http://opus-codec.org/">Opus</a>.
 *
 * OS Compatibility: Windows and Linux
 * 
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @author Sahba Zojaji, sahba.zojaji@insa-rouen.fr
 * @version 1, 4/16/15
 * @version 2, 22/05/2017
 */
@ConfigureParams(mandatoryConfigurationParams = {"targets"},
                 optionalConfigurationParams = {"ipcamerafrom", "ipcamerato", "saveTo"})
public class VCStreamer extends SourceComponent {
    private List<String> targetHosts;
    private List<Integer> targetPorts;
    private String saveTo;
    private Integer ipCameraFrom, ipCameraTo;

    private Process gstreamer = null;

    public VCStreamer(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    @Override
    protected void setupComponent(ComponentConfig config) {
        CNClient client = ClientManager.getClient(ClientManager.CN);
        try {
            saveTo = config.getProperty("saveTo");
            Date date = new Date();
        	
        	DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
            int i = saveTo.indexOf('.');
            saveTo = saveTo.substring(0, i) + dateFormat.format(date) + saveTo.substring(i, saveTo.length());
            
            targetHosts = new ArrayList<>();
            targetPorts = new ArrayList<>();
            for (String target: config.getProperty("targets").split(";")) {
                String[] parts = target.split(":");
                targetHosts.add(InetAddress.getByName(client.resolveHost(parts[0])).getHostAddress());
                targetPorts.add(Integer.parseInt(parts[1]));
            }

            String rawIpCameraFrom = config.getProperty("ipcamerafrom"),
                   rawIpCameraTo   = config.getProperty("ipcamerato");
            if (rawIpCameraFrom != null && rawIpCameraTo != null) {
                ipCameraFrom = Integer.parseInt(rawIpCameraFrom);
                ipCameraTo = Integer.parseInt(rawIpCameraTo);
            }

            start();
        }
        catch (UnknownHostException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Logger.log(this, Logger.CRITICAL, sw.toString());
        }
    }

    /**
     * Checking type of output data.
     */
    @Override
    public void definePublishedData() {}

    /**
     * Closes connection and stops streaming user's webcam video and audio on http port.
     */
    @Override
    public void close() {
        super.close();
        stop();
    }

    /**
     * Starts streaming user's webcam video and audio on http port and defining video and audio format and gstreamer configuration.
     */
    private void start() {
        final boolean enableIpCamera = ipCameraFrom != null && ipCameraTo != null;

        if (enableIpCamera) {
            new IPCameraEmulator(ipCameraFrom, ipCameraTo).start();
        }

        String webcamsrc =
            gstElementExists("ksvideosrc") ? "ksvideosrc" :
            gstElementExists("osxvideosrc") ? "osxvideosrc" :
            gstElementExists("v4l2src") ? "v4l2src" :
            "autovideosrc";

        try {
            ProcessBuilder pb = new ProcessBuilder("gst-launch-1.0", "--eos-on-shutdown",
                "autoaudiosrc", "!", "queue", "!", "audioconvert", "!", "audioresample", "!", "audio/x-raw, rate=48000, channels=1",
                    "!", "opusenc", "!", "tee", "name=audio",
                webcamsrc, "!", "tee", "name=webcam", "!", "queue", "!", "videoconvert", "!", "video/x-raw, format=I420",
                    "!", "x264enc", "tune=zerolatency", "intra-refresh=true", "!", "tee", "name=video",

                "audio.", "!", "queue", "!", "rtpopuspay", "!", "tee", "name=rtpaudio",
                "video.", "!", "queue", "!", "rtph264pay", "!", "tee", "name=rtpvideo",

                "rtpbin", "name=rtpbin"
            ).inheritIO();

            for (int i = 0; i < targetHosts.size(); ++i) {
                String host = targetHosts.get(i);
                int basePort = targetPorts.get(i);
                int audioPad = i * 2;
                int videoPad = i * 2 + 1;
                pb.command().addAll(Arrays.asList(
                    "rtpaudio.", "!", "queue", "!", "rtpbin.send_rtp_sink_" + audioPad,
                    "rtpbin.send_rtp_src_" + audioPad, "!", "udpsink", "host=" + host, "port=" + basePort,
                    "rtpbin.send_rtcp_src_" + audioPad, "!", "udpsink", "host=" + host, "port=" + (basePort + 1), "sync=false", "async=false",
                    "udpsrc", "port=" + (basePort + 5), "!", "rtpbin.recv_rtcp_sink_" + audioPad,

                    "rtpvideo.", "!", "queue", "!", "rtpbin.send_rtp_sink_" + videoPad,
                    "rtpbin.send_rtp_src_" + videoPad, "!", "udpsink", "host=" + host, "port=" + (basePort + 2),
                    "rtpbin.send_rtcp_src_" + videoPad, "!", "udpsink", "host=" + host, "port=" + (basePort + 3), "sync=false", "async=false",
                    "udpsrc", "port=" + (basePort + 7), "!", "rtpbin.recv_rtcp_sink_" + videoPad
                ));
            }

            if (saveTo != null) {
                pb.command().addAll(Arrays.asList(
                    "matroskamux", "name=outputfile", "!", "filesink", "location=" + saveTo,
                    "video.", "!", "queue", "!", "outputfile.",
                    "audio.", "!", "queue", "!", "outputfile."
                ));
            }

            if (enableIpCamera) {
                pb.command().addAll(Arrays.asList(
                    "webcam.", "!", "queue",
                        "!", "jpegenc", "!", "multipartmux", "boundary=wxcvbn",
                        "!", "tcpclientsink", "port=" + ipCameraFrom
                ));
            }

            gstreamer = startWithExecutableExtension(pb);
        }
        catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Logger.log(this, Logger.CRITICAL, sw.toString());
        }
    }

    /**
     * Stops gstreamer.
     */
    private void stop() {
        if (gstreamer != null) {
            gstreamer.destroy();
        }
    }

    /**
     * Starts a process for running gstreamer with predefined configuration.
     * @param pb Process Builder to run gstreamer
     * @return process
     * @throws IOException IO exception 
     */
    public static Process startWithExecutableExtension(ProcessBuilder pb) throws IOException {
        List<String> extensions = new LinkedList<>();
        String pathext = System.getenv("PATHEXT");
        if (pathext != null) {
            extensions.addAll(Arrays.asList(pathext.split(";")));
        }
        extensions.add("");

        List<String> command = pb.command();
        String original = command.get(0);
        IOException exception = null;

        for (String extension: extensions) {
            command.set(0, original + extension);
            try {
                return pb.start();
            }
            catch (IOException e) {
                exception = e;
            }
        }

        throw exception;
    }

    /**
     * Checks if a specefici element exists in gstreamer.
     * @param element element name
     * @return boolean
     */
    private static boolean gstElementExists(String element) {
        try {
            return startWithExecutableExtension(new ProcessBuilder("gst-inspect-1.0", "--exists", element)).waitFor() == 0;
        }
        catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
