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

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of line consumer that stores in the list strings that are passed with method {@link #writeLine}.
 * <p/>
 * Implementation is not threadsafe and requires external synchronization if is used in multi-thread environment.
 *
 * @author andrew00x
 */
public class ListLineConsumer implements LineConsumer {
    protected final LinkedList<String> lines;

    public ListLineConsumer() {
        lines = new LinkedList<>();
    }

    @Override
    public void writeLine(String line) {
        lines.add(line);
    }

    @Override
    public void close() {
    }

    public void clear() {
        lines.clear();
    }

    public List<String> getLines() {
        return new LinkedList<>(lines);
    }

    public String getText() {
        if (lines.isEmpty()) {
            return "";
        }
        final StringBuilder output = new StringBuilder();
        int n = 0;
        for (String line : lines) {
            if (n > 0) {
                output.append('\n');
            }
            output.append(line);
            n++;
        }
        return output.toString();
    }
}
