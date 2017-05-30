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
package org.eclipse.che.maven.plugin;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test of the Che Bom plugin
 * @author Florent Benoit
 */
public class CheBomMojoTest {

    /**
     * Rule to manage the mojo (inject, get variables from mojo)
     */
    @Rule
    public MojoRule rule = new MojoRule();

    /**
     * Resources of each test mapped on the name of the method
     */
    @Rule
    public TestResources resources = new TestResources();


    /**
     * Helper method used to inject data in mojo
     * @param mojo the mojo
     * @param baseDir root dir on which we extract files
     * @throws IllegalAccessException if unable to set variables
     */
    protected void configure(CheBomMojo mojo, File baseDir) throws Exception {

        Object pbuilder = this.rule.getContainer().lookup(ProjectBuilder.class.getName());
        this.rule.setVariableValueToObject(mojo, "mavenProjectBuilder", pbuilder);

        LocalRepository localRepo = new LocalRepository(baseDir);
        DefaultRepositorySystemSession repoSession = MavenRepositorySystemUtils.newSession();
        RepositorySystem repositorySystem = mojo.getRepoSystem();

        LocalRepositoryManager lrm = repositorySystem.newLocalRepositoryManager(repoSession, localRepo);
        repoSession.setLocalRepositoryManager( lrm );
        this.rule.setVariableValueToObject(mojo, "repoSession", repoSession);

        List<ArtifactRepository> remoteMavenRepositories = new ArrayList<>(1);
        ArtifactRepository centralArtifactRepository = new MavenArtifactRepository("central", "http://repo.maven.apache.org/maven2",
                                                                                   new DefaultRepositoryLayout(), new ArtifactRepositoryPolicy(), new ArtifactRepositoryPolicy());
        remoteMavenRepositories.add(centralArtifactRepository);
        this.rule.setVariableValueToObject(mojo, "artifactRepositories", remoteMavenRepositories);


        this.rule.setVariableValueToObject(mojo, "rootPom", new File(new File(baseDir, "mymodule"), "pom.xml"));
        this.rule.setVariableValueToObject(mojo, "targetDirectory", this.resources.getBasedir(""));


    }


    /**
     * Test mojo
     */
    @Test
    public void testGenerateBom() throws Exception {

        File projectCopy = this.resources.getBasedir("project");
        File pom = new File(projectCopy, "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        CheBomMojo mojo = (CheBomMojo) this.rule.lookupMojo("build", pom);
        configure(mojo, projectCopy);
        mojo.execute();


        // read generated pom
        // ok, now load the maven project
        DefaultProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();
        projectBuildingRequest.setRepositorySession(mojo.getRepoSession());
        projectBuildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        projectBuildingRequest.setRemoteRepositories(mojo.getArtifactRepositories());
        projectBuildingRequest.setSystemProperties(System.getProperties());
        projectBuildingRequest.setProcessPlugins(false);
        //projectBuildingRequest.setUserProperties(repoSession.getUserProperties());

        ProjectBuildingResult projectBuildingResult = mojo.getMavenProjectBuilder().build(new File(mojo.getTargetDirectory(), "bom.xml"), projectBuildingRequest);
        MavenProject generatedProject = projectBuildingResult.getProject();
        assertNotNull(generatedProject);
        DependencyManagement dependencyManagement = generatedProject.getDependencyManagement();
        assertNotNull(dependencyManagement);
        List<Dependency> dependencies = dependencyManagement.getDependencies();

        // 2 dependencies
        // 1 with merged exclusion
        // 1 with artifact being added
        assertEquals(2, dependencies.size());

        Iterator<Dependency> itDependency = dependencies.iterator();
        Dependency apacheDependency = itDependency.next();
        assertEquals("org.apache.maven", apacheDependency.getGroupId());
        assertEquals("maven", apacheDependency.getArtifactId());
        assertEquals("3.3.3", apacheDependency.getVersion());

        // check exclusion of dependency
        List<Exclusion> exclusionList = apacheDependency.getExclusions();
        assertNotNull(exclusionList);

        // exclusions have been merged
        assertEquals(2, exclusionList.size());
        Iterator<Exclusion> itExclusion = exclusionList.iterator();
        Exclusion exclusion1 = itExclusion.next();
        assertEquals("org.eclipse.che.test.gId2", exclusion1.getGroupId());
        assertEquals("org.eclipse.che.test.aId2", exclusion1.getArtifactId());

        Exclusion exclusion2 = itExclusion.next();
        assertEquals("org.eclipse.che.test.gId", exclusion2.getGroupId());
        assertEquals("org.eclipse.che.test.aId", exclusion2.getArtifactId());


        Dependency eclipseCheTestDependency = itDependency.next();
        assertEquals("org.eclipse.che.test", eclipseCheTestDependency.getGroupId());
        assertEquals("my-sub-submodule", eclipseCheTestDependency.getArtifactId());
        assertEquals("1.0-SNAPSHOT", eclipseCheTestDependency.getVersion());

        // check plugins
        assertNotNull(generatedProject.getBuild());

        PluginManagement pluginManagement = generatedProject.getBuild().getPluginManagement();
        assertNotNull(pluginManagement);
        List<Plugin> plugins = pluginManagement.getPlugins();

        // plugins
        assertTrue(plugins.size() > 0);

        // search our plugin
        Optional<Plugin> cleanPlugin = plugins.stream().filter((pluginFilter -> "maven-clean-plugin".equals(pluginFilter.getArtifactId()))).findFirst();
        assertTrue(cleanPlugin.isPresent());


        Plugin plugin = cleanPlugin.get();
        assertEquals("org.apache.maven.plugins", plugin.getGroupId());
        assertEquals("maven-clean-plugin", plugin.getArtifactId());
        assertEquals("2.6.1", plugin.getVersion());

    }

}
