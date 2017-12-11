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
package org.eclipse.che.plugin.jdb.server;

import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.ensureDebuggerSuspendAtLocation;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.startJavaDebugger;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.DisconnectEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointConfigurationImpl;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class BreakpointConditionTest {

  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> events;

  @BeforeClass
  public void setUp() throws Exception {
    ProjectApiUtils.ensure();

    Location location =
        new LocationImpl("/test/src/org/eclipse/BreakpointsByConditionTest.java", 18, "/test");

    events = new ArrayBlockingQueue<>(10);
    debugger = startJavaDebugger(new BreakpointImpl(location), events);

    ensureDebuggerSuspendAtLocation(location, events);
  }

  @AfterClass
  public void tearDown() throws Exception {
    if (debugger != null) {
      terminateVirtualMachineQuietly(debugger);
    }
  }

  @Test
  public void shouldStopByHitCount() throws Exception {
    debugger.addBreakpoint(
        new BreakpointImpl(
            new LocationImpl("/test/src/org/eclipse/BreakpointsByConditionTest.java", 20, "/test"),
            true,
            new BreakpointConfigurationImpl(3)));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    debugger.resume(new ResumeActionImpl());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    assertEquals("2", debugger.evaluate("i"));
    assertEquals("2", debugger.evaluate("k"));
  }

  @Test(priority = 1)
  public void shouldStopByCondition() throws Exception {
    Breakpoint breakpoint =
        new BreakpointImpl(
            new LocationImpl("/test/src/org/eclipse/BreakpointsByConditionTest.java", 25, "/test"),
            true,
            new BreakpointConfigurationImpl("i==5"));

    debugger.addBreakpoint(breakpoint);

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    debugger.resume(new ResumeActionImpl());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    assertEquals("5", debugger.evaluate("i"));
    assertEquals("4", debugger.evaluate("k"));

    debugger.resume(new ResumeActionImpl());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof DisconnectEvent);
  }
}
