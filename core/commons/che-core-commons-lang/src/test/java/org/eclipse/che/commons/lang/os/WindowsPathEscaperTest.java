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
package org.eclipse.che.commons.lang.os;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Garagatyi
 */
public class WindowsPathEscaperTest {
    /**
     * E.g from https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
     *
     * Users should be /Users
     * /Users should be /Users
     * c/Users should be /c/Users
     * /c/Users should be /c/Users
     * c:/Users should be /c/Users
     */
    @Test(dataProvider = "pathProvider")
    public void shouldStaticallyEscapePathForWindowsHost(String windowsPath, String expectedPath) {
        assertEquals(WindowsPathEscaper.escapePathStatic(windowsPath), expectedPath);
    }

    /**
     * E.g from https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
     *
     * Users should be /Users
     * /Users should be /Users
     * c/Users should be /c/Users
     * /c/Users should be /c/Users
     * c:/Users should be /c/Users
     */
    @Test(dataProvider = "pathProvider")
    public void shouldNonStaticallyEscapePathForWindowsHost(String windowsPath, String expectedPath) {
        WindowsPathEscaper windowsPathEscaper = new WindowsPathEscaper();
        assertEquals(windowsPathEscaper.escapePath(windowsPath), expectedPath);
    }

    @DataProvider(name = "pathProvider")
    public static Object[][] pathProvider() {
        return new Object[][] {
                {"Users", "/Users"},
                {"/Users", "/Users"},
                {"c/Users", "/c/Users"},
                {"/c/Users", "/c/Users"},
                {"c:/Users", "/c/Users"},
                {"C:/Users", "/c/Users"},
                {"C:/Users/path/dir/from/host:/name/of/dir/in/container",
                 "/c/Users/path/dir/from/host:/name/of/dir/in/container"}
        };
    }
}
