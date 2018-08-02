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
package org.testng;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/** Class which analyze output messages from test framework and prints their to the stream. */
public class TestingMessageHelper {

  private static final char ESCAPE_SEPARATOR = '!';

  /**
   * Prints a message when the test reported was attached.
   *
   * @param str output stream
   */
  public static void reporterAttached(PrintStream str) {
    str.println(create("testReporterAttached", (List<Pair>) null));
  }

  /**
   * Prints the number of ran methods.
   *
   * @param out output stream
   * @param count the number of test methods
   */
  public static void methodCount(PrintStream out, int count) {
    out.println(create("testCount", new Pair("count", String.valueOf(count))));
  }

  /**
   * Prints information about the root test execution.
   *
   * @param out output stream
   * @param name name of root element
   * @param fileName name of the file
   */
  public static void rootPresentation(PrintStream out, String name, String fileName) {
    out.println(
        create("rootName", new Pair("name", name), new Pair("location", "file://" + fileName)));
  }

  /**
   * Message when the test is started.
   *
   * @param out output stream
   * @param name name of the test method
   */
  public static void testStarted(PrintStream out, String name) {
    out.println(create("testStarted", new Pair("name", escape(name))));
  }

  /**
   * Message when the test is started.
   *
   * @param out output stream
   * @param name a name of the test method
   * @param location location of the test
   * @param config {@code true} if configuration is included
   */
  public static void testStarted(PrintStream out, String name, String location, boolean config) {
    out.println(
        create(
            "testStarted",
            new Pair("name", escape(name)),
            new Pair("locationHint", "java:test://" + escape(location)),
            new Pair("config", String.valueOf(config))));
  }

  /**
   * Message when the test method is ignored.
   *
   * @param out output stream
   * @param name name of the test method
   */
  public static void testIgnored(PrintStream out, String name) {
    out.println(create("testIgnored", new Pair("name", escape(name))));
  }

  /**
   * Message when the test method is finished.
   *
   * @param out output stream
   * @param name name of the test method
   */
  public static void testFinished(PrintStream out, String name) {
    out.println(create("testFinished", new Pair("name", escape(name))));
  }

  /**
   * Message when the test method is finished.
   *
   * @param out output stream
   * @param methodName name of the test method
   * @param duration duration of the test employment
   */
  public static void testFinished(PrintStream out, String methodName, long duration) {
    out.println(
        create(
            "testFinished",
            new Pair("name", escape(methodName)),
            new Pair("duration", String.valueOf(duration))));
  }

  /**
   * Message when the test suite is finished.
   *
   * @param out output stream
   * @param name name of the suite
   */
  public static void testSuiteFinished(PrintStream out, String name) {
    out.println(create("testSuiteFinished", new Pair("name", escape(name))));
  }

  /**
   * Message when the test suite is started.
   *
   * @param out output stream
   * @param name name of an output strean
   * @param location path to the suite
   * @param provideLocation {@code true} when the location is exist
   */
  public static void testSuiteStarted(
      PrintStream out, String name, String location, boolean provideLocation) {
    out.println(
        create(
            "testSuiteStarted",
            new Pair("name", escape(name)),
            new Pair("location", (provideLocation ? location : ""))));
  }

  /**
   * Message when the test is failed.
   *
   * @param out output stream
   * @param params special parameters which can contain the name of failed test, error message
   */
  public static void testFailed(PrintStream out, Map<String, String> params) {

    List<Pair> attributes = new ArrayList<>(params.size());
    for (String key : params.keySet()) {
      attributes.add(new Pair(key, escape(params.get(key))));
    }
    out.println(create("testFailed", attributes));
  }

  private static String create(String name, Pair... attributes) {
    List<Pair> pairList = null;
    if (attributes != null) {
      pairList = Arrays.asList(attributes);
    }
    return create(name, pairList);
  }

  private static String create(String name, List<Pair> attributes) {
    StringBuilder builder = new StringBuilder("@@<{\"name\":");
    builder.append('"').append(name).append('"');
    if (attributes != null) {
      builder.append(", \"attributes\":{");
      StringJoiner joiner = new StringJoiner(", ");
      for (Pair attribute : attributes) {
        joiner.add("\"" + attribute.first + "\":\"" + attribute.second + "\"");
      }
      builder.append(joiner.toString());
      builder.append("}");
    }

    builder.append("}>");
    return builder.toString();
  }

  private static String escape(String str) {
    if (str == null) {
      return null;
    }

    int escapeLength = calculateEscapedStringLength(str);
    if (escapeLength == str.length()) {
      return str;
    }

    char[] chars = new char[escapeLength];
    int currentOffset = 0;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      char escape = escapeChar(c);
      if (escape != 0) {
        chars[currentOffset++] = ESCAPE_SEPARATOR;
        chars[currentOffset++] = escape;
      } else {
        chars[currentOffset++] = c;
      }
    }

    return new String(chars);
  }

  private static int calculateEscapedStringLength(String string) {
    int result = 0;
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if (escapeChar(c) != 0) {
        result += 2;
      } else {
        result++;
      }
    }
    return result;
  }

  private static char escapeChar(char c) {
    switch (c) {
      case '\n':
        return 'n';
      case '\r':
        return 'r';
      case '\u0085':
        return 'x';
      case '\u2028':
        return 'l';
      case '\u2029':
        return 'p';
      default:
        return 0;
    }
  }

  private static class Pair {
    final String first;
    final String second;

    Pair(String first, String second) {
      this.first = first;
      this.second = second;
    }
  }
}
