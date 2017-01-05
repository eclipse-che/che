/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.gdb.server.parser;

import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class GdbInfoArgsTest {

    @Test
    public void testParse() throws Exception {
        GdbOutput gdbOutput = GdbOutput.of("i = 0\n" +
                                           "j = 34");

        GdbInfoArgs gdbInfoArgs = GdbInfoArgs.parse(gdbOutput);

        Map<String, String> variables = gdbInfoArgs.getVariables();
        assertEquals(variables.get("i"), "0");
        assertEquals(variables.get("j"), "34");
    }
}
