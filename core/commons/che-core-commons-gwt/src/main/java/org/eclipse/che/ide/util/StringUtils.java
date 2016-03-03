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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** Utility methods for string operations. */
public class StringUtils {


    /**
     * Convert milliseconds to human readable format like 1d:10h:15m:07s
     * @param timeInMs time in milliseconds
     * @return formatted string
     */
    public static String timeMlsToHumanReadable(long timeInMs) {
        return timeSecToHumanReadable(timeInMs / 1000);
    }


    /**
     * Convert seconds to human readable format like 0d:00h:00m:07s
     * @param timeInSec time in seconds
     * @return formatted string
     */
    public static String timeSecToHumanReadable(long timeInSec) {
        int ss = (int)timeInSec;
        int mm = 0;
        if (ss >= 60) {
            mm = ss / 60;
            ss = ss % 60;
        }
        int hh = 0;
        if (mm >= 60) {
            hh = mm / 60;
            mm = mm % 60;
        }
        int d = 0;
        if (hh >= 24) {
            d = hh / 24;
            hh = hh % 24;
        }

        if (d>0)
            return String.valueOf(d + "d:" + getDoubleDigit(hh) + "h:" + getDoubleDigit(mm) + "m:" + getDoubleDigit(ss) + "s");

        if (d == 0 && hh > 0)
            return String.valueOf(hh + "h:" + getDoubleDigit(mm) + "m:" + getDoubleDigit(ss) + "s");

        if (d==0 && hh == 0 && mm > 0)
            return String.valueOf(mm + "m:" + getDoubleDigit(ss) + "s");


        return String.valueOf(getDoubleDigit(ss) + "s");
    }


    /** Get a double digit int from a single, e.g. 1 = "01", 2 = "02". */
    public static String getDoubleDigit(int i) {
        final String doubleDigitI;
        switch (i) {
            case 0:
                doubleDigitI = "00";
                break;
            case 1:
                doubleDigitI = "01";
                break;
            case 2:
                doubleDigitI = "02";
                break;
            case 3:
                doubleDigitI = "03";
                break;
            case 4:
                doubleDigitI = "04";
                break;
            case 5:
                doubleDigitI = "05";
                break;
            case 6:
                doubleDigitI = "06";
                break;
            case 7:
                doubleDigitI = "07";
                break;
            case 8:
                doubleDigitI = "08";
                break;
            case 9:
                doubleDigitI = "09";
                break;
            default:
                doubleDigitI = Integer.toString(i);
        }
        return doubleDigitI;
    }

    /**
     * Map [N] -> string of N spaces. Used by {@link #getSpaces} to cache strings
     * of spaces.
     */
    private static final Map<Integer, String> cachedSpaces = new HashMap<>();
    /**
     * By default, this is a pure java implementation, but can be set to a more
     * optimized version by the client
     */
    private static       Implementation       implementation = GWT.isClient() || !GWT.isScript() ?
                                                               new PureJavaImplementation() : new NativeImplementation();

    /** Sets the implementation for methods */
    public static void setImplementation(Implementation implementation) {
        StringUtils.implementation = implementation;
    }

    /**
     * @return largest n such that
     * {@code string1.substring(0, n).equals(string2.substring(0, n))}
     */
    public static int findCommonPrefixLength(String string1, String string2) {
        int limit = Math.min(string1.length(), string2.length());
        int result = 0;
        while (result < limit) {
            if (string2.charAt(result) != string1.charAt(result)) {
                break;
            }
            result++;
        }
        return result;
    }

    ;

    public static int countNumberOfOccurrences(String s, String pattern) {
        int count = 0;
        int i = 0;

        while ((i = s.indexOf(pattern, i)) >= 0) {
            count++;
            i += pattern.length();
        }

        return count;
    }

    /**
     * Check if a String ends with a specified suffix, ignoring case
     *
     * @param s
     *         the String to check, may be null
     * @param suffix
     *         the suffix to find, may be null
     * @return true if s ends with suffix or both s and suffix are null, false
     * otherwise.
     */
    public static boolean endsWithIgnoreCase(String s, String suffix) {
        if (s == null || suffix == null) {
            return (s == null && suffix == null);
        }
        if (suffix.length() > s.length()) {
            return false;
        }
        return s.regionMatches(true, s.length() - suffix.length(), suffix, 0, suffix.length());
    }

