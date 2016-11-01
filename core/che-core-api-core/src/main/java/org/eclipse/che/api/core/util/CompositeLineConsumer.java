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

import org.eclipse.che.api.core.util.lineconsumer.ConsumerAlreadyClosedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This line consumer consists of several consumers and copies each consumed line into all subconsumers.
 * Is used when lines should be written into two or more consumers.
 * <br/>
 * This class is not thread safe.
 * Also see multithreaded implementation {@link org.eclipse.che.api.core.util.lineconsumer.ConcurrentCompositeLineConsumer}
 *
 * @author andrew00x
 * @author Mykola Morhun
 */
public class CompositeLineConsumer implements LineConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(CompositeLineConsumer.class);

    private final List<LineConsumer> lineConsumers;
    private boolean                  isOpen;

    public CompositeLineConsumer(LineConsumer... lineConsumers) {
        this.lineConsumers = new CopyOnWriteArrayList<>(lineConsumers);
        this.isOpen = true;
    }

    /**
     * Closes all unclosed subconsumers.
     */
    @Override
    public void close() {
        if (isOpen) {
            isOpen = false;
            for (LineConsumer lineConsumer : lineConsumers) {
                try {
                    lineConsumer.close();
                } catch (IOException e) {
                    LOG.error(String.format("An error occurred while closing the line consumer %s", lineConsumer), e);
                }
            }
        }
    }

    /**
     * Writes given line to each subconsumer.
     * Do nothing if this consumer is closed or all subconsumers are closed.
     *
     * @param line
     *         line to write
     */
    @Override
    public void writeLine(String line) {
        if (isOpen) {
            for (LineConsumer lineConsumer : lineConsumers) {
                try {
                    lineConsumer.writeLine(line);
                } catch (ConsumerAlreadyClosedException | ClosedByInterruptException e) {
                    lineConsumers.remove(lineConsumer); // consumer is already closed, so we cannot write into it any more
                    if (lineConsumers.size() == 0) { // if all consumers are closed then we can close this one
                        isOpen = false;
                    }
                } catch (IOException e) {
                    LOG.error(String.format("An error occurred while writing line to the line consumer %s", lineConsumer), e);
                }
            }
        }
    }

}
