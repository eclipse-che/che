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
package org.eclipse.che.plugin.svn.server.upstream;

import org.eclipse.che.api.core.util.LineConsumer;

import java.io.IOException;
import java.util.List;

/**
 * Implementation of {@link LineConsumer} handling output of executing command line.
 */
public class CommandLineOutputProcessor implements LineConsumer {

    private List<String> output;

    public CommandLineOutputProcessor(final List<String> output) {
        this.output = output;
    }

    @Override
    public void writeLine(String line) throws IOException {
        output.add(line);
    }

    @Override
    public void close() throws IOException {
        //nothing to close
    }

    public List<String> getOutput() {
        return output;
    }

}
