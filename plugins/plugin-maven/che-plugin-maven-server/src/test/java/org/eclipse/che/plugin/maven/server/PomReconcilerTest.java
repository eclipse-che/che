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
package org.eclipse.che.plugin.maven.server;

import com.google.gson.JsonObject;
import com.google.inject.Provider;

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.plugin.maven.server.core.EclipseWorkspaceProvider;
import org.eclipse.che.plugin.maven.server.core.MavenCommunication;
import org.eclipse.che.plugin.maven.server.core.MavenExecutorService;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.MavenWorkspace;
import org.eclipse.che.plugin.maven.server.core.classpath.ClasspathManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.server.rest.MavenServerService;
import org.eclipse.che.plugin.maven.server.rmi.MavenServerManagerTest;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.che.plugin.maven.shared.MessageType;
import org.eclipse.che.plugin.maven.shared.dto.NotificationMessage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Evgen Vidolob
 */
public class PomReconcilerTest extends BaseTest {


    private MavenProjectManager projectManager;
    private MavenWorkspace      mavenWorkspace;

    @BeforeMethod
    public void setUp() throws Exception {
        Provider<ProjectRegistry> projectRegistryProvider = (Provider<ProjectRegistry>)mock(Provider.class);
        when(projectRegistryProvider.get()).thenReturn(projectRegistry);
        MavenServerManagerTest.MyMavenServerProgressNotifier mavenNotifier = new MavenServerManagerTest.MyMavenServerProgressNotifier();
        MavenTerminal terminal = new MavenTerminal() {
            @Override
            public void print(int level, String message, Throwable throwable) throws RemoteException {
                System.out.println(message);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }
        };

        File localRepository = new File(new File("target/localRepo").getAbsolutePath());
        localRepository.mkdirs();
        mavenServerManager.setLocalRepository(localRepository);

        MavenWrapperManager wrapperManager = new MavenWrapperManager(mavenServerManager);
        projectManager =
                new MavenProjectManager(wrapperManager, mavenServerManager, terminal, mavenNotifier, new EclipseWorkspaceProvider());


        ClasspathManager classpathManager =
                new ClasspathManager(root.getAbsolutePath(), wrapperManager, projectManager, terminal, mavenNotifier);

        mavenWorkspace = new MavenWorkspace(projectManager, mavenNotifier, new MavenExecutorService(), projectRegistryProvider,
                                            new MavenCommunication() {
                                                @Override
                                                public void sendUpdateMassage(Set<MavenProject> updated, List<MavenProject> removed) {

                                                }

                                                @Override
                                                public void sendNotification(NotificationMessage message) {
                                                    System.out.println(message.toString());
                                                }

                                                @Override
                                                public void send(JsonObject object, MessageType type) {

                                                }
                                            }, classpathManager, eventService, new EclipseWorkspaceProvider());

    }

    @Test
    public void testProblemPosition() throws Exception {
        MavenServerService serverService = new MavenServerService(null, projectRegistry, pm, projectManager, null, null);
        FolderEntry testProject = createTestProject("A", "");
        VirtualFileEntry child = testProject.getChild("pom.xml");
        String newContent = getPomContent("<ss");
        child.getVirtualFile().updateContent(newContent);

        List<Problem> problems = serverService.reconcilePom("/A/pom.xml");
        assertThat(problems).isNotEmpty();
        Problem problem = problems.get(0);

        assertThat(problem.getSourceStart()).isEqualTo(newContent.indexOf("<ss") + 3);
        assertThat(problem.getSourceEnd()).isEqualTo(newContent.indexOf("<ss") + 4);

    }

    @Test
    public void testReconcilePomWhenMavenProjectIsNotFound() throws Exception {
        MavenServerService serverService = new MavenServerService(null, projectRegistry, pm, projectManager, null, null);
        FolderEntry testProject = createTestProject(PROJECT_NAME, "");
        VirtualFileEntry pom = testProject.getChild("pom.xml");

        List<Problem> problems = serverService.reconcilePom(String.format("/%s/pom.xml", PROJECT_NAME));

        assertThat(problems).isEmpty();
        assertThat(pom).isNotNull();
    }

