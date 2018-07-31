/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.che;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

/** @author Evgen Vidolob */
public class Utils {
  /**
   * Generate a display string from the given String.
   *
   * @param inputString the given input string
   *     <p>Example of use: [org.eclipse.jdt.core.tests.util.Util.displayString("abc\ndef\tghi")]
   */
  public static String displayString(String inputString) {
    return displayString(inputString, 0);
  }

  /**
   * Generate a display string from the given String. It converts:
   *
   * <ul>
   *   <li>\t to \t
   *   <li>\r to \\r
   *   <li>\n to \n
   *   <li>\b to \\b
   *   <li>\f to \\f
   *   <li>\" to \\\"
   *   <li>\' to \\'
   *   <li>\\ to \\\\
   *   <li>All other characters are unchanged.
   * </ul>
   *
   * This method doesn't convert \r\n to \n.
   *
   * <p>Example of use: <o>
   * <li>
   *
   *     <pre>
   * input string = "abc\ndef\tghi",
   * indent = 3
   * result = "\"\t\t\tabc\\n" +
   * 			"\t\t\tdef\tghi\""
   * </pre>
   *
   * <li>
   *
   *     <pre>
   * input string = "abc\ndef\tghi\n",
   * indent = 3
   * result = "\"\t\t\tabc\\n" +
   * 			"\t\t\tdef\tghi\\n\""
   * </pre>
   *
   * <li>
   *
   *     <pre>
   * input string = "abc\r\ndef\tghi\r\n",
   * indent = 3
   * result = "\"\t\t\tabc\\r\\n" +
   * 			"\t\t\tdef\tghi\\r\\n\""
   * </pre>
   *
   * </ol>
   *
   * @param inputString the given input string
   * @param indent number of tabs are added at the begining of each line.
   * @return the displayed string
   */
  public static String displayString(String inputString, int indent) {
    return displayString(inputString, indent, false);
  }

  public static String displayString(String inputString, int indent, boolean shift) {
    if (inputString == null) return "null";
    int length = inputString.length();
    StringBuffer buffer = new StringBuffer(length);
    java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true);
    for (int i = 0; i < indent; i++) buffer.append("\t");
    if (shift) indent++;
    buffer.append("\"");
    while (tokenizer.hasMoreTokens()) {

      String token = tokenizer.nextToken();
      if (token.equals("\r")) {
        buffer.append("\\r");
        if (tokenizer.hasMoreTokens()) {
          token = tokenizer.nextToken();
          if (token.equals("\n")) {
            buffer.append("\\n");
            if (tokenizer.hasMoreTokens()) {
              buffer.append("\" + \n");
              for (int i = 0; i < indent; i++) buffer.append("\t");
              buffer.append("\"");
            }
            continue;
          }
          buffer.append("\" + \n");
          for (int i = 0; i < indent; i++) buffer.append("\t");
          buffer.append("\"");
        } else {
          continue;
        }
      } else if (token.equals("\n")) {
        buffer.append("\\n");
        if (tokenizer.hasMoreTokens()) {
          buffer.append("\" + \n");
          for (int i = 0; i < indent; i++) buffer.append("\t");
          buffer.append("\"");
        }
        continue;
      }

      StringBuffer tokenBuffer = new StringBuffer();
      for (int i = 0; i < token.length(); i++) {
        char c = token.charAt(i);
        switch (c) {
          case '\r':
            tokenBuffer.append("\\r");
            break;
          case '\n':
            tokenBuffer.append("\\n");
            break;
          case '\b':
            tokenBuffer.append("\\b");
            break;
          case '\t':
            tokenBuffer.append("\t");
            break;
          case '\f':
            tokenBuffer.append("\\f");
            break;
          case '\"':
            tokenBuffer.append("\\\"");
            break;
          case '\'':
            tokenBuffer.append("\\'");
            break;
          case '\\':
            tokenBuffer.append("\\\\");
            break;
          default:
            tokenBuffer.append(c);
        }
      }
      buffer.append(tokenBuffer.toString());
    }
    buffer.append("\"");
    return buffer.toString();
  }

  public static void appendProblem(
      StringBuffer problems, IProblem problem, char[] source, int problemCount) {
    problems.append(problemCount + (problem.isError() ? ". ERROR" : ". WARNING"));
    problems.append(" in " + new String(problem.getOriginatingFileName()));
    if (source != null) {
      problems.append(((DefaultProblem) problem).errorReportSource(source));
    }
    problems.append("\n");
    problems.append(problem.getMessage());
    problems.append("\n");
  }

  public static String convertToIndependantLineDelimiter(String source) {
    if (source == null) return "";
    if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1) return source;
    StringBuffer buffer = new StringBuffer();
    for (int i = 0, length = source.length(); i < length; i++) {
      char car = source.charAt(i);
      if (car == '\r') {
        buffer.append('\n');
        if (i < length - 1 && source.charAt(i + 1) == '\n') {
          i++; // skip \n after \r
        }
      } else {
        buffer.append(car);
      }
    }
    return buffer.toString();
  }
}
