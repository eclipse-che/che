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
package org.eclipse.che.plugin.java.plain.server.generator;

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.plugin.java.plain.server.BaseTest;
import org.eclipse.che.plugin.java.plain.server.projecttype.ClasspathBuilder;
import org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_LIBRARY_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Valeriy Svydenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class PlainJavaProjectGeneratorTest extends BaseTest {
    @InjectMocks
    private PlainJavaProjectGenerator generator;

    @Mock
    private ClasspathBuilder classpathBuilder;

    private FolderEntry                 baseFolder;
    private Map<String, AttributeValue> attributes;
    private Map<String, String>         options;

    @Test
    public void checkProjectTypeId() throws Exception {
        assertEquals(PlainJavaProjectConstants.JAVAC_PROJECT_ID, generator.getProjectType());
    }

    @BeforeMethod
    public void setUp() throws Exception {
        baseFolder = new FolderEntry(vfsProvider.getVirtualFileSystem().getRoot().createFolder("project"), projectRegistry);
    }

    @AfterMethod
    public void clearProject() throws Exception {
        vfsProvider.getVirtualFileSystem().getRoot().getChild(Path.of("project")).delete();
    }

    @Test
    public void projectShouldBeCreatedWithDefaultContent() throws Exception {
        attributes = new HashMap<>();
        options = new HashMap<>();

        generator.onCreateProject(baseFolder, attributes, options);

        IJavaProject project = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject("project");

        assertTrue(project.exists());
        verify(classpathBuilder).generateClasspath(project,
                                                   singletonList(DEFAULT_SOURCE_FOLDER_VALUE),
                                                   singletonList(DEFAULT_LIBRARY_FOLDER_VALUE));
    }

    @Test
    public void projectShouldBeCreatedWithCustomSourceFolders() throws Exception {
        attributes = new HashMap<>();
        options = new HashMap<>();

        attributes.put(Constants.SOURCE_FOLDER, new AttributeValue("src2"));
        generator.onCreateProject(baseFolder, attributes, options);

        IJavaProject project = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject("project");

        VirtualFileEntry mainClass = baseFolder.getChild("/src2/Main.java");
        assertNotNull(mainClass);
        assertTrue(project.exists());
        verify(classpathBuilder).generateClasspath(project,
                                                   singletonList("src2"),
                                                   singletonList(DEFAULT_LIBRARY_FOLDER_VALUE));
    }
}
