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

package org.syn.n.bad.pattern.token;

import org.syn.n.bad.annotation.Annotation;
import org.syn.n.bad.annotation.AnnotationToken;
import org.syn.n.bad.annotation.TextAnnotation;
import org.syn.n.bad.annotation.TextAnnotationConstants;

import java.util.Collection;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/5/13
 */
public class POSMatcher extends TokenMatcher {
    private Set<Byte> posLabels;

    public POSMatcher(String pattern, boolean isMandatory) {
        super("<" + pattern + ">", isMandatory);
        pattern = setupVariables(pattern);
        posLabels = TextAnnotationConstants.getGenericPOSLabel(pattern);
    }

    private String setupVariables(String pattern) {
        if (pattern.contains("#")) {
            //contains variable label
            String[] parts = pattern.split("#");
            setVariableLabel("#" + parts[parts.length - 1]);
            if (parts.length > 2) {
                // this is the case when "#" is a tag
                return "#";
            } else {
                return parts[0];
            }
        }
        return pattern;
    }

    public boolean match(TextAnnotation tokens, int index) {
        if (posLabels.isEmpty()) {
            return false;
        }

        Annotation annotation = tokens.getAnnotation(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS));
        if (annotation == null) {
            return false;
        }

        Collection<AnnotationToken> annotationTokenCollection = annotation.getTokens(index);
        if (annotationTokenCollection == null || annotationTokenCollection.isEmpty()) {
            return false;
        }

        for (AnnotationToken token : annotationTokenCollection) {
            if (posLabels.contains(token.getAnnotationLabel())) {
                return true;
            }
        }

        return false;
    }
}
