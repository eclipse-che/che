/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Christian Walther (Indel AG) -
 * Bug 402009: Disallow "whole word" together with regex
 * *****************************************************************************
 */
package org.eclipse.search.internal.core.text;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.search.internal.ui.SearchMessages;

/** */
public class PatternConstructor {

  private PatternConstructor() {
    // don't instantiate
  }

  public static Pattern createPattern(String pattern, boolean isCaseSensitive, boolean isRegex)
      throws PatternSyntaxException {
    return createPattern(pattern, isRegex, true, isCaseSensitive, false);
  }

  /**
   * Creates a pattern element from the pattern string which is either a reg-ex expression or in our
   * old 'StringMatcher' format.
   *
   * @param pattern The search pattern
   * @param isRegex <code>true</code> if the passed string already is a reg-ex pattern
   * @param isStringMatcher <code>true</code> if the passed string is in the StringMatcher format.
   * @param isCaseSensitive Set to <code>true</code> to create a case insensitive pattern
   * @param isWholeWord <code>true</code> to create a pattern that requires a word boundary at the
   *     beginning and the end.
   * @return The created pattern
   * @throws PatternSyntaxException if "\R" is at an illegal position
   */
  public static Pattern createPattern(
      String pattern,
      boolean isRegex,
      boolean isStringMatcher,
      boolean isCaseSensitive,
      boolean isWholeWord)
      throws PatternSyntaxException {
    if (isRegex) {
      pattern = substituteLinebreak(pattern);
      Assert.isTrue(!isWholeWord, "isWholeWord unsupported together with isRegex"); // $NON-NLS-1$
    } else {
      int len = pattern.length();
      StringBuffer buffer = new StringBuffer(len + 10);
      // don't add a word boundary if the search text does not start with
      // a word char. (this works around a user input error).
      if (isWholeWord && len > 0 && isWordChar(pattern.charAt(0))) {
        buffer.append("\\b"); // $NON-NLS-1$
      }
      appendAsRegEx(isStringMatcher, pattern, buffer);
      if (isWholeWord && len > 0 && isWordChar(pattern.charAt(len - 1))) {
        buffer.append("\\b"); // $NON-NLS-1$
      }
      pattern = buffer.toString();
    }

    int regexOptions = Pattern.MULTILINE;
    if (!isCaseSensitive) {
      regexOptions |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    }
    return Pattern.compile(pattern, regexOptions);
  }

  /**
   * Copied from {@link org.eclipse.jface.text.FindReplaceDocumentAdapter}' to support '\R'
   *
   * @param findString the string to substitute
   * @return the new string
   * @throws PatternSyntaxException if "\R" is at an illegal position
   */
  private static String substituteLinebreak(String findString) throws PatternSyntaxException {
    int length = findString.length();
    StringBuffer buf = new StringBuffer(length);

    int inCharGroup = 0;
    int inBraces = 0;
    boolean inQuote = false;
    for (int i = 0; i < length; i++) {
      char ch = findString.charAt(i);
      switch (ch) {
        case '[':
          buf.append(ch);
          if (!inQuote) inCharGroup++;
          break;

        case ']':
          buf.append(ch);
          if (!inQuote) inCharGroup--;
          break;

        case '{':
          buf.append(ch);
          if (!inQuote && inCharGroup == 0) inBraces++;
          break;

        case '}':
          buf.append(ch);
          if (!inQuote && inCharGroup == 0) inBraces--;
          break;

        case '\\':
          if (i + 1 < length) {
            char ch1 = findString.charAt(i + 1);
            if (inQuote) {
              if (ch1 == 'E') inQuote = false;
              buf.append(ch).append(ch1);
              i++;

            } else if (ch1 == 'R') {
              if (inCharGroup > 0 || inBraces > 0) {
                String msg = SearchMessages.PatternConstructor_error_line_delim_position;
                throw new PatternSyntaxException(msg, findString, i);
              }
              buf.append("(?>\\r\\n?|\\n)"); // $NON-NLS-1$
              i++;

            } else {
              if (ch1 == 'Q') {
                inQuote = true;
              }
              buf.append(ch).append(ch1);
              i++;
            }
          } else {
            buf.append(ch);
          }
          break;

        default:
          buf.append(ch);
          break;
      }
    }
    return buf.toString();
  }

  private static boolean isWordChar(char c) {
    return Character.isLetterOrDigit(c);
  }

  /**
   * Creates a pattern element from an array of patterns in the old 'StringMatcher' format.
   *
   * @param patterns The search patterns
   * @param isCaseSensitive Set to <code>true</code> to create a case insensitive pattern
   * @return The created pattern
   * @throws PatternSyntaxException if "\R" is at an illegal position
   */
  public static Pattern createPattern(String[] patterns, boolean isCaseSensitive)
      throws PatternSyntaxException {
    StringBuffer pattern = new StringBuffer();
    for (int i = 0; i < patterns.length; i++) {
      if (i > 0) {
        // note that this works only as we know that the operands of the
        // or expression will be simple and need no brackets.
        pattern.append('|');
      }
      appendAsRegEx(true, patterns[i], pattern);
    }
    return createPattern(pattern.toString(), true, true, isCaseSensitive, false);
  }

