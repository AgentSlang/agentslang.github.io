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

package org.ib.component.base;

import org.ib.communication.InboundManager;
import org.ib.communication.OutboundManager;
import org.ib.component.ScheduleManager;
import org.ib.component.consistency.ConsistencyManager;
import org.ib.component.consistency.DataChecker;
import org.ib.component.model.ComponentConfig;
import org.ib.data.ComponentHeartbeat;
import org.ib.data.GenericData;
import org.ib.data.SystemEvent;
import org.ib.data.SystemHeartbeat;
import org.ib.logger.Logger;
import org.ib.service.generic.ClientManager;
import org.ib.service.topic.TopicClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/20/12
 */
public abstract class AbstractComponent implements ActiveListener, Publisher, Closeable, ReactiveListener {
    protected OutboundManager outboundManager;
    protected InboundManager inboundManager;

    protected ComponentConfig config;

    private final Set<String> systemTopics = new HashSet<String>();
    private final Map<String, String> topicMapper = new HashMap<String, String>();

    private String localMachinePattern;
    protected String heartbeatTopic;
    protected String systemEventTopic;

    protected static final String INBOUND_GROUP = "__INBOUND_";

    private ConsistencyManager consistencyManager;

    public AbstractComponent(String outboundPort, ComponentConfig config) {
        this.config = config;

        consistencyManager = new ConsistencyManager();
        localMachinePattern = config.getProperty(ComponentConfig.PROPERTY_MACHINE_NAME) + ":" + outboundPort;

        outboundManager = new OutboundManager(this, outboundPort);
        inboundManager = new InboundManager(this);

        inboundManager.subscribe(ScheduleManager.heartbeatTopic, this, config.getProperty(ComponentConfig.PROPERTY_SCHEDULER));
        addInboundTypeChecker(SystemHeartbeat.class);

        heartbeatTopic = generateHeartbeatTopic();
        systemTopics.add(heartbeatTopic);
        publish(heartbeatTopic, heartbeatTopic);
        addOutboundTypeChecker(heartbeatTopic, ComponentHeartbeat.class);

        systemTopics.add(Logger.generateTopic(this));
        Logger.setupDebug(this);

        systemEventTopic = generateSystemTopic();
        systemTopics.add(systemEventTopic);
        publish(systemEventTopic, systemEventTopic);
        addOutboundTypeChecker(systemEventTopic, SystemEvent.class);
        addInboundTypeChecker(SystemEvent.class);
    }

    public final void react(GenericData data) {
        if (data instanceof SystemHeartbeat) {
            if (this.act()) {
                // beat --
                publishData(heartbeatTopic, new ComponentHeartbeat(localMachinePattern, System.currentTimeMillis()));
            }
        } else if (data instanceof SystemEvent) {
            handleSystemEvents((SystemEvent) data);
        } else {
            if (checkConsistency(INBOUND_GROUP, data)) {
                handleData(data);
                // beat --
                publishData(heartbeatTopic, new ComponentHeartbeat(localMachinePattern, System.currentTimeMillis()));
            }
        }
    }

    public void setupComponentConfig(ComponentConfig config) {
        setupComponent(config);
        publishData(systemEventTopic, new SystemEvent(1, SystemEvent.SYSTEM_WAKE, this.getClass().getName()));
    }

    protected abstract void setupComponent(ComponentConfig config);

    protected abstract void handleData(GenericData data);

    protected void handleSystemEvents(SystemEvent event) {
    }

    private boolean checkConsistency(String group, GenericData data) {
        return consistencyManager.check(group, data, true);
    }

    private void addTypeChecker(String group, Class<? extends GenericData> type) {
        consistencyManager.addChecker(group, new DataChecker(type));
    }

    public final void addInboundTypeChecker(Class<? extends GenericData> type) {
        addTypeChecker(INBOUND_GROUP, type);
    }

    public final void addOutboundTypeChecker(String internalTopic, Class<? extends GenericData> type) {
        addTypeChecker(internalTopic, type);
    }

    public void publish(String externalTopic, String internalTopic) {
        mapInternalTopic(externalTopic, internalTopic);
        TopicClient tc = ClientManager.getClient(ClientManager.TOPIC);
        tc.addTopic(externalTopic, localMachinePattern);
    }

    public void subscribe(String topic, String host, ReactiveListener listener) {
        inboundManager.subscribe(topic, listener, host);
    }

    public void publishData(String internalTopic, GenericData data) {
        if (checkConsistency(internalTopic, data)) {
            String externalTopic = getExternalTopic(internalTopic);
            if (externalTopic != null) {
                outboundManager.publishData(externalTopic, data);
            } else {
                // Nobody subscribed to this information ...
                // Did you consider not publishing it ?
            }
        }
    }

    public String getMachinePortPattern() {
        return localMachinePattern;
    }

    private void mapInternalTopic(String externalTopic, String internalTopic) {
        if (systemTopics.contains(internalTopic) || consistencyManager.getDefinedGroups().contains(internalTopic)) {
            synchronized (topicMapper) {
                topicMapper.put(internalTopic, externalTopic);
            }
        } else {
            Logger.log(this, Logger.CRITICAL, "Invalid internal topic for mapping defined: " + internalTopic);
        }
    }

    private String getExternalTopic(String internalTopic) {
        synchronized (topicMapper) {
            return topicMapper.get(internalTopic);
        }
    }

    private String generateHeartbeatTopic() {
        return this.getClass().getName() + ".heartbeat";
    }

    protected String generateSystemTopic() {
        return this.getClass().getName() + ".system";
    }

    public void close() {
        publishData(systemEventTopic, new SystemEvent(2, SystemEvent.SYSTEM_SHUTDOWN, this.getClass().getName()));
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            //-- ignore
        }

        inboundManager.close();
        outboundManager.close();
    }
}
