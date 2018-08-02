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
import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
public class GdbInfoLocalsTest {

  @Test
  public void testParse() throws Exception {
    GdbOutput gdbOutput = GdbOutput.of("i = 0\n" + "j = 34");

    GdbInfoLocals gdbInfoLocals = GdbInfoLocals.parse(gdbOutput);

    Map<String, String> variables = gdbInfoLocals.getVariables();
    assertEquals(variables.get("i"), "0");
    assertEquals(variables.get("j"), "34");
  }
}
