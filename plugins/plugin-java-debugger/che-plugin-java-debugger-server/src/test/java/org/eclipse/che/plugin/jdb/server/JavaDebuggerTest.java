/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.jdb.server;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
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
import org.eclipse.che.api.debug.shared.model.impl.action.StepIntoActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOutActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOverActionImpl;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class JavaDebuggerTest {

    private JavaDebugger                 debugger;
    private BlockingQueue<DebuggerEvent> events;

    @BeforeClass
    protected void setUp() throws Exception {
        ProjectApiUtils.ensure();

        events = new ArrayBlockingQueue<>(10);
        Map<String, String> connectionProperties = ImmutableMap.of("host", "localhost",
                                                                   "port", System.getProperty("debug.port"));
        JavaDebuggerFactory factory = new JavaDebuggerFactory();
        debugger = (JavaDebugger)factory.create(connectionProperties, events::add);
    }


    @AfterClass
    public void tearDown() throws Exception {
        terminateVirtualMachineQuietly(debugger);
    }


    @Test(priority = 10)
    public void testGetInfo() throws Exception {
        DebuggerInfo info = debugger.getInfo();

        assertEquals(info.getHost(), "localhost");
        assertEquals(info.getPort(), Integer.parseInt(System.getProperty("debug.port")));

        assertNotNull(info.getName());
        assertNotNull(info.getVersion());
    }

    @Test(priority = 20)
    public void testStartDebugger() throws Exception {
        BreakpointImpl breakpoint = new BreakpointImpl(new LocationImpl("com.HelloWorld", 17), false, null);
        debugger.start(new StartActionImpl(singletonList(breakpoint)));

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);

        Location location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getLineNumber(), 17);
        assertEquals(location.getTarget(), "com.HelloWorld");
    }

    @Test(priority = 30)
    public void testAddBreakpoint() throws Exception {
        int breakpointsCount = debugger.getAllBreakpoints().size();

        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("com.HelloWorld", 18), false, null));

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

        Breakpoint breakpoint = ((BreakpointActivatedEvent)debuggerEvent).getBreakpoint();
        assertEquals(breakpoint.getLocation().getLineNumber(), 18);
        assertEquals(breakpoint.getLocation().getTarget(), "com.HelloWorld");
        assertTrue(breakpoint.isEnabled());

        assertEquals(debugger.getAllBreakpoints().size(), breakpointsCount + 1);
    }

    @Test(priority = 50, expectedExceptions = DebuggerException.class)
    public void testAddBreakpointToUnExistedLocation() throws Exception {
        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("com.HelloWorld", 1), false, null));
    }

    @Test(priority = 60)
    public void testRemoveBreakpoint() throws Exception {
        debugger.deleteBreakpoint(new LocationImpl("com.HelloWorld", 17));
        assertEquals(debugger.getAllBreakpoints().size(), 1);
    }

    @Test(priority = 70)
    public void testRemoveUnExistedBreakpoint() throws Exception {
        int breakpointsCount = debugger.getAllBreakpoints().size();

        debugger.deleteBreakpoint(new LocationImpl("com.HelloWorld", 2));

        assertEquals(debugger.getAllBreakpoints().size(), breakpointsCount);
    }

    @Test(priority = 80)
    public void testGetAllBreakpoints() throws Exception {
        assertFalse(debugger.getAllBreakpoints().isEmpty());

        debugger.deleteAllBreakpoints();

        assertTrue(debugger.getAllBreakpoints().isEmpty());

        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("com.HelloWorld", 18), false, null));

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

        assertEquals(debugger.getAllBreakpoints().size(), 1);

        Breakpoint breakpoint = debugger.getAllBreakpoints().get(0);
        assertEquals(breakpoint.getLocation().getLineNumber(), 18);
        assertEquals(breakpoint.getLocation().getTarget(), "com.HelloWorld");
        assertTrue(breakpoint.isEnabled());
    }

    @Test(priority = 90)
    public void testSteps() throws Exception {
        debugger.deleteAllBreakpoints();

        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("com.HelloWorld", 20), false, null));

        assertTrue(events.take() instanceof BreakpointActivatedEvent);

        debugger.resume(new ResumeActionImpl());

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        Location location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 20);
        assertEquals(location.getExternalResourceId(), -1);
        assertEquals(location.getResourceProjectPath(), "/test");
        assertEquals(location.getResourcePath(), "/test/src/com/HelloWorld.java");

        debugger.stepInto(new StepIntoActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 28);

        debugger.stepOut(new StepOutActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 20);

        debugger.stepOver(new StepOverActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 21);

        debugger.stepOver(new StepOverActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 23);

        debugger.stepOver(new StepOverActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 24);
    }

    @Test(priority = 100)
    public void testEvaluateExpression() throws Exception {
        assertEquals(debugger.evaluate("2+2"), "4");
        assertEquals(debugger.evaluate("\"hello\""), "\"hello\"");
        assertEquals(debugger.evaluate("test"), "\"hello\"");
    }

    @Test(priority = 110)
    public void testSetAndGetValue() throws Exception {
        assertEquals(debugger.getValue(new VariablePathImpl("test")).getString(), "\"hello\"");
        assertEquals(debugger.getValue(new VariablePathImpl("msg")).getString(), "\"Hello, debugger!\"");

        debugger.setValue(new VariableImpl(new SimpleValueImpl("\"new hello\""), (new VariablePathImpl("test"))));

        assertEquals(debugger.getValue(new VariablePathImpl("test")).getString(), "\"new hello\"");

        StackFrameDump stackFrameDump = debugger.dumpStackFrame();
        Set<String> vars = stackFrameDump.getVariables().stream().map(Variable::getName).collect(Collectors.toSet());
        assertTrue(vars.contains("args"));
        assertTrue(vars.contains("msg"));
        assertTrue(vars.contains("test"));
    }

    @Test(priority = 120)
    public void testDisconnect() throws Exception {
        debugger.disconnect();

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof DisconnectEvent);
    }


}
