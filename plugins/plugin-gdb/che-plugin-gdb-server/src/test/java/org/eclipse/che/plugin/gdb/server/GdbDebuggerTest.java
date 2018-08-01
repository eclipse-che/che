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
package org.eclipse.che.plugin.gdb.server;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.DisconnectEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOutActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOverActionImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
public class GdbDebuggerTest {

  private String file;
  private Path sourceDirectory;
  private Debugger gdbDebugger;
  private BlockingQueue<DebuggerEvent> events;

  @BeforeClass
  public void beforeClass() throws Exception {
    file = GdbTest.class.getResource("/hello").getFile();
    sourceDirectory = Paths.get(GdbTest.class.getResource("/h.cpp").getFile()).getParent();
    events = new ArrayBlockingQueue<>(10);
  }

  @Test
  public void testDebugger() throws Exception {
    initializeDebugger();
    addBreakpoint();
    startDebugger();
    doSetAndGetValues();
    // stepInto();
    stepOver();
    stepOut();
    resume();
    deleteAllBreakpoints();
    disconnect();
  }

  private void deleteAllBreakpoints() throws DebuggerException {
    List<Breakpoint> breakpoints = gdbDebugger.getAllBreakpoints();
    assertEquals(breakpoints.size(), 1);

    gdbDebugger.deleteAllBreakpoints();

    breakpoints = gdbDebugger.getAllBreakpoints();
    assertTrue(breakpoints.isEmpty());
  }

  private void resume() throws DebuggerException, InterruptedException {
    gdbDebugger.resume(new ResumeActionImpl());

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    SuspendEvent suspendEvent = (SuspendEvent) debuggerEvent;
    assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
    assertEquals(suspendEvent.getLocation().getLineNumber(), 7);
  }

  private void stepOut() throws DebuggerException, InterruptedException {
    try {
      gdbDebugger.stepOut(new StepOutActionImpl(SuspendPolicy.ALL));
    } catch (DebuggerException e) {
      // ignore
    }

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);
  }

  private void stepOver() throws DebuggerException, InterruptedException {
    gdbDebugger.stepOver(new StepOverActionImpl(SuspendPolicy.ALL));

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    SuspendEvent suspendEvent = (SuspendEvent) debuggerEvent;
    assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
    assertEquals(suspendEvent.getLocation().getLineNumber(), 5);

    gdbDebugger.stepOver(new StepOverActionImpl(SuspendPolicy.ALL));

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    suspendEvent = (SuspendEvent) debuggerEvent;
    assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
    assertEquals(suspendEvent.getLocation().getLineNumber(), 6);

    gdbDebugger.stepOver(new StepOverActionImpl(SuspendPolicy.ALL));

    debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    suspendEvent = (SuspendEvent) debuggerEvent;
    assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
    assertEquals(suspendEvent.getLocation().getLineNumber(), 7);
  }

  private void doSetAndGetValues() throws DebuggerException {
    VariablePath variablePath = new VariablePathImpl("i");
    Variable variable = new VariableImpl("int", "i", new SimpleValueImpl("2"), true, variablePath);

    SimpleValue value = gdbDebugger.getValue(variablePath);
    assertEquals(value.getString(), "0");

    gdbDebugger.setValue(variable);

    value = gdbDebugger.getValue(variablePath);

    assertEquals(value.getString(), "2");

    String expression = gdbDebugger.evaluate("i");
    assertEquals(expression, "2");

    expression = gdbDebugger.evaluate("10 + 10");
    assertEquals(expression, "20");

    StackFrameDump stackFrameDump = gdbDebugger.dumpStackFrame();
    assertTrue(stackFrameDump.getFields().isEmpty());
    assertEquals(stackFrameDump.getVariables().size(), 1);
    assertEquals(stackFrameDump.getVariables().get(0).getName(), "i");
    assertEquals(stackFrameDump.getVariables().get(0).getValue().getString(), "2");
    assertEquals(stackFrameDump.getVariables().get(0).getType(), "int");
  }

  private void startDebugger() throws DebuggerException, InterruptedException {
    gdbDebugger.start(new StartActionImpl(Collections.emptyList()));

    assertEquals(events.size(), 1);

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof SuspendEvent);

    SuspendEvent suspendEvent = (SuspendEvent) debuggerEvent;
    assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
    assertEquals(suspendEvent.getLocation().getLineNumber(), 7);
  }

  private void disconnect() throws DebuggerException, InterruptedException {
    gdbDebugger.disconnect();

    assertEquals(events.size(), 1);

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof DisconnectEvent);
  }

  private void addBreakpoint() throws DebuggerException, InterruptedException {
    Location location = new LocationImpl("h.cpp", 7);
    Breakpoint breakpoint = new BreakpointImpl(location);

    gdbDebugger.addBreakpoint(breakpoint);

    assertEquals(events.size(), 1);

    DebuggerEvent debuggerEvent = events.take();
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

    BreakpointActivatedEvent breakpointActivatedEvent = (BreakpointActivatedEvent) debuggerEvent;
    assertEquals(breakpointActivatedEvent.getBreakpoint().getLocation().getTarget(), "h.cpp");
    assertEquals(breakpointActivatedEvent.getBreakpoint().getLocation().getLineNumber(), 7);
  }

  private void initializeDebugger() throws DebuggerException {
    final String gdbPort = System.getProperty("debug.port");

    Map<String, String> properties =
        ImmutableMap.of(
            "host",
            "localhost",
            "port",
            gdbPort,
            "binary",
            file,
            "sources",
            sourceDirectory.toString());

    GdbDebuggerFactory gdbDebuggerFactory = new GdbDebuggerFactory();
    gdbDebugger = gdbDebuggerFactory.create(properties, events::add);

    DebuggerInfo debuggerInfo = gdbDebugger.getInfo();

    assertEquals(debuggerInfo.getFile(), file);
    assertEquals(debuggerInfo.getHost(), "localhost");
    assertEquals(debuggerInfo.getPort(), Integer.parseInt(gdbPort));
    assertNotNull(debuggerInfo.getName());
    assertNotNull(debuggerInfo.getVersion());
  }
}
