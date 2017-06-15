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

import org.eclipse.che.api.debug.shared.dto.ThreadDumpDto;
import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.ThreadStatus;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.DtoConverter;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class ThreadDumpTest {
    private Debugger debugger;
    private BlockingQueue<DebuggerEvent> events = new ArrayBlockingQueue<>(10);

    @BeforeClass
    public void setUp() throws Exception {
        ProjectApiUtils.ensure();

        initJavaDebugger();
        endureSuspendAtDesiredLocation();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        debugger.resume(new ResumeActionImpl());
    }

    @Test
    public void testGetThreadDumps() throws Exception {
        List<ThreadDumpDto> threads = debugger.getThreadDumps().stream().map(DtoConverter::asDto).collect(toList());

        Optional<ThreadDumpDto> mainThread = threads.stream().filter(t -> t.getName().equals("main")).findAny();
        assertTrue(mainThread.isPresent());

        ThreadDump threadDump = mainThread.get();
        assertEquals(threadDump.getName(), "main");
        assertEquals(threadDump.getGroupName(), "main");
        assertTrue(threadDump.isSuspended());
        assertEquals(threadDump.getStatus(), ThreadStatus.RUNNABLE);

        List<? extends StackFrameDump> frames = threadDump.getFrames();
        assertEquals(frames.size(), 1);

        StackFrameDump stackFrameDump = frames.get(0);

        List<? extends Variable> variables = stackFrameDump.getVariables();
        assertFalse(variables.isEmpty());

        List<? extends Field> fields = stackFrameDump.getFields();
        assertTrue(fields.isEmpty());

        Location location = stackFrameDump.getLocation();
        assertEquals(location.getLineNumber(), 26);
        assertEquals(location.getTarget(), "org.eclipse.ThreadDumpTest");
        assertEquals(location.getExternalResourceId(), -1);
        assertEquals(location.getResourceProjectPath(), "/test");
        assertEquals(location.getResourcePath(), "/test/src/org/eclipse/ThreadDumpTest.java");

        Method method = location.getMethod();
        assertEquals(method.getName(), "main");

        List<? extends Variable> arguments = method.getArguments();
        assertEquals(arguments.size(), 1);
    }

    private void initJavaDebugger() throws DebuggerException, InterruptedException {
        debugger = new JavaDebugger("localhost", parseInt(getProperty("debug.port")), events::add);

        BreakpointImpl breakpoint = new BreakpointImpl(new LocationImpl("org.eclipse.ThreadDumpTest", 26));
        debugger.start(new StartActionImpl(Collections.singletonList(breakpoint)));
    }

    private void endureSuspendAtDesiredLocation() throws InterruptedException {
        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);

        SuspendEvent suspendEvent = (SuspendEvent)debuggerEvent;
        assertEquals(suspendEvent.getLocation().getLineNumber(), 26);
    }
}
