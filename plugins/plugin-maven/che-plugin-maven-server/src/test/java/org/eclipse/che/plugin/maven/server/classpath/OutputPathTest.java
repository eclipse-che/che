/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.server.classpath;

import com.google.gson.JsonObject;
import com.google.inject.Provider;

import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.maven.server.MavenTerminal;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for checking output location for test sources.
 */
public class OutputPathTest extends BaseTest {
    private MavenProjectManager mavenProjectManager;
    private MavenWorkspace      mavenWorkspace;
    private ClasspathManager    classpathManager;

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
        MavenWrapperManager wrapperManager = new MavenWrapperManager(mavenServerManager);
        mavenProjectManager =
                new MavenProjectManager(wrapperManager, mavenServerManager, terminal, mavenNotifier, new EclipseWorkspaceProvider());
        mavenWorkspace = new MavenWorkspace(mavenProjectManager,
                                            mavenNotifier,
                                            new MavenExecutorService(),
                                            projectRegistryProvider,
                                            new ClasspathManager(root.getAbsolutePath(), wrapperManager, mavenProjectManager, terminal,
                                                                 mavenNotifier), eventService, new EclipseWorkspaceProvider());
    }

    @Test
    public void testSourceClasspathEntryShouldHaveOutputLocationPath() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testOutputLocation</artifactId>" +
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

        JavaProject javaProject = (JavaProject)JavaCore.create(test);
        IClasspathEntry[] classpath = javaProject.getResolvedClasspath();
        IClasspathEntry srcMainJava = null;
        for (IClasspathEntry entry : classpath) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getPath().toOSString().endsWith("src/main/java")) {
                srcMainJava = entry;
                break;
            }
        }

        assertThat(srcMainJava).isNotNull();
        assertThat(srcMainJava.getOutputLocation()).isNotNull();
        assertThat(srcMainJava.getOutputLocation().toOSString()).endsWith("target/classes");
    }

    @Test
    public void testSourceClasspathEntryShouldHaveCustomOutputLocationPath() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testOutputLocation</artifactId>" +
                     "<version>42</version>" +
                     "<dependencies>" +
                     "    <dependency>" +
                     "        <groupId>junit</groupId>" +
                     "        <artifactId>junit</artifactId>" +
                     "        <version>4.12</version>" +
                     "    </dependency>" +
                     "</dependencies>" +
                     "<build>" +
                     "  <outputDirectory>bin/classes</outputDirectory>" +
                     "</build>";

        createTestProject("test", pom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();

        JavaProject javaProject = (JavaProject)JavaCore.create(test);
        IClasspathEntry[] classpath = javaProject.getResolvedClasspath();
        IClasspathEntry srcMainJava = null;
        for (IClasspathEntry entry : classpath) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getPath().toOSString().endsWith("src/main/java")) {
                srcMainJava = entry;
                break;
            }
        }

        assertThat(srcMainJava).isNotNull();
        assertThat(srcMainJava.getOutputLocation()).isNotNull();
        assertThat(srcMainJava.getOutputLocation().toOSString()).endsWith("bin/classes");
    }

    @Test
    public void testTestSourceClasspathEntryShouldHaveOutputLocationPath() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testOutputLocation</artifactId>" +
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

        JavaProject javaProject = (JavaProject)JavaCore.create(test);
        IClasspathEntry[] classpath = javaProject.getResolvedClasspath();
        IClasspathEntry srcMainJava = null;
        for (IClasspathEntry entry : classpath) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getPath().toOSString().endsWith("src/test/java")) {
                srcMainJava = entry;
                break;
            }
        }

        assertThat(srcMainJava).isNotNull();
        assertThat(srcMainJava.getOutputLocation()).isNotNull();
        assertThat(srcMainJava.getOutputLocation().toOSString()).endsWith("target/test-classes");
    }

    @Test
    public void testTestSourceClasspathEntryShouldHaveCustomOutputLocationPath() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testOutputLocation</artifactId>" +
                     "<version>42</version>" +
                     "<dependencies>" +
                     "    <dependency>" +
                     "        <groupId>junit</groupId>" +
                     "        <artifactId>junit</artifactId>" +
                     "        <version>4.12</version>" +
                     "    </dependency>" +
                     "</dependencies>" +
                     "<build>" +
                     "  <testOutputDirectory>test/test-classes</testOutputDirectory>" +
                     "</build>";

        createTestProject("test", pom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();

        JavaProject javaProject = (JavaProject)JavaCore.create(test);
        IClasspathEntry[] classpath = javaProject.getResolvedClasspath();
        IClasspathEntry srcMainJava = null;
        for (IClasspathEntry entry : classpath) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getPath().toOSString().endsWith("src/test/java")) {
                srcMainJava = entry;
                break;
            }
        }

        assertThat(srcMainJava).isNotNull();
        assertThat(srcMainJava.getOutputLocation()).isNotNull();
        assertThat(srcMainJava.getOutputLocation().toOSString()).endsWith("test/test-classes");
    }
}
