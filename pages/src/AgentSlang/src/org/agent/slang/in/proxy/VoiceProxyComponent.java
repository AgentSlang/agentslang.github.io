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

package org.agent.slang.in.proxy;

import org.agent.slang.in.proxy.socket.BTDataSocket;
import org.agent.slang.in.proxy.socket.DataSocket;
import org.agent.slang.in.proxy.socket.TCPDataSocket;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.SourceComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import org.ib.logger.Logger;

import java.util.Locale;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/5/13
 */
@ConfigureParams(optionalConfigurationParams = {"voiceProxy", "voiceBTuuid", "voiceBTmac"},
        outputChannels = "voice.data", outputDataTypes = StringData.class)
public class VoiceProxyComponent extends SourceComponent implements DataSocket.DataListener {
    private static final String voiceChannel = "voice.data";
    private static final String voiceProxyPort = "voiceProxy";
    private static final String voiceBTuuid = "voiceBTuuid";
    private static final String voiceBTmac = "voiceBTmac";
    private DataSocket socket;
    private long messageID;

    public VoiceProxyComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    protected void setupComponent(ComponentConfig config) {
        setupProxy(config.getProperty(voiceProxyPort), config.getProperty(voiceBTuuid), config.getProperty(voiceBTmac));
    }

    public void definePublishedData() {
        addOutboundTypeChecker(voiceChannel, StringData.class);
    }

    public void close() {
        super.close();
        socket.close();
    }

    private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }

    public void dataReceived(String data) {
        if (data != null && data.trim().length() > 0) {
            publishData(voiceChannel, new StringData(getMessageID(), data, LanguageUtils.getLanguageCodeByLocale(Locale.US)));
        }
    }

    private void setupProxy(String proxyPort, String uuid, String macAddress) {
        if (proxyPort != null && proxyPort.trim().length() > 0) {
            try {
                int port = Integer.parseInt(proxyPort);

                socket = new TCPDataSocket(this, port, this);
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid proxy port provided");
            }
        } else if (uuid != null && uuid.trim().length() > 0) {
            try {
                if (macAddress != null) {
                    System.setProperty("bluecove.deviceAddress", macAddress);
                }
                socket = new BTDataSocket(this, uuid, this);
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid proxy port provided");
            }
        } else {
            Logger.log(this, Logger.CRITICAL, "Invalid proxy port provided");
        }
    }
}
