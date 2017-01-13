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
package org.eclipse.che.plugin.gdb.server;

import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

import java.nio.file.Paths;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * @author Anatoliy Bazko
 */
public class GdbDebuggerFactory implements DebuggerFactory {
    private static final String TYPE = "gdb";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Debugger create(Map<String, String> properties, Debugger.DebuggerCallback debuggerCallback) throws DebuggerException {
        Map<String, String> normalizedProps = properties.entrySet()
                                                        .stream()
                                                        .collect(toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));


        String host = normalizedProps.get("host");

        int port;
        try {
            port = Integer.parseInt(normalizedProps.getOrDefault("port", "0"));
        } catch (NumberFormatException e) {
            throw new DebuggerException("Unknown port property format: " + normalizedProps.get("port"));
        }

        String file = normalizedProps.get("binary");
        if (file == null) {
            throw new DebuggerException("binary property is null. Debugger can't be started");
        }

        String sources = normalizedProps.get("sources");
        if (sources == null) {
            sources = Paths.get(file).getParent().toString();
        }

        return GdbDebugger.newInstance(host, port, file, sources, debuggerCallback);
    }
}
