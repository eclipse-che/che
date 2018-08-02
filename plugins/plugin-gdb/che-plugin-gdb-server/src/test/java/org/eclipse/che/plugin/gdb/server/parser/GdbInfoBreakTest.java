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

import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
public class GdbInfoBreakTest {

  @Test
  public void testParse() throws Exception {
    GdbOutput gdbOutput =
        GdbOutput.of(
            "Num     Type           Disp Enb Address            What\n"
                + "1       breakpoint     keep y   0x00000000004008ca in main() at h.cpp:7\n"
                + "2       breakpoint     keep y   0x00000000004008ca in main() at h.cpp:8\n");

    GdbInfoBreak gdbInfoBreak = GdbInfoBreak.parse(gdbOutput);
    List<Breakpoint> breakpoints = gdbInfoBreak.getBreakpoints();

    assertEquals(breakpoints.size(), 2);

    Breakpoint breakpoint = breakpoints.get(0);
    assertEquals(breakpoint.getLocation().getTarget(), "h.cpp");
    assertEquals(breakpoint.getLocation().getLineNumber(), 7);

    breakpoint = breakpoints.get(1);
    assertEquals(breakpoint.getLocation().getTarget(), "h.cpp");
    assertEquals(breakpoint.getLocation().getLineNumber(), 8);
  }
}
