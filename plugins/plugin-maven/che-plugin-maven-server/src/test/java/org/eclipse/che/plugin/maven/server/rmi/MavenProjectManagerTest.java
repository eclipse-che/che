/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.rmi;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import org.eclipse.che.maven.data.MavenConstants;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.plugin.maven.server.MavenServerManager;
import org.eclipse.che.plugin.maven.server.MavenWrapperManager;
import org.eclipse.che.plugin.maven.server.core.EclipseWorkspaceProvider;
import org.eclipse.che.plugin.maven.server.core.MavenProjectListener;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.MavenTerminalImpl;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.server.core.project.MavenProjectModifications;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Path;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Evgen Vidolob */
@Listeners(value = {MockitoTestNGListener.class})
public class MavenProjectManagerTest {

  private final String mavenServerPath =
      MavenProjectManagerTest.class.getResource("/maven-server").getPath();

  private MavenServerManager manager =
      new MavenServerManager(
          mavenServerPath, "-XX:MaxRAM=128m -XX:MaxRAMFraction=1 -XX:+UseParallelGC");

  private MavenProjectManager projectManager;

  @Mock private IProject project;

  @Mock private IFile pom;

  @Mock private MavenProjectListener listener;

  @Mock private EclipseWorkspaceProvider workspaceProvider;

  @Mock private IWorkspace workspace;

  @Mock private IWorkspaceRoot workspaceRoot;

  @Captor private ArgumentCaptor<Map<MavenProject, MavenProjectModifications>> mapArgument;

  @BeforeMethod
  public void setUp() throws Exception {
    MavenWrapperManager wrapperManager = new MavenWrapperManager(manager);
    projectManager =
        new MavenProjectManager(
            wrapperManager,
            manager,
            new MavenTerminalImpl(),
            new MavenServerManagerTest.MyMavenServerProgressNotifier(),
            workspaceProvider);
    when(workspaceProvider.get()).thenReturn(workspace);
    when(workspace.getRoot()).thenReturn(workspaceRoot);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    manager.shutdown();
  }

  @Test
  public void testResolveProject() throws Exception {
    when(project.getFile(MavenConstants.POM_FILE_NAME)).thenReturn(pom);
    when(pom.getLocation())
        .thenReturn(
            new Path(MavenProjectManagerTest.class.getResource("/FirstProject/pom.xml").getFile()));
    when(pom.getFullPath()).thenReturn(new Path("/FirstProject/pom.xml"));
    when(project.getFullPath()).thenReturn(new Path("/FirstProject/"));

    projectManager.addListener(listener);
    MavenProject mavenProject = new MavenProject(project, workspace);
    mavenProject.read(project, manager);
    MavenKey mavenKey = mavenProject.getMavenKey();
    assertThat(mavenKey).isNotNull();

    projectManager.resolveMavenProject(project, mavenProject);
    verify(listener).projectResolved(any(), any());
  }

  @Test
  public void testResolveProjectWithProfiles() throws Exception {
    when(project.getFile(MavenConstants.POM_FILE_NAME)).thenReturn(pom);
    when(pom.getLocation())
        .thenReturn(
            new Path(
                MavenProjectManagerTest.class
                    .getResource("/multi-module-with-profiles/pom.xml")
                    .getFile()));
    when(pom.getFullPath()).thenReturn(new Path("/multi-module-with-profiles/pom.xml"));
    when(project.getFullPath()).thenReturn(new Path("/multi-module-with-profiles/"));

    projectManager.addListener(listener);
    MavenProject mavenProject = new MavenProject(project, workspace);
    mavenProject.read(project, manager);
    MavenKey mavenKey = mavenProject.getMavenKey();
    assertThat(mavenKey).isNotNull();

    projectManager.resolveMavenProject(project, mavenProject);
    verify(listener).projectResolved(any(), any());
    assertThat(mavenProject.getModules().size()).isEqualTo(3);
  }

