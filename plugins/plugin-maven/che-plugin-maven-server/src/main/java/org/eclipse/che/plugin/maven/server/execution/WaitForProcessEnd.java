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
package org.eclipse.che.plugin.maven.server.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Wait until the {@code Process} has terminated.
 *
 * @author Evgen Vidolob
 */
public class WaitForProcessEnd {

    private static final Logger LOG = LoggerFactory.getLogger(WaitForProcessEnd.class);

    private final Future<?> waitFor;
    private final BlockingQueue<Consumer<Integer>> endCallback = new ArrayBlockingQueue<Consumer<Integer>>(1);

    public WaitForProcessEnd(Process process, Executor executor) {
        waitFor = executor.execute(() -> {
            int exitCode = 0;
            try {
                while (true) {
                    try {
                        exitCode = process.waitFor();
                        break;
                    } catch (InterruptedException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

            } finally {
                try {
                    endCallback.take().accept(exitCode);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
    }

    public void setEndCallback(Consumer<Integer> callback) {
        endCallback.offer(callback);
    }

    public void stop() {
        waitFor.cancel(true);
    }

}