  public static StringBuffer appendAsRegEx(
      boolean isStringMatcher, String pattern, StringBuffer buffer) {
    boolean isEscaped = false;
    for (int i = 0; i < pattern.length(); i++) {
      char c = pattern.charAt(i);
      switch (c) {
          // the backslash
        case '\\':
          // the backslash is escape char in string matcher
          if (isStringMatcher && !isEscaped) {
            isEscaped = true;
          } else {
            buffer.append("\\\\"); // $NON-NLS-1$
            isEscaped = false;
          }
          break;
          // characters that need to be escaped in the regex.
        case '(':
        case ')':
        case '{':
        case '}':
        case '.':
        case '[':
        case ']':
        case '$':
        case '^':
        case '+':
        case '|':
          if (isEscaped) {
            buffer.append("\\\\"); // $NON-NLS-1$
            isEscaped = false;
          }
          buffer.append('\\');
          buffer.append(c);
          break;
        case '?':
          if (isStringMatcher && !isEscaped) {
            buffer.append('.');
          } else {
            buffer.append('\\');
            buffer.append(c);
            isEscaped = false;
          }
          break;
        case '*':
          if (isStringMatcher && !isEscaped) {
            buffer.append(".*"); // $NON-NLS-1$
          } else {
            buffer.append('\\');
            buffer.append(c);
            isEscaped = false;
          }
          break;
        default:
          if (isEscaped) {
            buffer.append("\\\\"); // $NON-NLS-1$
            isEscaped = false;
          }
          buffer.append(c);
          break;
      }
    }
    if (isEscaped) {
      buffer.append("\\\\"); // $NON-NLS-1$
      isEscaped = false;
    }
    return buffer;
  }

  /**
   * Interprets escaped characters in the given replace pattern.
   *
   * @param replaceText the replace pattern
   * @param foundText the found pattern to be replaced
   * @param lineDelim the line delimiter to use for \R
   * @return a replace pattern with escaped characters substituted by the respective characters
   * @since 3.4
   */
  public static String interpretReplaceEscapes(
      String replaceText, String foundText, String lineDelim) {
    return new ReplaceStringConstructor(lineDelim).interpretReplaceEscapes(replaceText, foundText);
  }

  /**
   * Copied from {@link FindReplaceDocumentAdapter}}
   *
   * <p>FindReplaceDocumentAdapter with contributions from: Cagatay Calli <ccalli@gmail.com> -
   * [find/replace] retain caps when replacing - https://bugs.eclipse.org/bugs/show_bug.cgi?id=28949
   * Cagatay Calli <ccalli@gmail.com> - [find/replace] define & fix behavior of retain caps with
   * other escapes and text before \C - https://bugs.eclipse.org/bugs/show_bug.cgi?id=217061
   */
  private static class ReplaceStringConstructor {

    private static final int RC_MIXED = 0;
    private static final int RC_UPPER = 1;
    private static final int RC_LOWER = 2;
    private static final int RC_FIRSTUPPER = 3;

    private int fRetainCaseMode;
    private final String fLineDelim;

    public ReplaceStringConstructor(String lineDelim) {
      fLineDelim = lineDelim;
    }

    /**
     * Interprets escaped characters in the given replace pattern.
     *
     * @param replaceText the replace pattern
     * @param foundText the found pattern to be replaced
     * @return a replace pattern with escaped characters substituted by the respective characters
     * @since 3.4
     */
    private String interpretReplaceEscapes(String replaceText, String foundText) {
      int length = replaceText.length();
      boolean inEscape = false;
      StringBuffer buf = new StringBuffer(length);

      /* every string we did not check looks mixed at first
       * so initialize retain case mode with RC_MIXED
       */
      fRetainCaseMode = RC_MIXED;

      for (int i = 0; i < length; i++) {
        final char ch = replaceText.charAt(i);
        if (inEscape) {
          i = interpretReplaceEscape(ch, i, buf, replaceText, foundText);
          inEscape = false;

        } else if (ch == '\\') {
          inEscape = true;

        } else if (ch == '$') {
          buf.append(ch);

          /*
           * Feature in java.util.regex.Matcher#replaceFirst(String):
           * $00, $000, etc. are interpreted as $0 and
           * $01, $001, etc. are interpreted as $1, etc. .
           * If we support \0 as replacement pattern for capturing group 0,
           * it would not be possible any more to write a replacement pattern
           * that appends 0 to a capturing group (like $0\0).
           * The fix is to interpret \00 and $00 as $0\0, and
           * \01 and $01 as $0\1, etc.
           */
          if (i + 2 < length) {
            char ch1 = replaceText.charAt(i + 1);
            char ch2 = replaceText.charAt(i + 2);
            if (ch1 == '0' && '0' <= ch2 && ch2 <= '9') {
              buf.append("0\\"); // $NON-NLS-1$
              i++; // consume the 0
            }
          }
        } else {
          interpretRetainCase(buf, ch);
        }
      }

      if (inEscape) {
        // '\' as last character is invalid, but we still add it to get an error message
        buf.append('\\');
      }
      return buf.toString();
    }

