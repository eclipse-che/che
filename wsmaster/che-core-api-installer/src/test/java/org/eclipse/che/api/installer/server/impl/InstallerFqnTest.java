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

import org.eclipse.che.api.installer.server.exception.IllegalInstallerKey;
import org.eclipse.che.api.installer.server.impl.LocalInstallerRegistry.InstallerFqn;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test for {@link InstallerFqn}.
 *
 * @author Sergii Leshchenko
 */
public class InstallerFqnTest {
    @Test
    public void testInstallerFqnWithIdAndVersion() {
        InstallerFqn installerFqn = InstallerFqn.parse("id:1");

        assertEquals(installerFqn.getId(), "id");
        assertEquals(installerFqn.getVersion(), "1");
    }

    @Test
    public void testParseInstallerFqnWithId() {
        InstallerFqn agentKey = InstallerFqn.parse("id");

        assertEquals(agentKey.getId(), "id");
        assertEquals(agentKey.getVersion(), "latest");
    }

    @Test(expectedExceptions = IllegalInstallerKey.class)
    public void testParseInstallerFqnFails() {
        InstallerFqn.parse("id:1:2");
    }
}
