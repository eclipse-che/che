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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'info args' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbInfoArgs {

    private static final Pattern GDB_ARGS = Pattern.compile("(.*) = (.*)");

    private final Map<String, String> variables;

    public GdbInfoArgs(Map<String, String> variables) {
        this.variables = variables;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    /**
     * Factory method.
     */
    public static GdbInfoArgs parse(GdbOutput gdbOutput) throws GdbParseException {
        String output = gdbOutput.getOutput();

        Map<String, String> variables = new HashMap<>();

        for (String line : output.split("\n")) {
            Matcher matcher = GDB_ARGS.matcher(line);
            if (matcher.find()) {
                String variable = matcher.group(1);
                String value = matcher.group(2);
                variables.put(variable, value);
            }
        }

        return new GdbInfoArgs(variables);
    }
}
