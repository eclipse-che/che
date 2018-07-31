/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
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
public class GdbPrintTest {

  @Test
  public void testParse() throws Exception {
    GdbOutput gdbOutput = GdbOutput.of("$9 = 0\n");

    GdbPrint gdbPrint = GdbPrint.parse(gdbOutput);

    assertEquals(gdbPrint.getValue(), "0");
  }

  @Test(expectedExceptions = GdbParseException.class)
  public void testParseFail() throws Exception {
    GdbOutput gdbOutput = GdbOutput.of("some text");
    GdbPrint.parse(gdbOutput);
  }
}
