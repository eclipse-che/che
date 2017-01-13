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
package org.eclipse.che.plugin.nodejsdbg.server.parser;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code backtrace} command parser.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsBackTraceParser implements NodeJsOutputParser<Location> {

    public static final NodeJsBackTraceParser INSTANCE = new NodeJsBackTraceParser();
    public static final Pattern               PATTERN  = Pattern.compile("#0(.*) (.*):(.*):(.*)");

    private NodeJsBackTraceParser() { }

    @Override
    public boolean match(NodeJsOutput nodeJsOutput) {
        return nodeJsOutput.getOutput().startsWith("#0");
    }

    @Override
    public Location parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
        String output = nodeJsOutput.getOutput();

        for (String line : output.split("\n")) {
            Matcher matcher = PATTERN.matcher(line);
            if (matcher.find()) {
                String file = matcher.group(2);
                String lineNumber = matcher.group(3);
                return new LocationImpl(file, Integer.parseInt(lineNumber));
            }
        }

        throw new NodeJsDebuggerParseException(Location.class, output);
    }
}
