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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/16/13
 */
public class ModelValidationResult {
    private Set<String> errorList = null;
    private Set<String> warningList = null;

    public ModelValidationResult() {
    }

    public void addErrorMessage(String errorMessage) {
        if (errorList == null) {
            errorList = new LinkedHashSet<String>();
        }
        errorList.add(errorMessage);
    }

    public void addErrorMessages(Set<String> errorMessages) {
        if (errorList == null) {
            errorList = new LinkedHashSet<String>();
        }
        errorList.addAll(errorMessages);
    }

    public Set<String> getErrorList() {
        if (errorList == null) {
            return Collections.emptySet();
        } else {
            return errorList;
        }
    }

    public boolean hasErrors() {
        return errorList != null && !errorList.isEmpty();
    }

    public void addWarningMessage(String warningMessage) {
        if (warningList == null) {
            warningList = new LinkedHashSet<String>();
        }
        warningList.add(warningMessage);
    }

    public void addWarningMessages(Set<String> warningMessages) {
        if (warningList == null) {
            warningList = new LinkedHashSet<String>();
        }
        warningList.addAll(warningMessages);
    }

    public Set<String> getWarningList() {
        if (warningList == null) {
            return Collections.emptySet();
        } else {
            return warningList;
        }
    }

    public boolean hasWarnings() {
        return warningList != null && !warningList.isEmpty();
    }

    public boolean isValid() {
        return !hasErrors();
    }
}
