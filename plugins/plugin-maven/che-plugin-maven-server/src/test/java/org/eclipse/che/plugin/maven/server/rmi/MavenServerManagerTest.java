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
package org.eclipse.che.plugin.maven.server.rmi;

import org.eclipse.che.plugin.maven.server.MavenServerManager;
import org.eclipse.che.plugin.maven.server.MavenServerWrapper;
import org.eclipse.che.plugin.maven.server.core.MavenProgressNotifier;
import org.eclipse.che.plugin.maven.server.execution.JavaParameters;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenWorkspaceCache;
import org.eclipse.che.maven.server.MavenProjectInfo;
import org.eclipse.che.maven.server.MavenServerResult;
import org.eclipse.che.maven.server.MavenTerminal;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Evgen Vidolob
 */
public class MavenServerManagerTest {

    private final String mavenServerPath = MavenServerManagerTest.class.getResource("/maven-server").getPath();

    private MavenServerManager manager = new MavenServerManager(mavenServerPath);

    private MavenServerWrapper mavenServer;

    private MavenWorkspaceCache workspaceCache;


    @BeforeMethod
    public void setUp() throws Exception {
        workspaceCache = new MavenWorkspaceCache();
        workspaceCache.put(new MavenKey("com.codenvy.ide", "codenvy-ide-subModule", "1.0.0-TEST-SNAPSHOT"),
                           new File(MavenServerManagerTest.class.getResource("/multimoduleProject/subModule/pom.xml").getFile()));
        mavenServer = manager.createMavenServer();
        mavenServer.customize(workspaceCache, new MyMavenTerminal(), new MyMavenServerProgressNotifier(), true, false);
    }

    @AfterMethod
    public void cleanUp() {
        mavenServer.dispose();
        manager.shutdown();
    }

    @Test
    public void testBuildMavenServerParametersMainWorkDirExec() throws Exception {
        MavenServerManager test = new MavenServerManager("test");
        try {
            JavaParameters parameters = test.buildMavenServerParameters();
            assertThat(parameters.getMainClassName()).isEqualTo("org.eclipse.che.maven.server.MavenServerMain");
            assertThat(parameters.getWorkingDirectory()).isEqualTo(System.getProperty("java.io.tmpdir"));
            assertThat(parameters.getJavaExecutable()).isEqualTo("java");

        } finally {
            test.shutdown();
        }
    }

    @Test
    public void testBuildMavenServerParametersClassPathMain() throws Exception {
        JavaParameters parameters = manager.buildMavenServerParameters();
        List<String> classPath = parameters.getClassPath();
        assertThat(classPath).contains(mavenServerPath + "/maven-server-impl.jar")
                             .contains(mavenServerPath + "/maven-server-api.jar");
    }

    @Test
    public void testBuildMavenServerParametersClassPathMavenLib() throws Exception {
        JavaParameters parameters = manager.buildMavenServerParameters();
        List<String> classPath = parameters.getClassPath();
        String mavenHome = System.getenv("M2_HOME");
        File libs = new File(mavenHome, "lib");
        File[] listFiles = libs.listFiles((dir, name) -> name.endsWith(".jar"));
        List<String> libPaths = Arrays.stream(listFiles).map(File::getAbsolutePath).collect(Collectors.toList());
        assertThat(classPath).contains(libPaths.toArray());
    }

    @Test
    public void testLaunchMavenServer() throws Exception {
        MavenServerWrapper server = manager.createMavenServer();
        assertThat(server).isNotNull();
    }

    @Test
    public void testEffectivePom() throws Exception {
        MavenServerWrapper mavenServer = manager.createMavenServer();
        String effectivePom =
                mavenServer.getEffectivePom(new File(MavenServerManagerTest.class.getResource("/EffectivePom/pom.xml").getFile()),
                                            Collections.emptyList(),
                                            Collections.emptyList());
        assertThat(effectivePom).isNotNull().isNotEmpty().contains("<!-- Effective POM for project")
                                .contains("'org.eclipse.che.parent:maven-parent-pom:pom:4.0.0-M6-SNAPSHOT'");
    }

    @Test
    public void testResolveProject() throws Exception {
        MavenServerResult resolveProject = mavenServer
                .resolveProject(new File(MavenServerManagerTest.class.getResource("/FirstProject/pom.xml").getFile()),
                                Collections.emptyList(),
                                Collections.emptyList());
        assertNotNull(resolveProject);
        assertNotNull(resolveProject.getProjectInfo());
    }

