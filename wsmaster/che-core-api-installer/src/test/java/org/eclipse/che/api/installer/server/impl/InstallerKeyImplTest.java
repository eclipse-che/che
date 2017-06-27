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
package org.eclipse.che.api.installer.server.impl;

import org.eclipse.che.api.installer.server.model.impl.InstallerKeyImpl;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Anatolii Bazko
 */
public class InstallerKeyImplTest {

    @Test
    public void testInstallerKeyWithNameAndVersion() {
        InstallerKeyImpl installerKey = InstallerKeyImpl.parse("id:1");

        assertEquals(installerKey.getId(), "id");
        assertEquals(installerKey.getVersion(), "1");
    }

    @Test
    public void testParseInstallerKeyWithId() {
        InstallerKeyImpl installerKey = InstallerKeyImpl.parse("id");

        assertEquals(installerKey.getId(), "id");
        assertEquals(installerKey.getVersion(), "latest");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseInstallerKeyFails() {
        InstallerKeyImpl.parse("id:1:2");
    }
}
