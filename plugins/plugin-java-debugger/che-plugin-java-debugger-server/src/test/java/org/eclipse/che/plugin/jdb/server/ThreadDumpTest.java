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

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.ensureDebuggerSuspendAtLocation;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.startJavaDebugger;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.dto.ThreadStateDto;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.ThreadStatus;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.api.debugger.server.DtoConverter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class ThreadDumpTest {
  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> events = new ArrayBlockingQueue<>(10);

  @BeforeClass
  public void setUp() throws Exception {
    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/ThreadDumpTest.java", 27, false, null, "/test", null, -1);
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
  public void shouldGetThreadDumpWhenApplicationIsStopped() throws Exception {
    List<ThreadStateDto> threads =
        debugger.getThreadDump().stream().map(DtoConverter::asDto).collect(toList());

    validateMainThreadDump(threads);
    validateSomeThreadDump(threads);
    validateFinalizerThreadDump(threads);
  }

  @Test(priority = 1)
  public void shouldGetThreadDumpWhenApplicationIsRun() throws Exception {
    debugger.resume(new ResumeActionImpl());

    List<ThreadStateDto> threads =
        debugger.getThreadDump().stream().map(DtoConverter::asDto).collect(toList());

    for (ThreadState t : threads) {
      assertFalse(t.isSuspended());
      assertTrue(t.getFrames().isEmpty());
    }
  }

  private void validateMainThreadDump(List<ThreadStateDto> threads) {
    Optional<ThreadStateDto> mainThread =
        threads.stream().filter(t -> t.getName().equals("main")).findAny();
    assertTrue(mainThread.isPresent());

    ThreadState threadState = mainThread.get();
    assertEquals(threadState.getName(), "main");
    assertEquals(threadState.getGroupName(), "main");
    assertTrue(threadState.isSuspended());
    assertEquals(threadState.getStatus(), ThreadStatus.RUNNING);

    List<? extends StackFrameDump> frames = threadState.getFrames();
    assertEquals(frames.size(), 1);

    StackFrameDump stackFrameDump = frames.get(0);
    assertTrue(stackFrameDump.getVariables().isEmpty());
    assertTrue(stackFrameDump.getFields().isEmpty());

    Location location = stackFrameDump.getLocation();
    assertEquals(location.getLineNumber(), 27);
    assertEquals(location.getTarget(), "/test/src/org/eclipse/ThreadDumpTest.java");
    assertNull(location.getExternalResourceId());
    assertEquals(location.getResourceProjectPath(), "/test");

    Method method = location.getMethod();
    assertEquals(method.getName(), "main");
  }

  private void validateFinalizerThreadDump(List<ThreadStateDto> threads) {
    Optional<ThreadStateDto> finalizerThread =
        threads.stream().filter(t -> t.getName().equals("Finalizer")).findAny();
    assertTrue(finalizerThread.isPresent());

    ThreadState threadState = finalizerThread.get();
    assertEquals(threadState.getName(), "Finalizer");
    assertEquals(threadState.getGroupName(), "system");
    assertTrue(threadState.isSuspended());
    assertEquals(threadState.getStatus(), ThreadStatus.WAIT);

    List<? extends StackFrameDump> frames = threadState.getFrames();
    assertEquals(frames.size(), 4);

    StackFrameDump stackFrameDump = frames.get(0);
    assertTrue(stackFrameDump.getVariables().isEmpty());
    assertTrue(stackFrameDump.getFields().isEmpty());

    Location location = stackFrameDump.getLocation();
    assertEquals(location.getLineNumber(), -1);
    assertEquals(location.getTarget(), "java.lang.Object");
    assertEquals(location.getResourceProjectPath(), "/test");

    Method method = location.getMethod();
    assertEquals(method.getName(), "wait");
    assertTrue(method.getArguments().isEmpty());
  }

  private void validateSomeThreadDump(List<ThreadStateDto> threads) {
    Optional<ThreadStateDto> someThread =
        threads.stream().filter(t -> t.getName().equals("SomeThread")).findAny();
    assertTrue(someThread.isPresent());

    ThreadState threadState = someThread.get();
    assertEquals(threadState.getName(), "SomeThread");
    assertEquals(threadState.getGroupName(), "main");
    assertTrue(threadState.isSuspended());
    assertEquals(threadState.getStatus(), ThreadStatus.RUNNING);
  }
}
