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

import org.eclipse.che.api.debug.shared.model.Location;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Anatolii Bazko
 */
public class NodeJsBackTraceTest {

    @Test(dataProvider = "backtrace")
    public void testParseBackTrace(String backtrace, String script, int line) throws Exception {
        NodeJsOutput output = NodeJsOutput.of(backtrace);
        NodeJsBackTrace nodeJsBackTrace = NodeJsBackTrace.parse(output);

        Location location = nodeJsBackTrace.getLocation();

        assertNotNull(location);
        assertEquals(location.getTarget(), script);
        assertEquals(location.getLineNumber(), line);
    }

    @DataProvider(name = "backtrace")
    public static Object[][] backtrace() {
        return new Object[][] {{"#0 Object.defineProperty.get bootstrap_node.js:253:9", "bootstrap_node.js", 253},
                               {"#0 app.js:1:71", "app.js", 1}};

    }
}
