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

import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;
import org.testng.annotations.Test;

/**
 * @author Anatoliy Bazko
 */
public class GdbDeleteTest {

    @Test
    public void testParse() throws Exception {
        GdbOutput gdbOutput = GdbOutput.of("Delete all breakpoints? (y or n) [answered Y; input not from terminal]\n");

        GdbDelete.parse(gdbOutput);
    }

    @Test(expectedExceptions = GdbParseException.class)
    public void testParseFail() throws Exception {
        GdbOutput gdbOutput = GdbOutput.of("some text");

        GdbDelete.parse(gdbOutput);
    }
}