    public static boolean endsWithChar(CharSequence s, char suffix) {
        return s != null && s.length() != 0 && s.charAt(s.length() - 1) == suffix;
    }

    public static boolean looksLikeImage(String path) {
        String lowercase = path.toLowerCase();
        return lowercase.endsWith(".jpg") || lowercase.endsWith(".jpeg") || lowercase.endsWith(".ico")
               || lowercase.endsWith(".png") || lowercase.endsWith(".gif");
    }

    public static <T> String join(T[] items, String separator) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            s.append(items[i]).append(separator);
        }
        s.setLength(s.length() - separator.length());

        return s.toString();
    }

    public static <T> String join(List<T> items, String separator) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            s.append(items.get(i)).append(separator);
        }
        s.setLength(s.length() - separator.length());

        return s.toString();
    }

    /**
     * Check that string starts with specified prefix.
     * <p/>
     * <p>If {@code caseInsensitive == false} this check is equivalent
     * to {@link String#startsWith(String)}.
     * <p/>
     * <p>Otherwise {@code prefix} should be lower-case and check ignores
     * case of {@code string}.
     */
    public static boolean startsWith(
            String prefix, String string, boolean caseInsensitive) {
        if (caseInsensitive) {
            int prefixLength = prefix.length();
            if (string.length() < prefixLength) {
                return false;
            }
            return prefix.equals(string.substring(0, prefixLength).toLowerCase());
        } else {
            return string.startsWith(prefix);
        }
    }

    /**
     * Returns if the given {@link java.lang.String} named {@code first} contains the string {@code second} ignoring the case.
     *
     * @param first
     *         the string to test.
     * @param second
     *         the sequence to search.
     * @return {@code true} if string {@code first} contains the string {@code second} ignoring the case, {@code false} otherwise.
     */
    public static boolean containsIgnoreCase(String first, String second) {
        return first.toLowerCase().contains(second.toLowerCase());
    }

    /**
     * @return the length of the starting whitespace for the line, or the string
     * length if it is all whitespace
     */
    public static int lengthOfStartingWhitespace(String s) {
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            // TODO: This currently only deals with ASCII whitespace.
            // Read until a non-space
            if (c != ' ' && c != '\t') {
                return i;
            }
        }

        return n;
    }

    /**
     * @return first character in the string that is not a whitespace, or
     * {@code 0} if there is no such characters
     */
    public static char firstNonWhitespaceCharacter(String s) {
        for (int i = 0, n = s.length(); i < n; ++i) {
            char c = s.charAt(i);
            if (!isWhitespace(c)) {
                return c;
            }
        }
        return 0;
    }

    /**
     * @return last character in the string that is not a whitespace, or
     * {@code 0} if there is no such characters
     */
    public static char lastNonWhitespaceCharacter(String s) {
        for (int i = s.length() - 1; i >= 0; --i) {
            char c = s.charAt(i);
            if (!isWhitespace(c)) {
                return c;
            }
        }
        return 0;
    }

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNullOrWhitespace(String s) {
        return s == null || "".equals(s.trim());
    }

    public static String trimNullToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    public static String trimStart(String s, String prefix) {
        if (s.startsWith(prefix)) {
            return s.substring(prefix.length());
        }
        return s;
    }

    public static String ensureNotEmpty(String s, String defaultStr) {
        return isNullOrEmpty(s) ? defaultStr : s;
    }

    public static boolean isWhitespace(char ch) {
        return (ch <= ' ');
    }

    public static boolean isAlpha(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
    }

    public static boolean isNumeric(char ch) {
        return ('0' <= ch && ch <= '9');
    }

    public static boolean isQuote(char ch) {
        return ch == '\'' || ch == '\"';
    }

    public static boolean isAlphaNumOrUnderscore(char ch) {
        return isAlpha(ch) || isNumeric(ch) || ch == '_';
    }

    public static long toLong(String longStr) {
        return longStr == null ? 0 : Long.parseLong(longStr);
    }

    /**
     * @return true, if both strings are empty or {@code null}, or if they are
     * equal
     */
    public static boolean equalStringsOrEmpty(String a, String b) {
        return nullToEmpty(a).equals(nullToEmpty(b));
    }

    /** @return true, if the strings are not empty or {@code null}, and equal */
    public static boolean equalNonEmptyStrings(String a, String b) {
        return !isNullOrEmpty(a) && a.equals(b);
    }

    /**
     * Splits with the contract of the contract of the JavaScript String.split().
     * <p/>
     * <p>More specifically: empty segments will be produced for adjacent and
     * trailing separators.
     * <p/>
     * <p>If an empty string is used as the separator, the string is split
     * between each character.
     * <p/>
     * <p>Examples:<ul>
     * <li>{@code split("a", "a")} should produce {@code ["", ""]}
     * <li>{@code split("a\n", "\n")} should produce {@code ["a", ""]}
     * <li>{@code split("ab", "")} should produce {@code ["a", "b"]}
     * </ul>
     */
    public static List<String> split(String s, String separator) {
        return implementation.split(s, separator);
    }

    /** @return the number of editor lines this text would take up */
    public static int countNumberOfVisibleLines(String text) {
        return countNumberOfOccurrences(text, "\n") + (text.endsWith("\n") ? 0 : 1);
    }

    public static String capitalizeFirstLetter(String s) {
        if (!isNullOrEmpty(s)) {
            s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }
        return s;
    }

    public static String ensureStartsWith(String s, String startText) {
        if (isNullOrEmpty(s)) {
            return startText;
        } else if (!s.startsWith(startText)) {
            return startText + s;
        } else {
            return s;
        }
    }

    /**
     * Like {@link String#substring(int)} but allows for the {@code count}
     * parameter to extend past the string's bounds.
     */
    public static String substringGuarded(String s, int position, int count) {
        int sLength = s.length();
        if (sLength - position <= count) {
            return position == 0 ? s : s.substring(position);
        } else {
            return s.substring(position, position + count);
        }
    }

    public static String repeatString(String s, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(s);
        }
        return builder.toString();
    }

    /**
     * Gets a {@link String} consisting of the given number of spaces.
     * <p/>
     * <p>NB: The result is cached in {@link #cachedSpaces}.
     *
     * @param size
     *         the number of spaces
     * @return a {@link String} consisting of {@code size} spaces
     */
    public static String getSpaces(int size) {
        if (cachedSpaces.containsKey(size)) {
            return cachedSpaces.get(size);
        }

        char[] fill = new char[size];
        for (int i = 0; i < size; i++) {
            fill[i] = ' ';
        }

        String spaces = new String(fill);
        cachedSpaces.put(size, spaces);
        return spaces;
    }

    /**
     * If this given string is of length {@code maxLength} or less, it will
     * be returned as-is.
     * Otherwise it will be trucated to {@code maxLength}, regardless of whether
     * there are any space characters in the String. If an ellipsis is requested
     * to be appended to the truncated String, the String will be truncated so
     * that the ellipsis will also fit within maxLength.
     * If no truncation was necessary, no ellipsis will be added.
     *
     * @param source
     *         the String to truncate if necessary
     * @param maxLength
     *         the maximum number of characters to keep
     * @param addEllipsis
     *         if true, and if the String had to be truncated,
     *         add "..." to the end of the String before returning. Additionally,
     *         the ellipsis will only be added if maxLength is greater than 3.
     * @return the original string if it's length is less than or equal to
     * maxLength, otherwise a truncated string as mentioned above
     */
    public static String truncateAtMaxLength(String source, int maxLength,
                                             boolean addEllipsis) {

        if (source.length() <= maxLength) {
            return source;
        }
        if (addEllipsis && maxLength > 3) {
            return unicodePreservingSubstring(source, 0, maxLength - 3) + "...";
        }
        return unicodePreservingSubstring(source, 0, maxLength);
    }

    /**
     * Normalizes {@code index} such that it respects Unicode character
     * boundaries in {@code str}.
     * <p/>
     * <p>If {@code index} is the low surrogate of a unicode character,
     * the method returns {@code index - 1}. Otherwise, {@code index} is
     * returned.
     * <p/>
     * <p>In the case in which {@code index} falls in an invalid surrogate pair
     * (e.g. consecutive low surrogates, consecutive high surrogates), or if
     * if it is not a valid index into {@code str}, the original value of
     * {@code index} is returned.
     *
     * @param str
     *         the String
     * @param index
     *         the index to be normalized
     * @return a normalized index that does not split a Unicode character
     */
    private static int unicodePreservingIndex(String str, int index) {
        if (index > 0 && index < str.length()) {
            if (Character.isHighSurrogate(str.charAt(index - 1)) &&
                Character.isLowSurrogate(str.charAt(index))) {
                return index - 1;
            }
        }
        return index;
    }

    /**
     * Returns a substring of {@code str} that respects Unicode character
     * boundaries.
     * <p/>
     * <p>The string will never be split between a [high, low] surrogate pair,
     * as defined by {@link Character#isHighSurrogate} and
     * {@link Character#isLowSurrogate}.
     * <p/>
     * <p>If {@code begin} or {@code end} are the low surrogate of a unicode
     * character, it will be offset by -1.
     * <p/>
     * <p>This behavior guarantees that
     * {@code str.equals(StringUtil.unicodePreservingSubstring(str, 0, n) +
     * StringUtil.unicodePreservingSubstring(str, n, str.length())) } is
     * true for all {@code n}.
     * </pre>
     * <p/>
     * <p>This means that unlike {@link String#substring(int, int)}, the length of
     * the returned substring may not necessarily be equivalent to
     * {@code end - begin}.
     *
     * @param str
     *         the original String
     * @param begin
     *         the beginning index, inclusive
     * @param end
     *         the ending index, exclusive
     * @return the specified substring, possibly adjusted in order to not
     * split unicode surrogate pairs
     * @throws IndexOutOfBoundsException
     *         if the {@code begin} is negative,
     *         or {@code end} is larger than the length of {@code str}, or
     *         {@code begin} is larger than {@code end}
     */
    private static String unicodePreservingSubstring(
            String str, int begin, int end) {
        return str.substring(unicodePreservingIndex(str, begin),
                             unicodePreservingIndex(str, end));
    }

    /**
     * Indicates whether {@code c} is one of the twenty-six uppercase ASCII alphabetic characters
     * between {@code 'A'} and {@code 'Z'} inclusive. All others (including non-ASCII characters)
     * return {@code false}.
     */
    public static boolean isUpperCase(char c) {
        return (c >= 'A') && (c <= 'Z');
    }

    /**
     * Interface that defines string utility methods used by shared code but have
     * differing client and server implementations.
     */
    public interface Implementation {
        List<String> split(String string, String separator);
    }

    private static class PureJavaImplementation implements Implementation {
        @Override
        public List<String> split(String string, String separator) {
            List<String> result = new ArrayList<>();

            int sepLength = separator.length();
            if (sepLength == 0) {
                for (int i = 0, n = string.length(); i < n; i++) {
                    result.add(string.substring(i, i + 1));
                }
                return result;
            }

            int position = 0;
            while (true) {
                int index = string.indexOf(separator, position);
                if (index == -1) {
                    result.add(string.substring(position));
                    return result;
                }
                result.add(string.substring(position, index));
                position = index + sepLength;
            }
        }
    }

    private static class NativeImplementation implements Implementation {
        // call to native JS Split
        public static native JsArrayString nativeSplit(String s, String separator) /*-{
            return s.split(separator);
        }-*/;

        @Override
        public List<String> split(String string, String separator) {
            JsArrayString jsArrayString = nativeSplit(string, separator);

            List<String> result = new ArrayList<>();
            for (int i = 0; i < jsArrayString.length(); i++) {
                result.add(jsArrayString.get(i));
            }
            return result;
        }

    }
}
