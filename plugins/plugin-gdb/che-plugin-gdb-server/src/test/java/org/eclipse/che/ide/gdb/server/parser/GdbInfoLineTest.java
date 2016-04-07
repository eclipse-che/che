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
package org.eclipse.che.ide.gdb.server.parser;

import org.eclipse.che.ide.ext.debugger.shared.Location;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class GdbInfoLineTest {

    @Test
    public void testParse() throws Exception {
        GdbOutput gdbOutput = GdbOutput.of("Line 6 of \"h.cpp\" starts at address 0x4008ae <main()+17> and ends at 0x4008ca <main()+45>.\n");

        GdbInfoLine gdbInfoLine = GdbInfoLine.parse(gdbOutput);
        Location location = gdbInfoLine.getLocation();

        assertEquals(location.getClassName(), "h.cpp");
        assertEquals(location.getLineNumber(), 6);
    }
}