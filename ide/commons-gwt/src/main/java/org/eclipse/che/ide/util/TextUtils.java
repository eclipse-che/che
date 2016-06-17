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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility methods for text operations. This differs from {@link StringUtils} by
 * operating on a higher-level (for example, words and identifiers).
 */
public class TextUtils {

    /**
     * Finds the next character which is not a mark or other character. Will
     * return column if the end of the line is reached or column is a non-mark or
     * other character.
     */
    public static int findNextCharacterInclusive(String text, int column) {
        MatchResult result = RegExpUtils.findMatchAfterIndex(
                UnicodeUtils.regexpNotMarkOrOtherExcludingTabAndNewline, text, column - 1);
        // if result is null, then it's likely we're at the \n (I think).
        return result == null ? column : result.getIndex();
    }

    /** Finds the next character which is not a combining character. */
    public static int findNonMarkNorOtherCharacter(String text, int column) {
    /*
     * If moving forward: if next character is combining mark, skip to next
     * non-combining mark character, else go forward one character.
     */
        if (column + 1 >= text.length()) {
            return text.length() + 1;
        }

        MatchResult match = RegExpUtils.findMatchAfterIndex(
                UnicodeUtils.regexpNotMarkOrOtherExcludingTabAndNewline, text, column);
        if (match == null) {
            return text.length() + 1;
        } else {
            return match.getIndex();
        }
    }

    /** Finds the previous character which is not a combining character. */
    public static int findPreviousNonMarkNorOtherCharacter(String text, int column) {
    /*
     * If moving backward: if previous character is combining mark, skip to
     * before first non-combining mark character. If it isn't a combining mark,
     * proceed back one character.
     */
        if (column - 1 < 0) {
            return -1;
        }

        MatchResult match = RegExpUtils.findMatchBeforeIndex(
                UnicodeUtils.regexpNotMarkOrOtherExcludingTabAndNewline, text, column);
        if (match == null) {
            return -1;
        } else {
            return match.getIndex();
        }
    }

    /**
     * Finds the index of the next non-similar word. There are two groups of
     * words: Javascript identifiers and the remaining non-whitespace characters.
     * <p/>
     * Consider the text "hello there". With {@code skipWhitespaceBeforeWord}
     * true, the return value would be at the 't'. With it false, the return value
     * would be at the ' '.
     * <p/>
     * Consider the text "someFunction(foo); // Test" and
     * {@code skipWhitespaceBeforeWord} is true. findNextWord(text, 0) will return
     * the index of the '(', since it is the first word that is not an identifier.
     * findNextWord(text, 12) will return the 13 ('f' from "foo").
     * findNextWord(text, 17) will return 19 ('/').
     *
     * @param skipWhitespaceBeforeWord
     *         true to skip the whitespace before the next
     *         word (thus returning the position of the first letter of the word),
     *         false to return the position of the first whitespace before the word
     * @return the index according to {@code skipWhitespaceBeforeWord}, or if the
     * given {@code column} is beyond the string's length, this will
     * return the length plus one.
     */
    public static int findNextWord(String text, int column, boolean skipWhitespaceBeforeWord) {
        if (column + 1 >= text.length()) {
            return text.length() + 1;
        }

        int initialColumn = column;
        if (skipWhitespaceBeforeWord) {
            column = skipNonwhitespaceSimilar(text, column, true);
            column = skipWhitespace(text, column, true);
        } else {
            column = skipWhitespace(text, column, true);
            column = skipNonwhitespaceSimilar(text, column, true);
        }

        return column;
    }

    /** Counts number of whitespaces at the beginning of line. */
    public static int countWhitespacesAtTheBeginningOfLine(String text) {
        MatchResult result = RegExpUtils.findMatchAfterIndex(
                UnicodeUtils.regexpNotWhitespaceExcludingNewlineAndCarriageReturn, text, -1);

        return result == null ? text.length() : result.getIndex();
    }

    /**
     * Similar to {@link #findNextWord}, but searches backwards.
     * <p/>
     * <p>Character at {@code column} position is ignored, because it denotes the
     * symbol after "cursor".
     */
    public static int findPreviousWord(String text, int column, boolean skipWhitespaceBeforeWord) {
        column--;

        if (column < 0) {
            return -1;
        }

        if (skipWhitespaceBeforeWord) {
            column = skipNonwhitespaceSimilar(text, column, false);
            column = skipWhitespace(text, column, false);
        } else {
            column = skipWhitespace(text, column, false);
            if (column >= 0) {
                column = skipNonwhitespaceSimilar(text, column, false);
            }
        }

        column++;

        return column;
    }

