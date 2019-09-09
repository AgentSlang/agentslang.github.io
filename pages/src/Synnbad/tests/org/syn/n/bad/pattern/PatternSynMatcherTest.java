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
import org.junit.BeforeClass;
import org.junit.Test;
import org.syn.n.bad.annotation.*;
import org.syn.n.bad.dictionary.Dictionary;
import org.syn.n.bad.dictionary.DictionaryException;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1/6/13
 */
public class PatternSynMatcherTest {
    private Matcher matcher;

    @BeforeClass
    public static void setupClass() throws DictionaryException, URISyntaxException {
        Dictionary.setupDictionary(new File(ClassLoader.getSystemClassLoader().getResource("dictionaryExtensions.xml").toURI()));
    }

    @Before()
    public void setup() {
        matcher = new Matcher();
        matcher.addMatcher(new PatternMatcher("1", "[animal] goes wild"));
        matcher.addMatcher(new PatternMatcher("2", "brute goes wild"));
        matcher.addMatcher(new PatternMatcher("3", "feeling [good|RB*#attribute]"));
    }

    @Test()
    public void testSynMatcherNoRestriction() {
        TextAnnotation tokens = new TextAnnotation(4);
        tokens.addTextToken(new TextToken("brute"));
        tokens.addTextToken(new TextToken("goes"));
        tokens.addTextToken(new TextToken("wild"));
        tokens.addTextToken(new TextToken("now"));

        MatchResult result = matcher.match(tokens, 0, tokens.size());
        assertEquals("1", result.getTemplateID());
        assertEquals(3, result.getMatchedTokens());
    }

    @Test()
    public void testSynMatcherRestriction1() {
        TextAnnotation tokens = new TextAnnotation(4);
        tokens.addTextToken(new TextToken("brute"));
        tokens.addTextToken(new TextToken("goes"));
        tokens.addTextToken(new TextToken("wild"));
        tokens.addTextToken(new TextToken("now"));

        Annotation annotation = new Annotation();
        annotation.addToken(new AnnotationToken(0, TextAnnotationConstants.transformAnnotationLabel(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS), "NN")));
        tokens.addAnnotation(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS), annotation);

        MatchResult result = matcher.match(tokens, 0, tokens.size());
        assertEquals("1", result.getTemplateID());
        assertEquals(3, result.getMatchedTokens());
    }

    @Test()
    public void testSynMatcherRestriction2() {
        TextAnnotation tokens = new TextAnnotation(4);
        tokens.addTextToken(new TextToken("brute"));
        tokens.addTextToken(new TextToken("goes"));
        tokens.addTextToken(new TextToken("wild"));
        tokens.addTextToken(new TextToken("now"));

        Annotation annotation = new Annotation();
        annotation.addToken(new AnnotationToken(0, TextAnnotationConstants.transformAnnotationLabel(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS), "JJ")));
        tokens.addAnnotation(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS), annotation);

        MatchResult result = matcher.match(tokens, 0, tokens.size());
        assertEquals("2", result.getTemplateID());
        assertEquals(3, result.getMatchedTokens());
    }

    @Test()
    public void testSynMatcherRestriction3() {
        TextAnnotation tokens = new TextAnnotation(3);
        tokens.addTextToken(new TextToken("feeling"));
        tokens.addTextToken(new TextToken("well"));
        tokens.addTextToken(new TextToken("now"));

        Annotation annotation = new Annotation();
        annotation.addToken(new AnnotationToken(1, TextAnnotationConstants.transformAnnotationLabel(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS), "RB")));
        tokens.addAnnotation(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS), annotation);

        MatchResult result = matcher.match(tokens, 0, tokens.size());
        assertEquals("3", result.getTemplateID());
        assertEquals(2, result.getMatchedTokens());

        assertEquals("well", result.getMatchedVars().get("#attribute"));
    }

    @Test()
    public void testYeah() {
        Matcher matcher1 = new Matcher();
        matcher1.addMatcher(new PatternMatcher("1", "[car]"));

        TextAnnotation tokens = new TextAnnotation(1);
        tokens.addTextToken(new TextToken("yeah"));

        MatchResult result = matcher1.match(tokens, 0, tokens.size());
        assertNull(result.getTemplateID());
        assertEquals(-1, result.getMatchedTokens());
    }
}
