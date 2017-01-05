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
package org.eclipse.che.plugin.docker.client;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Roman Nikitenko
 */
public class LogMessagePumperTest {
    private final static String CONTENT         = "test content";
    private final static String LINE_FEED       = "\n";
    private final static String CARRIAGE_RETURN = "\r";

    @Test
    public void shouldIncludeCarriageReturnCharInLogMessage() throws Exception {
        final String src = CONTENT + CARRIAGE_RETURN;
        MessageProcessor<LogMessage> messageProcessor = new MessageProcessor<LogMessage>() {
            @Override
            public void process(LogMessage message) {
                String actual = message.getContent();

                assertEquals(actual, src);
                assertTrue(actual.endsWith(CARRIAGE_RETURN));
            }
        };
        LogMessagePumper logMessagePumper = new LogMessagePumper(getTestInputStream(src), messageProcessor);
        logMessagePumper.start();
    }

    @Test
    public void shouldNotIncludeCarriageReturnCharInLogMessageWhenLineFeedCharFollow() throws Exception {
        final String src = CONTENT + CARRIAGE_RETURN + LINE_FEED;
        MessageProcessor<LogMessage> messageProcessor = new MessageProcessor<LogMessage>() {
            @Override
            public void process(LogMessage message) {
                String actual = message.getContent();

                assertEquals(actual, CONTENT);
                assertFalse(actual.endsWith(CARRIAGE_RETURN));
            }
        };
        LogMessagePumper logMessagePumper = new LogMessagePumper(getTestInputStream(src), messageProcessor);
        logMessagePumper.start();
    }

    private InputStream getTestInputStream(String src) throws UnsupportedEncodingException {
        final byte remaining = (byte)src.getBytes("UTF-8").length;
        final byte[] stdoutHeader = {1, 0, 0, 0, 0, 0, 0, remaining};
        final int capacity = stdoutHeader.length + src.getBytes().length;

        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);
        byteBuffer.put(stdoutHeader);
        byteBuffer.put(src.getBytes());

        return new ByteArrayInputStream(byteBuffer.array());
    }
}
