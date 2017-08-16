/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.jdb.server;

import com.google.common.collect.ImmutableList;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.ensureSuspendAtDesiredLocation;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.startJavaDebugger;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test ThreadDump when all threads are suspended.
 *
 * @author Anatolii Bazko
 */
public class GetValueTest1 {

    private JavaDebugger                 debugger;
    private BlockingQueue<DebuggerEvent> debuggerEvents;

    @BeforeClass
    public void setUp() throws Exception {
        ProjectApiUtils.ensure();

        Location location = new LocationImpl("org.eclipse.GetValueTest1", 26);

        debuggerEvents = new ArrayBlockingQueue<>(10);
        debugger = startJavaDebugger(new BreakpointImpl(location), debuggerEvents);

        ensureSuspendAtDesiredLocation(location, debuggerEvents);
    }

    @AfterClass
    public void tearDown() throws Exception {
        if (debugger != null) {
            terminateVirtualMachineQuietly(debugger);
        }
    }

    @Test(dataProvider = "getVariablePaths")
    public void shouldGetValue(List<String> path, int frameIndex, String value) throws Exception {
        Optional<ThreadDump> main = debugger.getThreadDumps().stream().filter(t -> t.getName().equals("main")).findAny();
        assertTrue(main.isPresent());

        ThreadDump mainThread = main.get();

        SimpleValue debuggerValue = debugger.getValue(new VariablePathImpl(path), mainThread.getId(), frameIndex);

        if (debuggerValue == null) {
            assertNull(value);
        } else {
            assertEquals(debuggerValue.getString(), value);
        }
    }

    @DataProvider(name = "getVariablePaths")
    public static Object[][] getVariablePaths() {
        return new Object[][] {{ImmutableList.of("i"), 0, "2"},
                               {ImmutableList.of("i"), 1, null},
                               {ImmutableList.of("var1"), 0, "\"var1\""},
                               {ImmutableList.of("var1"), 1, null},
                               {ImmutableList.of("var2"), 0, "\"var2\""},
                               {ImmutableList.of("static", "var2"), 0, "\"field2\""},
                               {ImmutableList.of("static", "var2"), 1, "\"field2\""},
                               {ImmutableList.of("args"), 0, null}};
    }

    @Test(dataProvider = "setVariable")
    public void shouldSetValue(List<String> path, String newValue, int frameIndex) throws Exception {
        Optional<ThreadDump> main = debugger.getThreadDumps().stream().filter(t -> t.getName().equals("main")).findAny();
        assertTrue(main.isPresent());

        ThreadDump mainThread = main.get();

        VariablePathImpl variablePath = new VariablePathImpl(path);
        debugger.setValue(new VariableImpl(new SimpleValueImpl(newValue), variablePath), mainThread.getId(), frameIndex);

        SimpleValue debuggerValue = debugger.getValue(variablePath, mainThread.getId(), frameIndex);
        assertEquals(debuggerValue.getString(), newValue);
    }

    @DataProvider(name = "setVariable")
    public static Object[][] getSetVariable() {
        return new Object[][] {{ImmutableList.of("i"), "3", 0}};
    }
}
