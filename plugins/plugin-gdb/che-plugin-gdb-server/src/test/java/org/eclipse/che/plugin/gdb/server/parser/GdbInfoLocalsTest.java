/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
