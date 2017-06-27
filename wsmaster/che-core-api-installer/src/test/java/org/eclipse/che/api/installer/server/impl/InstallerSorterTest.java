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

import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.server.model.impl.InstallerKeyImpl;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.installer.shared.model.InstallerKey;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class InstallerSorterTest {

    @Mock
    private InstallerRegistry installerRegistry;
    @Mock
    private Installer         installer1;
    @Mock
    private Installer         installer2;
    @Mock
    private Installer         installer3;

    @InjectMocks
    private InstallerSorter installerSorter;

    @BeforeMethod
    public void setUp() throws Exception {
        when(installerRegistry.getInstaller(eq(InstallerKeyImpl.parse("fqn1")))).thenReturn(installer1);
        when(installerRegistry.getInstaller(eq(InstallerKeyImpl.parse("fqn2")))).thenReturn(installer2);
        when(installerRegistry.getInstaller(eq(InstallerKeyImpl.parse("fqn3")))).thenReturn(installer3);
        when(installerRegistry.getInstaller(eq(InstallerKeyImpl.parse("fqn4")))).thenThrow(new InstallerNotFoundException("Installer not found"));

        when(installer1.getDependencies()).thenReturn(singletonList("fqn3"));
        when(installer1.getId()).thenReturn("fqn1");

        when(installer2.getDependencies()).thenReturn(singletonList("fqn3"));
        when(installer2.getId()).thenReturn("fqn2");

        when(installer3.getId()).thenReturn("fqn3");
    }

    @Test
    public void sortInstallersRespectingDependencies() throws Exception {
        List<InstallerKey> sorted = installerSorter.sort(Arrays.asList("fqn1", "fqn2", "fqn3"));

        assertEquals(sorted.size(), 3);
        assertEquals(sorted.get(0).getId(), "fqn3");
        assertEquals(sorted.get(1).getId(), "fqn1");
        assertEquals(sorted.get(2).getId(), "fqn2");
    }

    @Test(expectedExceptions = InstallerException.class, expectedExceptionsMessageRegExp = ".*fqn1.*fqn2.*")
    public void sortingShouldFailIfCircularDependenciesFound() throws Exception {
        when(installer1.getDependencies()).thenReturn(singletonList("fqn2"));
        when(installer2.getDependencies()).thenReturn(singletonList("fqn1"));

        installerSorter.sort(Arrays.asList("fqn1", "fqn2"));
    }

    @Test(expectedExceptions = InstallerNotFoundException.class)
    public void sortingShouldFailIfInstallerNotFound() throws Exception {
        installerSorter.sort(singletonList("fqn4"));
    }
}
