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
package org.eclipse.che.api.core.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class ErrorConsumerTest {
    private ErrorConsumer errorDetector;

    @BeforeMethod
    public void setUp() throws Exception {
        errorDetector = new ErrorConsumer();
    }

    @Test
    public void shouldHaveError() throws Exception {
        errorDetector.writeLine("Line 1");
        errorDetector.writeLine("Line 2");
        errorDetector.writeLine("[STDERR] Line 3");
        errorDetector.writeLine("[STDERR] Line 4");
        errorDetector.writeLine("Line 5");

        assertTrue(errorDetector.hasError());
        assertEquals(errorDetector.getError(), "[STDERR] Line 3\n[STDERR] Line 4");
    }
}
