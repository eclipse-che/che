/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepIntoActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOutActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOverActionImpl;
import org.eclipse.che.api.fs.server.FsManager;
import org.testng.annotations.Test;

/**
 * Class providing different tests for active Zend Debugger session.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgSessionTest extends AbstractZendDbgSessionTest {

  private final String dbgHelloFile =
      (new File(ZendDbgConfigurationTest.class.getResource("/php/hello.php").getPath()))
          .getAbsolutePath();
  private final String dbgClassesFile =
      (new File(ZendDbgConfigurationTest.class.getResource("/php/classes.php").getPath()))
          .getAbsolutePath();

  private final FsManager fsManager = mock(FsManager.class);

  @Test(
      groups = {"zendDbg"},
      dependsOnGroups = {"checkPHP"})
  public void testSslConnection() throws Exception {
    triggerSession(dbgHelloFile, getDbgSettings(true, true), fsManager);
    awaitSuspend(dbgHelloFile, 2);
    debugger.resume(new ResumeActionImpl());
  }

  @Test(
      groups = {"zendDbg"},
      dependsOnGroups = {"checkPHP"})
  public void testStepping() throws Exception {
    triggerSession(dbgHelloFile, getDbgSettings(true, false), fsManager);
    awaitSuspend(dbgHelloFile, 2);
    debugger.stepOver(new StepOverActionImpl(SuspendPolicy.ALL));
    awaitSuspend(dbgHelloFile, 4);
    debugger.stepInto(new StepIntoActionImpl(SuspendPolicy.ALL));
    awaitSuspend(dbgClassesFile, 9);
    debugger.stepOut(new StepOutActionImpl(SuspendPolicy.ALL));
    awaitSuspend(dbgHelloFile, 4);
  }

  @Test(
      groups = {"zendDbg"},
      dependsOnGroups = {"checkPHP"})
  public void testEvaluation() throws Exception {
    triggerSession(dbgHelloFile, getDbgSettings(true, false), fsManager);
    awaitSuspend(dbgHelloFile, 2);
    String result = debugger.evaluate("2+2");
    assertEquals(result, "4");
    result = debugger.evaluate("array(1,2,3)");
    assertEquals(result, "array [3]");
    result = debugger.evaluate("new XYZ()");
    assertEquals(result, "null");
  }

  @Test(
      groups = {"zendDbg"},
      dependsOnGroups = {"checkPHP"})
  public void testBreakpoints() throws Exception {
    List<Breakpoint> breakpoints = new ArrayList<>();
    Breakpoint bp1 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgHelloFile, 4));
    Breakpoint bp2 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgHelloFile, 8));
    breakpoints.add(bp1);
    breakpoints.add(bp2);
    triggerSession(dbgHelloFile, getDbgSettings(true, false), breakpoints, fsManager);
    awaitBreakpointActivated(bp1);
    awaitBreakpointActivated(bp2);
    awaitSuspend(dbgHelloFile, 2);
    Breakpoint bp3 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgClassesFile, 10));
    debugger.addBreakpoint(bp3);
    awaitBreakpointActivated(bp3);
    Breakpoint bp4 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgClassesFile, 16));
    debugger.addBreakpoint(bp4);
    awaitBreakpointActivated(bp4);
    debugger.deleteBreakpoint(ZendDbgLocationHandler.createDBG(dbgHelloFile, 8));
    debugger.deleteBreakpoint(ZendDbgLocationHandler.createDBG(dbgClassesFile, 16));
    assertEquals(debugger.getAllBreakpoints().size(), 2);
    debugger.deleteAllBreakpoints();
    assertTrue(debugger.getAllBreakpoints().isEmpty());
  }

  @Test(
      groups = {"zendDbg"},
      dependsOnGroups = {"checkPHP"})
  public void testBreaking() throws Exception {
    List<Breakpoint> breakpoints = new ArrayList<>();
    Breakpoint bp1 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgHelloFile, 4));
    Breakpoint bp2 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgClassesFile, 10));
    breakpoints.add(bp1);
    breakpoints.add(bp2);
    triggerSession(dbgHelloFile, getDbgSettings(false, false), breakpoints, fsManager);
    awaitBreakpointActivated(bp1);
    awaitBreakpointActivated(bp2);
    awaitSuspend(dbgHelloFile, 4);
    debugger.resume(new ResumeActionImpl());
    awaitSuspend(dbgClassesFile, 10);
  }

  @Test(
      groups = {"zendDbg"},
      dependsOnGroups = {"checkPHP"})
  public void testVariables() throws Exception {
    List<Breakpoint> breakpoints = new ArrayList<>();
    Breakpoint bp1 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgClassesFile, 16));
    Breakpoint bp2 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgClassesFile, 25));
    breakpoints.add(bp1);
    breakpoints.add(bp2);
    triggerSession(dbgHelloFile, getDbgSettings(false, false), breakpoints, fsManager);
    awaitBreakpointActivated(bp1);
    awaitBreakpointActivated(bp2);
    awaitSuspend(dbgClassesFile, 16);
    StackFrameDump stackFrameDump = debugger.dumpStackFrame();
    assertEquals(stackFrameDump.getVariables().size(), 1);
    assertEquals(stackFrameDump.getVariables().get(0).getName(), "$this");
    assertEquals(stackFrameDump.getVariables().get(0).getValue().getString(), "A");
    assertEquals(stackFrameDump.getVariables().get(0).getType(), "object");
    debugger.resume(new ResumeActionImpl());
    awaitSuspend(dbgClassesFile, 25);
    stackFrameDump = debugger.dumpStackFrame();
    assertEquals(stackFrameDump.getVariables().size(), 3);
    assertEquals(stackFrameDump.getVariables().get(0).getName(), "$this");
    assertEquals(stackFrameDump.getVariables().get(0).getValue().getString(), "B");
    assertEquals(stackFrameDump.getVariables().get(0).getType(), "object");
    assertEquals(stackFrameDump.getVariables().get(1).getName(), "$p");
    assertEquals(stackFrameDump.getVariables().get(1).getValue().getString(), "123");
    assertEquals(stackFrameDump.getVariables().get(1).getType(), "int");
    assertEquals(stackFrameDump.getVariables().get(2).getName(), "$v");
    assertEquals(stackFrameDump.getVariables().get(2).getValue().getString(), "\"B\"");
    assertEquals(stackFrameDump.getVariables().get(2).getType(), "string");
  }

  @Test(
      groups = {"zendDbg"},
      dependsOnGroups = {"checkPHP"})
  public void testGetValue() throws Exception {
    List<Breakpoint> breakpoints = new ArrayList<>();
    Breakpoint bp1 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgClassesFile, 16));
    breakpoints.add(bp1);
    triggerSession(dbgHelloFile, getDbgSettings(false, false), breakpoints, fsManager);
    awaitBreakpointActivated(bp1);
    awaitSuspend(dbgClassesFile, 16);
    debugger.dumpStackFrame();
    VariablePath variablePath = new VariablePathImpl("0");
    SimpleValue simpleValue = debugger.getValue(variablePath);
    assertEquals(simpleValue.getVariables().size(), 3);
    List<String> path = Arrays.asList("0", "0");
    variablePath = new VariablePathImpl(path);
    simpleValue = debugger.getValue(variablePath);
    assertEquals(simpleValue.getString(), "\"A\"");
    path = Arrays.asList("0", "1");
    variablePath = new VariablePathImpl(path);
    simpleValue = debugger.getValue(variablePath);
    assertEquals(simpleValue.getString(), "123");
    path = Arrays.asList("0", "2");
    variablePath = new VariablePathImpl(path);
    simpleValue = debugger.getValue(variablePath);
    assertEquals(simpleValue.getString(), "array [3]");
  }

  @Test(
      groups = {"zendDbg"},
      dependsOnGroups = {"checkPHP"})
  public void testSetValue() throws Exception {
    List<Breakpoint> breakpoints = new ArrayList<>();
    Breakpoint bp1 = new BreakpointImpl(ZendDbgLocationHandler.createDBG(dbgHelloFile, 5));
    breakpoints.add(bp1);
    triggerSession(dbgHelloFile, getDbgSettings(false, false), breakpoints, fsManager);
    awaitBreakpointActivated(bp1);
    awaitSuspend(dbgHelloFile, 5);
    StackFrameDump stackFrameDump = debugger.dumpStackFrame();
    int lastVar = stackFrameDump.getVariables().size() - 1;
    Variable variableToFind =
        new VariableImpl(
            null,
            null,
            new SimpleValueImpl("123"),
            false,
            new VariablePathImpl(String.valueOf(lastVar)));
    debugger.setValue(variableToFind);
    assertEquals(stackFrameDump.getVariables().get(lastVar).getValue().getString(), "123");
    variableToFind =
        new VariableImpl(
            null,
            null,
            new SimpleValueImpl("\"ABC\""),
            false,
            new VariablePathImpl(String.valueOf(lastVar)));
    debugger.setValue(variableToFind);
    assertEquals(stackFrameDump.getVariables().get(lastVar).getValue().getString(), "\"ABC\"");
  }
}
