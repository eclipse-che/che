/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.testing;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Set;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Test for {@link ProjectClasspathProvider} */
@Listeners(value = {MockitoTestNGListener.class})
public class ProjectClasspathProviderTest {

  private static final String PROJECTS_PATH = "/projects";

  private static ResourcesPlugin RESOURCE_PLUGIN =
      new ResourcesPlugin(
          "target/test-classes/index",
          new DummyProvider(new File(PROJECTS_PATH)),
          () -> null,
          () -> null,
          () -> null);

  @Mock private IJavaProject javaProject;

  private ProjectClasspathProvider classpathProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    classpathProvider = new ProjectClasspathProvider(new DummyProvider(new File(PROJECTS_PATH)));
  }

  @Test
  public void classpathProviderShouldProvideClasspathPaths() throws Exception {
    IClasspathEntry classpathEntry = mock(IClasspathEntry.class);
    when(classpathEntry.getEntryKind()).thenReturn(IClasspathEntry.CPE_SOURCE);
    IPath path = new Path("/testProject/target/classes");
    when(classpathEntry.getOutputLocation()).thenReturn(path);

    IClasspathEntry[] entries = new IClasspathEntry[] {classpathEntry};
    when(javaProject.getResolvedClasspath(false)).thenReturn(entries);

    Set<String> classPath = classpathProvider.getProjectClassPath(javaProject);
    assertThat(classPath)
        .isNotNull()
        .isNotEmpty()
        .contains(PROJECTS_PATH + "/testProject/target/classes");
  }

  @Test
  public void classpathProviderShouldProvideClasspathPathsWithExternalDependencies()
      throws Exception {
    IClasspathEntry classpathEntry = mock(IClasspathEntry.class);
    when(classpathEntry.getEntryKind()).thenReturn(IClasspathEntry.CPE_SOURCE);
    IPath path = new Path("/testProject/target/classes");
    when(classpathEntry.getOutputLocation()).thenReturn(path);

    IClasspathEntry jarClasspathEntry = mock(IClasspathEntry.class);
    when(jarClasspathEntry.getEntryKind()).thenReturn(IClasspathEntry.CPE_LIBRARY);
    IPath jarPath = new Path("/absolute/path/to/jar.file");
    when(jarClasspathEntry.getPath()).thenReturn(jarPath);

    IClasspathEntry[] entries = new IClasspathEntry[] {classpathEntry, jarClasspathEntry};
    when(javaProject.getResolvedClasspath(false)).thenReturn(entries);

    Set<String> classPath = classpathProvider.getProjectClassPath(javaProject);

    assertThat(classPath)
        .isNotNull()
        .isNotEmpty()
        .contains(PROJECTS_PATH + "/testProject/target/classes", "/absolute/path/to/jar.file");
  }

  @Test
  public void classpathProviderShouldProvideClasspathPathsWithAnotherProject() throws Exception {
    JavaModel model = mock(JavaModel.class);
    Field javaModel = JavaModelManager.class.getDeclaredField("javaModel");
    javaModel.setAccessible(true);
    javaModel.set(JavaModelManager.getJavaModelManager(), model);
    IClasspathEntry entry =
        mockClasspathEntry(
            IClasspathEntry.CPE_SOURCE, "/anotherProject/src", "/anotherProject/target/classes");

    IJavaProject anotherProject = mock(IJavaProject.class);
    when(anotherProject.getResolvedClasspath(false)).thenReturn(new IClasspathEntry[] {entry});
    when(model.getJavaProject("/anotherProject")).thenReturn(anotherProject);

    IClasspathEntry classpathEntry =
        mockClasspathEntry(IClasspathEntry.CPE_SOURCE, "", "/testProject/target/classes");
    IClasspathEntry jarClasspathEntry =
        mockClasspathEntry(IClasspathEntry.CPE_LIBRARY, "/absolute/path/to/jar.file", null);
    IClasspathEntry projectEntry =
        mockClasspathEntry(IClasspathEntry.CPE_PROJECT, "/anotherProject", null);

    IClasspathEntry[] entries =
        new IClasspathEntry[] {classpathEntry, jarClasspathEntry, projectEntry};
    when(javaProject.getResolvedClasspath(false)).thenReturn(entries);

    Set<String> classPath = classpathProvider.getProjectClassPath(javaProject);

    assertThat(classPath)
        .isNotNull()
        .isNotEmpty()
        .contains(
            PROJECTS_PATH + "/testProject/target/classes",
            "/absolute/path/to/jar.file",
            PROJECTS_PATH + "/anotherProject/target/classes");
  }

  private IClasspathEntry mockClasspathEntry(int kind, String path, String outputPath) {
    IClasspathEntry result = mock(IClasspathEntry.class);
    when(result.getEntryKind()).thenReturn(kind);
    when(result.getPath()).thenReturn(new Path(path));
    if (outputPath != null) {
      when(result.getOutputLocation()).thenReturn(new Path(outputPath));
    }
    return result;
  }

  private static class DummyProvider extends RootDirPathProvider {
    public DummyProvider(File file) {
      this.rootFile = file;
    }
  }
}
