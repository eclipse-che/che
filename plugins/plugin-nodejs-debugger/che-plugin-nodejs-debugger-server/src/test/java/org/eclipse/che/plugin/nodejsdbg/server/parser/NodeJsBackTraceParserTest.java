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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class NodeJsBackTraceParserTest {

  private NodeJsBackTraceParser parser;

  @BeforeMethod
  public void setUp() throws Exception {
    parser = NodeJsBackTraceParser.INSTANCE;
  }

  @Test(dataProvider = "match")
  public void testMatch(String output, Boolean result) throws Exception {
    NodeJsOutput nodeJsOutput = NodeJsOutput.of(output);

    assertTrue(parser.match(nodeJsOutput) == result);
  }

  @Test(dataProvider = "parse")
  public void testParse(String output, String script, int line) throws Exception {
    NodeJsOutput nodeJsOutput = NodeJsOutput.of(output);

    Location location = parser.parse(nodeJsOutput);

    assertEquals(location.getTarget(), script);
    assertEquals(location.getLineNumber(), line);
  }

  @DataProvider(name = "match")
  public static Object[][] match() {
    return new Object[][] {
      {"#0 app.js:1:71", true},
      {"#0 app.js:1:71\n#1 app.js:1:71", true},
      {"#0 Object.defineProperty.get bootstrap_node.js:253:9", true},
      {"#1 app.js:1:71", false}
    };
  }

  @DataProvider(name = "parse")
  public static Object[][] parse() {
    return new Object[][] {
      {"#0 app.js:1:71", "app.js", 1},
      {"#0 app.js:1:71\n#1 app.js:1:71", "app.js", 1},
      {"#0 Object.defineProperty.get bootstrap_node.js:253:9", "bootstrap_node.js", 253}
    };
  }
}
