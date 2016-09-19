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
package org.eclipse.che.plugin.nodejsdbg.server.parser;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class NodeJsBreakpointsTest {

    @Test
    public void testParseBreakpoints() throws Exception {
        NodeJsOutput breakpointsOutput = NodeJsOutput.of("{ breakpoints: \n" +
                                                         "   [ { number: 1,\n" +
                                                         "       line: 1,\n" +
                                                         "       column: null,\n" +
                                                         "       groupId: null,\n" +
                                                         "       active: true,\n" +
                                                         "       condition: null,\n" +
                                                         "       actual_locations: [Object],\n" +
                                                         "       type: 'scriptId',\n" +
                                                         "       script_id: '63' } ],\n" +
                                                         "  breakOnExceptions: false,\n" +
                                                         "  breakOnUncaughtExceptions: false }");

        NodeJsOutput scriptsOutput = NodeJsOutput.of("  35: bootstrap_node.js\n" +
                                                     "* 63: app.js\n");

        NodeJsBreakpoints nodeJsBreakpoints = NodeJsBreakpoints.parse(scriptsOutput, breakpointsOutput);

        List<Breakpoint> breakpoints = nodeJsBreakpoints.getBreakpoints();
        assertEquals(breakpoints.size(), 1);

        Breakpoint breakpoint = breakpoints.get(0);
        assertEquals(breakpoint.getLocation().getLineNumber(), 1);
        assertEquals(breakpoint.getLocation().getTarget(), "app.js");
        assertNull(breakpoint.getCondition());
        assertTrue(breakpoint.isEnabled());
    }

    @Test
    public void testParseBreakpointsWhenScriptIsNotLoaded() throws Exception {
        NodeJsOutput breakpointsOutput = NodeJsOutput.of("{ breakpoints: \n" +
                                                         "   [ { number: 1,\n" +
                                                         "       line: 1,\n" +
                                                         "       column: null,\n" +
                                                         "       groupId: null,\n" +
                                                         "       active: true,\n" +
                                                         "       condition: null,\n" +
                                                         "       actual_locations: [Object],\n" +
                                                         "       type: 'scriptRegExp',\n" +
                                                         "       script_regexp: '^(.*[\\\\/\\\\\\\\])?df3dfasdfs\\\\.js$' } ]," +
                                                         "  breakOnExceptions: false,\n" +
                                                         "  breakOnUncaughtExceptions: false }");

        NodeJsOutput scriptsOutput = NodeJsOutput.of("  35: bootstrap_node.js\n" +
                                                     "* 63: app.js\n");

        NodeJsBreakpoints nodeJsBreakpoints = NodeJsBreakpoints.parse(scriptsOutput, breakpointsOutput);

        List<Breakpoint> breakpoints = nodeJsBreakpoints.getBreakpoints();
        assertEquals(breakpoints.size(), 1);

        Breakpoint breakpoint = breakpoints.get(0);
        assertEquals(breakpoint.getLocation().getLineNumber(), 1);
        assertEquals(breakpoint.getLocation().getTarget(), "^(.*[\\/\\\\])?df3dfasdfs\\.js$");
        assertNull(breakpoint.getCondition());
        assertTrue(breakpoint.isEnabled());
    }
}
