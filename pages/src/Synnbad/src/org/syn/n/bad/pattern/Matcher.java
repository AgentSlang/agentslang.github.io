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

import org.syn.n.bad.annotation.TextAnnotation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/1/13
 */
public class Matcher {
    private final List<PatternMatcher> matchers = new LinkedList<PatternMatcher>();
    private final Map<String, String> styleLabels = new HashMap<String, String>();

    public void addMatcher(PatternMatcher matcher) {
        synchronized (matchers) {
            if (matcher.isValid()) {
                matchers.add(matcher);

                for (String label : matcher.getStyleLabels().keySet()) {
                    if (!styleLabels.containsKey(label)) {
                        styleLabels.put(label, "*");
                    }
                }
            }
        }
    }

    public void removeMatcher(PatternMatcher matcher) {
        synchronized (matchers) {
            matchers.remove(matcher);
        }
    }

    public MatchResult match(TextAnnotation textTokens, int from, int to) {
        MatchResult result = new MatchResult(null, -1, null, null);
        synchronized (matchers) {
            int matchCount = 0;

            for (PatternMatcher matcher : matchers) {
                PatternMatcher.PatternMatch pm = matcher.match(textTokens, from, to);
                if ((pm.getMatchCount() > matchCount) || (pm.getMatchCount() == matchCount && pm.getMatchWindow() < result.getMatchedTokens())) {
                    result = new MatchResult(matcher.getId(), pm.getMatchWindow(), matcher.getMatchedVars(), setupStyles(matcher.getStyleLabels()));
                    matchCount = pm.getMatchCount();
                }
            }
        }
        return result;
    }

    private Map<String, String> setupStyles(Map<String, String> mathcherStyles) {
        Map<String, String> result = new HashMap<String, String>(styleLabels);
        result.putAll(mathcherStyles);

        return result;
    }

    public TemplateMatchResult match(TextAnnotation textTokens) {
        TemplateMatchResult result = new TemplateMatchResult();
        int i = -1;
        int window = 1;
        while (window > 0 && i < textTokens.size()) {
            i += window;
            MatchResult res = match(textTokens, i, textTokens.size());
            if (res.getTemplateID() != null) {
                window = res.getMatchedTokens();
                result.addTemplateId(res.getTemplateID());
                result.updateVariables(res.getMatchedVars());
                result.updateStyles(res.getStyleLabels());
            } else {
                window = 1;
            }
        }

        return result;
    }
}
