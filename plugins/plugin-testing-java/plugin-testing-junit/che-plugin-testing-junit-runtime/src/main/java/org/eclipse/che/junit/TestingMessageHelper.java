/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.junit;


import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Class annalise an output information from test runner and redirect it to the special stream.
 */
public class TestingMessageHelper {
    private static final char ESCAPE_SEPARATOR = '!';

    /**
     * Prints a message when the test reported was attached.
     *
     * @param out
     *         output stream
     */
    public static void reporterAttached(PrintStream out) {
        out.println(create("testReporterAttached", (List<Pair>)null));
    }

    /**
     * Prints an information about the root test execution.
     *
     * @param out
     *         output stream
     * @param description
     *         information about test cass
     */
    public static void rootPresentation(PrintStream out, Description description) {
        String name = "Default Suite";
        out.println(create("rootName",
                           new Pair("name", name),
                           new Pair("location", "file://" + description.getClassName())));
    }

    /**
     * Prints an information when an atomic test is about to be started.
     *
     * @param description
     *         information about test
     * @param out
     *         output stream
     */
    public static void testStarted(PrintStream out, Description description) {
        String location = description.getClassName() + "." + description.getMethodName();
        out.println(create("testStarted",
                           new Pair("name", escape(description.getMethodName())),
                           new Pair("locationHint", "java:test://" + escape(location))));
    }

    /**
     * Prints an information when a test will not be run.
     *
     * @param name
     *         method name
     * @param out
     *         output stream
     */
    public static void testIgnored(PrintStream out, String name) {
        out.println(create("testIgnored", new Pair("name", escape(name))));
    }

    /**
     * Prints an information when an atomic test has finished.
     *
     * @param description
     *         information about test method
     * @param out
     *         output stream
     * @param duration
     *         time of test running
     */
    public static void testFinished(PrintStream out, Description description, long duration) {
        out.println(create("testFinished",
                           new Pair("name", escape(description.getMethodName())),
                           new Pair("duration", String.valueOf(duration))));
    }

    /**
     * Prints an information when an test node has added.
     *
     * @param description
     *         information about test node
     * @param out
     *         output stream
     */
    public static void treeNode(PrintStream out, Description description) {
        String location = description.getClassName() + "." + description.getMethodName();
        out.println(create("suiteTreeNode",
                           new Pair("name", escape(description.getMethodName())),
                           new Pair("location", "java:test://" + escape(location))));
    }

    /**
     * Prints an information when running of test suite started.
     *
     * @param currentSuite
     *         name of test suite
     * @param out
     *         output stream
     */
    public static void testSuiteFinished(PrintStream out, String currentSuite) {
        out.println(create("testSuiteFinished", new Pair("name", escape(currentSuite))));
    }

    /**
     * Prints an information when running of test suite started.
     *
     * @param description
     *         information about suite
     * @param out
     *         output stream
     */
    public static void testSuiteStarted(PrintStream out, Description description) {
        out.println(create("testSuiteStarted",
                           new Pair("name", escape(description.getClassName())),
                           new Pair("location", escape(description.getClassName()))));
    }

    /**
     * Prints an information when building of test tree started.
     *
     * @param description
     *         information about suite
     * @param out
     *         output stream
     */
    public static void suiteTreeNodeStarted(PrintStream out, Description description) {
        out.println(create("suiteTreeStarted",
                           new Pair("name", escape(description.getClassName())),
                           new Pair("location", "java:test://" + escape(description.getClassName()))));
    }

    /**
     * Prints an information when building of test tree ended.
     *
     * @param description
     *         information about suite
     * @param out
     *         output stream
     */
    public static void suiteTreeNodeEnded(PrintStream out, Description description) {
        out.println(create("suiteTreeEnded",
                           new Pair("name", escape(description.getClassName())),
                           new Pair("location", "java:test://" + escape(description.getClassName()))));
    }

    /**
     * Prints an information when a test fails.
     *
     * @param out
     *         output stream
     * @param failure
     *         describes the test that failed and the exception that was thrown
     * @param duration
     *         time of test running
     */
    public static void testFailed(PrintStream out, Failure failure, long duration) {
        List<Pair> attributes = new ArrayList<>();
        attributes.add(new Pair("name", escape(failure.getDescription().getMethodName())));
        Throwable exception = failure.getException();
        if (exception != null) {
            String failMessage = failure.getMessage();
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            exception.printStackTrace(printWriter);
            String stackTrace = writer.getBuffer().toString();
            attributes.add(new Pair("message", escape(failMessage)));
            attributes.add(new Pair("details", escape(stackTrace)));
        } else {
            attributes.add(new Pair("message", ""));
        }
        attributes.add(new Pair("duration", String.valueOf(duration)));

        out.println(create("testFailed", attributes));
    }

    /**
     * Prints an information about result of the test running.
     *
     * @param out
     *         output stream
     * @param result
     *         the summary of the test run, including all the tests that failed
     */
    public static void testRunFinished(PrintStream out, Result result) {
        out.printf("Total tests run: %d, Failures: %d, Skips: %d", result.getRunCount(), result.getFailureCount(), result.getIgnoreCount());
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
