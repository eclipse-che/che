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

import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.ensureSuspendAtDesiredLocation;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.startJavaDebugger;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class BreakpointsTest {

  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> events;

  @BeforeClass
  public void setUp() throws Exception {
    ProjectApiUtils.ensure();

    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/BreakpointsTest.java", 20, false, -1, "/test", null, -1);

    events = new ArrayBlockingQueue<>(10);
    debugger = startJavaDebugger(new BreakpointImpl(location), events);

    ensureSuspendAtDesiredLocation(location, events);
  }

  @AfterClass
  public void tearDown() throws Exception {
    if (debugger != null) {
      terminateVirtualMachineQuietly(debugger);
    }
  }

  @Test
  public void shouldAddBreakpointInsideMethod() throws Exception {
    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/BreakpointsTest.java", 36, false, -1, "/test", null, -1);

    try {
      debugger.addBreakpoint(new BreakpointImpl(location, false, null));
    } catch (DebuggerException e) {
      // class might not be loaded yet
    }

    debugger.resume(new ResumeActionImpl());

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 36);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointsTest.java");
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 36);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointsTest.java");
  }

  @Test(priority = 1)
  public void shouldAddBreakpointByFqn() throws Exception {
    Location location =
        new LocationImpl("org.eclipse.BreakpointsTest", 21, false, -1, "/test", null, -1);

    try {
      debugger.addBreakpoint(new BreakpointImpl(location, false, null));
    } catch (DebuggerException e) {
      // class might not be loaded yet
    }

    debugger.resume(new ResumeActionImpl());

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 21);
    assertEquals(actualLocation.getTarget(), "org.eclipse.BreakpointsTest");
    assertEquals(actualLocation.getResourceProjectPath(), "/test");
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 21);
    assertEquals(actualLocation.getTarget(), "org.eclipse.BreakpointsTest");
    assertEquals(actualLocation.getResourceProjectPath(), "/test");
  }

  @Test(priority = 2)
  public void shouldAddBreakpointInsideInnerClass() throws Exception {
    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/BreakpointsTest.java", 41, false, -1, "/test", null, -1);

    try {
      debugger.addBreakpoint(new BreakpointImpl(location, false, null));
    } catch (DebuggerException e) {
      // class might not be loaded yet
    }

    debugger.resume(new ResumeActionImpl());

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 41);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointsTest.java");
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 41);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointsTest.java");
  }

  @Test(priority = 3)
  public void shouldAddBreakpointInsideAnonymousClass() throws Exception {
    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/BreakpointsTest.java", 25, false, -1, "/test", null, -1);

    try {
      debugger.addBreakpoint(new BreakpointImpl(location, false, null));
    } catch (DebuggerException e) {
      // class might not be loaded yet
    }

    debugger.resume(new ResumeActionImpl());

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 25);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointsTest.java");
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 25);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointsTest.java");
  }

  @Test(priority = 4)
  public void shouldAddBreakpointInsideLambdaFunction() throws Exception {
    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/BreakpointsTest.java", 31, false, -1, "/test", null, -1);

    try {
      debugger.addBreakpoint(new BreakpointImpl(location, false, null));
    } catch (DebuggerException e) {
      // class might not be loaded yet
    }

    debugger.resume(new ResumeActionImpl());

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 31);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointsTest.java");
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 31);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointsTest.java");
  }

  @Test(priority = 4)
  public void shouldRemoveAllBreakpoints() throws Exception {
    assertFalse(debugger.getAllBreakpoints().isEmpty());

    debugger.deleteAllBreakpoints();

    assertTrue(debugger.getAllBreakpoints().isEmpty());
  }

  @Test(priority = 5)
  public void shouldRemoveBreakpoint() throws Exception {
    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/BreakpointsTest.java", 36, false, -1, "/test", null, -1);

    debugger.addBreakpoint(new BreakpointImpl(location, false, null));
    assertEquals(debugger.getAllBreakpoints().size(), 1);

    debugger.deleteBreakpoint(location);
    assertTrue(debugger.getAllBreakpoints().isEmpty());
  }

  @Test(priority = 6)
  public void shouldReturnAllBreakpoints() throws Exception {
    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/BreakpointsTest.java", 36, false, -1, "/test", null, -1);
    debugger.addBreakpoint(new BreakpointImpl(location, false, null));

    List<Breakpoint> breakpoints = debugger.getAllBreakpoints();
    assertEquals(breakpoints.size(), 1);

    Breakpoint actualBreakpoint = breakpoints.get(0);
    Location actualLocation = actualBreakpoint.getLocation();

    assertEquals(actualLocation.getLineNumber(), 36);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointsTest.java");
    assertEquals(actualLocation.getResourceProjectPath(), "/test");
    assertTrue(actualBreakpoint.isEnabled());
  }

  @Test(priority = 7, expectedExceptions = DebuggerException.class)
  public void shouldNotAddBreakpointToCommentedLine() throws Exception {
    Location location = new LocationImpl("/test/src/org/eclipse/BreakpointsTest.java", 2, "/test");
    debugger.addBreakpoint(new BreakpointImpl(location));
  }

  @Test(priority = 8, expectedExceptions = DebuggerException.class)
  public void shouldNotAddBreakpointToNonExecutedLine() throws Exception {
    Location location = new LocationImpl("/test/src/org/eclipse/BreakpointsTest.java", 43, "/test");
    debugger.addBreakpoint(new BreakpointImpl(location));
  }
}
