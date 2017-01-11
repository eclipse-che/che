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
package org.eclipse.che.ide.util;

import com.google.common.hash.Hashing;

import org.junit.Test;

import static java.nio.charset.Charset.defaultCharset;
import static org.junit.Assert.assertEquals;

/**
 * @author Valeriy Svydenko
 */
public class TextUtilsTest {
    private static final String TEXT = "to be or not to be";

    @Test
    public void textShouldBeEncodedInMD5Hash() {
        assertEquals(TextUtils.md5(TEXT), Hashing.md5().hashString(TEXT, defaultCharset()).toString());
    }
}