    /**
     * Jumps to the previous or next best match given the parameters below. This
     * may be inside the current word. For example, if the cursor is at index 1 in
     * "hey bob", and moveByWord is called with returnCursorAtEnd=true, then the
     * returned value will be 2 (y). If returnCursorAtEnd is false, it would
     * return 4 (b).
     *
     * @param column
     *         the start column for the search
     * @param forward
     *         true for forward match, false for backwards match
     * @param returnCursorAtEnd
     *         if true, the cursor position returned will be for
     *         the last character of the next/previous word found
     * @return the calculated column, -1 for no valid match found
     */
  /*
   * TODO: Make sure we look at this, it is only used by the {@link
   * VimScheme} and I think it can be made significantly less complicated as
   * well as use the {@link #findNextWord(String, int, boolean)} and {@link
   * #findPreviousWord(String, int, boolean)} API.
   */
    public static int moveByWord(
            String text, int column, boolean forward, boolean returnCursorAtEnd) {
        int curColumn = column;
        int length = text.length();
        int direction = forward ? 1 : -1;
        boolean farWordEnd =
                ((direction == 1 && returnCursorAtEnd) || (direction == -1 && !returnCursorAtEnd));
        boolean foundEarlyMatch = false;

        if (!UnicodeUtils.isWhitespace(text.charAt(curColumn))) {
            // land on the first whitespace character after the last letter
            curColumn = skipNonwhitespaceSimilar(text, curColumn, forward);
            if (farWordEnd && curColumn - direction != column) {
                // found a match within the same word
                curColumn -= direction; // go back to last non-whitespace character
                foundEarlyMatch = true;
            }
        }

        if (!foundEarlyMatch && curColumn >= 0 && curColumn < length) {
            // land on the first non-whitespace character of the next word
            curColumn = skipWhitespace(text, curColumn, forward);
            if (farWordEnd && curColumn >= 0 && curColumn < length) {
                // land on the last non-whitespace character of the next word
                curColumn = skipNonwhitespaceSimilar(text, curColumn, forward) - direction;
            }
        }

        if (curColumn < 0 || curColumn >= length) {
            return -1;
        }
        return curColumn;
    }

    /**
     * Returns the entire word that the cursor at {@code column} falls into, or
     * null if the cursor is over whitespace.
     */
    public static String getWordAtColumn(String text, int column) {
        if (UnicodeUtils.isWhitespace(text.charAt(column))) {
            return null;
        }
        int leftColumn = skipNonwhitespaceSimilar(text, column, false) + 1;
        int rightColumn = skipNonwhitespaceSimilar(text, column, true);
        if (leftColumn >= 0 && rightColumn < text.length()) {
            return text.substring(leftColumn, rightColumn);
        }
        return null;
    }

    public static boolean isValidIdentifierCharacter(char c) {
        return !RegExpUtils.resetAndTest(
                UnicodeUtils.regexpNotJavascriptIdentifierCharacter, String.valueOf(c));
    }

    public static boolean isNonIdentifierAndNonWhitespace(char c) {
        return RegExpUtils.resetAndTest(UnicodeUtils.regexpIdentifierOrWhitespace, String.valueOf(c));
    }

    private static int skipIdentifier(String text, int column, boolean forward) {
        return directionalRegexp(
                forward, UnicodeUtils.regexpNotJavascriptIdentifierCharacter, text, column);
    }

    public static int skipNonwhitespaceNonidentifier(String text, int column, boolean forward) {
        if (column >= 0 && column < text.length()) {
            return directionalRegexp(forward, UnicodeUtils.regexpIdentifierOrWhitespace, text, column);
        }
        return column;
    }

    public static int skipNonwhitespaceSimilar(String text, int column, boolean forward) {
        if (isValidIdentifierCharacter(text.charAt(column))) {
            return skipIdentifier(text, column, forward);
        } else {
            return skipNonwhitespaceNonidentifier(text, column, forward);
        }
    }

    public static String stripWildcards(String text) {
        return RegExp.compile("\\*", "g").replace(text, "");
    }

    private static int skipWhitespace(String text, int column, boolean forward) {
        // we only execute the whitespace skip if the current character is in fact
        // whitespace
        if (column >= 0 && column < text.length() && UnicodeUtils.isWhitespace(text.charAt(column))) {
            return directionalRegexp(forward, UnicodeUtils.regexpNotWhitespace, text, column);
        }
        return column;
    }

    /**
     * Depending on the supplied direction, it will call either
     * findMatchAfterIndex or findMatchBeforeIndex. Once the result is obtained it
     * will return either the match index or the appropriate bound column
     * (text.length() or -1).
     */
    private static int directionalRegexp(boolean forward, RegExp regexp, String text, int column) {
        MatchResult result =
                forward ? RegExpUtils.findMatchAfterIndex(regexp, text, column)
                        : RegExpUtils.findMatchBeforeIndex(regexp, text, column);
        int fallback = forward ? text.length() : -1;
        return result == null ? fallback : result.getIndex();
    }

    /**
     * Encode string in md5 hash
     *
     * @param text to encode
     * @return md5 hash of string if exception return not changed text
     */
    public static String md5(String text) {
       try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(text.getBytes());

            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            String hashText = bigInt.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText;
        } catch (NoSuchAlgorithmException e) {
            return text;
        }
    }
}
