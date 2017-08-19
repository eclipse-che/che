/**
 * ***************************************************************************** Copyright (c)
 * 2012-2017 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.jdb.server;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.dto.ThreadDumpDto;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.ThreadStatus;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.debugger.server.DtoConverter;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
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

    initJavaDebugger();
    ensureSuspendAtDesiredLocation();
  }

  @AfterClass
  public void tearDown() throws Exception {
    terminateVirtualMachineQuietly(debugger);
  }

  @Test
  public void shouldGetThreadDump() throws Exception {
    List<ThreadDumpDto> threads =
        debugger.getThreadDumps().stream().map(DtoConverter::asDto).collect(toList());

    validateMainThreadDump(threads);
    validateSomeThreadDump(threads);
    validateFinalizerThreadDump(threads);
  }

  private void validateMainThreadDump(List<ThreadDumpDto> threads) {
    Optional<ThreadDumpDto> mainThread =
        threads.stream().filter(t -> t.getName().equals("main")).findAny();
    assertTrue(mainThread.isPresent());

    ThreadDump threadDump = mainThread.get();
    assertEquals(threadDump.getName(), "main");
    assertEquals(threadDump.getGroupName(), "main");
    assertTrue(threadDump.isSuspended());
    assertEquals(threadDump.getStatus(), ThreadStatus.RUNNING);

    List<? extends StackFrameDump> frames = threadDump.getFrames();
    assertEquals(frames.size(), 1);

    StackFrameDump stackFrameDump = frames.get(0);
    assertTrue(stackFrameDump.getVariables().isEmpty());
    assertTrue(stackFrameDump.getFields().isEmpty());

    Location location = stackFrameDump.getLocation();
    assertEquals(location.getLineNumber(), 26);
    assertEquals(location.getTarget(), "org.eclipse.ThreadDumpTest1");
    assertEquals(location.getExternalResourceId(), -1);
    assertEquals(location.getResourceProjectPath(), "/test");
    assertEquals(location.getResourcePath(), "/test/src/org/eclipse/ThreadDumpTest1.java");

    Method method = location.getMethod();
    assertEquals(method.getName(), "main");
    assertTrue(method.getArguments().isEmpty());
  }

  private void validateFinalizerThreadDump(List<ThreadDumpDto> threads) {
    Optional<ThreadDumpDto> finalizerThread =
        threads.stream().filter(t -> t.getName().equals("Finalizer")).findAny();
    assertTrue(finalizerThread.isPresent());

    ThreadDump threadDump = finalizerThread.get();
    assertEquals(threadDump.getName(), "Finalizer");
    assertEquals(threadDump.getGroupName(), "system");
    assertTrue(threadDump.isSuspended());
    assertEquals(threadDump.getStatus(), ThreadStatus.WAIT);

    List<? extends StackFrameDump> frames = threadDump.getFrames();
    assertEquals(frames.size(), 4);

    StackFrameDump stackFrameDump = frames.get(0);
    assertTrue(stackFrameDump.getVariables().isEmpty());
    assertTrue(stackFrameDump.getFields().isEmpty());

    Location location = stackFrameDump.getLocation();
    assertEquals(location.getLineNumber(), -1);
    assertEquals(location.getTarget(), "java.lang.Object");
    assertNull(location.getResourceProjectPath());
    assertNull(location.getResourcePath());

    Method method = location.getMethod();
    assertEquals(method.getName(), "wait");
    assertTrue(method.getArguments().isEmpty());
  }

  private void validateSomeThreadDump(List<ThreadDumpDto> threads) {
    Optional<ThreadDumpDto> someThread =
        threads.stream().filter(t -> t.getName().equals("SomeThread")).findAny();
    assertTrue(someThread.isPresent());

    ThreadDump threadDump = someThread.get();
    assertEquals(threadDump.getName(), "SomeThread");
    assertEquals(threadDump.getGroupName(), "main");
    assertTrue(threadDump.isSuspended());
    assertEquals(threadDump.getStatus(), ThreadStatus.RUNNING);

    List<? extends StackFrameDump> frames = threadDump.getFrames();
    assertEquals(frames.size(), 1);

    StackFrameDump stackFrameDump = frames.get(0);
    assertTrue(stackFrameDump.getVariables().isEmpty());
    assertTrue(stackFrameDump.getFields().isEmpty());

    Location location = stackFrameDump.getLocation();
    assertEquals(location.getLineNumber(), 41);
    assertEquals(location.getTarget(), "org.eclipse.ThreadDumpTest1$SomeThread");
    assertEquals(location.getExternalResourceId(), -1);
    assertEquals(location.getResourceProjectPath(), "/test");
    assertEquals(location.getResourcePath(), "/test/src/org/eclipse/ThreadDumpTest1.java");

    Method method = location.getMethod();
    assertEquals(method.getName(), "run");
    assertTrue(method.getArguments().isEmpty());
  }

  private void initJavaDebugger() throws DebuggerException, InterruptedException {
    debugger = new JavaDebugger("localhost", parseInt(getProperty("debug.port")), events::add);

    BreakpointImpl breakpoint =
        new BreakpointImpl(new LocationImpl("org.eclipse.ThreadDumpTest1", 26));
    debugger.start(new StartActionImpl(Collections.singletonList(breakpoint)));
  }

  private void ensureSuspendAtDesiredLocation() throws InterruptedException {
    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    SuspendEvent suspendEvent = (SuspendEvent) debuggerEvent;
    assertEquals(suspendEvent.getLocation().getLineNumber(), 26);
  }
}
