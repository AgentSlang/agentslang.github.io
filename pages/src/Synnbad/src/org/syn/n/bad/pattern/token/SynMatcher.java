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
import org.syn.n.bad.dictionary.Dictionary;
import org.syn.n.bad.dictionary.DictionaryException;
import org.syn.n.bad.dictionary.POS;

import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/25/13
 */
public class SynMatcher extends TokenMatcher {
    private static final Map<String, POS> genericPOSMapping = new HashMap<String, POS>();

    static {
        genericPOSMapping.put("RB*", POS.ADVERB);
        genericPOSMapping.put("JJ*", POS.ADJECTIVE);
        genericPOSMapping.put("NN*", POS.NOUN);
        genericPOSMapping.put("VB*", POS.VERB);
    }

    private static final POS[] noRestrictions = new POS[]{POS.ADVERB, POS.ADJECTIVE, POS.VERB, POS.NOUN};
    private static Dictionary dictionary;

    static {
        try {
            dictionary = Dictionary.getInstance();
        } catch (DictionaryException e) {
            throw new IllegalArgumentException("Invalid default dictionary setup provided. Please check it and try again ...", e);
        }
    }

    private Set<String> synsets;
    private Locale language;

    public SynMatcher(Locale language, String pattern, boolean isMandatory) {
        super("[" + pattern + "]", isMandatory);
        this.language = language;
        pattern = setupVariables(pattern);
        Map.Entry<String, POS[]> restrictions = setupRestrictions(pattern);
        pattern = restrictions.getKey();
        synsets = createSynset(language, pattern, restrictions.getValue());
    }

    private Map.Entry<String, POS[]> setupRestrictions(String pattern) {
        if (pattern.contains("|")) {
            String[] parts = pattern.split("[|]");
            POS posRestriction = genericPOSMapping.get(parts[1]);
            if (posRestriction != null) {
                return new AbstractMap.SimpleEntry<String, POS[]>(parts[0], new POS[]{posRestriction});
            } else {
                return new AbstractMap.SimpleEntry<String, POS[]>(parts[0], new POS[0]);
            }
        }
        return new AbstractMap.SimpleEntry<String, POS[]>(pattern, new POS[0]);
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

    private Set<String> createSynset(Locale language, String word, POS... pos) {
        try {
            return dictionary.getWord(language, word, pos).getSynsetIds();
        } catch (DictionaryException e) {
            //-- ignore it
            return Collections.emptySet();
        }
    }

    private POS[] convert(Collection<AnnotationToken> annotationTokens) {
        List<POS> result = new LinkedList<POS>();
        if (annotationTokens != null) {
            for (AnnotationToken annotationToken : annotationTokens) {
                String label = TextAnnotationConstants.getGenericPOSLabel(annotationToken.getAnnotationLabel());
                if (label != null && genericPOSMapping.get(label) != null) {
                    result.add(genericPOSMapping.get(label));
                }
            }
        }
        return convert(result);
    }

    private POS[] convert(List<POS> list) {
        POS[] result = new POS[list.size()];
        int i = 0;
        for (POS item : list) {
            result[i++] = item;
        }
        return result;
    }

    public boolean match(TextAnnotation tokens, int index) {
        if (synsets == null || synsets.isEmpty()) {
            return false;
        }

        POS[] restrictions;
        Annotation annotation = tokens.getAnnotation(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS));
        if (annotation == null) {
            restrictions = noRestrictions;
        } else {
            Collection<AnnotationToken> annotationTokenCollection = annotation.getTokens(index);
            if (annotationTokenCollection == null || annotationTokenCollection.isEmpty()) {
                restrictions = noRestrictions;
            } else {
                restrictions = convert(annotationTokenCollection);
            }
        }

        String token = tokens.getToken(index).getText();
        Set<String> tokenSynset = createSynset(language, token, restrictions);
        tokenSynset.retainAll(synsets);

        return !tokenSynset.isEmpty();
    }
}
