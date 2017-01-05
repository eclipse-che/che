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
package org.eclipse.che.plugin.gdb.server.parser;

import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'directory' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbDirectory {

    private static final Pattern GDB_DIRECTORY = Pattern.compile("^Source directories searched: (.*)\n");

    private final String directories;

    public GdbDirectory(String directories) {this.directories = directories;}

    public String getDirectories() {
        return directories;
    }

    /**
     * Factory method.
     */
    public static GdbDirectory parse(GdbOutput gdbOutput) throws GdbParseException {
        String output = gdbOutput.getOutput();

        Matcher matcher = GDB_DIRECTORY.matcher(output);
        if (matcher.find()) {
            String directory = matcher.group(1);
            return new GdbDirectory(directory);
        }

        throw new GdbParseException(GdbDirectory.class, output);
    }
}
