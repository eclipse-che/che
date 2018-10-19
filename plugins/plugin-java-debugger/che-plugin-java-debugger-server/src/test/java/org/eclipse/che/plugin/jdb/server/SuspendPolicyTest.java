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
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.findMainThreadId;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.startJavaDebugger;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointConfigurationImpl;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test thread dump feature when none of threads are suspended.
 *
 * @author Anatolii Bazko
 */
public class SuspendPolicyTest {
  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> events = new ArrayBlockingQueue<>(10);

  @BeforeClass
  public void setUp() throws Exception {
    ProjectApiUtils.ensure();

    Location location =
        new LocationImpl("/test/src/org/eclipse/SuspendPolicyTest.java", 16, "/test");

    events = new ArrayBlockingQueue<>(10);
    debugger =
        startJavaDebugger(
            new BreakpointImpl(location, true, new BreakpointConfigurationImpl(SuspendPolicy.ALL)),
            events);

    ensureDebuggerSuspendAtLocation(location, events);
  }

  @AfterClass
  public void tearDown() throws Exception {
    terminateVirtualMachineQuietly(debugger);
  }

  @Test
  public void shouldReturnStackFrameDumpForAllThreads() throws Exception {
    for (ThreadState threadState : debugger.getThreadDump()) {
      assertTrue(threadState.isSuspended());
    }
  }

  @Test(priority = 1)
  public void shouldReturnStackFrameDumpOnlyForSuspendedThread() throws Exception {
    debugger.addBreakpoint(
        new BreakpointImpl(
            new LocationImpl("/test/src/org/eclipse/SuspendPolicyTest.java", 17, "/test"),
            true,
            new BreakpointConfigurationImpl(SuspendPolicy.THREAD)));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    debugger.resume(new ResumeActionImpl());

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    long mainThreadId = findMainThreadId(debugger);
    for (ThreadState threadState : debugger.getThreadDump()) {
      if (threadState.getId() == mainThreadId) {
        assertTrue(threadState.isSuspended());
      } else {
        assertFalse(threadState.isSuspended());
      }
    }
  }

  @Test(priority = 3)
  public void allThreadsShouldBeResumedWhenApplicationIsRun() throws Exception {
    debugger.resume(new ResumeActionImpl());

    for (ThreadState threadState : debugger.getThreadDump()) {
      assertFalse(threadState.isSuspended());
    }
  }
}
