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
package org.eclipse.che.api.vfs.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;

public class ZipContentTest {

//    @Rule
    public ExpectedException thrown = ExpectedException.none();

//    @Test
    public void failsWhenDetectZipBomb() throws Exception {
        try (InputStream fileIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("zipbomb.zip")) {
            thrown.expect(IOException.class);
            thrown.expectMessage("Zip bomb detected");

            ZipContent.of(fileIn);
        }
    }
}