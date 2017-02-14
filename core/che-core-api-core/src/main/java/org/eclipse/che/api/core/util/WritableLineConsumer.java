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
import java.io.Writer;

/**
 * Line consumer that consume lines to provided Writer.
 *
 * @author Sergii Kabashniuk
 */
public class WritableLineConsumer implements LineConsumer {

    private final Writer writer;

    public WritableLineConsumer(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void writeLine(String line) throws IOException {
        writer.write(line);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
