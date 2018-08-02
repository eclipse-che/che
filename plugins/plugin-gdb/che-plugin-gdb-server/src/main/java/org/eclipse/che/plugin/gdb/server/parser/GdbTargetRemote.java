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
package org.eclipse.che.plugin.gdb.server.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;

/**
 * 'target remote' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbTargetRemote {

  private static final Pattern GDB_TARGET_REMOTE =
      Pattern.compile("Remote debugging using (.*):(.*)\n.*");
  private static final Pattern CONNECTION_TIMED_OUT = Pattern.compile(".*Connection timed out.*");

  private final String host;
  private final String port;

  public GdbTargetRemote(String host, String port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  /** Factory method. */
  public static GdbTargetRemote parse(GdbOutput gdbOutput)
      throws GdbParseException, DebuggerException {
    String output = gdbOutput.getOutput();

    Matcher matcher = GDB_TARGET_REMOTE.matcher(output);
    if (matcher.find()) {
      String host = matcher.group(1);
      String port = matcher.group(2);
      return new GdbTargetRemote(host, port);
    } else if (CONNECTION_TIMED_OUT.matcher(output).find()) {
      throw new DebuggerException(output);
    }

    throw new GdbParseException(GdbTargetRemote.class, output);
  }
}
