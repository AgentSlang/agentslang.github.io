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

package org.ib.logger;

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.SinkComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.DebugData;
import org.ib.data.GenericData;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/22/12
 */

@ConfigureParams(inputDataTypes = DebugData.class)
public class LogComponent extends SinkComponent {
    public LogComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    protected void setupComponent(ComponentConfig config) {
    }

    public void defineReceivedData() {
        addInboundTypeChecker(DebugData.class);
    }

    protected void handleData(GenericData data) {
        if (data instanceof DebugData) {
            DebugData debugData = (DebugData) data;

            if (debugData.hasException()) {
                printException(debugData.getLevel(), debugData.getSource(), formatMessage(debugData), debugData.getException());
            } else {
                printMessage(debugData.getLevel(), debugData.getSource(), formatMessage(debugData));
            }
        }
    }

    protected void printMessage(int level, String source, String message) {
        System.out.println(message);
    }

    protected void printException(int level, String source, String message, String e) {
        System.out.println(message);
        if (e != null) {
            System.err.println(e);
        }
    }

    private String formatMessage(DebugData debugData) {
        return String.format("(%s)[%s] %s", getLevel(debugData.getLevel()), debugData.getSource(), debugData.getMessage());
    }

    protected String getLevel(int level) {
        switch (level) {
            case DebugData.DEBUG:
                return "DEBUG";
            case DebugData.INFORM:
                return "INFORM";
            case DebugData.CRITICAL:
                return "CRITICAL";
            default:
                return "NONE";
        }
    }
}
