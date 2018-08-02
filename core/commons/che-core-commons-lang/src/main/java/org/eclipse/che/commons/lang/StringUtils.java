/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.lang;

/** Set of useful String methods */
public class StringUtils {

  /**
   * Trim last char of the string if string ends with that char.
   *
   * @param s the string to trim
   * @param suffix the suffix
   * @return trimmed string
   */
  public static String trimEnd(String s, char suffix) {
    if (endWithChar(s, suffix)) {
      return s.substring(0, s.length() - 1);
    }
    return s;
  }

  /** Check if string ends with char. */
  public static boolean endWithChar(String s, char suffix) {
    return s != null && !s.isEmpty() && s.charAt(s.length() - 1) == suffix;
  }

  /** Add to builder 'times' symbol 'symbol' */
  public static void repeatSymbol(StringBuilder builder, char symbol, int times) {
    for (int i = 0; i < times; i++) {
      builder.append(symbol);
    }
  }

  /** Check if CharSequence end with suffix */
  public static boolean endWith(CharSequence builder, CharSequence suffix) {
    int bl = builder.length();
    int sl = suffix.length();
    if (bl < sl) {
      return false;
    }

    for (int i = bl - 1; i >= bl - sl; i--) {
      if (builder.charAt(i) != suffix.charAt(i + sl - bl)) {
        return false;
      }
    }
    return true;
  }

  /** Check that char sequences are equal */
  public static boolean equals(CharSequence c1, CharSequence c2) {
    if (c1 == null ^ c2 == null) {
      return false;
    }

    if (c1 == null) {
      return true;
    }
    if (c1.length() != c2.length()) {
      return false;
    }

    for (int i = 0; i < c1.length(); i++) {
      if (c1.charAt(i) != c2.charAt(i)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns the index within this string of the last occurrence of the specified char, searching in
   * specified range
   */
  public static int lastIndexOf(CharSequence s, char c, int start, int end) {
    start = Math.max(start, 0);
    for (int i = Math.min(end, s.length()) - 1; i >= start; i--) {
      if (s.charAt(i) == c) {
        return i;
      }
    }
    return -1;
  }
}
