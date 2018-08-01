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
package org.eclipse.che.plugin.nodejsdbg.server.command;

import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Future;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsDebugProcess;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerParseException;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsOutputParser;

/**
 * Basic implementation of {@link NodeJsDebugCommand}. When command is executed, it scans outputs
 * until appropriate found, parses it and returns result of execution.
 *
 * @author Anatolii Bazko
 */
public class NodeJsDebugCommandImpl<T> implements NodeJsDebugCommand<T> {

  private final SettableFuture<T> result;
  public final NodeJsOutputParser<T> parser;
  private final String input;

  public NodeJsDebugCommandImpl(NodeJsOutputParser<T> parser, String input) {
    this.parser = parser;
    this.input = input;
    this.result = SettableFuture.create();
  }

  @Override
  public Future<T> execute(NodeJsDebugProcess process) throws NodeJsDebuggerException {
    process.send(input);
    return result;
  }

  @Override
  public boolean onOutputProduced(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
    if (parser.match(nodeJsOutput)) {
      T t = parser.parse(nodeJsOutput);
      return result.set(t);
    }

    return false;
  }
}
