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

import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Anatolii Bazko
 */
public class ErrorConsumer implements LineConsumer {

    private final ErrorIndicator errorIndicator;
    private final List<String>   errors;

    public ErrorConsumer(ErrorIndicator errorIndicator) {
        this.errorIndicator = errorIndicator;
        this.errors = new LinkedList<>();
    }

    public ErrorConsumer() {
        this(line -> line.contains("[STDERR]"));
    }

    public boolean hasError() {
        return !errors.isEmpty();
    }

    public String getError() {
        return Joiner.on("\n").join(errors);
    }

    @Override
    public void writeLine(String line) throws IOException {
        if (errorIndicator.isError(line)) {
            errors.add(line);
        }
    }

    @Override
    public void close() throws IOException { }

    /**
     * Indicates if line is a error message.
     */
    @FunctionalInterface
    public interface ErrorIndicator {
        boolean isError(String line);
    }
}
