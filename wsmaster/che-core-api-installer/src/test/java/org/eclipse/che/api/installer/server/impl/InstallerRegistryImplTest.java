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

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.installer.server.model.impl.InstallerKeyImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class InstallerRegistryImplTest {

    private InstallerRegistry registry;

    @BeforeMethod
    public void setUp() throws Exception {
        registry = new LocalInstallerRegistry(new HashSet<Installer>() {{
            add(DtoFactory.newDto(InstallerDto.class).withId("id1").withVersion("v1").withName("id1:v1"));
            add(DtoFactory.newDto(InstallerDto.class).withId("id1").withVersion("v2").withName("id1:v2"));
            add(DtoFactory.newDto(InstallerDto.class).withId("id2").withName("id2:latest"));
            add(DtoFactory.newDto(InstallerDto.class).withId("id3").withVersion("v1").withName("id3:v1"));
            add(DtoFactory.newDto(InstallerDto.class).withId("id3").withName("id3:latest"));
        }});
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldNotRegisterInstallerWithSameIdAndVersion() throws Exception {
        new LocalInstallerRegistry(new HashSet<Installer>() {{
            add(DtoFactory.newDto(InstallerDto.class).withId("id1").withVersion("v1").withScript("s1"));
            add(DtoFactory.newDto(InstallerDto.class).withId("id1").withVersion("v1").withScript("s2"));
        }});
    }

    @Test(dataProvider = "versions")
    public void shouldReturnVersionsById(String id, Set<String> expectedVersions) throws Exception {
        List<String> versions = registry.getVersions(id);

        assertEquals(versions.size(), expectedVersions.size());
        for (String v : expectedVersions) {
            assertTrue(versions.contains(v));
        }
    }

    @DataProvider(name = "versions")
    public static Object[][] versions() {
        return new Object[][] {{"id1", ImmutableSet.of("v1", "v2")},
                               {"id2", ImmutableSet.of("latest")},
                               {"id3", ImmutableSet.of("v1", "latest")}};
    }

    @Test
    public void shouldReturnAllInstallers() throws Exception {
        Collection<Installer> installers = registry.getInstallers();

        assertEquals(installers.size(), 5);
    }

    @Test(dataProvider = "InstallerKeys")
    public void shouldReturnInstallerByIdAndVersion(String id, String version) throws Exception {
        Installer installer = registry.getInstaller(new InstallerKeyImpl(id, version));

        assertNotNull(installer);
        assertEquals(installer.getName(), String.format("%s:%s", id, (version == null ? "latest" : version)));
    }


    @DataProvider(name = "InstallerKeys")
    public static Object[][] InstallerKeys() {
        return new String[][] {{"id1", "v1"},
                               {"id1", "v2"},
                               {"id2", null},
                               {"id2", "latest"},
                               {"id3", "v1"},
                               {"id3", null},
                               {"id3", "latest"}};
    }
}
