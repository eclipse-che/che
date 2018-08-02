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
package org.eclipse.che.plugin.nodejsdbg.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.DisconnectEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepIntoActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOutActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOverActionImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class NodeJsDebuggerTest {
  private NodeJsDebugger debugger;
  private Debugger.DebuggerCallback callback;

  @BeforeMethod
  public void setUp() throws Exception {
    String file = NodeJsDebuggerTest.class.getResource("/app.js").getFile();

    callback = mock(Debugger.DebuggerCallback.class);
    debugger = NodeJsDebugger.newInstance(null, null, file, callback);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    debugger.disconnect();
  }

  @Test
  public void testGetInfo() throws Exception {
    DebuggerInfo info = debugger.getInfo();

    assertTrue(info.getFile().endsWith("app.js"));
    assertTrue(!isNullOrEmpty(info.getVersion()));
    assertTrue(info.getName().equals("'node'") || info.getName().equals("'nodejs'"));
  }

  @Test
  public void testManageBreakpoints() throws Exception {
    List<Breakpoint> breakpoints = debugger.getAllBreakpoints();
    assertEquals(breakpoints.size(), 1);

    debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("app.js", 2)));

    ArgumentCaptor<BreakpointActivatedEvent> breakpointActivated =
        ArgumentCaptor.forClass(BreakpointActivatedEvent.class);
    verify(callback).onEvent(breakpointActivated.capture());
    BreakpointActivatedEvent event = breakpointActivated.getValue();
    Breakpoint breakpoint = event.getBreakpoint();
    assertEquals(breakpoint.getLocation().getTarget(), "app.js");
    assertEquals(breakpoint.getLocation().getLineNumber(), 2);

    debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("app.js", 5)));

    breakpoints = debugger.getAllBreakpoints();
    assertEquals(breakpoints.size(), 3);

    debugger.deleteBreakpoint(new LocationImpl("app.js", 2));
    breakpoints = debugger.getAllBreakpoints();
    assertEquals(breakpoints.size(), 2);

    debugger.deleteAllBreakpoints();
    breakpoints = debugger.getAllBreakpoints();
    assertEquals(breakpoints.size(), 1);
  }

  @Test
  public void testEvaluation() throws Exception {
    String result = debugger.evaluate("2+2");
    assertEquals(result, "4");

    result = debugger.evaluate("console.log('hello')");
    assertEquals(result, "< hello");

    result = debugger.evaluate("var y=1");
    assertEquals(result, "undefined");
  }

  @Test
  public void testOver() throws Exception {
    debugger.stepOver(new StepOverActionImpl(SuspendPolicy.ALL));

    ArgumentCaptor<SuspendEvent> suspendEventCaptor = ArgumentCaptor.forClass(SuspendEvent.class);
    verify(callback, timeout(1000)).onEvent(suspendEventCaptor.capture());
    SuspendEvent suspendEvent = suspendEventCaptor.getValue();
    assertEquals(suspendEvent.getLocation().getLineNumber(), 2);
    assertTrue(suspendEvent.getLocation().getTarget().endsWith("app.js"));
  }

  @Test
  public void testIntoAndOut() throws Exception {
    ArgumentCaptor<SuspendEvent> suspendEventCaptor = ArgumentCaptor.forClass(SuspendEvent.class);

    debugger.stepInto(new StepIntoActionImpl(SuspendPolicy.ALL));

    verify(callback).onEvent(suspendEventCaptor.capture());
    SuspendEvent suspendEvent = suspendEventCaptor.getValue();
    assertEquals(suspendEvent.getLocation().getLineNumber(), 2);
    assertTrue(suspendEvent.getLocation().getTarget().endsWith("app.js"));

    Mockito.reset(callback);
    debugger.stepInto(new StepIntoActionImpl(SuspendPolicy.ALL));

    verify(callback, timeout(1000)).onEvent(suspendEventCaptor.capture());
    suspendEvent = suspendEventCaptor.getValue();
    assertEquals(suspendEvent.getLocation().getLineNumber(), 5);
    assertTrue(suspendEvent.getLocation().getTarget().endsWith("app.js"));

    Mockito.reset(callback);
    debugger.stepOut(new StepOutActionImpl(SuspendPolicy.ALL));

    verify(callback, timeout(1000)).onEvent(suspendEventCaptor.capture());
    suspendEvent = suspendEventCaptor.getValue();
    assertEquals(suspendEvent.getLocation().getLineNumber(), 9);
    assertTrue(suspendEvent.getLocation().getTarget().endsWith("app.js"));
  }

  @Test
  public void testResume() throws Exception {
    debugger.resume(new ResumeActionImpl());

    ArgumentCaptor<DebuggerEvent> eventCaptor = ArgumentCaptor.forClass(DebuggerEvent.class);
    verify(callback, timeout(5000)).onEvent(eventCaptor.capture());
    assertTrue(eventCaptor.getValue() != null);
    assertTrue(eventCaptor.getValue() instanceof DisconnectEvent);
  }
}