    @Test
    public void testReconcilePomWhenPomContainsCorrectDependency() throws Exception {
        String dependency = "    <dependency>\n" +
                            "        <groupId>junit</groupId>\n" +
                            "        <artifactId>junit</artifactId>\n" +
                            "        <version>3.8.1</version>\n" +
                            "        <scope>test</scope>\n" +
                            "    </dependency>\n";
        MavenServerService serverService = new MavenServerService(null, projectRegistry, pm, projectManager, null, null);
        FolderEntry testProject = createTestProject(PROJECT_NAME, getPomContentWithDependency(dependency));
        VirtualFileEntry pom = testProject.getChild("pom.xml");
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
        mavenWorkspace.update(Collections.singletonList(project));
        mavenWorkspace.waitForUpdate();

        List<Problem> problems = serverService.reconcilePom(String.format("/%s/pom.xml", PROJECT_NAME));

        assertThat(problems).isEmpty();
        assertThat(pom).isNotNull();
    }

    @Test
    public void testReconcilePomWhenPomContainsDependecyWithIncorrectVersion() throws Exception {
        String brokenDependency = "    <dependency>\n" +
                                  "        <groupId>junit</groupId>\n" +
                                  "        <artifactId>junit</artifactId>\n" +
                                  "        <version>33333333.8.1</version>\n" +
                                  "        <scope>test</scope>\n" +
                                  "    </dependency>\n";
        MavenServerService serverService = new MavenServerService(null, projectRegistry, pm, projectManager, null, null);
        FolderEntry testProject = createTestProject(PROJECT_NAME, getPomContentWithDependency(brokenDependency));
        VirtualFileEntry pom = testProject.getChild("pom.xml");
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
        mavenWorkspace.update(Collections.singletonList(project));
        mavenWorkspace.waitForUpdate();

        List<Problem> problems = serverService.reconcilePom(String.format("/%s/pom.xml", PROJECT_NAME));

        assertThat(problems).hasSize(1);
        assertThat(problems.get(0).isError()).isTrue();
        assertThat(pom).isNotNull();
    }

    @Test
    public void testReconcilePomWhenPomContainsDependecyWithIncorrectGroupId() throws Exception {
        String brokenDependency = "    <dependency>\n" +
                                  "        <groupId>junittttt</groupId>\n" +
                                  "        <artifactId>junit</artifactId>\n" +
                                  "        <version>3.8.1</version>\n" +
                                  "        <scope>test</scope>\n" +
                                  "    </dependency>\n";
        MavenServerService serverService = new MavenServerService(null, projectRegistry, pm, projectManager, null, null);
        FolderEntry testProject = createTestProject(PROJECT_NAME, getPomContentWithDependency(brokenDependency));
        VirtualFileEntry pom = testProject.getChild("pom.xml");
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
        mavenWorkspace.update(Collections.singletonList(project));
        mavenWorkspace.waitForUpdate();

        List<Problem> problems = serverService.reconcilePom(String.format("/%s/pom.xml", PROJECT_NAME));

        assertThat(problems).hasSize(1);
        assertThat(problems.get(0).isError()).isTrue();
        assertThat(pom).isNotNull();
    }

    @Test
    public void testReconcilePomWhenPomContainsDependecyWithIncorrectAtrifactId() throws Exception {
        String brokenDependency = "    <dependency>\n" +
                                  "        <groupId>junit</groupId>\n" +
                                  "        <artifactId>jjjjjjjunit</artifactId>\n" +
                                  "        <version>3.8.1</version>\n" +
                                  "        <scope>test</scope>\n" +
                                  "    </dependency>\n";
        MavenServerService serverService = new MavenServerService(null, projectRegistry, pm, projectManager, null, null);
        FolderEntry testProject = createTestProject(PROJECT_NAME, getPomContentWithDependency(brokenDependency));
        VirtualFileEntry pom = testProject.getChild("pom.xml");
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
        mavenWorkspace.update(Collections.singletonList(project));
        mavenWorkspace.waitForUpdate();

        List<Problem> problems = serverService.reconcilePom(String.format("/%s/pom.xml", PROJECT_NAME));

        assertThat(problems).hasSize(1);
        assertThat(problems.get(0).isError()).isTrue();
        assertThat(pom).isNotNull();
    }

    private String getPomContentWithDependency(String dependency) {
        return String.format("<groupId>org.eclipse.che.examples</groupId>\n" +
                             "<artifactId>web-java-spring</artifactId>\n" +
                             "<packaging>war</packaging>\n" +
                             "<version>1.0-SNAPSHOT</version>\n" +
                             "<name>SpringDemo</name>" +
                             "<dependencies>\n" +
                             "%s" +
                             "</dependencies>", dependency);
    }
}
