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
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.server.MavenTerminal;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.fest.assertions.Condition;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.che.plugin.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Evgen Vidolob
 */
public class WorkspaceTest extends BaseTest {

    private MavenWorkspace      mavenWorkspace;
    private MavenProjectManager mavenProjectManager;

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
                                            }, new ClasspathManager(root.getAbsolutePath(), wrapperManager, mavenProjectManager, terminal,
                                                                    mavenNotifier), eventService, new EclipseWorkspaceProvider());
    }


    @Test
    public void testUpdateProject() throws Exception {
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
        MavenProject mavenProject = mavenProjectManager.findMavenProject(test);

        List<MavenArtifact> dependencies = mavenProject.getDependencies();
        assertThat(dependencies).isNotNull().hasSize(2);
        assertThat(dependencies).onProperty("artifactId").contains("junit", "hamcrest-core");
        assertThat(dependencies).onProperty("groupId").contains("junit", "org.hamcrest");
        assertThat(dependencies).onProperty("version").contains("4.12", "1.3");
    }

    @Test
    public void testUpdateProjectShuldSetName() throws Exception {
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

        RegisteredProject project = projectRegistry.getProject("test");
        assertThat(project.getName()).isEqualTo("test");
    }

    @Test
    public void testProjectWithParent() throws Exception {
        String pom = "<parent>" +
                     "   <groupId>testParent</groupId>" +
                     "   <artifactId>testParentArtifact</artifactId>" +
                     "   <version>42</version>" +
                     "</parent>" +
                     "<artifactId>testArtifact</artifactId>" +
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
        MavenProject mavenProject = mavenProjectManager.findMavenProject(test);

        MavenKey mavenKey = mavenProject.getMavenKey();
        assertThat(mavenKey.getArtifactId()).isEqualTo("testArtifact");
        assertThat(mavenKey.getGroupId()).isEqualTo("testParent");
        assertThat(mavenKey.getVersion()).isEqualTo("42");

    }

    @Test
    public void testProjectNameShuldUseArtifactIdIfNotDeclared() throws Exception {
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
        MavenProject mavenProject = mavenProjectManager.findMavenProject(test);

        String name = mavenProject.getName();
        assertThat(name).isNotNull().isNotEmpty().isEqualTo("testArtifact");
    }

    @Test
    public void testProjectNameUsedFromPom() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testArtifact</artifactId>" +
                     "<version>42</version>" +
                     " <name>testName</name>" +
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
        MavenProject mavenProject = mavenProjectManager.findMavenProject(test);

        String name = mavenProject.getName();
        assertThat(name).isNotNull().isNotEmpty().isEqualTo("testName");
    }

    @Test
    public void testSingleProjectClasspath() throws Exception {
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

        JavaProject javaProject = (JavaProject)JavaCore.create(test);
        IClasspathEntry[] classpath = javaProject.getResolvedClasspath();
        assertThat(classpath).onProperty("path").is(new Condition<Object[]>() {
            @Override
            public boolean matches(Object[] value) {
                return Stream.of(value).filter(o -> {
                    if (o instanceof IPath) {
                        return ((IPath)o).lastSegment().endsWith("junit-4.12.jar");
                    }
                    return false;
                }).findFirst().isPresent();
            }
        });
    }

    @Test
    public void testProjectHasBuildWithoutSources() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testArtifact</artifactId>" +
                     "<version>42</version>" +
                     "<dependencies>" +
                     "    <dependency>" +
                     "        <groupId>junit</groupId>" +
                     "        <artifactId>junit</artifactId>" +
                     "        <version>4.12</version>" +
                     "    </dependency>" +
                     "</dependencies>" +
                     "<build>" +
                     "</build>";

        createTestProject("test", pom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();

        JavaProject javaProject = (JavaProject)JavaCore.create(test);
        IClasspathEntry[] classpath = javaProject.getResolvedClasspath();
        assertThat(classpath).onProperty("path").is(new Condition<Object[]>() {
            @Override
            public boolean matches(Object[] value) {
                return Stream.of(value).filter(o -> {
                    if (o instanceof IPath) {
                        return ((IPath)o).toOSString().endsWith("src/main/java");
                    }
                    return false;
                }).findFirst().isPresent();
            }
        });
    }

    @Test
    public void testShouldContainsDefaultTestSourceDirectory() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testArtifact</artifactId>" +
                     "<version>42</version>" +
                     "<dependencies>" +
                     "    <dependency>" +
                     "        <groupId>junit</groupId>" +
                     "        <artifactId>junit</artifactId>" +
                     "        <version>4.12</version>" +
                     "    </dependency>" +
                     "</dependencies>" +
                     "<build>" +
                     "</build>";

        createTestProject("test", pom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();

        JavaProject javaProject = (JavaProject)JavaCore.create(test);
        IClasspathEntry[] classpath = javaProject.getResolvedClasspath();
        assertThat(classpath).onProperty("path").is(new Condition<Object[]>() {
            @Override
            public boolean matches(Object[] value) {
                return Stream.of(value).filter(o -> {
                    if (o instanceof IPath) {
                        return ((IPath)o).toOSString().endsWith("src/test/java");
                    }
                    return false;
                }).findFirst().isPresent();
            }
        });
    }

    @Test
    public void testShouldContainsCustomTestSourceDirectory() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testArtifact</artifactId>" +
                     "<version>42</version>" +
                     "<dependencies>" +
                     "    <dependency>" +
                     "        <groupId>junit</groupId>" +
                     "        <artifactId>junit</artifactId>" +
                     "        <version>4.12</version>" +
                     "    </dependency>" +
                     "</dependencies>" +
                     "<build>" +
                     "<testSourceDirectory>/mytest</testSourceDirectory>" +
                     "</build>";

        createTestProject("test", pom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();

        JavaProject javaProject = (JavaProject)JavaCore.create(test);
        IClasspathEntry[] classpath = javaProject.getResolvedClasspath();
        assertThat(classpath).onProperty("path").is(new Condition<Object[]>() {
            @Override
            public boolean matches(Object[] value) {
                return Stream.of(value).filter(o -> {
                    if (o instanceof IPath) {
                        return ((IPath)o).toOSString().endsWith("test");
                    }
                    return false;
                }).findFirst().isPresent();
            }
        });
    }

    @Test
    public void testUpdateProjectThatHasDependencyInWorkspace() throws Exception {
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
        createTestProject("test1", pom);

        String pom2 = "<groupId>test2</groupId>" +
                      "<artifactId>testArtifact2</artifactId>" +
                      "<version>2</version>" +
                      "<dependencies>" +
                      "    <dependency>" +
                      "        <groupId>junit</groupId>" +
                      "        <artifactId>junit</artifactId>" +
                      "        <version>4.12</version>" +
                      "    </dependency>" +
                      "    <dependency>" +
                      "        <groupId>test</groupId>" +
                      "        <artifactId>testArtifact</artifactId>" +
                      "        <version>42</version>" +
                      "    </dependency>" +
                      "</dependencies>";
        createTestProject("test2", pom2);

        IProject project1 = ResourcesPlugin.getWorkspace().getRoot().getProject("test1");
        IProject project2 = ResourcesPlugin.getWorkspace().getRoot().getProject("test2");
        mavenWorkspace.update(Arrays.asList(project1, project2));
        mavenWorkspace.waitForUpdate();
        MavenProject mavenProject = mavenProjectManager.findMavenProject(project2);

        List<MavenArtifact> dependencies = mavenProject.getDependencies();
        assertThat(dependencies).isNotNull().hasSize(3);
        assertThat(dependencies).onProperty("artifactId").contains("junit", "hamcrest-core", "testArtifact");
        assertThat(dependencies).onProperty("groupId").contains("junit", "org.hamcrest", "test");
        assertThat(dependencies).onProperty("version").contains("4.12", "1.3", "42");

        List<File> depFiles = dependencies.stream().map(MavenArtifact::getFile).collect(Collectors.toList());
        List<String> paths = depFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        assertThat(paths).contains(new java.io.File(root, "test1/pom.xml").getAbsolutePath());
    }

    @Test
    public void testClasspathProjectThatHasDependencyInWorkspace() throws Exception {
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
        createTestProjectWithPackages("test1", pom, "org.eclipse.che.maven.test");

        String pom2 = "<groupId>test2</groupId>" +
                      "<artifactId>testArtifact2</artifactId>" +
                      "<version>2</version>" +
                      "<dependencies>" +
                      "    <dependency>" +
                      "        <groupId>junit</groupId>" +
                      "        <artifactId>junit</artifactId>" +
                      "        <version>4.12</version>" +
                      "    </dependency>" +
                      "    <dependency>" +
                      "        <groupId>test</groupId>" +
                      "        <artifactId>testArtifact</artifactId>" +
                      "        <version>42</version>" +
                      "    </dependency>" +
                      "</dependencies>";
        createTestProject("test2", pom2);

        IProject project1 = ResourcesPlugin.getWorkspace().getRoot().getProject("test1");
        IProject project2 = ResourcesPlugin.getWorkspace().getRoot().getProject("test2");
        mavenWorkspace.update(Arrays.asList(project1, project2));
        mavenWorkspace.waitForUpdate();

        JavaProject javaProject = (JavaProject)JavaCore.create(project2);
        IJavaElement packageFragment = javaProject.findPackageFragment("org.eclipse.che.maven.test");
        assertThat(packageFragment).isNotNull();
    }

    @Test
    public void testUpdateMultimoduleProject() throws Exception {
        createMultimoduleProject();

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("parent");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();

        List<RegisteredProject> projects = projectRegistry.getProjects();
        assertThat(projects).hasSize(3);
    }

    @Test
    public void testClasspathMultimoduleProject() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testArtifact</artifactId>" +
                     "<version>42</version>" +
                     "<modules>" +
                     "    <module>module1</module>" +
                     "    <module>module2</module>" +
                     "</modules>";
        createTestProject("parent", pom);

        String pomModule1 = "<groupId>test</groupId>" +
                            "<artifactId>testModule1</artifactId>" +
                            "<version>1</version>" +
                            "<dependencies>" +
                            "    <dependency>" +
                            "        <groupId>junit</groupId>" +
                            "        <artifactId>junit</artifactId>" +
                            "        <version>4.12</version>" +
                            "    </dependency>" +
                            "</dependencies>";
        createTestProjectWithPackages("parent/module1", pomModule1, "org.eclipse.multi.module");

        String pomModule2 = "<groupId>test</groupId>" +
                            "<artifactId>testModule2</artifactId>" +
                            "<version>2</version>" +
                            "<dependencies>" +
                            "    <dependency>" +
                            "        <groupId>test</groupId>" +
                            "        <artifactId>testModule1</artifactId>" +
                            "        <version>1</version>" +
                            "    </dependency>" +
                            "</dependencies>";
        createTestProject("parent/module2", pomModule2);
        IProject parent = ResourcesPlugin.getWorkspace().getRoot().getProject("parent");
        mavenWorkspace.update(Collections.singletonList(parent));
        mavenWorkspace.waitForUpdate();

        IProject module2 = ResourcesPlugin.getWorkspace().getRoot().getProject("parent/module2");
        JavaProject javaProject = (JavaProject)JavaCore.create(module2);
        IJavaElement packageFragment = javaProject.findPackageFragment("org.eclipse.multi.module");
        assertThat(packageFragment).isNotNull();
    }

    @Test
    public void testAddingNewModule() throws Exception {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testArtifact</artifactId>" +
                     "<version>42</version>" +
                     "<modules>" +
                     "    <module>module1</module>" +
                     "</modules>";
        FolderEntry parentFolder = createTestProject("parent", pom);
        String pomModule1 = "<groupId>test</groupId>" +
                            "<artifactId>testModule1</artifactId>" +
                            "<version>1</version>" +
                            "<dependencies>" +
                            "    <dependency>" +
                            "        <groupId>junit</groupId>" +
                            "        <artifactId>junit</artifactId>" +
                            "        <version>4.12</version>" +
                            "    </dependency>" +
                            "</dependencies>";
        createTestProject("parent/module1", pomModule1);

        IProject parent = ResourcesPlugin.getWorkspace().getRoot().getProject("parent");
        mavenWorkspace.update(Collections.singletonList(parent));
        mavenWorkspace.waitForUpdate();
        assertThat(projectRegistry.getProjects()).hasSize(2).onProperty("path").containsOnly("/parent", "/parent/module1");

        VirtualFile parentPom = parentFolder.getChild("pom.xml").getVirtualFile();
        Model model = Model.readFrom(parentPom);
        List<String> modules = new ArrayList<>(model.getModules());
        modules.add("module2");
        model.setModules(modules);
        model.writeTo(parentPom);

        String pomModule2 = "<groupId>module2</groupId>" +
                            "<artifactId>testModule2</artifactId>" +
                            "<version>2</version>" +
                            "<dependencies>" +
                            "    <dependency>" +
                            "        <groupId>junit</groupId>" +
                            "        <artifactId>junit</artifactId>" +
                            "        <version>4.12</version>" +
                            "    </dependency>" +
                            "</dependencies>";
        createTestProject("parent/module2", pomModule2);

        mavenWorkspace.update(Collections.singletonList(parent));
        mavenWorkspace.waitForUpdate();
        assertThat(projectRegistry.getProjects()).hasSize(3).onProperty("path")
                                                 .containsOnly("/parent", "/parent/module1", "/parent/module2");

    }

    @Test
    public void testRemovingModule() throws Exception {
        FolderEntry parentFolder = createMultimoduleProject();

        IProject parent = ResourcesPlugin.getWorkspace().getRoot().getProject("parent");
        mavenWorkspace.update(Collections.singletonList(parent));
        mavenWorkspace.waitForUpdate();

        assertThat(projectRegistry.getProjects()).hasSize(3).onProperty("path")
                                                 .containsOnly("/parent", "/parent/module1", "/parent/module2");


        VirtualFile parentPom = parentFolder.getChild("pom.xml").getVirtualFile();
        Model model = Model.readFrom(parentPom);
        List<String> modules = new ArrayList<>(model.getModules());
        ListIterator<String> listIterator = modules.listIterator();
        while (listIterator.hasNext()) {
            if ("module2".equals(listIterator.next())) {
                listIterator.remove();
                break;
            }
        }
        model.setModules(modules);
        model.writeTo(parentPom);

        mavenWorkspace.update(Collections.singletonList(parent));
        mavenWorkspace.waitForUpdate();

        assertThat(projectRegistry.getProjects()).hasSize(2).onProperty("path")
                                                 .containsOnly("/parent", "/parent/module1");
    }

    @Test
    public void testWsShouldAddSourceFolderFromBuildHelperPlugin() throws Exception {
        String pom = "<groupId>test</groupId>\n" +
                     "<artifactId>testArtifact</artifactId>\n" +
                     "<version>42</version>\n" +
                     "<properties>\n" +
                     "    <dto-generator-out-directory>${project.build.directory}/generated-sources/dto/</dto-generator-out-directory>\n" +
                     "</properties>\n" +
                     "<dependencies>\n" +
                     "    <dependency>\n" +
                     "        <groupId>junit</groupId>\n" +
                     "        <artifactId>junit</artifactId>\n" +
                     "        <version>4.12</version>\n" +
                     "    </dependency>\n" +
                     "</dependencies>\n" +
                     "<build>\n" +
                     "   <plugins>\n" +
                     "       <plugin>\n" +
                     "          <groupId>org.codehaus.mojo</groupId>\n" +
                     "          <artifactId>build-helper-maven-plugin</artifactId>\n" +
                     "              <executions>\n" +
                     "                  <execution>\n" +
                     "                      <id>add-source</id>\n" +
                     "                      <phase>process-sources</phase>\n" +
                     "                      <goals>\n" +
                     "                          <goal>add-source</goal>\n" +
                     "                      </goals>\n" +
                     "                      <configuration>\n" +
                     "                          <sources>\n" +
                     "                              <source>${dto-generator-out-directory}</source>\n" +
                     "                          </sources>\n" +
                     "                      </configuration>\n" +
                     "                  </execution>\n" +
                     "                  <execution>\n" +
                     "                      <id>add-test-source</id>\n" +
                     "                      <phase>generate-sources</phase>\n" +
                     "                      <goals>\n" +
                     "                          <goal>add-test-source</goal>\n" +
                     "                      </goals>\n" +
                     "                      <configuration>\n" +
                     "                          <sources>\n" +
                     "                              <source>${dto-generator-out-directory}src-gen/test/java</source>\n" +
                     "                          </sources>\n" +
                     "                      </configuration>\n" +
                     "                  </execution>" +
                     "              </executions>\n" +
                     "       </plugin>\n" +
                     "   </plugins>\n" +
                     "</build>";

        createTestProject("test", pom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();

        IJavaProject javaProject = JavaCore.create(test);
        IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
        assertThat(rawClasspath).onProperty("path").contains(new Path("/test/target/generated-sources/dto/"));

        //attributes should be updated
        List<String> sourceFolders = projectRegistry.getProject("test").getAttributes().get(Constants.SOURCE_FOLDER);
        List<String> testSourceFolders = projectRegistry.getProject("test").getAttributes().get(TEST_SOURCE_FOLDER);

        assertEquals(2, sourceFolders.size());
        assertThat(sourceFolders, hasItems("src/main/java", "target/generated-sources/dto/"));
        assertEquals(2, testSourceFolders.size());
        assertThat(testSourceFolders, hasItems("src/test/java", "target/generated-sources/dto/src-gen/test/java"));
    }

    @Test
    public void testImportMultimoduleProjectDeleteAndImportAgain() throws Exception {
        String pom = "<groupId>com.codenvy.workspacebf11inh2ze5i06bk</groupId>\n" +
                     "<artifactId>multimodule</artifactId>\n" +
                     "   <packaging>pom</packaging>\n" +
                     "   <version>1.0-SNAPSHOT</version>\n" +
                     "   <properties>\n" +
                     "      <maven.compiler.source>1.6</maven.compiler.source>\n" +
                     "      <maven.compiler.target>1.6</maven.compiler.target>\n" +
                     "   </properties>\n" +
                     "   <modules>\n" +
                     "      <module>my-lib</module>\n" +
                     "      <module>my-webapp</module>\n" +
                     "   </modules>";
        createTestProject("parent", pom);

        String myLibPom = " <parent>\n" +
                          "<groupId>com.codenvy.workspacebf11inh2ze5i06bk</groupId>\n" +
                          "<artifactId>multimodule</artifactId>\n" +
                          "    <version>1.0-SNAPSHOT</version>\n" +
                          "  </parent>\n" +
                          "  <artifactId>my-lib</artifactId>\n" +
                          "  <version>1.0-SNAPSHOT</version>\n" +
                          "  <packaging>jar</packaging>\n" +
                          "\n" +
                          "  <name>sample-lib</name>\n" +
                          "\n" +
                          "  <properties>\n" +
                          "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                          "  </properties>\n" +
                          "\n" +
                          "  <dependencies>\n" +
                          "    <dependency>\n" +
                          "      <groupId>junit</groupId>\n" +
                          "      <artifactId>junit</artifactId>\n" +
                          "      <version>3.8.1</version>\n" +
                          "      <scope>test</scope>\n" +
                          "    </dependency>\n" +
                          "  </dependencies>";

        createTestProject("parent/my-lib", myLibPom);
        String myWebApp = " <parent>\n" +
                          "<groupId>com.codenvy.workspacebf11inh2ze5i06bk</groupId>\n" +
                          "<artifactId>multimodule</artifactId>\n" +
                          "    <version>1.0-SNAPSHOT</version>\n" +
                          "  </parent>\n" +
                          "  <artifactId>my-webapp</artifactId>\n" +
                          "  <packaging>war</packaging>\n" +
                          "  <version>1.0</version>\n" +
                          "  <name>SpringDemo</name>\n" +
                          "  <properties>\n" +
                          "    <maven.compiler.source>1.6</maven.compiler.source>\n" +
                          "    <maven.compiler.target>1.6</maven.compiler.target>\n" +
                          "  </properties>\n" +
                          "  <dependencies>\n" +
                          "      <dependency>\n" +
                          "<groupId>com.codenvy.workspacebf11inh2ze5i06bk</groupId>\n" +
                          "         <artifactId>my-lib</artifactId>\n" +
                          "         <version>1.0-SNAPSHOT</version>\n" +
                          "      </dependency>\n" +
                          "      <dependency>\n" +
                          "         <groupId>javax.servlet</groupId>\n" +
                          "         <artifactId>servlet-api</artifactId>\n" +
                          "         <version>2.5</version>\n" +
                          "         <scope>provided</scope>\n" +
                          "      </dependency>\n" +
                          "      <dependency>\n" +
                          "         <groupId>org.springframework</groupId>\n" +
                          "         <artifactId>spring-webmvc</artifactId>\n" +
                          "         <version>3.0.5.RELEASE</version>\n" +
                          "      </dependency>\n" +
                          "      <dependency>\n" +
                          "         <groupId>junit</groupId>\n" +
                          "         <artifactId>junit</artifactId>\n" +
                          "         <version>3.8.1</version>\n" +
                          "         <scope>test</scope>\n" +
                          "      </dependency>\n" +
                          "  </dependencies>\n" +
                          "  <build>\n" +
                          "    <finalName>greeting</finalName>\n" +
                          "  </build>";
        createTestProject("parent/my-webapp", myWebApp);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("parent");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();
        MavenProject mavenProject = mavenProjectManager.findMavenProject(test);
        assertThat(mavenProject).isNotNull();

        pm.delete("parent");

        createTestProject("parent2", pom);
        createTestProject("parent2/my-lib", myLibPom);
        createTestProject("parent2/my-webapp", myWebApp);

        IProject test2 = ResourcesPlugin.getWorkspace().getRoot().getProject("parent2");
        mavenWorkspace.update(Collections.singletonList(test2));
        mavenWorkspace.waitForUpdate();
        MavenProject mavenProject2 = mavenProjectManager.findMavenProject(test2);
        assertThat(mavenProject2).isNotNull();
    }


    @Test
    public void testImportParentInSiblingFolder() throws Exception {
        String parentPom = "" +
                "  <groupId>com.mycompany.app</groupId>\n" +
                "  <artifactId>my-app</artifactId>\n" +
                "  <version>1</version>\n" +
                "  <packaging>pom</packaging>\n" +
                " \n" +
                "  <modules>\n" +
                "    <module>../my-module</module>\n" +
                "  </modules>";

        createTestProject("parent", parentPom);

        String relativePom = "<parent>\n" +
                "    <groupId>com.mycompany.app</groupId>\n" +
                "    <artifactId>my-app</artifactId>\n" +
                "    <version>1</version>\n" +
                "    <relativePath>../parent/pom.xml</relativePath>\n" +
                "  </parent>\n" +
                "  <artifactId>my-module</artifactId>\n";

        createTestProject("my-module", relativePom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("my-module");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();
        MavenProject mavenProject = mavenProjectManager.findMavenProject(test);
        assertThat(mavenProject).isNotNull();

    }

    @Test
    public void testImportRelativeChildPom() throws Exception {
        String parentPom = "" +
                "  <groupId>com.mycompany.app</groupId>\n" +
                "  <artifactId>my-app</artifactId>\n" +
                "  <version>1</version>\n" +
                "  <packaging>pom</packaging>\n" +
                " \n" +
                "  <modules>\n" +
                "    <module>../my-module/pom.xml</module>\n" +
                "  </modules>";

        createTestProject("parent", parentPom);

        String relativePom = "<parent>\n" +
                "    <groupId>com.mycompany.app</groupId>\n" +
                "    <artifactId>my-app</artifactId>\n" +
                "    <version>1</version>\n" +
                "    <relativePath>../parent/pom.xml</relativePath>\n" +
                "  </parent>\n" +
                "  <artifactId>my-module</artifactId>\n";

        createTestProject("my-module", relativePom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("parent");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();
        IProject myModule = ResourcesPlugin.getWorkspace().getRoot().getProject("my-module");
        MavenProject mavenProject = mavenProjectManager.findMavenProject(myModule);
        assertThat(mavenProject).isNotNull();
        assertThat(mavenProject.getDependencies()).isNotNull();

    }
}
