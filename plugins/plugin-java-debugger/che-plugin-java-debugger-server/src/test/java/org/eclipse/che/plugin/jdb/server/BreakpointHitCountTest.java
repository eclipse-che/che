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
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Igor Vinokur */
public class BreakpointHitCountTest {

  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> events;

  @BeforeClass
  public void setUp() throws Exception {
    ProjectApiUtils.ensure();

    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/BreakpointHitCountTest.java", 21, false, -1, "/test", null, -1);

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
    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/BreakpointHitCountTest.java", 22, false, -1, "/test", null, -1);

    try {
      debugger.addBreakpoint(
          new BreakpointImpl(location, new BreakpointConfigurationImpl(null, 4), true));
    } catch (DebuggerException e) {
      // class might not be loaded yet
    }

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    Breakpoint actualBreakpoint = ((BreakpointActivatedEvent) debuggerEvent).getBreakpoint();
    Location actualLocation = actualBreakpoint.getLocation();
    assertEquals(actualLocation.getLineNumber(), 22);
    assertEquals(actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointHitCountTest.java");
    assertTrue(actualBreakpoint.isEnabled());

    for (int i = 0; i < 5; i++) {
      debugger.resume(new ResumeActionImpl());

      debuggerEvent = events.take();
      if (i == 4) {
        assertTrue(debuggerEvent instanceof DisconnectEvent);
      } else {
        assertTrue(debuggerEvent instanceof SuspendEvent);

        Location suspendLocation = ((SuspendEvent) debuggerEvent).getLocation();
        assertEquals(suspendLocation.getLineNumber(), 22);
        assertEquals(
            actualLocation.getTarget(), "/test/src/org/eclipse/BreakpointHitCountTest.java");
      }
    }
  }
}
