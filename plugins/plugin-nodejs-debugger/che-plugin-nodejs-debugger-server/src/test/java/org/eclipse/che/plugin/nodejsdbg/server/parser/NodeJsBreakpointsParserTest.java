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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Anatolii Bazko
 * @author Igor Vinokur
 */
public class NodeJsBreakpointsParserTest {

  private NodeJsBreakpointsParser parser;

  @BeforeMethod
  public void setUp() {
    parser = new NodeJsBreakpointsParser();
  }

  @Test
  public void shouldParseNoBreakpointsOutput() {
    NodeJsOutput nodeJsOutput = NodeJsOutput.of("No breakpoints yet");

    assertTrue(parser.match(nodeJsOutput));

    List<Breakpoint> breakpoints = parser.parse(nodeJsOutput).getAll();
    assertTrue(breakpoints.isEmpty());
  }

  @Test
  public void shouldParseBreakpoint() {
    NodeJsOutput nodeJsOutput = NodeJsOutput.of("#0 node/app/app.js:7");

    assertTrue(parser.match(nodeJsOutput));

    List<Breakpoint> breakpoints = parser.parse(nodeJsOutput).getAll();
    assertEquals(breakpoints.size(), 1);

    Breakpoint breakpoint = breakpoints.get(0);
    assertEquals(breakpoint.getLocation().getLineNumber(), 7);
    assertEquals(breakpoint.getLocation().getTarget(), "node/app/app.js");
    assertNotNull(breakpoint.getBreakpointConfiguration());
    assertTrue(breakpoint.isEnabled());
  }

  @Test
  public void shouldParseSeveralBreakpoints() {
    NodeJsOutput nodeJsOutput = NodeJsOutput.of("#0 node/app/app.js:1\n#1 node/app/app.js:7");

    assertTrue(parser.match(nodeJsOutput));

    List<Breakpoint> breakpoints = parser.parse(nodeJsOutput).getAll();
    assertEquals(breakpoints.size(), 2);

    Breakpoint breakpoint1 = breakpoints.get(0);
    assertEquals(breakpoint1.getLocation().getLineNumber(), 1);
    assertEquals(breakpoint1.getLocation().getTarget(), "node/app/app.js");
    assertNotNull(breakpoint1.getBreakpointConfiguration());
    assertTrue(breakpoint1.isEnabled());

    Breakpoint breakpoint2 = breakpoints.get(1);
    assertEquals(breakpoint2.getLocation().getLineNumber(), 7);
    assertEquals(breakpoint2.getLocation().getTarget(), "node/app/app.js");
    assertNotNull(breakpoint2.getBreakpointConfiguration());
    assertTrue(breakpoint2.isEnabled());
  }
}
