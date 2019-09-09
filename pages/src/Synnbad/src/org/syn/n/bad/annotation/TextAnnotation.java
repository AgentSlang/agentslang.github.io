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

package org.syn.n.bad.annotation;

import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/9/12
 */
public class TextAnnotation {
    public static final List<TextToken> EMPTY_LIST = new LinkedList<TextToken>();
    //--
    private List<TextToken> tokens;
    private TextToken[] encodings;
    private List<TextToken> lemmas;
    private Map<Integer, Annotation> annotations;

    public TextAnnotation() {
        this(20);
    }

    public TextAnnotation(int textLength) {
        this.tokens = new ArrayList<TextToken>(textLength);
        this.annotations = new HashMap<Integer, Annotation>();
    }

    public Annotation getAnnotation(int level) {
        return annotations.get(level);
    }

    public void addAnnotation(int level, Annotation annotation) {
        annotations.put(level, annotation);
    }

    public int addTextToken(TextToken textToken) {
        textToken.setIndex(tokens.size());
        tokens.add(textToken);
        return textToken.getIndex();
    }

    public TextToken getToken(int index) {
        if (tokens == null || index < 0 || index > tokens.size()) {
            return null;
        } else {
            return tokens.get(index);
        }
    }

    public int size() {
        return tokens.size();
    }

    public void setEncoding(int index, TextToken encoding) {
        if (encodings == null) {
            encodings = new TextToken[tokens.size()];
        }
        encodings[index] = encoding;
    }

    public TextToken getEncoding(int index) {
        if (encodings == null || index < 0 || index >= encodings.length) {
            return null;
        } else {
            return encodings[index];
        }
    }

    public List<TextToken> getEncoding() {
        return encodings == null ? EMPTY_LIST : Arrays.asList(encodings);
    }

    public List<TextToken> getLemmas() {
        if (lemmas == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(lemmas);
        }
    }

    public void setLemma(int index, TextToken lemma) {
        if (lemmas == null) {
            lemmas = new ArrayList<TextToken>(tokens.size());
        }

        while (lemmas.size() < index) {
            lemmas.add(null);
        }

        lemmas.add(index, lemma);
    }

    public TextToken getLemma(int index) {
        if (lemmas == null || index < 0 || index >= lemmas.size()) {
            return getToken(index);
        } else {
            return lemmas.get(index) == null ? getToken(index) : lemmas.get(index);
        }
    }

    public String getRawLemma(int index) {
        if (lemmas == null || index < 0 || index >= lemmas.size()) {
            return "?";
        } else {
            return lemmas.get(index) == null ? "?" : lemmas.get(index).getText();
        }
    }

    public void addAnnotationToken(int level, AnnotationToken token) {
        Annotation annotation = annotations.get(level);
        if (annotation != null) {
            token.setAnnotationLevel(level);
            annotation.addToken(token);
        }
    }

    public String[] getPlainTranscription() {
        String[] result = new String[tokens.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = tokens.get(i).getText();
        }

        return result;
    }

    public String getTranscription() {
        StringBuilder sb = new StringBuilder();
        for (TextToken item : tokens) {
            sb.append(item.getText()).append(" ");
        }
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TextToken item : tokens) {
            sb.append(item);
            StringBuilder ann = new StringBuilder();
            for (Annotation annotation : annotations.values()) {
                for (AnnotationToken token : annotation.getTokens(item.getIndex())) {
                    if (ann.length() > 0) {
                        ann.append(",");
                    }
                    ann.append(token);
                }
            }
            sb.append("(").append(getRawLemma(item.getIndex())).append(") ");
            sb.append("{").append(ann.toString()).append("} ");
        }

        if (encodings != null) {
            sb.append(" -> ");
            for (TextToken encoding : encodings) {
                sb.append(encoding.toString()).append(" ");
            }
        }

        return sb.toString();
    }

    public List<TextToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<TextToken> tokens) {
        this.tokens = tokens;
    }

    public TextToken[] getEncodings() {
        return encodings;
    }

    public void setEncodings(TextToken[] encodings) {
        this.encodings = encodings;
    }

    public Map<Integer, Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<Integer, Annotation> annotations) {
        this.annotations = annotations;
    }
}
