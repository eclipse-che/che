/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.gdb.server.parser;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;
import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
public class GdbBreakTest {

  @Test
  public void testParse() throws Exception {
    GdbOutput gdbOutput =
        GdbOutput.of(
            "Note: breakpoint 1 also set at pc 0x4008ca.\n"
                + "Breakpoint 2 at 0x4008ca: file h.cpp, line 7.\n");

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
