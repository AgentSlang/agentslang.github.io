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
import org.syn.n.bad.pattern.token.TokenMatcher;
import org.syn.n.bad.pattern.token.TokenMatcherFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/1/13
 */
public class PatternMatcher {
    private final Map<String, String> styleLabels = new HashMap<String, String>();
    private String id;
    private String pattern;
    private Map<String, String> matchedVars = new HashMap<String, String>();
    private TokenMatcher start;

    protected PatternMatcher(String id) {
        this.id = id;
    }

    public PatternMatcher(String id, String pattern) {
        this(id, pattern, null, Locale.ENGLISH);
    }

    public PatternMatcher(String id, String pattern, String styles) {
        this(id, pattern, styles, Locale.ENGLISH);
    }

    public PatternMatcher(String id, String pattern, Locale language) {
        this(id, pattern, null, language);
    }

    public PatternMatcher(String id, String pattern, String styles, Locale language) {
        this(id);
        if (pattern != null && pattern.trim().length() > 0) {
            start = TokenMatcherFactory.factoryPatternMatcher(language, pattern);
        }
        this.pattern = pattern;
        setStylesLabels(styles);
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getStyleLabels() {
        return styleLabels;
    }

    public void addStyleLabels(Map<String, String> styleLabels) {
        this.styleLabels.putAll(styleLabels);
    }

    public void addStyleLabel(String label, String value) {
        this.styleLabels.put(label, value);
    }

    public void setStylesLabels(String stylesLabels) {
        if (stylesLabels != null) {
            String[] groups = stylesLabels.split(";");
            for (String group : groups) {
                String[] values = group.split("=");
                if (values.length == 2) {
                    String label = values[0].trim();
                    String value = values[1].trim();
                    if (label.length() > 0 && value.length() > 0) {
                        addStyleLabel(label, value);
                    }
                }
            }
        }
    }

    public boolean isValid() {
        return start != null && !start.getMatcherList().isEmpty();
    }

    private void reportVariable(String label, String value) {
        if (matchedVars.containsKey(label)) {
            matchedVars.put(label, matchedVars.get(label) + " " + value);
        } else {
            matchedVars.put(label, value);
        }
    }

    public Map<String, String> getMatchedVars() {
        return matchedVars;
    }

    public PatternMatch match(TextAnnotation tokens, int from, int to) {
    	//System.out.println("\nZJ patterns tokens: "+tokens);
    	
    	matchedVars = new HashMap<String, String>();

        TokenMatcher currentMatcher = start;

        boolean matched = true;
        int matchWindow = 0;
        int match = 0;

        StringBuilder consumptionBuffer = null;
        String consumptionLabel = null;

        while (matched && (from + matchWindow < to)) {
            matched = false;
            for (TokenMatcher matcher : currentMatcher.getMatcherList()) {
                if (matcher.match(tokens, from + matchWindow)) {
                    matched = true;

                    if (matcher.hasVariableLabel()) {
                        if (consumptionLabel == null) {
                            // start consumption
                            consumptionLabel = matcher.getVariableLabel();
                            consumptionBuffer = new StringBuilder(tokens.getToken(from + matchWindow).getText());
                        } else if (consumptionLabel.equals(matcher.getVariableLabel())) {
                            // consume more
                            consumptionBuffer.append(" ").append(tokens.getToken(from + matchWindow).getText());
                        } else {
                            // report last variable and consume new one
                            reportVariable(consumptionLabel, consumptionBuffer.toString());
                            consumptionLabel = matcher.getVariableLabel();
                            consumptionBuffer = new StringBuilder(tokens.getToken(from + matchWindow).getText());
                        }
                    } else if (consumptionLabel != null) {
                        // end consumption and report the last one
                        reportVariable(consumptionLabel, consumptionBuffer.toString());
                        consumptionLabel = null;
                        consumptionBuffer = null;
                    }

                    matchWindow++;
                    if (matcher.getPriority() != TokenMatcher.PRIORITY_SKIP) {
                        match++;
                    }

                    currentMatcher = matcher;
                    break;
                }
            }
        }

        if (consumptionLabel != null) {
            // end consumption and report the last one
            reportVariable(consumptionLabel, consumptionBuffer.toString());
        }

        if (currentMatcher.isFinal()) {
        	//System.out.println("\nZJ patterns tokens: currentMatcher.isFinal(): match: "+match+ ",  matchWindow: "+matchWindow);
            return new PatternMatch(match, matchWindow);
        } else {
        	//System.out.println("\nZJ patterns tokens: (0, 0)");
            return new PatternMatch(0, 0);
        }
    }

    public String toString() {
        return pattern;
    }

    public static final class PatternMatch {
        private int matchCount;
        private int matchWindow;

        public PatternMatch(int matchCount, int matchWindow) {
            this.matchCount = matchCount;
            this.matchWindow = matchWindow;
        }

        public int getMatchCount() {
            return matchCount;
        }

        public int getMatchWindow() {
            return matchWindow;
        }

        public String toString() {
            return "PatternMatch{" +
                    "matchCount=" + matchCount +
                    ", matchWindow=" + matchWindow +
                    '}';
        }
    }
}
