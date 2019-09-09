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

package org.agent.slang.service.km.cismef.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/19/13
 */
public class Concept {
    public static final String LABEL_FR = "fr";
    public static final String LABEL_EN = "en";

    private boolean isKeyWord = false;
    private boolean isPrincipal = false;
    private String conceptType = null;
    private Map<String, String> conceptLabels = new HashMap<String, String>();
    private List<RelatedConcept> relatedConcepts = new LinkedList<RelatedConcept>();

    public Concept() {
    }

    public boolean isKeyWord() {
        return isKeyWord;
    }

    public void setKeyWord(boolean keyWord) {
        isKeyWord = keyWord;
    }

    public boolean isPrincipal() {
        return isPrincipal;
    }

    public void setPrincipal(boolean principal) {
        isPrincipal = principal;
    }

    public String getConceptType() {
        return conceptType;
    }

    public void setConceptType(String conceptType) {
        this.conceptType = conceptType;
    }

    public String getConceptLabels(String label) {
        return conceptLabels.get(label);
    }

    public void setConceptLabels(String conceptLabel, String value) {
        conceptLabels.put(conceptLabel, value);
    }

    public void setConceptLabels(Map<String, String> conceptLabels) {
        this.conceptLabels.putAll(conceptLabels);
    }

    public List<RelatedConcept> getRelatedConcepts() {
        return relatedConcepts;
    }

    public void addRelatedConcepts(RelatedConcept relatedConcept) {
        this.relatedConcepts.add(relatedConcept);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!relatedConcepts.isEmpty()) {
            sb.append("\n");
            for (RelatedConcept relatedConcept : getRelatedConcepts()) {
                sb.append("\t").append(relatedConcept.toString()).append("\n");
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        return "Concept{" + "isPrincipal=" + isPrincipal() + ", isKeyWord=" + isKeyWord() +
                ", conceptType='" + getConceptType() + '\'' + ", conceptLabels=" + conceptLabels + '}' + sb.toString();
    }
}
