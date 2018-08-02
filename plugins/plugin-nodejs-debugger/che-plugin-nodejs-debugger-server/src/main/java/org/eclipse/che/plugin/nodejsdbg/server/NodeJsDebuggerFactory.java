/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.nodejsdbg.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toMap;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/**
 * Factory to create nodejs debugger instance. Allowed the following connection properties:
 *
 * <p>uri - connects to the process via the URI such as localhost:5858 pid - connects to the process
 * via the pid script - entrypoint to start debugger
 *
 * @author Anatoliy Bazko
 */
public class NodeJsDebuggerFactory implements DebuggerFactory {
  private static final String TYPE = "nodejsdbg";

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

    Integer pid = null;
    URI uri = null;

    String pidStr = normalizedProps.get("pid");
    if (!isNullOrEmpty(pidStr)) {
      try {
        pid = Integer.valueOf(pidStr);
      } catch (NumberFormatException e) {
        throw new DebuggerException(
            String.format("Illegal 'pid' format %s. Debugger can't be started.", pidStr));
      }
    }

    String uriStr = normalizedProps.get("uri");
    if (!isNullOrEmpty(uriStr)) {
      try {
        uri = URI.create(uriStr);
      } catch (IllegalArgumentException e) {
        throw new DebuggerException(
            String.format("Illegal 'uri' format %s. Debugger can't be started.", uriStr));
      }
    }

    String script = normalizedProps.get("script");
    if (!isNullOrEmpty(script) && !Files.exists(Paths.get(script))) {
      throw new DebuggerException(
          String.format("Script '%s' to debug not found. Debugger can't be started.", script));
    }

    if (isNullOrEmpty(pidStr) && isNullOrEmpty(uriStr) && isNullOrEmpty(script)) {
      throw new DebuggerException(
          "Unrecognized debug connection options. Allowed only: pid, uri or script.");
    }

    return NodeJsDebugger.newInstance(pid, uri, script, debuggerCallback);
  }
}
