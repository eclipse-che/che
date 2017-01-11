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
public class GdbClearTest {

    @Test
    public void testParse() throws Exception {
        GdbOutput gdbOutput = GdbOutput.of("Deleted breakpoint 1\n");

        GdbClear.parse(gdbOutput);
    }

    @Test(expectedExceptions = GdbParseException.class)
    public void testParseFail() throws Exception {
        GdbOutput gdbOutput = GdbOutput.of("some text");
        GdbClear.parse(gdbOutput);
    }
}
