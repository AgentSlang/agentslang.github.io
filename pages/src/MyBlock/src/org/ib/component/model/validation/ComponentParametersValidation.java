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
import org.ib.component.model.ComponentInfo;
import org.ib.component.model.ComponentModel;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/16/13
 */
public class ComponentParametersValidation implements ModelValidation {
    public ModelValidationResult checkModel(ComponentModel model) {
        ModelValidationResult result = new ModelValidationResult();

        ComponentModel.ComponentType type = ComponentModel.ComponentType.COMPONENT;
        for (String componentID : model.getAllIds(type)) {
            ComponentInfo info = model.getComponent(type, componentID);
            try {
                Class clazz = Class.forName(info.getName());
                if (clazz.isAnnotationPresent(ConfigureParams.class)) {
                    ConfigureParams annotation = (ConfigureParams) clazz.getAnnotation(ConfigureParams.class);
                    for (String param : annotation.mandatoryConfigurationParams()) {
                        param = param.trim();
                        if (param.length() > 0 && !info.getProperties().hasProperty(param)) {
                            result.addErrorMessage("Mandatory configuration parameter: " + param + " is missing for component: " + info.getName());
                        }
                    }

                    for (String param : annotation.optionalConfigurationParams()) {
                        param = param.trim();
                        if (param.length() > 0 && !info.getProperties().hasProperty(param)) {
                            result.addWarningMessage("Optional configuration parameter: " + param + " is missing for component: " + info.getName());
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
