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

import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;
import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
public class GdbTargetRemoteTest {

  @Test
  public void testParse() throws Exception {
    GdbOutput gdbOutput =
        GdbOutput.of(
            "Remote debugging using localhost:1111\n"
                + "warning: Could not load vsyscall page because no executable was specified\n"
                + "try using the \"file\" command first.\n"
                + "0x00007ffff7ddb2d0 in ?? ()\n.");

    GdbTargetRemote gdbTargetRemote = GdbTargetRemote.parse(gdbOutput);

    assertEquals(gdbTargetRemote.getHost(), "localhost");
    assertEquals(gdbTargetRemote.getPort(), "1111");
  }

  @Test(expectedExceptions = GdbParseException.class)
  public void testParseFail() throws Exception {
    GdbOutput gdbOutput = GdbOutput.of("some text");
    GdbTargetRemote.parse(gdbOutput);
  }

  @Test(
      expectedExceptions = DebuggerException.class,
      expectedExceptionsMessageRegExp = "localhost:1223: Connection timed out.")
  public void testShouldThrowDebuggerExceptionIfConnectionTimedOut() throws Exception {
    GdbOutput gdbOutput = GdbOutput.of("localhost:1223: Connection timed out.");

    GdbTargetRemote.parse(gdbOutput);
  }
}
