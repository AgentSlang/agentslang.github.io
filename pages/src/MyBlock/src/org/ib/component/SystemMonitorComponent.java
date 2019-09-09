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

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.SystemEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/18/13
 */

@ConfigureParams(outputChannels = "system.monitor.data", outputDataTypes = SystemEvent.class, inputDataTypes = SystemEvent.class)
public class SystemMonitorComponent extends MixedComponent {
    private static final String outboundTopic = "system.monitor.data";

    private final Map<String, Boolean> componentActivation = new HashMap<String, Boolean>();
    private int activatedComponents = 0;

    public SystemMonitorComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    protected void setupComponent(ComponentConfig config) {
        activatedComponents = 0;

        for (String subscriptionItem : config.getPropertyList(ComponentConfig.PROPERTY_SUBSCRIBE)) {
            subscriptionItem = subscriptionItem.split("@")[0];
            String subscriptionSuffix = subscriptionItem.substring(subscriptionItem.lastIndexOf('.'));
            subscriptionItem = subscriptionItem.substring(0, subscriptionItem.lastIndexOf('.'));

            if (".system".equals(subscriptionSuffix)) {
                componentActivation.put(subscriptionItem, false);
            }
        }
    }

    protected void handleData(GenericData data) {
    }

    protected void handleSystemEvents(SystemEvent event) {
        Boolean flag = componentActivation.get(event.getSourceName());
        if (flag == null) {
            System.err.println("Invalid component name received: " + event.getSourceName());
        } else {
            if (event.getEvent() == SystemEvent.SYSTEM_WAKE) {
                if (!flag) {
                    componentActivation.put(event.getSourceName(), true);
                    activatedComponents++;
                    if (activatedComponents == componentActivation.size()) {
                        System.err.println("System wake !");
                        publishData(outboundTopic, new SystemEvent(1, SystemEvent.SYSTEM_WAKE, "SystemMonitor"));
                    }
                }
            } else if (event.getEvent() == SystemEvent.SYSTEM_SHUTDOWN) {
                componentActivation.remove(event.getSourceName());
                activatedComponents--;
                if (activatedComponents == 0) {
                    publishData(outboundTopic, new SystemEvent(2, SystemEvent.SYSTEM_SHUTDOWN, "SystemMonitor"));
                }
            }
        }
    }

    public void defineReceivedData() {
        addOutboundTypeChecker(outboundTopic, SystemEvent.class);
    }

    public void definePublishedData() {
        addInboundTypeChecker(SystemEvent.class);
    }
}
