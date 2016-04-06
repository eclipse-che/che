/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.gdb.server;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.debugger.shared.Breakpoint;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointActivatedEvent;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointEvent;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerEvent;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerEventList;
import org.eclipse.che.ide.ext.debugger.shared.Location;
import org.eclipse.che.ide.ext.debugger.shared.StackFrameDump;
import org.eclipse.che.ide.ext.debugger.shared.StepEvent;
import org.eclipse.che.ide.ext.debugger.shared.Value;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class GdbDebuggerTest {

    private String                       file;
    private GdbServer                    gdbServer;
    private GdbDebugger                  gdbDebugger;
    private BlockingQueue<DebuggerEvent> events;

    @BeforeClass
    public void beforeClass() throws Exception {
        file = GdbTest.class.getResource("/hello").getFile();
        events = new ArrayBlockingQueue<>(10);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        gdbServer = GdbServer.start("localhost", 1111, file);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        gdbServer.stop();
    }

    @Test
    public void testDebugger() throws Exception {
        initializeDebugger();
        addBreakpoint();
        startDebugger();
        doSetAndGetValues();
        stepInto();
        stepOver();
        stepOut();
        resume();
        deleteAllBreakpoints();
        disconnect();
    }

    private void deleteAllBreakpoints() throws GdbDebuggerException {
        List<Breakpoint> breakpoints = gdbDebugger.getBreakpoints().getBreakpoints();
        assertEquals(breakpoints.size(), 1);

        gdbDebugger.deleteAllBreakPoints();

        breakpoints = gdbDebugger.getBreakpoints().getBreakpoints();
        assertTrue(breakpoints.isEmpty());
    }

    private void resume() throws GdbDebuggerException, InterruptedException {
        gdbDebugger.resume();

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointEvent);

        BreakpointEvent breakpointEvent = (BreakpointEvent)debuggerEvent;
        assertEquals(breakpointEvent.getBreakpoint().getLocation().getClassName(), "h.cpp");
        assertEquals(breakpointEvent.getBreakpoint().getLocation().getLineNumber(), 7);
    }

    private void stepOut() throws GdbDebuggerException {
        try {
            gdbDebugger.stepOut();
        } catch (GdbDebuggerException e) {
            // ignore
        }

        assertTrue(events.isEmpty());
    }

    private void stepOver() throws GdbDebuggerException, InterruptedException {
        gdbDebugger.stepOver();

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof StepEvent);

        StepEvent stepEvent = (StepEvent)debuggerEvent;
        assertEquals(stepEvent.getLocation().getClassName(), "h.cpp");
        assertEquals(stepEvent.getLocation().getLineNumber(), 5);

        gdbDebugger.stepOver();

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof StepEvent);

        stepEvent = (StepEvent)debuggerEvent;
        assertEquals(stepEvent.getLocation().getClassName(), "h.cpp");
        assertEquals(stepEvent.getLocation().getLineNumber(), 6);

        gdbDebugger.stepOver();

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof StepEvent);

        stepEvent = (StepEvent)debuggerEvent;
        assertEquals(stepEvent.getLocation().getClassName(), "h.cpp");
        assertEquals(stepEvent.getLocation().getLineNumber(), 7);
    }

    private void stepInto() throws GdbDebuggerException, InterruptedException {
        gdbDebugger.stepInto();

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof StepEvent);

        StepEvent stepEvent = (StepEvent)debuggerEvent;
        assertEquals(stepEvent.getLocation().getClassName(), "h.cpp");
        assertEquals(stepEvent.getLocation().getLineNumber(), 5);

        gdbDebugger.stepInto();

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof StepEvent);

        stepEvent = (StepEvent)debuggerEvent;
        assertEquals(stepEvent.getLocation().getClassName(), "h.cpp");
        assertEquals(stepEvent.getLocation().getLineNumber(), 6);

        gdbDebugger.stepInto();

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof StepEvent);

        stepEvent = (StepEvent)debuggerEvent;
        assertEquals(stepEvent.getLocation().getClassName(), "h.cpp");
        assertEquals(stepEvent.getLocation().getLineNumber(), 7);
    }

    private void doSetAndGetValues() throws GdbDebuggerException {
        Value value = gdbDebugger.getValue("i");
        assertEquals(value.getValue(), "0");

        gdbDebugger.setValue("i", "2");
        value = gdbDebugger.getValue("i");

        assertEquals(value.getValue(), "2");

        String expression = gdbDebugger.expression("i");
        assertEquals(expression, "2");

        expression = gdbDebugger.expression("10 + 10");
        assertEquals(expression, "20");

        StackFrameDump stackFrameDump = gdbDebugger.dumpStackFrame();
        assertTrue(stackFrameDump.getFields().isEmpty());
        assertEquals(stackFrameDump.getLocalVariables().size(), 1);
        assertEquals(stackFrameDump.getLocalVariables().get(0).getName(), "i");
        assertEquals(stackFrameDump.getLocalVariables().get(0).getValue(), "2");
        assertEquals(stackFrameDump.getLocalVariables().get(0).getType(), "int");
    }

    private void startDebugger() throws GdbDebuggerException, InterruptedException {
        gdbDebugger.start();

        assertEquals(events.size(), 1);

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointEvent);

        BreakpointEvent breakpointEvent = (BreakpointEvent)debuggerEvent;
        assertEquals(breakpointEvent.getBreakpoint().getLocation().getClassName(), "h.cpp");
        assertEquals(breakpointEvent.getBreakpoint().getLocation().getLineNumber(), 7);
    }

    private void disconnect() throws GdbDebuggerException, InterruptedException {
        gdbDebugger.disconnect();

        assertEquals(events.size(), 1);

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof DebuggerEvent);
    }

    private void addBreakpoint() throws GdbDebuggerException, InterruptedException {
        Location location = DtoFactory.newDto(Location.class);
        location.setClassName("h.cpp");
        location.setLineNumber(7);

        Breakpoint breakpoint = DtoFactory.newDto(Breakpoint.class);
        breakpoint.setLocation(location);
        gdbDebugger.addBreakpoint(breakpoint);

        assertEquals(events.size(), 1);

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

        BreakpointActivatedEvent breakpointActivatedEvent = (BreakpointActivatedEvent)debuggerEvent;
        assertEquals(breakpointActivatedEvent.getBreakpoint().getLocation().getClassName(), "h.cpp");
        assertEquals(breakpointActivatedEvent.getBreakpoint().getLocation().getLineNumber(), 7);
    }

    private void initializeDebugger() throws GdbDebuggerException {
        gdbDebugger = spy(GdbDebugger.newInstance("localhost", 1111, file));

        doAnswer(invocation -> {
            DebuggerEventList eventList = (DebuggerEventList)invocation.getArguments()[0];
            events.addAll(eventList.getEvents());
            return null;
        }).when(gdbDebugger).publishWebSocketMessage(any(), any());

        assertEquals(gdbDebugger.getFile(), file);
        assertEquals(gdbDebugger.getHost(), "localhost");
        assertEquals(gdbDebugger.getPort(), 1111);
        assertNotNull(gdbDebugger.getVersion());
        assertNotNull(gdbDebugger.getName());
        assertNotNull(gdbDebugger.getId(), "1");
    }
}