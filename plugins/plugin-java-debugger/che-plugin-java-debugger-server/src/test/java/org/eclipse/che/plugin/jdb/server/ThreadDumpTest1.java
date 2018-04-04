/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.server;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.ensureSuspendAtDesiredLocation;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
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
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.debugger.server.DtoConverter;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test ThreadDump when all threads are suspended.
 *
 * @author Anatolii Bazko
 */
public class ThreadDumpTest1 {
  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> events = new ArrayBlockingQueue<>(10);

  @BeforeClass
  public void setUp() throws Exception {
    ProjectApiUtils.ensure();

    debugger = new JavaDebugger("localhost", parseInt(getProperty("debug.port")), events::add);
    Location location =
        new LocationImpl(
            "/test/src/org/eclipse/ThreadDumpTest1.java", 26, false, -1, "/test", null, -1);
    BreakpointImpl breakpoint = new BreakpointImpl(location);

    debugger.start(new StartActionImpl(Collections.singletonList(breakpoint)));
    ensureSuspendAtDesiredLocation(location, events);
  }

  @AfterClass
  public void tearDown() throws Exception {
    if (debugger != null) {
      terminateVirtualMachineQuietly(debugger);
    }
  }

  @Test
  public void shouldGetThreadDump() throws Exception {
    List<ThreadStateDto> threads =
        debugger.getThreadDump().stream().map(DtoConverter::asDto).collect(toList());

    validateMainThreadDump(threads);
    validateSomeThreadDump(threads);
    validateFinalizerThreadDump(threads);
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
    assertEquals(location.getLineNumber(), 26);
    assertEquals(location.getTarget(), "/test/src/org/eclipse/ThreadDumpTest1.java");
    assertEquals(location.getExternalResourceId(), -1);
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
    assertNull(location.getResourceProjectPath());

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
