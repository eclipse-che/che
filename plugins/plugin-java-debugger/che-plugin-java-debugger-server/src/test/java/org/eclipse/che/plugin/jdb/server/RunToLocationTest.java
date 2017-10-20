/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.server;

import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.*;
import static org.testng.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointConfigurationImpl;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.RunToLocationActionImpl;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Igor Vinokur */
public class RunToLocationTest {

  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> events;

  @BeforeClass
  public void setUp() throws Exception {
    ProjectApiUtils.ensure();

    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/RunToLocationTest.java", 19, false, -1, "/test", null, -1);

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
  public void shouldRunToLocationInsideClass() throws Exception {
    debugger.runToLocation(
        new RunToLocationActionImpl("/test/src/org/eclipse/RunToLocationTest.java", 20));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 20);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/RunToLocationTest.java");
    assertTrue(actualBreakpoint.getBreakpointConfiguration().getHitCount() == -1);
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 20);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/RunToLocationTest.java");
  }

  @Test(priority = 1)
  public void shouldNotSuspendOnTemporaryBreakPointIfUsualBreakpointWasActivated()
      throws Exception {
    Location location1 =
        new LocationImpl(
            "/test/src/org/eclipse/RunToLocationTest.java", 21, false, -1, "/test", null, -1);

    Location location2 =
        new LocationImpl(
            "/test/src/org/eclipse/RunToLocationTest.java", 23, false, -1, "/test", null, -1);
    debugger.addBreakpoint(
        new BreakpointImpl(location1, new BreakpointConfigurationImpl(null, 0), false));
    debugger.addBreakpoint(
        new BreakpointImpl(location2, new BreakpointConfigurationImpl(null, 0), false));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 21);
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 23);
    assertTrue(actualBreakpoint.isEnabled());

    debugger.runToLocation(
        new RunToLocationActionImpl("/test/src/org/eclipse/RunToLocationTest.java", 22));

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 22);
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 21);

    debugger.resume(new ResumeActionImpl());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 23);
  }

  @Test(priority = 2)
  public void shouldRunToLocationInsideMethod() throws Exception {
    debugger.runToLocation(
        new RunToLocationActionImpl("/test/src/org/eclipse/RunToLocationTest.java", 40));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 40);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/RunToLocationTest.java");
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 40);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/RunToLocationTest.java");
  }

  @Test(priority = 3)
  public void shouldRunToLocationByFqn() throws Exception {
    debugger.runToLocation(new RunToLocationActionImpl("org.eclipse.RunToLocationTest", 25));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 25);
    assertEquals(actualLocation.getTarget(), "org.eclipse.RunToLocationTest");
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 25);
    assertEquals(actualLocation.getTarget(), "org.eclipse.RunToLocationTest");
  }

  @Test(priority = 4)
  public void shouldRunToLocationInsideInnerClass() throws Exception {
    debugger.runToLocation(
        new RunToLocationActionImpl("/test/src/org/eclipse/RunToLocationTest.java", 45));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 45);
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 45);
  }

  @Test(priority = 5)
  public void shouldRunToLocationInsideAnonymousClass() throws Exception {
    debugger.runToLocation(
        new RunToLocationActionImpl("/test/src/org/eclipse/RunToLocationTest.java", 30));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 30);
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 30);
  }

  @Test(priority = 6)
  public void shouldRunToLocationInsideLambdaFunction() throws Exception {
    debugger.runToLocation(
        new RunToLocationActionImpl("/test/src/org/eclipse/RunToLocationTest.java", 36));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 36);
    assertTrue(actualBreakpoint.isEnabled());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
    assertEquals(suspendLocation.getLineNumber(), 36);
  }
}