    /**
     * Interprets the escaped character <code>ch</code> at offset <code>i</code> of the <code>
     * replaceText</code> and appends the interpretation to <code>buf</code>.
     *
     * @param ch the escaped character
     * @param i the offset
     * @param buf the output buffer
     * @param replaceText the original replace pattern
     * @param foundText the found pattern to be replaced
     * @return the new offset
     * @since 3.4
     */
    private int interpretReplaceEscape(
        final char ch, int i, StringBuffer buf, String replaceText, String foundText) {
      int length = replaceText.length();
      switch (ch) {
        case 'r':
          buf.append('\r');
          break;
        case 'n':
          buf.append('\n');
          break;
        case 't':
          buf.append('\t');
          break;
        case 'f':
          buf.append('\f');
          break;
        case 'a':
          buf.append('\u0007');
          break;
        case 'e':
          buf.append('\u001B');
          break;
        case 'R': // see http://www.unicode.org/unicode/reports/tr18/#Line_Boundaries
          buf.append(fLineDelim);
          break;
          /*
           * \0 for octal is not supported in replace string, since it
           * would conflict with capturing group \0, etc.
           */
        case '0':
          buf.append('$').append(ch);
          /*
           * See explanation in "Feature in java.util.regex.Matcher#replaceFirst(String)"
           * in interpretReplaceEscape(String) above.
           */
          if (i + 1 < length) {
            char ch1 = replaceText.charAt(i + 1);
            if ('0' <= ch1 && ch1 <= '9') {
              buf.append('\\');
            }
          }
          break;

        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          buf.append('$').append(ch);
          break;

        case 'c':
          if (i + 1 < length) {
            char ch1 = replaceText.charAt(i + 1);
            interpretRetainCase(buf, (char) (ch1 ^ 64));
            i++;
          } else {
            String msg = SearchMessages.PatternConstructor_error_escape_sequence;
            throw new PatternSyntaxException(msg, replaceText, i);
          }
          break;

        case 'x':
          if (i + 2 < length) {
            int parsedInt;
            try {
              parsedInt = Integer.parseInt(replaceText.substring(i + 1, i + 3), 16);
              if (parsedInt < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
              String msg = SearchMessages.PatternConstructor_error_hex_escape_sequence;
              throw new PatternSyntaxException(msg, replaceText, i);
            }
            interpretRetainCase(buf, (char) parsedInt);
            i += 2;
          } else {
            String msg = SearchMessages.PatternConstructor_error_hex_escape_sequence;
            throw new PatternSyntaxException(msg, replaceText, i);
          }
          break;

        case 'u':
          if (i + 4 < length) {
            int parsedInt;
            try {
              parsedInt = Integer.parseInt(replaceText.substring(i + 1, i + 5), 16);
              if (parsedInt < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
              String msg = SearchMessages.PatternConstructor_error_unicode_escape_sequence;
              throw new PatternSyntaxException(msg, replaceText, i);
            }
            interpretRetainCase(buf, (char) parsedInt);
            i += 4;
          } else {
            String msg = SearchMessages.PatternConstructor_error_unicode_escape_sequence;
            throw new PatternSyntaxException(msg, replaceText, i);
          }
          break;

        case 'C':
          if (foundText.toUpperCase().equals(foundText)) // is whole match upper-case?
          fRetainCaseMode = RC_UPPER;
          else if (foundText.toLowerCase().equals(foundText)) // is whole match lower-case?
          fRetainCaseMode = RC_LOWER;
          else if (Character.isUpperCase(foundText.charAt(0))) // is first character upper-case?
          fRetainCaseMode = RC_FIRSTUPPER;
          else fRetainCaseMode = RC_MIXED;
          break;

        default:
          // unknown escape k: append uninterpreted \k
          buf.append('\\').append(ch);
          break;
      }
      return i;
    }

    /**
     * Interprets current Retain Case mode (all upper-case,all lower-case,capitalized or mixed) and
     * appends the character <code>ch</code> to <code>buf</code> after processing.
     *
     * @param buf the output buffer
     * @param ch the character to process
     * @since 3.4
     */
    private void interpretRetainCase(StringBuffer buf, char ch) {
      if (fRetainCaseMode == RC_UPPER) buf.append(String.valueOf(ch).toUpperCase());
      else if (fRetainCaseMode == RC_LOWER) buf.append(String.valueOf(ch).toLowerCase());
      else if (fRetainCaseMode == RC_FIRSTUPPER) {
        buf.append(String.valueOf(ch).toUpperCase());
        fRetainCaseMode = RC_MIXED;
      } else buf.append(ch);
    }
  }
}
