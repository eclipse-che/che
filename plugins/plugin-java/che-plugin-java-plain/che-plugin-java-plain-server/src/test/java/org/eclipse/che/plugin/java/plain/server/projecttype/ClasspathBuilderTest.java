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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.che.plugin.java.plain.server.BaseTest;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Valeriy Svydenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ClasspathBuilderTest extends BaseTest {
    private final static String SOURCE_FOLDER1 = "/src1";
    private final static String SOURCE_FOLDER2 = "/src/src2";
    private final static String LIBRARY        = "/projects/lib/lib1";

    @InjectMocks
    private ClasspathBuilder classpathBuilder;

    @Mock
    private IJavaProject iJavaProject;
    @Mock
    private IProject     iProject;

    private List<String> sourceFolders;
    private List<String> library;

    @BeforeMethod
    public void setUp() throws Exception {
        sourceFolders = new ArrayList<>();
        library = new ArrayList<>();

        when(iJavaProject.getProject()).thenReturn(iProject);
    }

    @Test
    public void classpathShouldBeUpdatedOnlyWithJREContainer() throws Exception {
        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, library);

        ArgumentCaptor<IClasspathEntry[]> classpathEntries = ArgumentCaptor.forClass(IClasspathEntry[].class);
        verify(iJavaProject).setRawClasspath(classpathEntries.capture(), eq(null));

        assertEquals(1, classpathEntries.getValue().length);
        assertEquals(new Path(JREContainerInitializer.JRE_CONTAINER), classpathEntries.getValue()[0].getPath());
    }

    @Test
    public void sourceFoldersShouldBeAddedToClasspath() throws Exception {
        IFolder sourceFolder1 = mock(IFolder.class);
        IFolder sourceFolder2 = mock(IFolder.class);

        when(iProject.getFolder(SOURCE_FOLDER1)).thenReturn(sourceFolder1);
        when(iProject.getFolder(SOURCE_FOLDER2)).thenReturn(sourceFolder2);
        when(sourceFolder1.exists()).thenReturn(true);
        when(sourceFolder1.getFullPath()).thenReturn(new Path(SOURCE_FOLDER1));
        when(sourceFolder2.exists()).thenReturn(true);
        when(sourceFolder2.getFullPath()).thenReturn(new Path(SOURCE_FOLDER2));

        sourceFolders.add(SOURCE_FOLDER1);
        sourceFolders.add(SOURCE_FOLDER2);

        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, library);

        verify(iProject, times(2)).getFolder(anyString());

        ArgumentCaptor<IClasspathEntry[]> classpathEntriesCapture = ArgumentCaptor.forClass(IClasspathEntry[].class);
        verify(iJavaProject).setRawClasspath(classpathEntriesCapture.capture(), eq(null));

        List<IClasspathEntry> classpathEntries = asList(classpathEntriesCapture.getValue());
        assertEquals(3, classpathEntries.size());
        assertThat(classpathEntries).onProperty("path").containsOnly(new Path(JREContainerInitializer.JRE_CONTAINER),
                                                                     new Path(SOURCE_FOLDER1),
                                                                     new Path(SOURCE_FOLDER2));
    }

    @Test
    public void folderShouldNotBeAddedToClasspathIfItNotExist() throws Exception {
        IFolder sourceFolder1 = mock(IFolder.class);

        when(iProject.getFolder(SOURCE_FOLDER1)).thenReturn(sourceFolder1);
        when(sourceFolder1.exists()).thenReturn(false);

        sourceFolders.add(SOURCE_FOLDER1);

        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, library);

        verify(iProject).getFolder(anyString());

        verifyIfOnlyJREContainerInClasspath();
    }

    @Test
    public void libraryFolderShouldNotBeAddedIfListOfLibrariesIsNull() throws Exception {
        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, null);

        verifyIfOnlyJREContainerInClasspath();
    }

    @Test
    public void libraryFolderShouldNotBeAddedIfItIsEmpty() throws Exception {
        library.add("");

        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, library);

        verifyIfOnlyJREContainerInClasspath();
    }

    @Test
    public void libraryFolderShouldNotBeAddedIfItIsNotExist() throws Exception {
        library.add(LIBRARY);
        IFolder libraryFolder1 = mock(IFolder.class);

        when(iProject.getFolder(LIBRARY)).thenReturn(libraryFolder1);
        when(libraryFolder1.exists()).thenReturn(false);

        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, library);

        verifyIfOnlyJREContainerInClasspath();
    }

    @Test
    public void elementShouldNotBeAddedAsLibToClasspathIfItIsFolder() throws Exception {
        library.add(LIBRARY);
        IFolder libraryFolder1 = mock(IFolder.class);
        IResourceProxy iResourceProxy = mock(IResourceProxy.class);

        when(iProject.getFolder(LIBRARY)).thenReturn(libraryFolder1);
        when(libraryFolder1.exists()).thenReturn(true);
        when(iResourceProxy.getType()).thenReturn(IResource.FOLDER);

        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, library);

        ArgumentCaptor<IResourceProxyVisitor> resourceProxyVisitorArgumentCaptor = ArgumentCaptor.forClass(IResourceProxyVisitor.class);
        verify(libraryFolder1).accept(resourceProxyVisitorArgumentCaptor.capture(), eq(IContainer.INCLUDE_PHANTOMS));

        resourceProxyVisitorArgumentCaptor.getValue().visit(iResourceProxy);
        verify(iResourceProxy).getType();

        verifyIfOnlyJREContainerInClasspath();
    }

    @Test
    public void elementShouldNotBeAddedAsLibToClasspathIfItIsNotJar() throws Exception {
        library.add(LIBRARY);
        IFolder libraryFolder1 = mock(IFolder.class);
        IResourceProxy iResourceProxy = mock(IResourceProxy.class);

        when(iProject.getFolder(LIBRARY)).thenReturn(libraryFolder1);
        when(libraryFolder1.exists()).thenReturn(true);
        when(iResourceProxy.getType()).thenReturn(IResource.FILE);
        when(iResourceProxy.requestFullPath()).thenReturn(new Path(LIBRARY));

        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, library);

        ArgumentCaptor<IResourceProxyVisitor> resourceProxyVisitorArgumentCaptor = ArgumentCaptor.forClass(IResourceProxyVisitor.class);
        verify(libraryFolder1).accept(resourceProxyVisitorArgumentCaptor.capture(), eq(IContainer.INCLUDE_PHANTOMS));

        resourceProxyVisitorArgumentCaptor.getValue().visit(iResourceProxy);
        verify(iResourceProxy).getType();

        verifyIfOnlyJREContainerInClasspath();
    }

    @Test
    public void elementShouldBeAddedAsLibToClasspathFromLibFolder() throws Exception {
        Path jarPath = new Path(LIBRARY + "/a.jar");

        library.add(LIBRARY);
        IFolder libraryFolder1 = mock(IFolder.class);
        IResourceProxy iResourceProxy = mock(IResourceProxy.class);
        IResource iResource = mock(IResource.class);

        when(iProject.getFolder(LIBRARY)).thenReturn(libraryFolder1);
        when(libraryFolder1.exists()).thenReturn(true);
        when(iResourceProxy.getType()).thenReturn(IResource.FILE);
        when(iResourceProxy.requestFullPath()).thenReturn(jarPath);
        when(iResourceProxy.requestResource()).thenReturn(iResource);
        when(iResource.getLocation()).thenReturn(jarPath);

        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, library);

        ArgumentCaptor<IResourceProxyVisitor> resourceProxyVisitorArgumentCaptor = ArgumentCaptor.forClass(IResourceProxyVisitor.class);
        verify(libraryFolder1).accept(resourceProxyVisitorArgumentCaptor.capture(), eq(IContainer.INCLUDE_PHANTOMS));

        resourceProxyVisitorArgumentCaptor.getValue().visit(iResourceProxy);
        verify(iResourceProxy).getType();

        assertEquals(jarPath, iResource.getLocation());
    }

    @Test
    public void rawClasspathShouldBeContained3Arguments() throws Exception {
        createTestProject();

        library.add("/lib");
        sourceFolders.add("/src");

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("/project");
        IJavaProject iJavaProject = JavaCore.create(project);

        classpathBuilder.generateClasspath(iJavaProject, sourceFolders, library);

        List<IClasspathEntry> classpathEntries = Arrays.asList(iJavaProject.getRawClasspath());
        assertThat(classpathEntries).onProperty("path").containsOnly(new Path(JREContainerInitializer.JRE_CONTAINER),
                                                                     new Path("/project/src"),
                                                                     new Path(root + "/project/lib/a.jar"));
    }

    private void verifyIfOnlyJREContainerInClasspath() throws JavaModelException {
        ArgumentCaptor<IClasspathEntry[]> classpathEntriesCapture = ArgumentCaptor.forClass(IClasspathEntry[].class);
        verify(iJavaProject).setRawClasspath(classpathEntriesCapture.capture(), eq(null));

        List<IClasspathEntry> classpathEntries = asList(classpathEntriesCapture.getValue());

        assertEquals(1, classpathEntries.size());
        assertThat(classpathEntries).onProperty("path").containsOnly(new Path(JREContainerInitializer.JRE_CONTAINER));
    }

}
