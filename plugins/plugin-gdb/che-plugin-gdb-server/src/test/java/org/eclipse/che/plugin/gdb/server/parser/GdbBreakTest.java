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
package org.eclipse.che.plugin.gdb.server.parser;

import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Anatoliy Bazko
 */
public class GdbBreakTest {

    @Test
    public void testParse() throws Exception {
        GdbOutput gdbOutput = GdbOutput.of("Note: breakpoint 1 also set at pc 0x4008ca.\n" +
                                           "Breakpoint 2 at 0x4008ca: file h.cpp, line 7.\n");

        GdbBreak gdbBreak = GdbBreak.parse(gdbOutput);

        assertEquals(gdbBreak.getFile(), "h.cpp");
        assertEquals(gdbBreak.getLineNumber(), "7");
        assertNotNull(gdbBreak.getAddress());
    }

    @Test(expectedExceptions = GdbParseException.class)
    public void testParseFail() throws Exception {
        GdbOutput gdbOutput = GdbOutput.of("some text");
        GdbBreak.parse(gdbOutput);
    }
}
