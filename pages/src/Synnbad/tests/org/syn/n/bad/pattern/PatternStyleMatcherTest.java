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

import org.junit.Before;
import org.junit.Test;
import org.syn.n.bad.annotation.TextAnnotation;
import org.syn.n.bad.annotation.TextToken;

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1/6/13
 */
public class PatternStyleMatcherTest {
    private Matcher matcher;

    @Before()
    public void setup() {
        matcher = new Matcher();
        matcher.addMatcher(new PatternMatcher("un", "xxx yyy zzz", "lang=fr;mood=angry"));
        matcher.addMatcher(new PatternMatcher("un", "xxx yyy", "lang=en"));
        matcher.addMatcher(new PatternMatcher("unt-t", "xxx aaa zzz", "mood=good"));
    }

    @Test()
    public void testSimpleMatcher1() {
        TextAnnotation tokens = new TextAnnotation(3);
        tokens.addTextToken(new TextToken("xxx"));
        tokens.addTextToken(new TextToken("yyy"));
        tokens.addTextToken(new TextToken("zzz"));

        MatchResult result = matcher.match(tokens, 0, tokens.size());
        assertEquals("un", result.getTemplateID());
        assertEquals("fr", result.getStyleLabels().get("lang"));
        assertEquals("angry", result.getStyleLabels().get("mood"));

        tokens = new TextAnnotation(2);
        tokens.addTextToken(new TextToken("xxx"));
        tokens.addTextToken(new TextToken("yyy"));

        result = matcher.match(tokens, 0, tokens.size());
        assertEquals("un", result.getTemplateID());
        assertEquals("en", result.getStyleLabels().get("lang"));
        assertEquals("*", result.getStyleLabels().get("mood"));

        tokens = new TextAnnotation(3);
        tokens.addTextToken(new TextToken("xxx"));
        tokens.addTextToken(new TextToken("aaa"));
        tokens.addTextToken(new TextToken("zzz"));

        result = matcher.match(tokens, 0, tokens.size());
        assertEquals("unt-t", result.getTemplateID());
        assertEquals("*", result.getStyleLabels().get("lang"));
        assertEquals("good", result.getStyleLabels().get("mood"));
    }
}
