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
package org.eclipse.che.plugin.nodejsdbg.server.parser;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class NodeJsStepParserTest {
  private NodeJsStepParser parser;

  @BeforeMethod
  public void setUp() throws Exception {
    parser = NodeJsStepParser.INSTANCE;
  }

  @Test
  public void testMatch() throws Exception {
    NodeJsOutput nodeJsOutput =
        NodeJsOutput.of(
            "break in module.js:559\n"
                + " 557   if (depth === 0) stat.cache = null;\n"
                + " 558   return result;\n"
                + ">559 };\n"
                + " 560 \n"
                + " 561");

    assertTrue(parser.match(nodeJsOutput));
  }

  @Test
  public void testParse() throws Exception {
    NodeJsOutput nodeJsOutput =
        NodeJsOutput.of(
            "break in module.js:559\n"
                + " 557   if (depth === 0) stat.cache = null;\n"
                + " 558   return result;\n"
                + ">559 };\n"
                + " 560 \n"
                + " 561");

    Location location = parser.parse(nodeJsOutput);

    assertEquals(location.getTarget(), "module.js");
    assertEquals(location.getLineNumber(), 559);
  }
}
