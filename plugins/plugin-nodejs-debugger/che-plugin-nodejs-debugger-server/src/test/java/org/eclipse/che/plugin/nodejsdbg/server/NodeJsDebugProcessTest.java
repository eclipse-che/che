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
package org.eclipse.che.plugin.nodejsdbg.server;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerTerminatedException;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsBackTrace;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsBreakpoints;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsExec;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsScripts;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class NodeJsDebugProcessTest {

    private String             file;
    private NodeJsDebugProcess nodeJsDebugProcess;

    @BeforeClass
    public void beforeClass() throws Exception {
        file = NodeJsDebugProcessTest.class.getResource("/app.js").getFile();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        nodeJsDebugProcess = NodeJsDebugProcess.start(file);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        try {
            nodeJsDebugProcess.quit();
        } catch (NodeJsDebuggerTerminatedException ignored) {
        }
    }

    @Test
    public void testQuit() throws Exception {
        nodeJsDebugProcess.quit();

        assertFalse(nodeJsDebugProcess.process.isAlive());
    }

    @Test
    public void testChangeVariable() throws Exception {
        nodeJsDebugProcess.setVar("y", "2");

        NodeJsExec y = nodeJsDebugProcess.getVar("y");

        assertEquals(y.getValue(), "2");
    }

    @Test(dataProvider = "evaluate")
    public void testEvaluateExpression(String expression, String result) throws Exception {
        NodeJsExec execResult = nodeJsDebugProcess.evaluate(expression);

        assertEquals(execResult.getValue(), result);
    }

    @DataProvider(name = "evaluate")
    public static Object[][] evaluate() {
        return new String[][] {{"y", "ReferenceError: y is not defined"},
                               {"x", "undefined"},
                               {"2+2", "4"},
                               {"console.log(\"Hello\")", "< Hello"}};
    }

    @Test
    public void testScripts() throws Exception {
        NodeJsScripts nodeJsScripts = nodeJsDebugProcess.findLoadedScripts();
        Map<Integer, String> scripts = nodeJsScripts.getScripts();

        assertFalse(scripts.isEmpty());
        assertTrue(scripts.values().contains("app.js"));
    }

    @Test
    public void testGetBreakpoints() throws Exception {
        NodeJsBreakpoints nodeJsBreakpoints = nodeJsDebugProcess.getBreakpoints();

        List<Breakpoint> breakpoints = nodeJsBreakpoints.getBreakpoints();
        assertEquals(breakpoints.size(), 1);

        Breakpoint breakpoint = breakpoints.get(0);
        assertEquals(breakpoint.getLocation().getLineNumber(), 0);
        assertEquals(breakpoint.getLocation().getTarget(), "app.js");
        assertNull(breakpoint.getCondition());
        assertTrue(breakpoint.isEnabled());
    }

    @Test
    public void testSetBreakpoints() throws Exception {
        nodeJsDebugProcess.setBreakpoint(null, 2);
        nodeJsDebugProcess.setBreakpoint("app.js", 3);

        NodeJsBreakpoints nodeJsBreakpoints = nodeJsDebugProcess.getBreakpoints();
        List<Breakpoint> breakpoints = nodeJsBreakpoints.getBreakpoints();

        assertEquals(breakpoints.size(), 3);
    }

    @Test
    public void testClearBreakpoints() throws Exception {
        nodeJsDebugProcess.setBreakpoint(null, 1);
        nodeJsDebugProcess.setBreakpoint("app.js", 2);

        NodeJsBreakpoints nodeJsBreakpoints = nodeJsDebugProcess.getBreakpoints();
        List<Breakpoint> breakpoints = nodeJsBreakpoints.getBreakpoints();

        assertEquals(breakpoints.size(), 3);

        nodeJsDebugProcess.clearBreakpoint("app.js", 1);
        nodeJsDebugProcess.clearBreakpoint("app.js", 2);

        nodeJsBreakpoints = nodeJsDebugProcess.getBreakpoints();
        breakpoints = nodeJsBreakpoints.getBreakpoints();

        assertEquals(breakpoints.size(), 1);
    }

    @Test
    public void testBacktrace() throws Exception {
        NodeJsBackTrace nodeJsBackTrace = nodeJsDebugProcess.backtrace();
        Location location = nodeJsBackTrace.getLocation();

        assertEquals(location.getLineNumber(), 1);
        assertEquals(location.getTarget(), "app.js");
    }


    @Test
    public void testNext() throws Exception {
        NodeJsBackTrace nodeJsBackTrace = nodeJsDebugProcess.next();
        Location location = nodeJsBackTrace.getLocation();

        assertEquals(location.getLineNumber(), 2);
        assertEquals(location.getTarget(), "app.js");
    }

    @Test
    public void testContinue() throws Exception {
        nodeJsDebugProcess.setBreakpoint(null, 2);

        NodeJsBackTrace nodeJsBackTrace = nodeJsDebugProcess.cont();
        Location location = nodeJsBackTrace.getLocation();

        assertEquals(location.getLineNumber(), 2);
        assertEquals(location.getTarget(), "app.js");
    }

    @Test
    public void testStepInOut() throws Exception {
        nodeJsDebugProcess.setBreakpoint(null, 2);
        nodeJsDebugProcess.cont();

        NodeJsBackTrace nodeJsBackTrace = nodeJsDebugProcess.stepIn();
        Location location = nodeJsBackTrace.getLocation();

        assertEquals(location.getLineNumber(), 5);
        assertEquals(location.getTarget(), "app.js");

        nodeJsBackTrace = nodeJsDebugProcess.stepOut();
        location = nodeJsBackTrace.getLocation();

        assertEquals(location.getLineNumber(), 9);
        assertEquals(location.getTarget(), "app.js");
    }

    @Test
    public void testFinishDebug() throws Exception {
        nodeJsDebugProcess.cont();
    }
}
