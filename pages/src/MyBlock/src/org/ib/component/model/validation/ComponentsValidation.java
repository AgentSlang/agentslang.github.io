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

import org.ib.component.ScheduleManager;
import org.ib.component.model.ComponentConfig;
import org.ib.component.model.ComponentInfo;
import org.ib.component.model.ComponentModel;

import java.lang.reflect.Constructor;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/16/13
 */
public class ComponentsValidation implements ModelValidation {
    @SuppressWarnings("unchecked")
    public ModelValidationResult checkModel(ComponentModel model) {
        ModelValidationResult result = new ModelValidationResult();
        for (ComponentModel.ComponentType type : model.getAvailableTypes()) {
            for (String componentID : model.getAllIds(type)) {
                ComponentInfo info = model.getComponent(type, componentID);

                if (type == ComponentModel.ComponentType.SCHEDULER) {
                    if (!info.getName().equals(ScheduleManager.class.getName())) {
                        result.addErrorMessage("Invalid scheduler class: " + info.getName());
                    }
                } else {
                    try {
                        Class clazz = Class.forName(info.getName());
                        Constructor constructor = null;
                        switch (type) {
                            case CLIENT:
                                constructor = clazz.getDeclaredConstructor(String.class, String.class);
                                break;
                            case SERVICE:
                                constructor = clazz.getDeclaredConstructor(String.class);
                                break;
                            case COMPONENT:
                                constructor = clazz.getDeclaredConstructor(String.class, ComponentConfig.class);
                                break;
                        }
                        if (constructor != null) {
                            constructor.setAccessible(true);
                        }
                    } catch (NoSuchMethodException e) {
                        result.addErrorMessage("Invalid class constructor for " + info.getName());
                    } catch (ClassNotFoundException e) {
                        result.addErrorMessage("Invalid class provided: " + info.getName());
                    }
                }

            }
        }
        return result;
    }
}
