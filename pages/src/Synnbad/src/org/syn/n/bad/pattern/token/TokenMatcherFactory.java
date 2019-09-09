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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/5/13
 */
public class TokenMatcherFactory {
    private static class TokenItem {
        private boolean mandatory = true;
        private boolean multiplier = true;
        private String pattern;

        private TokenItem(boolean mandatory, boolean multiplier, String pattern) {
            this.mandatory = mandatory;
            this.multiplier = multiplier;
            this.pattern = pattern;
        }
    }

    public static TokenMatcher factoryPatternMatcher(Locale language, String pattern) {
        String[] split = pattern.split(" ");
        TokenMatcher start = new SkipMatcher();
        TokenMatcher last = start;
        TokenMatcher lastMandatory = start;

        for (String token : split) {
            TokenItem tokenItem = factoryTokenItem(token);
            TokenMatcher currentToken = factoryTokenMatcher(language, tokenItem);
            last.addNext(currentToken);

            if (last != start) {
                setupSkips(last, currentToken);
            }

            if (tokenItem.multiplier) {
                currentToken.addNext(currentToken);
            }
            if (last != lastMandatory) {
                lastMandatory.addNext(currentToken);
            }

            if (tokenItem.mandatory) {
                lastMandatory = currentToken;
            }
            last = currentToken;
        }
        markFinal(lastMandatory);
        sortToken(start);

        return start;
    }

    private static void setupSkips(TokenMatcher last, TokenMatcher current) {
        List<TokenMatcher> skips = setupSkips(last);
        if (skips != null) {
            for (TokenMatcher skip : skips) {
                skip.addNext(current);
            }
        }
    }

    private static List<TokenMatcher> setupSkips(TokenMatcher tokenMatcher) {
        List<TokenMatcher> skips = new LinkedList<TokenMatcher>();

        TokenMatcher skip1 = new SkipMatcher();
        tokenMatcher.addNext(skip1);
        skips.add(skip1);

        TokenMatcher skip2 = new SkipMatcher();
        skip1.addNext(skip2);
        skips.add(skip2);

        return skips;
    }

    private static void sortToken(TokenMatcher tokenMatcher) {
        tokenMatcher.sortMatchers();
        for (TokenMatcher child : tokenMatcher.getMatcherList()) {
            if (child != tokenMatcher) {
                sortToken(child);
            }
        }
    }

    private static void markFinal(TokenMatcher matcher) {
        if (matcher.getPriority() != TokenMatcher.PRIORITY_SKIP) {
            matcher.setFinal(true);
            for (TokenMatcher child : matcher.getMatcherList()) {
                if (child != matcher) {
                    markFinal(child);
                }
            }
        }
    }

    private static TokenItem factoryTokenItem(String pattern) {
        TokenItem result = new TokenItem(true, false, pattern);

        if (pattern.endsWith("*")) {
            int index = pattern.lastIndexOf('*');
            if (index > 0) {
                result.mandatory = false;
                result.multiplier = true;
                result.pattern = pattern.substring(0, index);
            }
        }

        if (pattern.endsWith("?")) {
            int index = pattern.lastIndexOf('?');
            if (index > 0) {
                result.mandatory = false;
                result.multiplier = false;
                result.pattern = pattern.substring(0, index);
            }
        }

        return result;
    }

    private static TokenMatcher factoryTokenMatcher(Locale language, TokenItem token) {
        if (token.pattern.startsWith("<")) {
            token.pattern = token.pattern.substring(token.pattern.indexOf('<') + 1, validateLength(token.pattern.indexOf('>'), token.pattern.length()));
            return new POSMatcher(token.pattern, token.mandatory);
        } else if (token.pattern.startsWith("$")) {
            token.pattern = token.pattern.substring(token.pattern.indexOf('$') + 1, token.pattern.length());
            return new ConsumerMatcher(token.pattern);
        } else if (token.pattern.startsWith("[")) {
            token.pattern = token.pattern.substring(token.pattern.indexOf('[') + 1, validateLength(token.pattern.indexOf(']'), token.pattern.length()));
            return new SynMatcher(language, token.pattern, token.mandatory);
        } else {
            return new SimpleTokenMatcher(token.pattern, token.mandatory);
        }
    }

    private static int validateLength(int l, int defaultL) {
        if (l < 0) {
            return defaultL;
        } else {
            return l;
        }
    }
}
