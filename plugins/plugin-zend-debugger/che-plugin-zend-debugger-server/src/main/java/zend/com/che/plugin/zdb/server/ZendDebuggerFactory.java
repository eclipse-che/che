/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.server;

import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Zend debugger factory.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebuggerFactory implements DebuggerFactory {

    public static final String TYPE = "zend-debugger";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Debugger create(Map<String, String> properties, Debugger.DebuggerCallback debuggerCallback) throws DebuggerException {
        Map<String, String> normalizedProps = properties.entrySet()
                                                        .stream()
                                                        .collect(toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

        String clientHostIPProp = normalizedProps.get("client-host-ip");
        if (clientHostIPProp == null) {
            throw new DebuggerException("Can't establish connection: client host/IP property is unknown.");
        }

        String debugPortProp = normalizedProps.get("debug-port");
        if (debugPortProp == null) {
            throw new DebuggerException("Can't establish connection: debug port property is unknown.");
        }
        
        String broadcastPortProp = normalizedProps.get("broadcast-port");
        if (broadcastPortProp == null) {
            throw new DebuggerException("Can't establish connection: broadcast port property is unknown.");
        }
        
        String useSslEncryptionProp = normalizedProps.get("use-ssl-encryption");
        if (useSslEncryptionProp == null) {
            throw new DebuggerException("Can't establish connection: use SSL encryption property is unknown.");
        }


        String clientHostIP = clientHostIPProp;
        
        int debugPort;
        try {
            debugPort = Integer.parseInt(debugPortProp);
        } catch (NumberFormatException e) {
            throw new DebuggerException("Unknown debug port property format: " + debugPortProp);
        }
        
        int broadcastPort;
        try {
        	broadcastPort = Integer.parseInt(broadcastPortProp);
        } catch (NumberFormatException e) {
            throw new DebuggerException("Unknown broadcast port property format: " + broadcastPortProp);
        }
        
        boolean useSslEncryption;
        try {
        	useSslEncryption = Boolean.parseBoolean(useSslEncryptionProp);
        } catch (NumberFormatException e) {
            throw new DebuggerException("Unknown use SSL encryption property format: " + broadcastPortProp);
        }

        return new ZendDebugger(clientHostIP, debugPort, broadcastPort, useSslEncryption, debuggerCallback);
    }
    
}
