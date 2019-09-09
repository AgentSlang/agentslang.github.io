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

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.annotations.TestClass;
import org.ib.component.model.ComponentConfig;
import org.ib.component.model.ComponentInfo;
import org.ib.component.model.ComponentModel;

import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/16/13
 */
public class LinkValidation implements ModelValidation {
    public ModelValidationResult checkModel(ComponentModel model) {
        ModelValidationResult result = new ModelValidationResult();
        Set<String> externalChannels = new HashSet<String>();
        Set<String> systemChannels = new HashSet<String>();
        Set<String> ignoreClazz = new HashSet<String>();
        Map<String, String> externalChannelCounter = new HashMap<String, String>();

        ComponentModel.ComponentType type = ComponentModel.ComponentType.COMPONENT;
        for (String componentID : model.getAllIds(type)) {
            ComponentInfo info = model.getComponent(type, componentID);
            String hostPort = info.getProperties().getProperty(ComponentConfig.PROPERTY_MACHINE_NAME)
                    + ":" + info.getProperties().getProperty(ComponentConfig.PROPERTY_PORT);
            try {
                Class clazz = Class.forName(info.getName());
                Set<String> internalChannels = new HashSet<String>();
                if (clazz.isAnnotationPresent(TestClass.class)) {
                    ignoreClazz.add(info.getName());
                } else {
                    if (clazz.isAnnotationPresent(ConfigureParams.class)) {
                        ConfigureParams annotation = (ConfigureParams) clazz.getAnnotation(ConfigureParams.class);
                        internalChannels.addAll(Arrays.asList(annotation.outputChannels()));
                    }

                    for (String publishChannel : info.getProperties().getPropertyList(ComponentConfig.PROPERTY_PUBLISH)) {
                        String[] pair = publishChannel.split("@");
                        if (!internalChannels.contains(pair[1]) && !ignoreClazz.contains(info.getName())) {
                            result.addErrorMessage("Invalid internal channel binding: " + publishChannel
                                    + " * Component: " + info.getName());
                        }
                        externalChannels.add(pair[0] + "@" + hostPort);
                        externalChannelCounter.put(pair[0] + "@" + hostPort, publishChannel + " * Component: " + info.getName());
                    }
                }
                systemChannels.add(info.getName() + ".debug@" + hostPort);
                systemChannels.add(info.getName() + ".system@" + hostPort);
                systemChannels.add(info.getName() + ".heartbeat@" + hostPort);
            } catch (ClassNotFoundException e) {
                //-- ignore
            }
        }

        for (String componentID : model.getAllIds(type)) {
            ComponentInfo info = model.getComponent(type, componentID);
            if (!ignoreClazz.contains(info.getName())) {
                for (String publishChannel : info.getProperties().getPropertyList(ComponentConfig.PROPERTY_SUBSCRIBE)) {
                    if (externalChannels.contains(publishChannel)) {
                        externalChannelCounter.remove(publishChannel);
                    } else if (!systemChannels.contains(publishChannel)) {
                        result.addErrorMessage("Invalid subscribe channel binding. No publish link for: " + publishChannel
                                + " * Component: " + info.getName());
                    }
                }
            }
        }

        for (String externalChannel : externalChannelCounter.values()) {
            result.addWarningMessage("Invalid publish channel binding. No subscription link for: " + externalChannel);
        }
        return result;
    }
}
