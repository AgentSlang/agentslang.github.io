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

package org.ib.component.model.validation;

import org.ib.component.model.ComponentConfig;
import org.ib.component.model.ComponentInfo;
import org.ib.component.model.ComponentModel;
import org.ib.logger.LogComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/16/13
 */
public class DebugValidation implements ModelValidation {
    public ModelValidationResult checkModel(ComponentModel model) {
        ModelValidationResult result = new ModelValidationResult();
        Set<String> debuggerSubscriptions = new HashSet<String>();
        Set<String> availableDebugChannels = new HashSet<String>();

        boolean loggerConfigured = false;

        ComponentModel.ComponentType type = ComponentModel.ComponentType.COMPONENT;
        for (String componentID : model.getAllIds(type)) {
            ComponentInfo info = model.getComponent(type, componentID);
            String hostPort = info.getProperties().getProperty(ComponentConfig.PROPERTY_MACHINE_NAME)
                    + ":" + info.getProperties().getProperty(ComponentConfig.PROPERTY_PORT);
            try {
                Class clazz = Class.forName(info.getName());
                if (LogComponent.class.isAssignableFrom(clazz)) {
                    loggerConfigured = true;
                    for (String publishChannel : info.getProperties().getPropertyList(ComponentConfig.PROPERTY_SUBSCRIBE)) {
                        debuggerSubscriptions.add(publishChannel);
                    }
                } else {
                    availableDebugChannels.add(info.getName() + ".debug@" + hostPort);
                }
            } catch (ClassNotFoundException e) {
                //-- ignore
            }
        }

        if (!loggerConfigured) {
            result.addWarningMessage("No Logger is configured for this system. See Murphy's law for more details.");
        }

        Set<String> debuggerWarnings = new HashSet<String>(debuggerSubscriptions);
        debuggerWarnings.removeAll(availableDebugChannels);
        for (String warningChannel : debuggerWarnings) {
            result.addWarningMessage("The Logger subscribes, but nobody publishes this topic: " + warningChannel);
        }

        debuggerWarnings = new HashSet<String>(availableDebugChannels);
        debuggerWarnings.removeAll(debuggerSubscriptions);
        for (String warningChannel : debuggerWarnings) {
            result.addWarningMessage("Log data is available, but the logger is not interested by this topic: " + warningChannel);
        }

        return result;
    }
}
