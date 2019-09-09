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

package org.agent.slang.data.template;

import org.ib.data.IdentifiableData;
import org.ib.data.LanguageDependentData;
import org.ib.data.LanguageUtils;
import org.ib.data.TypeIdentification;

import java.util.*;

/**
 * A data type class to store Template data characteristics.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/31/12
 */

@TypeIdentification(typeID = 5)
public class TemplateData implements IdentifiableData, LanguageDependentData {
    private long id;
    private int language;
    private List<String> templateIds = new LinkedList<String>();
    private Map<String, String> extractedVars = new HashMap<String, String>();

    public TemplateData() {
    }

    public TemplateData(long id, int language) {
        this.id = id;
        this.language = language;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public Locale getLocale() {
        return LanguageUtils.getLocaleByCode(language);
    }

    public void addTemplateId(String templateID) {
        templateIds.add(templateID);
    }

    public void addTemplateIds(List<String> templateIds) {
        this.templateIds.addAll(templateIds);
    }

    public void updateVariables(Map<String, String> extractedVars) {
        this.extractedVars.putAll(extractedVars);
    }

    public Map<String, String> getExtractedVars() {
        return extractedVars;
    }

    public void setExtractedVars(Map<String, String> extractedVars) {
        this.extractedVars = extractedVars;
    }

    public List<String> getTemplateIDs() {
        return templateIds;
    }

    public boolean isEmpty() {
        return templateIds.isEmpty();
    }

    public List<String> getTemplateIds() {
        return templateIds;
    }

    public void setTemplateIds(List<String> templateIds) {
        this.templateIds = templateIds;
    }

    public String toString() {
        return "(" + id + ") " + templateIds + variablesToString();
    }

    private String variablesToString() {
        if (extractedVars.isEmpty()) {
            return "";
        } else {
            StringBuffer sb = new StringBuffer(" @Variables:");
            for (Map.Entry<String, String> item : extractedVars.entrySet()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(item.getKey()).append("=").append(item.getValue());
            }
            sb.append(" @");
            return sb.toString();
        }
    }
}
