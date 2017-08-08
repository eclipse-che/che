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
package org.eclipse.che.api.project.server.importer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Base abstraction for consuming project importing output events.
 * <p>
 * Consumes output lines into delayed queue and perform broadcasting the last ones through each type of implementation.
 * There are only two implementation of broadcasting type represented by {@link ProjectImportOutputWSLineConsumer} which
 * broadcasts events through the web socket and {@link ProjectImportOutputJsonRpcLineConsumer} which broadcasts events
 * through the json rpc protocol.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
public abstract class BaseProjectImportOutputLineConsumer implements LineConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(BaseProjectImportOutputLineConsumer.class);

    protected final String                   projectName;
    protected final BlockingQueue<String>    lineToSendQueue;
    protected final ScheduledExecutorService executor;

    public BaseProjectImportOutputLineConsumer(String projectName, int delayBetweenMessages) {
        this.projectName = projectName;
        lineToSendQueue = new ArrayBlockingQueue<>(1024);
        executor = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat(BaseProjectImportOutputLineConsumer.class.getSimpleName() + "-%d")
                                          .setDaemon(true)
                                          .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                          .build());
        executor.scheduleAtFixedRate(() -> {
            String lineToSend = null;
            while (!lineToSendQueue.isEmpty()) {
                lineToSend = lineToSendQueue.poll();
            }
            if (lineToSend == null) {
                return;
            }
            sendOutputLine(lineToSend);
        }, 0, delayBetweenMessages, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() throws IOException {
        executor.shutdown();
    }

    @Override
    public void writeLine(String line) throws IOException {
        try {
            lineToSendQueue.put(line);
        } catch (InterruptedException exception) {
            LOG.info(exception.getLocalizedMessage());
        }
    }

    /**
     * Perform sending the given {@code outputLine} through the specific algorithm.
     *
     * @param outputLine
     *         output line message
     */
    protected abstract void sendOutputLine(String outputLine);
}
