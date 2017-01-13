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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Roman Nikitenko
 */
public class ProcessInfoTest {

    @Test
    public void testParseProcessInfo() throws Exception {
        final String output = "436 /projects/cpp/a.out";
        checkProcessInfo(output);
    }

    @Test
    public void testParseProcessInfoWithWhiteSpaces() throws Exception {
        final String output = "         436      /projects/cpp/a.out        ";
        checkProcessInfo(output);
    }

    @Test
    public void testParseProcessInfoWithLeadingWhiteSpace() throws Exception {
        final String output = "      436 /projects/cpp/a.out";
        checkProcessInfo(output);
    }

    @Test
    public void testParseProcessInfoWithWhiteSpaceInTheMiddle() throws Exception {
        final String output = "436      /projects/cpp/a.out";
        checkProcessInfo(output);
    }

    @Test
    public void testParseProcessInfoWithWhiteSpaceAtTheEndLine() throws Exception {
        final String output = "436 /projects/cpp/a.out      ";
        checkProcessInfo(output);
    }

    @Test
    public void shouldTrowExceptionWhenOutputNotContainsProcessInfo() throws Exception {
        final String output = "PID CMD";
        try {
            ProcessInfo.parse(output);
            fail("Exception should be thrown when output not contains process info");
        } catch (Exception e) {
        }
    }

    private void checkProcessInfo(String output) throws Exception {
        final ProcessInfo processInfo = ProcessInfo.parse(output);
        final String processName = processInfo.getProcessName();
        final int pid = processInfo.getProcessId();

        assertEquals(processName, "/projects/cpp/a.out");
        assertEquals(pid, 436);
    }
}