  @Test
  public void testNotValidResolveProject() throws Exception {
    when(project.getFile(MavenConstants.POM_FILE_NAME)).thenReturn(pom);
    when(pom.getLocation())
        .thenReturn(
            new Path(MavenProjectManagerTest.class.getResource("/BadProject/pom.xml").getFile()));
    when(pom.getFullPath()).thenReturn(new Path("/BadProject/pom.xml"));
    when(project.getFullPath()).thenReturn(new Path("/BadProject"));

    projectManager.addListener(listener);
    MavenProject mavenProject = new MavenProject(project, workspace);
    mavenProject.read(project, manager);
    MavenKey mavenKey = mavenProject.getMavenKey();
    assertThat(mavenKey).isNotNull();

    projectManager.resolveMavenProject(project, mavenProject);
    verify(listener).projectResolved(any(), any());
    assertThat(mavenProject.getProblems()).isNotNull().isNotEmpty();
    mavenProject.getProblems().forEach(System.out::println);
  }

  @Test
  public void testUpdateProject() throws Exception {
    when(project.getFile(MavenConstants.POM_FILE_NAME)).thenReturn(pom);
    when(pom.getLocation())
        .thenReturn(
            new Path(MavenProjectManagerTest.class.getResource("/FirstProject/pom.xml").getFile()));
    when(pom.getFullPath()).thenReturn(new Path("/FirstProject/pom.xml"));
    when(project.getFullPath()).thenReturn(new Path("/FirstProject/"));

    projectManager.addListener(listener);
    MavenProject mavenProject = new MavenProject(project, workspace);
    mavenProject.read(project, manager);
    MavenKey mavenKey = mavenProject.getMavenKey();
    assertThat(mavenKey).isNotNull();

    projectManager.update(Collections.singletonList(project), true);
    verify(listener).projectUpdated(mapArgument.capture(), any());
  }

  @Test
  public void testUpdateMultimoduleProject() throws Exception {
    IProject testProject = mock(IProject.class);
    IFile testPom = mock(IFile.class);
    IProject subModuleProject = mock(IProject.class);
    IFile subPom = mock(IFile.class);
    IFile testFile = mock(IFile.class);

    when(project.getFile(MavenConstants.POM_FILE_NAME)).thenReturn(pom);
    when(pom.getLocation())
        .thenReturn(
            new Path(
                MavenProjectManagerTest.class
                    .getResource("/multimoduleProject/pom.xml")
                    .getFile()));
    when(pom.getFullPath()).thenReturn(new Path("/multimoduleProject/pom.xml"));
    when(project.getFullPath()).thenReturn(new Path("/multimoduleProject/"));

    when(testProject.getFile(MavenConstants.POM_FILE_NAME)).thenReturn(testPom);
    when(subModuleProject.getFile(MavenConstants.POM_FILE_NAME)).thenReturn(subPom);

    when(testProject.getFullPath()).thenReturn(new Path("/multimoduleProject/test"));
    when(subModuleProject.getFullPath()).thenReturn(new Path("/multimoduleProject/subModule"));

    when(testPom.getFullPath()).thenReturn(new Path("/multimoduleProject/test/pom.xml"));
    when(subPom.getFullPath()).thenReturn(new Path("/multimoduleProject/subModule/pom.xml"));

    when(testPom.getLocation())
        .thenReturn(
            new Path(
                MavenProjectManagerTest.class
                    .getResource("/multimoduleProject/test/pom.xml")
                    .getFile()));
    when(subPom.getLocation())
        .thenReturn(
            new Path(
                MavenProjectManagerTest.class
                    .getResource("/multimoduleProject/subModule/pom.xml")
                    .getFile()));
    when(workspaceRoot.getProject("/multimoduleProject/test")).thenReturn(testProject);
    when(workspaceRoot.getProject("/multimoduleProject/subModule")).thenReturn(subModuleProject);
    when(workspaceRoot.getFile(any())).thenReturn(testFile);
    when(testFile.exists()).thenReturn(false);

    projectManager.addListener(listener);
    MavenProject mavenProject = new MavenProject(project, workspace);
    mavenProject.read(project, manager);
    MavenKey mavenKey = mavenProject.getMavenKey();
    assertThat(mavenKey).isNotNull();

    projectManager.update(Collections.singletonList(project), true);
    verify(listener).projectUpdated(mapArgument.capture(), any());
  }
}
