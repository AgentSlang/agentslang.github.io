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

package org.ib.component;

import org.ib.communication.OutboundManager;
import org.ib.component.base.Closeable;
import org.ib.component.base.Publisher;
import org.ib.data.GenericData;
import org.ib.data.SystemHeartbeat;
import org.ib.logger.Logger;
import org.ib.service.generic.ClientManager;
import org.ib.service.topic.TopicClient;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/22/12
 */
public class ScheduleManager extends Thread implements Publisher, Closeable {
    private int timeout = 100;
    private boolean running = true;
    protected OutboundManager heartbeatManager;

    public final static String heartbeatTopic = "schedule.heartbeat";

    private String localMachinePattern;

    public ScheduleManager(int timeout, String port, String machineName) {
        localMachinePattern = machineName + ":" + port;
        this.timeout = timeout;
        heartbeatManager = new OutboundManager(this, port);

        TopicClient tc = ClientManager.getClient(ClientManager.TOPIC);
        tc.addTopic(heartbeatTopic, localMachinePattern);
        Logger.setupDebug(this);
    }

    public void kill() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                //-- ignore
            }
            heartbeatManager.publishData(heartbeatTopic, new SystemHeartbeat(System.currentTimeMillis()));
        }
        heartbeatManager.close();
    }

    public void addOutboundTypeChecker(String internalTopic, Class type) {
        //-- no checkers
    }

    public void publish(String externalTopic, String internalTopic) {
        TopicClient tc = ClientManager.getClient(ClientManager.TOPIC);
        tc.addTopic(externalTopic, localMachinePattern);
    }

    public void publishData(String topic, GenericData data) {
        heartbeatManager.publishData(topic, data);
    }

    public String getMachinePortPattern() {
        return localMachinePattern;
    }

    public void close() {
        kill();
    }
}
