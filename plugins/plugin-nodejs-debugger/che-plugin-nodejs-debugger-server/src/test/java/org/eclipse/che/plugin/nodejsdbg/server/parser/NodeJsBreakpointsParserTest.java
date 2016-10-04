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
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class NodeJsBreakpointsParserTest {

    private NodeJsBreakpointsParser parser;

    @BeforeMethod
    public void setUp() throws Exception {
        parser = new NodeJsBreakpointsParser();
    }

    @Test
    public void testParseBreakpoints() throws Exception {
        NodeJsOutput nodeJsOutput = NodeJsOutput.of("{ breakpoints: \n" +
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

        assertTrue(parser.match(nodeJsOutput));

        List<Breakpoint> breakpoints = parser.parse(nodeJsOutput).getAll();
        assertEquals(breakpoints.size(), 1);

        Breakpoint breakpoint = breakpoints.get(0);
        assertEquals(breakpoint.getLocation().getLineNumber(), 2);
        assertEquals(breakpoint.getLocation().getTarget(), "scriptId:63");
        assertNull(breakpoint.getCondition());
        assertTrue(breakpoint.isEnabled());
    }

    @Test
    public void testParseBreakpointsWhenScriptIsNotLoaded() throws Exception {
        NodeJsOutput nodeJsOutput = NodeJsOutput.of("{ breakpoints: \n" +
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

        assertTrue(parser.match(nodeJsOutput));

        List<Breakpoint> breakpoints = parser.parse(nodeJsOutput).getAll();
        assertEquals(breakpoints.size(), 1);

        Breakpoint breakpoint = breakpoints.get(0);
        assertEquals(breakpoint.getLocation().getLineNumber(), 2);
        assertEquals(breakpoint.getLocation().getTarget(), "scriptRegExp:^(.*[\\/\\\\])?df3dfasdfs\\.js$");
        assertNull(breakpoint.getCondition());
        assertTrue(breakpoint.isEnabled());
    }
}
