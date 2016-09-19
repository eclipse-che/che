/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.nodejsdbg.server.parser;

import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatolii Bazko
 */
public class NodeJsScriptsTest {

    @Test
    public void testParseScriptCommand() throws Exception {
        NodeJsOutput output = NodeJsOutput.of("  35: bootstrap_node.js\n" +
                                              "* 63: app.js\n");

        NodeJsScripts nodeJsScripts = NodeJsScripts.parse(output);
        Map<Integer, String> scripts = nodeJsScripts.getScripts();

        assertEquals(scripts.size(), 2);
        assertEquals(scripts.get(35), "bootstrap_node.js");
        assertEquals(scripts.get(63), "app.js");
    }
}