    @Test
    public void testProjectHasDependencies() throws Exception {
        MavenServerResult resolveProject = mavenServer
                .resolveProject(new File(MavenServerManagerTest.class.getResource("/FirstProject/pom.xml").getFile()),
                                Collections.emptyList(),
                                Collections.emptyList());
        assertNotNull(resolveProject);
        MavenProjectInfo projectInfo = resolveProject.getProjectInfo();
        assertNotNull(projectInfo);
        MavenModel mavenModel = projectInfo.getMavenModel();
        assertNotNull(mavenModel);
        List<MavenArtifact> dependencies = mavenModel.getDependencies();
        assertFalse(dependencies.isEmpty());
        assertEquals(26, dependencies.size());
    }

    @Test
    public void testResolveBadProject() throws Exception {
        MavenServerResult resolveProject = mavenServer
                .resolveProject(new File(MavenServerManagerTest.class.getResource("/BadProject/pom.xml").getFile()),
                                Collections.emptyList(),
                                Collections.emptyList());
        assertNotNull(resolveProject);
        assertNotNull(resolveProject.getProblems());
        assertEquals(1, resolveProject.getProblems().size());
    }

    @Test
    public void testResolveMultimoduleProjectMainPom() throws Exception {
        MavenServerResult resolveProject = mavenServer
                .resolveProject(new File(MavenServerManagerTest.class.getResource("/multimoduleProject/pom.xml").getFile()),
                                Collections.emptyList(),
                                Collections.emptyList());
        assertNotNull(resolveProject);
        MavenProjectInfo projectInfo = resolveProject.getProjectInfo();
        assertNotNull(projectInfo);

        MavenModel mavenModel = projectInfo.getMavenModel();
        assertNotNull(mavenModel);
        assertThat(mavenModel.getPackaging()).isEqualTo("pom");
        assertThat(mavenModel.getModules()).containsExactly("subModule", "test");
        assertThat(mavenModel.getDependencies()).isEmpty();
    }

    @Test
    public void testResolveMultimoduleProjectModulePom() throws Exception {
        MavenServerResult resolveProject = mavenServer
                .resolveProject(new File(MavenServerManagerTest.class.getResource("/multimoduleProject/subModule/pom.xml").getFile()),
                                Collections.emptyList(),
                                Collections.emptyList());
        assertNotNull(resolveProject);
        MavenProjectInfo projectInfo = resolveProject.getProjectInfo();
        assertNotNull(projectInfo);

        MavenModel mavenModel = projectInfo.getMavenModel();
        assertNotNull(mavenModel);
        assertThat(mavenModel.getPackaging()).isEqualTo("jar");
        assertThat(mavenModel.getModules()).isEmpty();
        assertThat(mavenModel.getDependencies()).isNotEmpty().hasSize(3);
    }

    @Test
    public void testMultimoduleProjectModuleHasDependencyOnAnotherModule() throws Exception {

        MavenServerResult resolveProject = mavenServer
                .resolveProject(new File(MavenServerManagerTest.class.getResource("/multimoduleProject/test/pom.xml").getFile()),
                                Collections.emptyList(),
                                Collections.emptyList());
        assertNotNull(resolveProject);
        MavenProjectInfo projectInfo = resolveProject.getProjectInfo();
        assertNotNull(projectInfo);

        MavenModel mavenModel = projectInfo.getMavenModel();
        assertNotNull(mavenModel);
        assertThat(mavenModel.getPackaging()).isEqualTo("jar");
        assertThat(mavenModel.getModules()).isEmpty();
        assertThat(mavenModel.getDependencies()).isNotEmpty().hasSize(6);
        mavenModel.getDependencies().forEach(mavenArtifact -> System.out.println(mavenArtifact.getFile().getAbsolutePath()));
    }

    private static class MyMavenTerminal implements MavenTerminal, Serializable {
        @Override
        public void print(int level, String message, Throwable throwable) throws RemoteException {
            System.out.print(message);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    }

    public static class MyMavenServerProgressNotifier implements MavenProgressNotifier, Serializable {
        @Override
        public void setText(String text) {
            System.out.println(text);
        }

        @Override
        public void setPercent(double percent) {
        }

        @Override
        public void setPercentUndefined(boolean undefined) {
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }
}
