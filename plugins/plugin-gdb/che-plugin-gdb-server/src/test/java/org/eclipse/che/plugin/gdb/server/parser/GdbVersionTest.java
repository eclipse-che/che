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

import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;
import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
public class GdbVersionTest {

  @Test
  public void testParse() throws Exception {
    GdbOutput gdbOutput =
        GdbOutput.of(
            "GNU gdb (Ubuntu 7.7.1-0ubuntu5~14.04.2) 7.7.1\n"
                + "Copyright (C) 2014 Free Software Foundation, Inc.\n"
                + "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>\n"
                + "This is free software: you are free to change and redistribute it.\n"
                + "There is NO WARRANTY, to the extent permitted by law.  Type \"show copying\"\n"
                + "and \"show warranty\" for details.\n"
                + "This GDB was configured as \"x86_64-linux-gnu\".\n"
                + "Type \"show configuration\" for configuration details.\n"
                + "For bug reporting instructions, please see:\n"
                + "<http://www.gnu.org/software/gdb/bugs/>.\n"
                + "Find the GDB manual and other documentation resources online at:\n"
                + "<http://www.gnu.org/software/gdb/documentation/>.\n"
                + "For help, type \"help\".\n"
                + "Type \"apropos word\" to search for commands related to \"word\".\n");

    GdbVersion gdbVersion = GdbVersion.parse(gdbOutput);

    assertEquals(gdbVersion.getVersion(), "7.7.1");
    assertEquals(gdbVersion.getName(), "GNU gdb (Ubuntu 7.7.1-0ubuntu5~14.04.2)");
  }

  @Test(expectedExceptions = GdbParseException.class)
  public void testParseFail() throws Exception {
    GdbOutput gdbOutput = GdbOutput.of("some text");
    GdbVersion.parse(gdbOutput);
  }
}
