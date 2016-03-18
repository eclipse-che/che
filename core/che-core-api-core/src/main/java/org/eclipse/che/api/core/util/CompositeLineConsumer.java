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

/**
 * @author andrew00x
 */
public class CompositeLineConsumer implements LineConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(CompositeLineConsumer.class);

    private final LineConsumer[] lineConsumers;

    public CompositeLineConsumer(LineConsumer... lineConsumers) {
        this.lineConsumers = lineConsumers;
    }

    @Override
    public void close() throws IOException {
        for (LineConsumer lineConsumer : lineConsumers) {
            try {
                lineConsumer.close();
            } catch (IOException e) {
                LOG.error(String.format("An error occurred while closing the line consumer %s", lineConsumer), e);
            }
        }
    }

    @Override
    public void writeLine(String line) throws IOException {
        for (LineConsumer lineConsumer : lineConsumers) {
            try {
                lineConsumer.writeLine(line);
            } catch (IOException e) {
                LOG.error(String.format("An error occurred while writing line to the line consumer %s", lineConsumer), e);
            }
        }
    }
}
