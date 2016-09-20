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
package org.eclipse.che.plugin.nodejsdbg.server.parser;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code backtrace} command parser.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsBackTrace {

    private static final Pattern BACKTRACE = Pattern.compile("#0(.*) (.*):(.*):(.*)");

    private final Location location;

    public NodeJsBackTrace(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Factory method.
     */
    public static NodeJsBackTrace parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
        String output = nodeJsOutput.getOutput();

        for (String line : output.split("\n")) {
            Matcher matcher = BACKTRACE.matcher(line);
            if (matcher.find()) {
                String file = matcher.group(2);
                String lineNumber = matcher.group(3);
                return new NodeJsBackTrace(new LocationImpl(file, Integer.parseInt(lineNumber)));
            }
        }

        throw new NodeJsDebuggerParseException(NodeJsBackTrace.class, output);
    }
}
