/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.server;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/** @author Anatoliy Bazko */
public class JavaDebuggerFactory implements DebuggerFactory {

  private static final String TYPE = "jdb";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Debugger create(Map<String, String> properties, Debugger.DebuggerCallback debuggerCallback)
      throws DebuggerException {
    Map<String, String> normalizedProps =
        properties
            .entrySet()
            .stream()
            .collect(toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

    String host = normalizedProps.get("host");
    if (host == null) {
      throw new DebuggerException("Can't establish connection: host property is unknown.");
    }

    String portProp = normalizedProps.get("port");
    if (portProp == null) {
      throw new DebuggerException("Can't establish connection: port property is unknown.");
    }

    int port;
    try {
      port = Integer.parseInt(portProp);
    } catch (NumberFormatException e) {
      throw new DebuggerException("Unknown port property format: " + portProp);
    }

    return new JavaDebugger(host, port, debuggerCallback);
  }
}
