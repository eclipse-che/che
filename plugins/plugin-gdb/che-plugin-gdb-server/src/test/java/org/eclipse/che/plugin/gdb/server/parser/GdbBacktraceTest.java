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

import java.util.Map;
import org.eclipse.che.api.debug.shared.model.Location;
import org.testng.annotations.Test;

/** @author Roman Nikitenko */
public class GdbBacktraceTest {

  private static String BACKTRACE_OUTPUT =
      "#0  0x00007ffff77fc710 in __write_nocancel () at ../sysdeps/unix/syscall-template.S:81\n"
          + "#1  0x00007ffff7b6a684 in std::ostream::put(char) () from /usr/lib/x86_64-linux-gnu/libstdc++.so.6"
          + "#2  0x00007ffff7b6a8b2 in std::basic_ostream<char, std::char_traits<char> >& std::endl<char, std::char_traits<char> >"
          + "(std::basic_ostream<char, std::char_traits<char> >&) () from /usr/lib/x86_64-linux-gnu/libstdc++.so.6"
          + "#3  0x00000000004008a8 in main () at hello.cc:16";

  @Test
  public void testParseFileLocation() throws Exception {
    GdbOutput gdbOutput = GdbOutput.of(BACKTRACE_OUTPUT);
    GdbBacktrace backtrace = GdbBacktrace.parse(gdbOutput);
    Map<Integer, Location> frames = backtrace.getFrames();

    Location frame0 = frames.get(0);
    Location frame3 = frames.get(3);

    assertEquals(frame0.getTarget(), "../sysdeps/unix/syscall-template.S");
    assertEquals(frame0.getLineNumber(), 81);

    assertEquals(frame3.getTarget(), "hello.cc");
    assertEquals(frame3.getLineNumber(), 16);
  }

  @Test
  public void testParseLibraryLocation() throws Exception {
    GdbOutput gdbOutput = GdbOutput.of(BACKTRACE_OUTPUT);
    GdbBacktrace backtrace = GdbBacktrace.parse(gdbOutput);
    Map<Integer, Location> frames = backtrace.getFrames();

    Location frame1 = frames.get(1);
    Location frame2 = frames.get(2);

    assertEquals(frame1.getTarget(), "/usr/lib/x86_64-linux-gnu/libstdc++.so.6");
    assertEquals(frame2.getTarget(), "/usr/lib/x86_64-linux-gnu/libstdc++.so.6");
  }
}
