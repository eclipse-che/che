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
package org.eclipse.che.plugin.maven.server.classpath;

import com.google.gson.JsonObject;
import com.google.inject.Provider;

import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.maven.server.BaseTest;
import org.eclipse.che.plugin.maven.server.MavenWrapperManager;
import org.eclipse.che.plugin.maven.server.core.EclipseWorkspaceProvider;
import org.eclipse.che.plugin.maven.server.core.MavenCommunication;
import org.eclipse.che.plugin.maven.server.core.MavenExecutorService;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.MavenWorkspace;
import org.eclipse.che.plugin.maven.server.core.classpath.ClasspathManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.server.rmi.MavenServerManagerTest;
import org.eclipse.che.plugin.maven.shared.MessageType;
import org.eclipse.che.plugin.maven.shared.dto.NotificationMessage;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Evgen Vidolob
 */
public class ClasspathManagerTest extends BaseTest {

    private   MavenProjectManager       mavenProjectManager;
    private   MavenWorkspace            mavenWorkspace;
    private   ClasspathManager          classpathManager;
    private   File                      localRepository;
    @Mock
    protected Provider<ProjectRegistry> projectRegistryProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        Provider<ProjectRegistry> projectRegistryProvider = (Provider<ProjectRegistry>)mock(Provider.class);
        when(projectRegistryProvider.get()).thenReturn(projectRegistry);
        MavenServerManagerTest.MyMavenServerProgressNotifier mavenNotifier = new MavenServerManagerTest.MyMavenServerProgressNotifier();
        MavenTerminal terminal = (level, message, throwable) -> {
            System.out.println(message);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        };
        localRepository = new File(new File("target/localRepo").getAbsolutePath());
        localRepository.mkdirs();
        mavenServerManager.setLocalRepository(localRepository);
        MavenWrapperManager wrapperManager = new MavenWrapperManager(mavenServerManager);
        mavenProjectManager =
                new MavenProjectManager(wrapperManager, mavenServerManager, terminal, mavenNotifier, new EclipseWorkspaceProvider());
        classpathManager = new ClasspathManager(root.getAbsolutePath(), wrapperManager, mavenProjectManager, terminal, mavenNotifier);
        mavenWorkspace = new MavenWorkspace(mavenProjectManager, mavenNotifier, new MavenExecutorService(), projectRegistryProvider,
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

    @AfterMethod
    public void tearDown() throws Exception {
        IoUtil.deleteRecursive(localRepository);
    }

    @Test
    public void testDownloadSources() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testArtifact</artifactId>" +
                     "<version>42</version>" +
                     "<dependencies>" +
                     "    <dependency>" +
                     "        <groupId>junit</groupId>" +
                     "        <artifactId>junit</artifactId>" +
                     "        <version>4.12</version>" +
                     "    </dependency>" +
                     "</dependencies>";
        createTestProject("test", pom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();
        boolean downloadSources = classpathManager.downloadSources(test.getFullPath().toOSString(), "org.junit.Test");
        assertTrue(downloadSources);
    }

    @Test
    public void testDownloadedSourcesShouldAttachToPackageFragmentRoot() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testArtifact</artifactId>" +
                     "<version>42</version>" +
                     "<dependencies>" +
                     "    <dependency>" +
                     "        <groupId>junit</groupId>" +
                     "        <artifactId>junit</artifactId>" +
                     "        <version>4.12</version>" +
                     "    </dependency>" +
                     "</dependencies>";
        createTestProject("test2", pom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("test2");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();
        IJavaProject javaProject = JavaCore.create(test);
        IType type = javaProject.findType("org.junit.Test");
        assertNull(type.getClassFile().getSourceRange());
        boolean downloadSources = classpathManager.downloadSources(test.getFullPath().toOSString(), "org.junit.Test");
        assertTrue(downloadSources);
        IType type2 = javaProject.findType("org.junit.Test");
        assertNotNull(type2.getClassFile().getSourceRange());
    }
}
