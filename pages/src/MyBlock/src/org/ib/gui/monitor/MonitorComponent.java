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

package org.ib.gui.monitor;

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.model.ComponentConfig;
import org.ib.data.ComponentHeartbeat;
import org.ib.data.DebugData;
import org.ib.data.GenericData;
import org.ib.logger.LogComponent;

import java.io.File;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/6/12
 */

@ConfigureParams(inputDataTypes = {DebugData.class, ComponentHeartbeat.class})
public class MonitorComponent extends LogComponent {
    private ComponentViewer viewer;

    public MonitorComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);

        if (config.hasProperty(ComponentConfig.PROPERTY_FILENAME)) {
            viewer = new ComponentViewer(new File(config.getProperty(ComponentConfig.PROPERTY_FILENAME)));
        } else {
            viewer = new ComponentViewer();
        }
        viewer.setVisible(true);
    }

    public void defineReceivedData() {
        super.defineReceivedData();
        addInboundTypeChecker(ComponentHeartbeat.class);
    }

    protected void handleData(GenericData data) {
        if (data instanceof DebugData) {
            super.handleData(data);
        } else if (data instanceof ComponentHeartbeat) {
            handleActivity(((ComponentHeartbeat) data).getSourceID(), data.getId());
        }
    }

    protected void handleActivity(String nodeID, long timestamp) {
        if (viewer != null && viewer.isVisible()) {
            viewer.highlightNode(nodeID, timestamp);
        }
    }

    protected void printMessage(int level, String source, String message) {
        if (viewer != null && viewer.isVisible()) {
            viewer.addMessage(level, source, message);
        } else {
            super.printMessage(level, source, message);
        }
    }

    protected void printException(int level, String source, String message, String e) {
        if (e != null) {
            if (viewer != null && viewer.isVisible()) {
                printMessage(level, source, message + "\n" + e);
            } else {
                super.printException(level, source, message, e);
            }
        }
    }
}
