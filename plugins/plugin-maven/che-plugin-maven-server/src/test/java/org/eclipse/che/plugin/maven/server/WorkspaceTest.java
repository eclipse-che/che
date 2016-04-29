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
package org.eclipse.che.plugin.maven.server;

import com.google.gson.JsonObject;

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.vfs.VirtualFile;
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
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.server.MavenTerminal;
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

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
public class WorkspaceTest extends BaseTest {

    private MavenWorkspace      mavenWorkspace;
    private MavenProjectManager projectManager;


    @BeforeMethod
    public void setUp() throws Exception {
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
        projectManager =
                new MavenProjectManager(wrapperManager, mavenServerManager, terminal, mavenNotifier, new EclipseWorkspaceProvider());
        mavenWorkspace = new MavenWorkspace(projectManager, mavenNotifier, new MavenExecutorService(), projectRegistry,
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
                                            }, new ClasspathManager(root.getAbsolutePath(), wrapperManager, projectManager, terminal,
                                                                    mavenNotifier), pm);
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
        MavenProject mavenProject = projectManager.findMavenProject(test);

        List<MavenArtifact> dependencies = mavenProject.getDependencies();
        assertThat(dependencies).isNotNull().hasSize(2);
        assertThat(dependencies).onProperty("artifactId").containsExactly("junit", "hamcrest-core");
        assertThat(dependencies).onProperty("groupId").containsExactly("junit", "org.hamcrest");
        assertThat(dependencies).onProperty("version").containsExactly("4.12", "1.3");
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
        MavenProject mavenProject = projectManager.findMavenProject(test);

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
        MavenProject mavenProject = projectManager.findMavenProject(test);

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
        MavenProject mavenProject = projectManager.findMavenProject(test);

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
        MavenProject mavenProject = projectManager.findMavenProject(project2);

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
            if("module2".equals(listIterator.next())){
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
                     "<properties>\n"+
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
                     "   <plugins>\n"+
                     "       <plugin>\n"+
                     "          <groupId>org.codehaus.mojo</groupId>\n" +
                     "          <artifactId>build-helper-maven-plugin</artifactId>\n" +
                     "              <executions>\n" +
                     "                  <execution>\n"+
                     "                      <id>add-source</id>\n"+
                     "                      <phase>process-sources</phase>\n"+
                     "                      <goals>\n"+
                     "                          <goal>add-source</goal>\n"+
                     "                      </goals>\n" +
                     "                      <configuration>\n" +
                     "                          <sources>\n" +
                     "                              <source>${dto-generator-out-directory}</source>\n"+
                     "                          </sources>\n"+
                     "                      </configuration>\n"+
                     "                  </execution>\n"+
                     "              </executions>\n"+
                     "       </plugin>\n" +
                     "   </plugins>\n" +
                     "</build>";

        createTestProject("test", pom);

        IProject test = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
        mavenWorkspace.update(Collections.singletonList(test));
        mavenWorkspace.waitForUpdate();
        MavenProject mavenProject = projectManager.findMavenProject(test);

        IJavaProject javaProject = JavaCore.create(test);
        IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
        assertThat(rawClasspath).onProperty("path").contains(new Path("/test/target/generated-sources/dto/"));
    }

}
