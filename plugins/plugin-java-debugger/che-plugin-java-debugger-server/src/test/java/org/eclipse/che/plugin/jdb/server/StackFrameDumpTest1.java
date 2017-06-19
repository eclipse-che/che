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

import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.MethodDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.debugger.server.DtoConverter.asDto;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.ensureSuspendAtDesiredLocation;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.startJavaDebugger;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test ThreadDump when all threads are suspended.
 *
 * @author Anatolii Bazko
 */
public class StackFrameDumpTest1 {
    private JavaDebugger debugger;
    private BlockingQueue<DebuggerEvent> callback = new ArrayBlockingQueue<>(10);

    @BeforeClass
    public void setUp() throws Exception {
        ProjectApiUtils.ensure();

        Location location = new LocationImpl("org.eclipse.StackFrameDumpTest1", 25);
        debugger = startJavaDebugger(new BreakpointImpl(location), callback);

        ensureSuspendAtDesiredLocation(location, callback);
    }

    @AfterClass
    public void tearDown() throws Exception {
        if (debugger != null) {
            terminateVirtualMachineQuietly(debugger);
        }
    }

    @Test
    public void shouldGetStackFrameDump() throws Exception {
        Optional<ThreadDump> main = debugger.getThreadDumps().stream().filter(t -> t.getName().equals("main")).findAny();
        assertTrue(main.isPresent());

        ThreadDump mainThread = main.get();
        assertEquals(mainThread.getFrames().size(), 3);

        validateFrame0(mainThread.getId());
        validateFrame1(mainThread.getId());
        validateFrame2(mainThread.getId());
    }

    private void validateFrame0(long threadId) throws DebuggerException {
        StackFrameDumpDto stackFrame = asDto(debugger.getStackFrameDump(threadId, 0));

        LocationDto location = stackFrame.getLocation();
        assertEquals(location.getLineNumber(), 25);
        assertEquals(location.getTarget(), "org.eclipse.StackFrameDumpTest1");

        MethodDto method = location.getMethod();
        assertEquals(method.getName(), "do2");

        List<VariableDto> arguments = method.getArguments();
        assertEquals(arguments.size(), 1);

        VariableDto variable = arguments.get(0);
        assertEquals(variable.getName(), "str");
        assertEquals(variable.getType(), "java.lang.String");
        assertEquals(variable.getVariablePath().getPath(), singletonList("str"));
        assertFalse(variable.isPrimitive());
        assertEquals(variable.getValue().getString(), "\"2\"");
        assertTrue(variable.getValue().getVariables().isEmpty());

        assertTrue(stackFrame.getFields().isEmpty());

        List<VariableDto> variables = stackFrame.getVariables();
        variable = variables.get(0);
        assertEquals(variable.getName(), "str");
        assertEquals(variable.getType(), "java.lang.String");
        assertEquals(variable.getVariablePath().getPath(), singletonList("str"));
        assertFalse(variable.isPrimitive());
        assertEquals(variable.getValue().getString(), "\"2\"");
        assertTrue(variable.getValue().getVariables().isEmpty());
    }

    private void validateFrame1(long threadId) throws DebuggerException {
        StackFrameDumpDto stackFrame = asDto(debugger.getStackFrameDump(threadId, 1));

        LocationDto location = stackFrame.getLocation();
        assertEquals(location.getLineNumber(), 21);
        assertEquals(location.getTarget(), "org.eclipse.StackFrameDumpTest1");

        MethodDto method = location.getMethod();
        assertEquals(method.getName(), "do1");
    }

    private void validateFrame2(long threadId) throws DebuggerException {
        StackFrameDumpDto stackFrame = asDto(debugger.getStackFrameDump(threadId, 2));

        LocationDto location = stackFrame.getLocation();
        assertEquals(location.getLineNumber(), 16);
        assertEquals(location.getTarget(), "org.eclipse.StackFrameDumpTest1");

        MethodDto method = location.getMethod();
        assertEquals(method.getName(), "main");

    }

}
