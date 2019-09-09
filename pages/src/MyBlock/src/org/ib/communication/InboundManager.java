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

package org.ib.communication;

import org.ib.component.base.Publisher;
import org.ib.component.base.ReactiveListener;
import org.ib.data.DataHelper;
import org.ib.data.InvalidDataException;
import org.ib.logger.Logger;
import org.ib.service.cns.CNClient;
import org.ib.service.generic.ClientManager;
import org.ib.service.topic.TopicClient;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/20/12
 */
public class InboundManager extends Thread {
    private class TopicWrapper {
        private ReactiveListener reactiveListener;

        private String connectionPattern;

        private TopicWrapper(ReactiveListener reactiveListener, String host) {
            this.reactiveListener = reactiveListener;
            CNClient cnClient = ClientManager.getClient(ClientManager.CN);
            this.connectionPattern = String.format("%s://%s", protocol, cnClient.resolveHost(host));
        }
    }

    private Map<String, TopicWrapper> topicMapper = new HashMap<String, TopicWrapper>();
    private boolean running = true;

    private static final String protocol = "tcp";

    private Map<String, Integer> connectedHosts = new HashMap<String, Integer>();

    private ZMQ.Context context;
    private ZMQ.Socket receiver;

    private Publisher component;

    public InboundManager(Publisher component) {
        this.component = component;

        context = ZMQ.context(1);
        receiver = context.socket(ZMQ.SUB);

        setName(component.getMachinePortPattern() + " Thread");
    }

    public void close() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
                DataHelper.DecodeResult data = DataHelper.decodeData(receiver.recv());

                String topic = data.getTopic().split("@")[0];

                TopicWrapper topicWrapper = topicMapper.get(topic);

                if (topicWrapper != null) {
                    topicWrapper.reactiveListener.react(data.getData());
                } else {
                    Logger.log(component, Logger.INFORM, "(Inbound) Received data for an invalid feed ...");
                }
            } catch (InvalidDataException e) {
                Logger.log(component, Logger.INFORM, "Invalid data exception: " + e, e);
            } catch (ClassCastException e) {
                Logger.log(component, Logger.INFORM, "Class cast exception: " + e, e);
            }
        }
        //shutdown 
        receiver.close();
        context.term();
    }

    public void subscribe(String topic, ReactiveListener listener, String host) {
        TopicWrapper topicWrapper = new TopicWrapper(listener, host);

        if (!connectedHosts.containsKey(topicWrapper.connectionPattern)) {
            receiver.connect(topicWrapper.connectionPattern);
            connectedHosts.put(topicWrapper.connectionPattern, 1);
        } else {
            Integer count = connectedHosts.get(topicWrapper.connectionPattern);
            connectedHosts.put(topicWrapper.connectionPattern, count + 1);
        }
        TopicClient tc = ClientManager.getClient(ClientManager.TOPIC);
        receiver.subscribe(tc.getEncoding(topic, host));

        topicMapper.put(topic, topicWrapper);

        if (!isAlive()) {
            start();
        }
    }

    public void unsubscribe(String topic, String host) {
        TopicWrapper topicWrapper = topicMapper.get(topic);

        if (topicWrapper != null) {
            TopicClient tc = ClientManager.getClient(ClientManager.TOPIC);
            receiver.unsubscribe(tc.getEncoding(topic, host));

            Integer count = connectedHosts.get(topicWrapper.connectionPattern);
            if (count != null && count > 1) {
                connectedHosts.put(topicWrapper.connectionPattern, count - 1);
            } else {
                connectedHosts.remove(topicWrapper.connectionPattern);
                receiver.disconnect(topicWrapper.connectionPattern);
            }

            topicMapper.remove(topic);
        }
    }
}
