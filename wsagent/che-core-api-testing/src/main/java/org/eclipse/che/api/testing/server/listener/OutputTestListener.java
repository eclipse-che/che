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
package org.eclipse.che.api.testing.server.listener;

import org.eclipse.che.api.core.util.WebsocketMessageConsumer;
import org.eclipse.che.api.testing.server.handler.TestingOutputImpl;
import org.eclipse.che.api.testing.shared.TestingOutput;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.eclipse.che.api.testing.shared.Constants.TESTING_OUTPUT_CHANNEL_NAME;

/**
 * Listener for the testing services to report their progress to the Che output view.
 *
 * @author David Festal
 */
@Deprecated
public class OutputTestListener extends AbstractTestListener implements AutoCloseable {
    private static final Logger                     LOG                 = LoggerFactory.getLogger(AbstractTestListener.class);
    private static final String                     consumeErrorMessage = "An exception occured while trying to send a 'TestingOutput' "
                                                                          + "object through web sockets on the following channel: "
                                                                          + TESTING_OUTPUT_CHANNEL_NAME;

    private WebsocketMessageConsumer<TestingOutput> consumer            = new WebsocketMessageConsumer<>(TESTING_OUTPUT_CHANNEL_NAME);
    private String                                  stackTraceRoot;

    public OutputTestListener(String strackTraceRoot) {
        this.stackTraceRoot = strackTraceRoot;
        writeLine("Starting Test Session", TestingOutput.LineType.SESSION_START);
    }

    private void writeLine(String line, TestingOutput.LineType lineType) {
        try {
            consumer.consume(DtoFactory.cloneDto(new TestingOutputImpl(line, lineType)));
        } catch (IOException e) {
            LOG.error(consumeErrorMessage, e);
        }
    }

    @Override
    public void close() throws Exception {
        writeLine("Finished Test Session", TestingOutput.LineType.SESSION_END);
        consumer.close();
    }

    @Override
    protected void startedTest(String testKey, String testName) {
        writeLine("[Starting Test] " + testName, TestingOutput.LineType.DETAIL);
    }

    @Override
    protected void endedTest(String testKey, String testName, TestSummary summary) {
        TestingOutput.LineType lineType;
        String detailText;
        if (summary == null || summary.succeeded()) {
            lineType = TestingOutput.LineType.SUCCESS;
            detailText = "successfully";
        } else {
            detailText = "with " + summary;
            if (summary.getErrors() > 0) {
                lineType = TestingOutput.LineType.ERROR;
            } else {
                lineType = TestingOutput.LineType.FAILURE;
            }
        }
        writeLine("[Finished Test] " + testName + " " + detailText, lineType);

    }

    private void addProblem(String testKey, Throwable throwable, boolean isError) {
        StringWriter sw = new StringWriter();
        TestingOutput.LineType lineType = isError ? TestingOutput.LineType.ERROR
            : TestingOutput.LineType.FAILURE;
        try (PrintWriter w = new PrintWriter(sw)) {
            throwable.printStackTrace(w);
        }
        writeLine("  [" + lineType.name() + "]", lineType);
        for (String line : sw.getBuffer().toString().split("\\n")) {
            if (line.contains(stackTraceRoot)) {
                break;
            }
            writeLine("    " + line, TestingOutput.LineType.DETAIL);
        }
    }

    @Override
    protected void addedFailure(String testKey, Throwable throwable) {
        addProblem(testKey, throwable, false);
    }

    @Override
    protected void addedError(String testKey, Throwable throwable) {
        addProblem(testKey, throwable, true);
    }
}
