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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import com.google.inject.Provider;

import org.eclipse.che.api.project.server.FileEntry;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.plugin.java.plain.server.BaseTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class PlainJavaValueProviderFactoryTest extends BaseTest {
    @InjectMocks
    PlainJavaValueProviderFactory plainJavaValueProviderFactory;

    @Mock
    private FolderEntry               rootProjectFolder;
    @Mock
    private FileEntry                 fileEntry;
    @Mock
    private Provider<ProjectRegistry> projectRegistryProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        when(fileEntry.getName()).thenReturn("Main.java");
        when(rootProjectFolder.getChildFiles()).thenReturn(Collections.singletonList(fileEntry));
        when(rootProjectFolder.getProject()).thenReturn("project");
    }

    @Test
    public void attributeShouldBeSet() throws Exception {
        Map<String, List<String>> attributes = new HashMap<>();
        RegisteredProject registeredProject = mock(RegisteredProject.class);
        ProjectRegistry pr = mock(ProjectRegistry.class);

        when(projectRegistryProvider.get()).thenReturn(pr);
        when(pr.getProject(anyString())).thenReturn(registeredProject);
        when(registeredProject.getAttributes()).thenReturn(attributes);
        plainJavaValueProviderFactory.newInstance(rootProjectFolder).setValues(SOURCE_FOLDER, Collections.singletonList("src"));

        assertThat(attributes.get(SOURCE_FOLDER).contains("src"));
    }

    @Test
    public void newValueOfAttributeShouldBeAdded() throws Exception {
        Map<String, List<String>> attributes = new HashMap<>();

        attributes.put(SOURCE_FOLDER, Arrays.asList("src1", "src2"));
        RegisteredProject registeredProject = mock(RegisteredProject.class);
        ProjectRegistry pr = mock(ProjectRegistry.class);

        when(projectRegistryProvider.get()).thenReturn(pr);
        when(pr.getProject(anyString())).thenReturn(registeredProject);
        when(registeredProject.getAttributes()).thenReturn(attributes);
        List<String> sources = new ArrayList<>();
        sources.add("src3");
        plainJavaValueProviderFactory.newInstance(rootProjectFolder).setValues(SOURCE_FOLDER, sources);

        assertThat(attributes.get(SOURCE_FOLDER).containsAll(Arrays.asList("src3", "src1", "src2")));
    }


    @Test
    public void sourceFolderShouldBeReturned() throws Exception {
        when(rootProjectFolder.getPath()).thenReturn(Path.of("project"));

        assertThat(plainJavaValueProviderFactory.newInstance(rootProjectFolder).getValues(SOURCE_FOLDER)).contains("/project");
    }

    @Test
    public void outputFolderShouldBeReturned() throws Exception {
        when(rootProjectFolder.getPath()).thenReturn(Path.of("/project"));

        assertThat(plainJavaValueProviderFactory.newInstance(rootProjectFolder).getValues(OUTPUT_FOLDER)).contains("bin");
    }
}
