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

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.ensureSuspendAtDesiredLocation;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.startJavaDebugger;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertTrue;

/**
 * Test ThreadDump when all threads are suspended.
 *
 * @author Anatolii Bazko
 */
public class GetValueTest1 {
    private JavaDebugger debugger;
    private BlockingQueue<DebuggerEvent> callback = new ArrayBlockingQueue<>(10);

    @BeforeClass
    public void setUp() throws Exception {
        ProjectApiUtils.ensure();

        Location location = new LocationImpl("org.eclipse.GetValueTest1", 23);
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
    public void testGetValue() throws Exception {
        Optional<ThreadDump> main = debugger.getThreadDumps().stream().filter(t -> t.getName().equals("main")).findAny();
        assertTrue(main.isPresent());

        ThreadDump mainThread = main.get();


//        debugger.getValue()
    }
}
