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
package org.eclipse.che.plugin.zdb.server;

import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgSettings;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Zend debugger for PHP factory.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgFactory implements DebuggerFactory {

    public static final String TYPE = "zend-debugger";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Debugger create(Map<String, String> properties,
                           Debugger.DebuggerCallback debuggerCallback) throws DebuggerException {

        Map<String, String> normalizedProps = properties.entrySet()
                                                        .stream()
                                                        .collect(toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

        String breakAtFirstLineProp = normalizedProps.get("break-at-first-line");
        if (breakAtFirstLineProp == null) {
            throw new DebuggerException("Can't establish connection: debug break at first line property is unknown.");
        }

        boolean breakAtFirstLine = Boolean.valueOf(breakAtFirstLineProp);
        String clientHostIPProp = normalizedProps.get("client-host-ip");
        if (clientHostIPProp == null) {
            throw new DebuggerException("Can't establish connection: client host/IP property is unknown.");
        }

        String debugPortProp = normalizedProps.get("debug-port");
        if (debugPortProp == null) {
            throw new DebuggerException("Can't establish connection: debug port property is unknown.");
        }

        int debugPort;
        try {
            debugPort = Integer.parseInt(debugPortProp);
        } catch (NumberFormatException e) {
            throw new DebuggerException("Unknown debug port property format: " + debugPortProp);
        }

        String useSslEncryptionProp = normalizedProps.get("use-ssl-encryption");
        if (useSslEncryptionProp == null) {
            throw new DebuggerException("Can't establish connection: debug use SSL encryption property is unknown.");
        }

        boolean useSslEncryption = Boolean.valueOf(useSslEncryptionProp);

        return new ZendDebugger(new ZendDbgSettings(debugPort, clientHostIPProp, breakAtFirstLine, useSslEncryption),
                                new ZendDbgLocationHandler(),
                                debuggerCallback);
    }

}
