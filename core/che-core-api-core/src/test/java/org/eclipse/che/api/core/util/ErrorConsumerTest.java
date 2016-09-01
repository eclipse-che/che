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

import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Anatolii Bazko
 */
public class ErrorConsumerTest {

    @Test
    public void testRedirect() throws Exception {
        LineConsumer lineConsumer = mock(LineConsumer.class);

        ErrorConsumer errorConsumer = new ErrorConsumer(lineConsumer);

        errorConsumer.writeLine("Line 1");
        errorConsumer.writeLine("Line 2");
        errorConsumer.writeLine("[STDERR] Line 3");
        errorConsumer.writeLine("[STDERR] Line 4");
        errorConsumer.writeLine("Line 5");

        verify(lineConsumer).writeLine("[STDERR] Line 3");
        verify(lineConsumer).writeLine("[STDERR] Line 4");
    }
}
