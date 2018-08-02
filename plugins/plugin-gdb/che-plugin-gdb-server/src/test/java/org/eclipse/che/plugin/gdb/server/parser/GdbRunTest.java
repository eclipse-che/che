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

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
public class GdbRunTest {

  @Test
  public void testParse() throws Exception {
    GdbOutput gdbOutput =
        GdbOutput.of(
            "Starting program: /home/tolusha/java/gdb/hello \n"
                + "0\n"
                + "\n"
                + "Breakpoint 1, main () at h.cpp:7\n"
                + "7\t\t  std::cout << \"Hello World!\" << std::endl;\n");

    GdbRun gdbRun = GdbRun.parse(gdbOutput);

    Breakpoint breakpoint = gdbRun.getBreakpoint();

    assertNotNull(breakpoint);
    assertEquals(breakpoint.getLocation().getTarget(), "h.cpp");
    assertEquals(breakpoint.getLocation().getLineNumber(), 7);
  }
}
