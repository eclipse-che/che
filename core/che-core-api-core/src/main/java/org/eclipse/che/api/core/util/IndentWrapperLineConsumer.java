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
package org.eclipse.che.api.core.util;

import java.io.IOException;

/**
 * Line consumer for adding system indent in the end of the line
 *
 * @author Andrienko Alexander
 */
public class IndentWrapperLineConsumer implements LineConsumer {

    private final LineConsumer lineConsumer;

    public IndentWrapperLineConsumer(LineConsumer lineConsumer) {
        this.lineConsumer = lineConsumer;
    }

    /** {@inheritDoc} */
    @Override
    public void writeLine(String line) throws IOException {
        lineConsumer.writeLine(line + System.lineSeparator());
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        lineConsumer.close();
    }
}
