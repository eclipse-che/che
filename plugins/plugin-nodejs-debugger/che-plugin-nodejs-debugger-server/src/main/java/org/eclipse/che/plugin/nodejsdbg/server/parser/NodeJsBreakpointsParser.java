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
package org.eclipse.che.plugin.nodejsdbg.server.parser;

import static java.lang.Integer.valueOf;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;

/**
 * {@code breakpoints} command parser.
 *
 * @author Anatoliy Bazko
 * @author Igor Vinokur
 */
public class NodeJsBreakpointsParser
    implements NodeJsOutputParser<NodeJsBreakpointsParser.Breakpoints> {

  public static final NodeJsBreakpointsParser INSTANCE = new NodeJsBreakpointsParser();
  public static final String NO_BREAKPOINTS_MESSAGE = "No breakpoints yet";

  @Override
  public boolean match(NodeJsOutput nodeJsOutput) {
    String output = nodeJsOutput.getOutput();
    return output.startsWith("#0 ") || output.equals(NO_BREAKPOINTS_MESSAGE);
  }

  @Override
  public Breakpoints parse(NodeJsOutput nodeJsOutput) {
    final List<Breakpoint> breakpoints = new ArrayList<>();

    if (!nodeJsOutput.getOutput().equals(NO_BREAKPOINTS_MESSAGE)) {
      for (String item : nodeJsOutput.getOutput().split("\n")) {
        breakpoints.add(
            new BreakpointImpl(
                new LocationImpl(
                    item.substring(item.indexOf(" ") + 1, item.indexOf(":")),
                    valueOf(item.substring(item.indexOf(":") + 1)))));
      }
    }

    return new Breakpoints(breakpoints);
  }

  public static class Breakpoints {
    private final List<Breakpoint> breakpoints;

    private Breakpoints(List<Breakpoint> breakpoints) {
      this.breakpoints = breakpoints;
    }

    public List<Breakpoint> getAll() {
      return breakpoints;
    }
  }
}
