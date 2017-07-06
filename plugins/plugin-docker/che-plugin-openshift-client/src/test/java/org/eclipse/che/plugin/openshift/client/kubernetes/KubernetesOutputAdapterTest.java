/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.plugin.openshift.client.kubernetes;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;


public class KubernetesOutputAdapterTest {

    private static LogMessage.Type LOG_TYPE = LogMessage.Type.DOCKER;
    private testMessageProcessor processor;
    private KubernetesOutputAdapter adapter;

    private class testMessageProcessor implements MessageProcessor<LogMessage> {

        private List<String> messages;
        private LogMessage.Type type = null;

        public testMessageProcessor() {
            this.messages = new ArrayList<>();
        }

        @Override
        public void process(LogMessage message) {
            LogMessage.Type messageType = message.getType();
            if (type == null) {
                type = messageType;
            }
            messages.add(message.getContent());
        }

        public List<String> getMessages() {
            return new ArrayList<>(messages);
        }

        public LogMessage.Type getType() {
            return type;
        }
    };

    @BeforeMethod
    public void setUp() {
        processor = new testMessageProcessor();
        adapter = new KubernetesOutputAdapter(LOG_TYPE, processor);
    }

    @Test
    public void shouldBreakLinesCorrectly() {
        // Given
        byte[] input = "line1\nline2\n".getBytes();
        List<String> expected = generateExpected("line1", "line2");

        // When
        adapter.call(input);

        // Then
        List<String> actual = processor.getMessages();
        assertEquals(actual, expected, "Should break lines on \\n char");
    }

    @Test
    public void shouldCacheUnfinishedLinesBetweenCalls() {
        // Given
        byte[] firstInput = "line1\nlin".getBytes();
        byte[] secondInput = "e2\nline3\n".getBytes();
        List<String> expected = generateExpected("line1", "line2", "line3");

        // When
        adapter.call(firstInput);
        adapter.call(secondInput);

        // Then
        List<String> actual = processor.getMessages();
        assertEquals(actual, expected, "Should store unfinished lines between calls");
    }

    @Test
    public void shouldUseProvidedLogMessageType() {
        for (LogMessage.Type type : LogMessage.Type.values()) {
            // Given
            byte[] input = "line1\n".getBytes();
            LogMessage.Type expected = type;
            processor = new testMessageProcessor();
            adapter = new KubernetesOutputAdapter(type, processor);

            // When
            adapter.call(input);

            // Then
            LogMessage.Type actual = processor.getType();
            assertEquals(actual, expected, "Should call MessageProcessor with provided type");
        }
    }

    @Test
    public void shouldBreakLinesNormallyWithCarriageReturn() {
        // Given
        byte[] input = "line1\r\nline2\n".getBytes();
        List<String> expected = generateExpected("line1", "line2");

        // When
        adapter.call(input);

        // Then
        List<String> actual = processor.getMessages();
        assertEquals(actual, expected, "Should break lines normally on \\r\\n characters");
    }

    @Test
    public void shouldNotIgnoreEmptyLines() {
        // Given
        byte[] input = "line1\n\nline2\n".getBytes();
        List<String> expected = generateExpected("line1", "", "line2");

        // When
        adapter.call(input);

        // Then
        List<String> actual = processor.getMessages();
        assertEquals(actual, expected, "Should call processor.process() with empty Strings");
    }

    @Test
    public void shouldNotCallWithoutFinalNewline() {
        // Given
        byte[] input = "line1\nline2".getBytes(); // No trailing \n
        List<String> firstExpected = generateExpected("line1");
        List<String> secondExpected = generateExpected("line1", "line2");

        // When
        adapter.call(input);

        // Then
        List<String> firstActual = processor.getMessages();
        assertEquals(firstActual, firstExpected, "Should only process lines when they are terminated by \\n or \\r\\n");

        // When
        adapter.call("\n".getBytes());

        // Then
        List<String> secondActual = processor.getMessages();
        assertEquals(secondActual, secondExpected, "Should buffer lines until newline is encountered.");

    }

    @Test
    public void shouldIgnoreNullCalls() {
        // Given
        byte[] firstInput = "line1\n".getBytes();
        byte[] secondInput = "line2\n".getBytes();
        List<String> expected = generateExpected("line1", "line2");

        // When
        adapter.call(firstInput);
        adapter.call(null);
        adapter.call(secondInput);

        // Then
        List<String> actual = processor.getMessages();
        assertEquals(actual, expected, "Should ignore calls with null arguments");
    }

    @Test
    public void shouldKeepBufferPastNullCalls() {
        // Given
        byte[] firstInput = "lin".getBytes();
        byte[] secondInput = "e1\nline2\n".getBytes();
        List<String> expected = generateExpected("line1", "line2");

        // When
        adapter.call(firstInput);
        adapter.call(null);
        adapter.call(secondInput);

        // Then
        List<String> actual = processor.getMessages();
        assertEquals(actual, expected, "Should ignore calls with null arguments");
    }

    @Test
    public void shouldDoNothingWhenExecOutputProcessorIsNull() {
        // Given
        byte[] firstInput = "line1\n".getBytes();
        byte[] secondInput = "line2\n".getBytes();
        adapter = new KubernetesOutputAdapter(LOG_TYPE, null);

        // When
        adapter.call(firstInput);
        adapter.call(secondInput);

        // Then
        List<String> actual = processor.getMessages();
        assertTrue(actual.isEmpty(), "Should do nothing when ExecOutputProcessor is null");
    }

    @Test
    public void shouldIgnoreCallsWhenDataIsEmpty() {
        // Given
        byte[] emptyInput = "".getBytes();
        byte[] firstInput = "line1\n".getBytes();
        byte[] secondInput = "line2\n".getBytes();
        List<String> expected = generateExpected("line1", "line2");

        // When
        adapter.call(emptyInput);
        adapter.call(firstInput);
        adapter.call(emptyInput);
        adapter.call(secondInput);
        adapter.call(emptyInput);

        // Then
        List<String> actual = processor.getMessages();
        assertEquals(actual, expected, "KubernetesOutputAdapter ignore empty data calls");

    }

    private List<String> generateExpected(String... strings) {
        List<String> expected = new ArrayList<>();
        for (String string : strings) {
            expected.add(string);
        }
        return expected;
    }


}
