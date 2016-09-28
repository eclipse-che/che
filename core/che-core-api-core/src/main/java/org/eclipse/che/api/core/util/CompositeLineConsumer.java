/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author andrew00x
 * @author Mykola Morhun
 */
public class CompositeLineConsumer implements LineConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(CompositeLineConsumer.class);

    private final List<LineConsumer> lineConsumers;
    private volatile boolean isClosed;

    public CompositeLineConsumer(LineConsumer... lineConsumers) {
        this.lineConsumers = new CopyOnWriteArrayList<>(lineConsumers);
        this.isClosed = false;
    }

    @Override
    public void close() throws IOException {
        if (!isClosed) {
            isClosed = true;
            for (LineConsumer lineConsumer : lineConsumers) {
                try {
                    lineConsumer.close();
                } catch (IOException e) {
                    LOG.error(String.format("An error occurred while closing the line consumer %s", lineConsumer), e);
                }
            }
        }
    }

    @Override
    public void writeLine(String line) throws IOException {
        if (!isClosed) {
            for (LineConsumer lineConsumer : lineConsumers) {
                try {
                    lineConsumer.writeLine(line);
                } catch (ConsumerAlreadyClosedException | ClosedByInterruptException e) {
                    lineConsumers.remove(lineConsumer); // consumer is already closed, so we cannot write into it any more
                    if (lineConsumers.size() == 0) { // if all consumers are closed then we can close this one
                        isClosed = true;
                    }
                } catch (IOException e) {
                    LOG.error(String.format("An error occurred while writing line to the line consumer %s", lineConsumer), e);
                }
            }
        }
    }

}
