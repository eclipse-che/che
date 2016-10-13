/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.server;

import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Helper methods for analyzing the Composer output for errors.
 * 
 * @author Kaloyan Raev
 */
public class ComposerOutputAnalyzer {

    private static final String EXCEPTION_REGEX = "\\s*\\[.*Exception.*\\]\\s*";
    private static final String DEPENDENCY_PROBLEM_ERROR_LINE = "Your requirements could not be resolved to an installable set of packages.";

    public static void checkForErrors(int exitCode, @NotNull List<String> output) throws IOException {
        switch (exitCode) {
        case 0: // no error
            return;

        case 1: // general error
            checkForGeneralError(exitCode, output);
            break;

        case 2: // dependency problem
            checkForDependencyProblem(exitCode, output);
            break;

        default: // unexpected
            throwExitCodeException(exitCode);
        }
    }

    private static void checkForGeneralError(int exitCode, List<String> output) throws IOException {
        Iterator<String> iterator = output.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.matches(EXCEPTION_REGEX)) {
                // next line contains the error message
                if (iterator.hasNext()) {
                    throw new IOException(iterator.next().trim());
                }
            }
        }
        throwExitCodeException(exitCode);
    }

    private static void checkForDependencyProblem(int exitCode, List<String> output) throws IOException {
        Iterator<String> iterator = output.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (DEPENDENCY_PROBLEM_ERROR_LINE.equals(line)) {
                // include this and next (up to 10) lines in the error message
                StringBuilder message = new StringBuilder(line);
                int lines = 0;
                while (iterator.hasNext() && lines < 10) {
                    message.append('\n').append(iterator.next());
                    lines++;
                }
                throw new IOException(message.toString());
            }
        }
        throwExitCodeException(exitCode);
    }

    private static void throwExitCodeException(int exitCode) throws IOException {
        throw new IOException(String.format("Composer exited with code %d.", exitCode));
    }

}
