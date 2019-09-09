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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/16/13
 */
public class ComponentDataTypeValidation implements ModelValidation {
    public ModelValidationResult checkModel(ComponentModel model) {
        ModelValidationResult result = new ModelValidationResult();
        Map<String, Class> externalChannels = new HashMap<String, Class>();

        ComponentModel.ComponentType type = ComponentModel.ComponentType.COMPONENT;
        for (String componentID : model.getAllIds(type)) {
            ComponentInfo info = model.getComponent(type, componentID);
            String hostPort = info.getProperties().getProperty(ComponentConfig.PROPERTY_MACHINE_NAME)
                    + ":" + info.getProperties().getProperty(ComponentConfig.PROPERTY_PORT);
            try {
                Class clazz = Class.forName(info.getName());
                if (!clazz.isAnnotationPresent(TestClass.class)) {
                    if (clazz.isAnnotationPresent(ConfigureParams.class)) {
                        ConfigureParams annotation = (ConfigureParams) clazz.getAnnotation(ConfigureParams.class);
                        if (annotation.outputChannels().length > 0 && annotation.outputChannels()[0].trim().length() > 0) {
                            if (annotation.outputChannels().length != annotation.outputDataTypes().length) {
                                result.addErrorMessage("Class: " + info.getName() + " does not properly annotate the outputDataTypes");
                            } else {
                                for (String publishChannel : info.getProperties().getPropertyList(ComponentConfig.PROPERTY_PUBLISH)) {
                                    String[] pair = publishChannel.split("@");
                                    boolean dataTypeDetermined = false;
                                    for (int i = 0; i < annotation.outputChannels().length; i++) {
                                        if (annotation.outputChannels()[i].equals(pair[1])) {
                                            externalChannels.put(pair[0] + "@" + hostPort, annotation.outputDataTypes()[i]);
                                            dataTypeDetermined = true;
                                            break;
                                        }
                                    }
                                    if (!dataTypeDetermined) {
                                        result.addErrorMessage("Could not determine the DataType of the channel: "
                                                + publishChannel + " * This could be linked to an invalid internal channel linking.");
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                //-- ignore
            }
        }

        for (String componentID : model.getAllIds(type)) {
            ComponentInfo info = model.getComponent(type, componentID);
            try {
                Class clazz = Class.forName(info.getName());
                if (!clazz.isAnnotationPresent(TestClass.class)) {
                    if (clazz.isAnnotationPresent(ConfigureParams.class)) {
                        ConfigureParams annotation = (ConfigureParams) clazz.getAnnotation(ConfigureParams.class);
                        if (annotation.outputChannels().length > 0 && annotation.outputChannels()[0].trim().length() > 0) {
                            if (annotation.inputDataTypes().length > 0) {
                                for (String subscribeChannel : info.getProperties().getPropertyList(ComponentConfig.PROPERTY_SUBSCRIBE)) {
                                    Class channelDataType = externalChannels.get(subscribeChannel);
                                    if (channelDataType != null) {
                                        boolean foundCompatibleInput = false;

                                        for (Class<?> inputDataType : annotation.inputDataTypes()) {
                                            if (inputDataType.isAssignableFrom(channelDataType)) {
                                                foundCompatibleInput = true;
                                                break;
                                            }
                                        }

                                        if (!foundCompatibleInput) {
                                            result.addErrorMessage("Component " + info.getName() + " subscribe topic " + subscribeChannel
                                                    + " does not match the accepted input. " + channelDataType + " is not compatible with "
                                                    + Arrays.toString(annotation.inputDataTypes()));
                                        }
                                    } else {
                                        result.addErrorMessage("Could not determine the DataType of the channel: " + subscribeChannel);
                                    }
                                }
                            } else if (info.getProperties().getPropertyList(ComponentConfig.PROPERTY_SUBSCRIBE).size() > 0) {
                                result.addErrorMessage("Component " + info.getName()
                                        + " subscribes to several topics, but id does not provide a proper inputDataType annotation.");
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                //-- ignore
            }
        }
        return result;
    }
}
