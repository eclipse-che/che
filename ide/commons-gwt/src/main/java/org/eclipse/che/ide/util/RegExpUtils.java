// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 */
public class RegExpUtils {

    private static final RegExp regexpEscape         = RegExp.compile("[.*?$^+|()\\[\\]{}\\\\]", "g");
    private static final RegExp regexpWildcardEscape = RegExp.compile("[.$^+|()\\[\\]{}\\\\]", "g");

    /**
     * Escapes all regexp special characters in a string .+?*|()[]{}\
     *
     * @param pattern
     * @return regexp safe string
     */
    public static String escape(String pattern) {
        return regexpEscape.replace(pattern, "\\$&");
    }

    /**
     * Creates a regexp which will only allow a wildcard type search and will
     * escape other extraneous regexp characters. IE: hel?? w*ld
     * <p/>
     * All regex characters in the pattern will be escaped. \S is substituted for
     * ? and \S+ is substituted for *. Wildcard's can be escaped using backslashes
     * so that they are treated as literals; likewise, backslashes immediately
     * preceding a wildcard can be escaped so as to match a literal backslash.
     * Backslashes not preceding a wildcard should not be escaped.
     * <p/>
     * TODO: Consider changing the regex from just \S to a class that
     * more suitable for programming such as [^\s()+\[\].] or similar
     */
    public static RegExp createRegExpForWildcardPattern(String wildcardPattern, String flags) {
        return RegExp.compile(createRegExpStringForWildcardPattern(wildcardPattern), flags);
    }

    /**
     * Creates a regular expression which will match the given wildcard pattern
     * <p/>
     * Backslashes can be used to escape a wildcard character and make it a
     * literal; likewise, backslashes before wildcard characters can be escaped.
     */
    private static String createRegExpStringForWildcardPattern(String wildcardPattern) {
        String escaped = regexpWildcardEscape.replace(wildcardPattern, "\\$&");

        /**
         * We have already run the pattern through the naive regex escape which
         * escapes all characters except the * and ?. This leads to double escaped \
         * characters that we have to inspect to determine if the user escaped the
         * wildcard or if we should replace it with it's regex equivalent.
         *
         *  NOTE: * is replaced with \S+ (matches all non-whitespace characters) and
         * ? is replaced with a single \S to match any non-whitespace
         */
        RegExp mimicLookbehind = RegExp.compile("([\\\\]*)([?*])", "g");
        StringBuilder wildcardStr = new StringBuilder(escaped);

        for (MatchResult match = mimicLookbehind.exec(wildcardStr.toString()); match != null;
             match = mimicLookbehind.exec(wildcardStr.toString())) {
            // in some browsers an optional group is null, in others its empty string
            if (match.getGroup(1) != null && !match.getGroup(1).isEmpty()) {
                // We undo double-escaping of backslashes performed by the naive escape
                int offset = match.getGroup(1).length() / 2;
                wildcardStr.delete(match.getIndex(), match.getIndex() + offset);
        /*
         * An even number of slashes means the wildcard was not escaped so we
         * must replace it with its regex equivalent.
         */
                if (offset % 2 == 0) {
                    if (match.getGroup(2).equals("?")) {
                        wildcardStr.replace(match.getIndex() + offset, match.getIndex() + offset + 1, "\\S");
                        // we added 1 more character, so we remove 1 less from the index
                        offset -= 1;
                    } else {
                        wildcardStr.replace(match.getIndex() + offset, match.getIndex() + offset + 1, "\\S+");
                        // we added 2 characters, so we need to remove 2 less from the index
                        offset -= 2;
                    }
                }
                mimicLookbehind.setLastIndex(mimicLookbehind.getLastIndex() - offset);
            } else if (match.getGroup(2).equals("?")) {
                wildcardStr.replace(match.getIndex(), match.getIndex() + 1, "\\S");
                mimicLookbehind.setLastIndex(mimicLookbehind.getLastIndex() + 1);
            } else {
                wildcardStr.replace(match.getIndex(), match.getIndex() + 1, "\\S+");
                mimicLookbehind.setLastIndex(mimicLookbehind.getLastIndex() + 2);
            }
        }

        return wildcardStr.toString();
    }

    /**
     * Returns the number of matches found in a string by a regexp. If the regexp
     * is not a global regexp this will return a maximum of 1. This does not
     * setLastIndex(0) automatically, you must do it manually.
     *
     * @returns number of matches
     */
    public static int getNumberOfMatches(RegExp regexp, String input) {
        if (regexp == null || input == null || input.isEmpty()) {
            return 0;
        }

        // if we don't check here we will loop forever
        if (!regexp.getGlobal()) {
            return regexp.test(input) ? 1 : 0;
        }

        int matches = 0;
        for (MatchResult result = regexp.exec(input);
             result != null && result.getGroup(0).length() != 0; result = regexp.exec(input)) {
            matches++;
        }
        return matches;
    }

    public static MatchResult findMatchBeforeIndex(
            RegExp regexp, String text, int exclusiveEndIndex) {
        regexp.setLastIndex(0);

        // Find the last match without going over our startIndex
        MatchResult lastMatch = null;
        for (MatchResult result = regexp.exec(text);
             result != null && result.getIndex() < exclusiveEndIndex; result = regexp.exec(text)) {
            lastMatch = result;
        }

        return lastMatch;
    }

    /** Find the next match after exclusiveStartIndex. */
    public static MatchResult findMatchAfterIndex(
            RegExp regexp, String text, int exclusiveStartIndex) {
        regexp.setLastIndex(exclusiveStartIndex + 1);
        return regexp.exec(text);
    }

    /**
     * Resets the RegExp lastIndex to 0 and returns the number of matches found in
     * a string by a regexp. If the regexp is not a global regexp this will return
     * a maximum of 1. This does not setLastIndex(0) automatically, you must do it
     * manually.
     *
     * @returns number of matches
     */
    public static int resetAndGetNumberOfMatches(RegExp regexp, String input) {
        regexp.setLastIndex(0);
        return getNumberOfMatches(regexp, input);
    }

    /**
     * Resets the RegExp lastIndex to 0 before testing. This is only useful for
     * global RegExps.
     */
    public static boolean resetAndTest(RegExp regexp, String input) {
        regexp.setLastIndex(0);
        return regexp.test(input);
    }
}
