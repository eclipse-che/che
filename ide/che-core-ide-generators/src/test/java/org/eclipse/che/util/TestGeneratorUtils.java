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
package org.eclipse.che.util;

import org.junit.Test;

import java.io.IOException;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a> */
public class TestGeneratorUtils {

    /** Should match package name */
    @Test
    public void shouldMatchPackage() {
        String packageString = "package org.eclipse.che.ide.util;" + "import junit.framework.Assert;";
        Matcher matcher = GeneratorUtils.PACKAGE_PATTERN.matcher(packageString);
        assertTrue(matcher.matches());
        assertEquals(1, matcher.groupCount());
        String group = matcher.group(1);

        assertEquals("org.eclipse.che.ide.util", group);
    }

    /**
     * Should match package name
     *
     * @throws IOException
     */
    @Test
    public void shouldExtractPackage() throws IOException {
        String packageString = "package org.eclipse.che.ide.util;" + "import junit.framework.Assert;";
        assertEquals("org.eclipse.che.ide.util", GeneratorUtils.getClassFQN("dummy", packageString));
    }
}
