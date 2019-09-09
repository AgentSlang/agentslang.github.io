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

import org.ib.component.model.ComponentModel;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/16/13
 */
public class ModelValidationManager {
    private final List<ModelValidation> validators = new LinkedList<ModelValidation>();

    public void addValidator(ModelValidation validator) {
        synchronized (validators) {
            validators.add(validator);
        }
    }

    public void removeValidator(ModelValidation validator) {
        synchronized (validators) {
            validators.remove(validator);
        }
    }

    public ModelValidationResult checkComponentModel(ComponentModel model, boolean haltOnFirstError) {
        ModelValidationResult result = new ModelValidationResult();
        synchronized (validators) {
            for (ModelValidation modelValidation : validators) {
                ModelValidationResult partial = modelValidation.checkModel(model);
                if (!partial.isValid()) {
                    result.addErrorMessages(partial.getErrorList());
                    if (partial.hasErrors() && haltOnFirstError) {
                        break;
                    } else {
                        result.addWarningMessages(partial.getWarningList());
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        ModelValidationManager manager = new ModelValidationManager();
        manager.addValidator(new ComponentsValidation());
        manager.addValidator(new HostnameValidation());
        manager.addValidator(new LinkValidation());
        manager.addValidator(new DebugValidation());
        manager.addValidator(new ComponentParametersValidation());
        manager.addValidator(new ComponentDataTypeValidation());

        ComponentModel model = new ComponentModel(new File("config-test.xml"));
        ModelValidationResult result = manager.checkComponentModel(model, false);
        if (result.isValid()) {
            System.out.println("Model is valid ...");
        } else {
            if (result.hasErrors()) {
                System.out.println("Model Errors:");
                for (String item : result.getErrorList()) {
                    System.out.println("\t" + item);
                }
            }

            if (result.hasWarnings()) {
                System.out.println("Model Warnings:");
                for (String item : result.getWarningList()) {
                    System.out.println("\t" + item);
                }
            }
        }
    }
}
