/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.filters;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.plugin.languageserver.ide.filters.Matcher.MatcherUtil.*;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class FuzzyMatches {

    private final Matcher FUZZY_SEPARATE   = or(new PrefixMatcher(true), new CamelCaseMatcher(), new SubStringMatcher());
    private final Matcher FUZZY_CONTIGUOUS = or(new PrefixMatcher(true), new CamelCaseMatcher(), new ContiguousSubStringMatcher());

    private Map<String, RegExp> regExpCache = new HashMap<>();

    public List<Match> fuzzyMatch(String word, String wordToMatch) {
        return fuzzyMatch(word, wordToMatch, false);
    }

    public List<Match> fuzzyMatch(String word, String wordToMatch, boolean substringMatch) {

        RegExp regExp = regExpCache.get(word);
        if (regExp == null) {
            regExp = convertWordToRegExp(word);
            regExpCache.put(word, regExp);
        }

        MatchResult matchResult = regExp.exec(wordToMatch);
        if (matchResult != null) {
            return Collections.singletonList(new Match(matchResult.getIndex(), matchResult.getIndex() + matchResult.getGroup(0).length()));
        }

        if (substringMatch) {
            return FUZZY_SEPARATE.match(word, wordToMatch);
        } else {
            return FUZZY_CONTIGUOUS.match(word, wordToMatch);
        }

    }


    private boolean isUpper(int code) {
        return 65 <= code && code <= 90;
    }

    private boolean isLower(int code) {
        return 97 <= code && code <= 122;
    }

    private boolean isWhitespace(int code) {
        return code != 32 && code != 9 && code != 10 && code != 13;
    }

    private boolean isAlphanumeric(int code) {
        return isLower(code) || isUpper(code) || isNumber(code);
    }

    private boolean isNumber(int code) {
        return 48 <= code && code <= 57;
    }

    private List<Match> join(Match match, List<Match> result) {
        if (result.isEmpty()) {
            result.add(match);
        } else if (match.getEnd() == result.get(0).getStart()) {
            result.get(0).setStart(match.getStart());
        } else {
            result.add(0, match);
        }
        return result;
    }

    private RegExp convertWordToRegExp(String word) {
        return RegExp.compile(createRegexpNative(word), "i");
    }

    private native String createRegexpNative(String word) /*-{
        return word.replace(/[\-\\\{\}\+\?\|\^\$\.\,\[\]\(\)\#\s]/g, '\\$&').replace(/[\*]/g, '.*');
    }-*/;


    private class ContiguousSubStringMatcher implements Matcher {

        @Override
        public List<Match> match(String word, String wordToMatch) {
            int index = wordToMatch.toLowerCase().indexOf(word.toLowerCase());
            if (index == -1) {
                return null;
            }
            return Collections.singletonList(new Match(index, index + word.length() + 1));
        }
    }

    private class SubStringMatcher implements Matcher {

        @Override
        public List<Match> match(String word, String wordToMatch) {
            return matchSubString(word.toLowerCase(), wordToMatch.toLowerCase(), 0, 0);
        }

        private List<Match> matchSubString(String word, String wodToMatch, int i, int j) {
            if (i == word.length()) {
                return new ArrayList<>();
            } else if (j == wodToMatch.length()) {
                return null;
            } else {
                if (word.charAt(i) == wodToMatch.charAt(j)) {
                    List<Match> result;
                    if ((result = matchSubString(word, wodToMatch, i + 1, j + 1)) != null) {
                        return join(new Match(j, j + 1), result);
                    }
                }

                return matchSubString(word, wodToMatch, i, j + 1);
            }
        }
    }

    private class CamelCaseMatcher implements Matcher {

        @Override
        public List<Match> match(String word, String wordToMatch) {
            if (wordToMatch == null || wordToMatch.isEmpty()) {
                return null;
            }

            if (!isCamelCasePattern(word)) {
                return null;
            }

            if (!isCamelCaseWord(wordToMatch)) {
                return null;
            }

            List<Match> result = null;

            int i = 0;
            while (i < wordToMatch.length() && (result = matchCamelCase(word.toLowerCase(), wordToMatch, 0, i)) == null) {
                i = nextAnchor(wordToMatch, i + 1);
            }
            return result;
        }

        private List<Match> matchCamelCase(String word, String wordToMatch, int i, int j) {
            if (i == word.length()) {
                return new ArrayList<>();
            } else if (j == wordToMatch.length()) {
                return null;
            } else if (word.charAt(i) != wordToMatch.toLowerCase().charAt(j)) {
                return null;
            } else {
                List<Match> result = null;
                int nextUpperIndex = j + 1;
                result = matchCamelCase(word, wordToMatch, i + 1, j + 1);
                while (result == null && (nextUpperIndex = nextAnchor(wordToMatch, nextUpperIndex)) < wordToMatch.length()) {
                    result = matchCamelCase(word, wordToMatch, i + 1, nextUpperIndex);
                    nextUpperIndex++;
                }
                return result == null ? null : join(new Match(j, j + 1), result);
            }
        }

        private int nextAnchor(String wordToMatch, int start) {
            for (int i = start; i < wordToMatch.length(); i++) {
                int c = wordToMatch.charAt(i);
                if (isUpper(c) || isNumber(c) || (i > 0 && !isAlphanumeric(wordToMatch.charAt(i - 1)))) {
                    return i;
                }
            }
            return wordToMatch.length();
        }

        private boolean isCamelCaseWord(String word) {
            if (word.length() > 60) {
                return false;
            }
            double upper = 0;
            double lower = 0;
            double alpha = 0;
            double numeric = 0;
            int code = 0;

            for (int i = 0; i < word.length(); i++) {
                code = word.charAt(i);
                if (isUpper(code)) {
                    upper++;
                }
                if (isLower(code)) {
                    lower++;
                }
                if (isAlphanumeric(code)) {
                    alpha++;
                }

                if (isNumber(code)) {
                    numeric++;
                }
            }
            double upperPercent = upper / word.length();
            double lowerPercent = lower / word.length();
            double alphaPercent = alpha / word.length();
            double numericPercent = numeric / word.length();

            return lowerPercent > 0.2d && upperPercent < 0.8d && alphaPercent > 0.6d && numericPercent < 0.5d;
        }


        private boolean isCamelCasePattern(String word) {
            int upper = 0;
            int lower = 0;
            int code;
            int whitespace = 0;

            for (int i = 0; i < word.length(); i++) {
                code = word.charAt(i);
                if (isUpper(code)) {
                    upper++;
                }
                if (isLower(code)) {
                    lower++;
                }
                if (isWhitespace(code)) {
                    whitespace++;
                }
            }

            if ((upper == 0 || lower == 0) && whitespace == 0) {
                return word.length() <= 30;
            } else {
                return upper <= 5;
            }
        }


    }

    private class PrefixMatcher implements Matcher {
        private final boolean ignoreCase;

        public PrefixMatcher(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
        }

        @Override
        public List<Match> match(String word, String wordToMatch) {
            if (wordToMatch == null || wordToMatch.isEmpty() || wordToMatch.length() < word.length()) {
                return null;
            }

            if (ignoreCase) {
                word = word.toLowerCase();
                wordToMatch = wordToMatch.toLowerCase();
            }

            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) != wordToMatch.charAt(i)) {
                    return null;
                }
            }

            return word.length() > 0 ? Collections.singletonList(new Match(0, word.length())) : Collections.<Match>emptyList();
        }
    }
}
