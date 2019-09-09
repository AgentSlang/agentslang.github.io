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

package org.syn.n.bad.pattern;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/24/13
 */
public class TemplateMatchResult {
    private List<String> templateIds = new LinkedList<String>();
    private Map<String, String> extractedVars = new HashMap<String, String>();
    private Map<String, String> extractedStyles = new HashMap<String, String>();

    public TemplateMatchResult() {
    }

    public void addTemplateId(String templateID) {
        templateIds.add(templateID);
    }

    public void updateVariables(Map<String, String> extractedVars) {
        this.extractedVars.putAll(extractedVars);
    }

    public Map<String, String> getExtractedVars() {
        return extractedVars;
    }

    public void updateStyles(Map<String, String> extractedStyles) {
        this.extractedStyles.putAll(extractedStyles);
    }

    public Map<String, String> getExtractedStyles() {
        return extractedStyles;
    }

    public List<String> getTemplateIDs() {
        return templateIds;
    }

    public boolean isEmpty() {
        return templateIds.isEmpty();
    }

    public String toString() {
        return "" + templateIds;
    }
}
