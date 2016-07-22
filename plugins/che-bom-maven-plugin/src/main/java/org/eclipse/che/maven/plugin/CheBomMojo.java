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
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mojo used to generate bom file based on a given pom.xml.
 * It will generate BOM based on parent of this pom and all modules of this given pom.xml
 *
 * @author Florent Benoit
 */
@Mojo(name = "build",
      defaultPhase = LifecyclePhase.PACKAGE,
      requiresProject = true,
      requiresDependencyCollection = ResolutionScope.RUNTIME)
public class CheBomMojo extends AbstractMojo {

    /**
     * Pom Packaging definition.
     */
    protected static final String POM_PACKAGING = "pom";

    /**
     * Project providing artifact id, version and dependencies.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * The maven project builder used to build MavenProject artifact from a pom.xml.
     */
    @Component(role = ProjectBuilder.class)
    private ProjectBuilder mavenProjectBuilder;

    /**
     * Repository system used to generate a repository session.
     */
    @Component
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     */
    @Parameter(defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repoSession;

    /**
     * The remote repositories used to get artifacts.
     */
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", required = true, readonly = true)
    private List<ArtifactRepository> artifactRepositories;

    /**
     * build directory used to write the intermediate bom file.
     */
    @Parameter(defaultValue = "${project.build.directory}")
    private File targetDirectory;

    /**
     * Path to the pom on which we need to collect dependencies
     */
    @Parameter
    private File rootPom;

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Entry point of the Che Bom mojo.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // start the analysis
        getLog().info("Generating BOM based on root pom " + rootPom);

        // create map for storing the project dependencies or plugins
        Map<String, Dependency> projectDependencies = new HashMap<>();
        Map<String, Plugin> projectPluginDependencies = new HashMap<>();

        getLog().debug("Start to analyze");
        try {
            analyze(rootPom, true, projectDependencies, projectPluginDependencies);
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException("Unable to analyze pom.xml", e);
        }
        getLog().debug("End analyze");

        // sorting dependencies and plugins
        List<Dependency> sortedDependencies = new ArrayList<>(projectDependencies.values());
        List<Plugin> sortedPlugins = new ArrayList<>(projectPluginDependencies.values());
        Collections.sort(sortedDependencies, new DependencyComparator());
        Collections.sort(sortedPlugins, new PluginComparator());

        // Generate new model
        Model model = generateModel();

        // add dependencyManagement and pluginManagement inside
        DependencyManagement dependencyManagement = new DependencyManagement();
        dependencyManagement.setDependencies(sortedDependencies);
        model.setDependencyManagement(dependencyManagement);
        PluginManagement pluginManagement = new PluginManagement();
        pluginManagement.setPlugins(sortedPlugins);
        Build build = new Build();
        build.setPluginManagement(pluginManagement);
        model.setBuild(build);


        // Now, write the bom file
        final MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
        File bomFile = new File(targetDirectory, "bom.xml");
        File parentDir = bomFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new MojoExecutionException("Unable to create a directory for writing pom result '" + parentDir + "'.");
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(bomFile);
             Writer fileWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");) {
            mavenXpp3Writer.write(fileWriter, model);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to create BOM file", e);
        }

        // attach this bom as being the new pom
        projectHelper.attachArtifact(project, "pom", bomFile);

    }


    /**
     * Merge provided dependencies (toAddDependencies) into the existing dependencies by merging exclusions
     *
     * @param existingDependencies
     *         current list of dependencies
     * @param toAddDependencies
     *         list of dependencies that will be merged (or added if not yet present)
     */
    protected void mergeDependencies(Map<String, Dependency> existingDependencies, List<Dependency> toAddDependencies) {
        for (Dependency dependency : toAddDependencies) {
            String key = dependency.getManagementKey();
            if (!existingDependencies.containsKey(key)) {
                // not existing, add it
                existingDependencies.put(key, dependency);
                continue;

            }
            // needs to merge exclusions
            Dependency existingDependency = existingDependencies.get(key);
            List<Exclusion> existingExclusions = existingDependency.getExclusions();
            List<Exclusion> toAddExclusions = dependency.getExclusions();
            if (existingExclusions == null || existingExclusions.isEmpty()) {
                existingDependency.setExclusions(toAddExclusions);
            } else if (toAddExclusions != null) {
                mergeExclusions(existingExclusions, toAddExclusions);
            }

        }
    }

    /**
     * Performs a merge of exclusions by adding exclusions if not there
     *
     * @param existingExclusions
     *         current list of exclusions of a given dependency
     * @param toAddExclusions
     *         list of exclusions that needs to be merged or added
     */
    protected void mergeExclusions(List<Exclusion> existingExclusions, List<Exclusion> toAddExclusions) {
        // check it is not existing, else add it
        for (Exclusion foundExclusion : toAddExclusions) {
            String gIdFoundExclusion = foundExclusion.getGroupId();
            String aIdFoundExclusion = foundExclusion.getArtifactId();
            Optional<Exclusion> matchingExclusion = existingExclusions.stream().filter(exclusion -> gIdFoundExclusion.equals(exclusion
                                                                                                                                     .getGroupId()) &&
                                                                                                    aIdFoundExclusion.equals(exclusion
                                                                                                                                     .getArtifactId()))
                                                                      .findFirst();
            // not found, add it
            if (!matchingExclusion.isPresent()) {
                existingExclusions.add(foundExclusion);
            }
        }
    }


    /**
     * Merge provided plugins into an existing list.
     *
     * @param existingPlugins
     *         contains all existing plugins
     * @param toAddPlugins
     *         add plugin if the key is not yet present
     */
    protected void mergePlugins(Map<String, Plugin> existingPlugins, List<Plugin> toAddPlugins) {
        for (Plugin plugin : toAddPlugins) {
            String key = plugin.getKey();
            if (!existingPlugins.containsKey(key)) {

                // drop configuration
                plugin.setConfiguration(null);

                // not existing, add it
                existingPlugins.put(key, plugin);
            }
        }
    }


    /**
     * Analyze the given pom (and its parent if isRootPom is true) and all modules.
     *
     * @param parentPom
     *         the path to the pom.mxl to analyze
     * @param isRootPom
     *         if true, parents of this pom will be analyzed too
     * @param projectDependencies
     *         the map of dependencies of this project, map key is the Dependency's getManagementKey method
     * @param projectPluginDependencies
     *         the map of plugins of this project, map key is the Plugin's getKey method
     * @throws ProjectBuildingException
     */
    private void analyze(File parentPom, boolean isRootPom, Map<String, Dependency> projectDependencies,
                         Map<String, Plugin> projectPluginDependencies) throws ProjectBuildingException {
        getLog().debug("Analyzing pom " + parentPom);

        // ok, now load the maven project
        DefaultProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();
        projectBuildingRequest.setRepositorySession(repoSession);
        projectBuildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        projectBuildingRequest.setRemoteRepositories(artifactRepositories);
        projectBuildingRequest.setSystemProperties(System.getProperties());
        projectBuildingRequest.setProcessPlugins(false);
        ProjectBuildingResult projectBuildingResult = mavenProjectBuilder.build(parentPom, projectBuildingRequest);

        // add this project as to be analyzed
        List<MavenProject> projectsToAnalyze = new ArrayList<>();
        MavenProject resultProject = projectBuildingResult.getProject();
        projectsToAnalyze.add(resultProject);

        // for root pom, analyze the parent as well
        if (isRootPom) {
            MavenProject parent = resultProject.getParent();
            while (parent != null) {
                getLog().debug("Adding parent" + parent.getGroupId() + ":" + parent.getArtifactId());
                projectsToAnalyze.add(parent);
                parent = parent.getParent();
            }
        }

        // compute list of modules to call
        List<String> modules = projectBuildingResult.getProject().getModules();
        if (modules != null) {
            for (String module : modules) {
                analyze(new File(new File(parentPom.getParent(), module), "pom.xml"), false, projectDependencies,
                        projectPluginDependencies);
            }
        }

        // ok so now we have all parents and children
        // get dependency management
        for (MavenProject mavenProject : projectsToAnalyze) {
            DependencyManagement dependencyManagement = mavenProject.getDependencyManagement();
            if (dependencyManagement != null && dependencyManagement.getDependencies() != null) {
                mergeDependencies(projectDependencies, dependencyManagement.getDependencies());
            }
            // add module itself as well if not a pom
            if (!POM_PACKAGING.equals(mavenProject.getPackaging())) {
                Dependency selfProjectDependency = new Dependency();
                selfProjectDependency.setGroupId(mavenProject.getGroupId());
                selfProjectDependency.setArtifactId(mavenProject.getArtifactId());
                selfProjectDependency.setVersion(mavenProject.getVersion());
                selfProjectDependency.setType(mavenProject.getPackaging());
                mergeDependencies(projectDependencies, Collections.singletonList(selfProjectDependency));
            }

            PluginManagement pluginManagement = mavenProject.getPluginManagement();
            if (pluginManagement != null && pluginManagement.getPlugins() != null) {
                mergePlugins(projectPluginDependencies, pluginManagement.getPlugins());
            }
        }

    }


    /**
     * Generates a default pom that will be our BOM by adding as well repositories and plugin repositories inside
     *
     * @return
     */
    protected Model generateModel() {
        Model model = new Model();
        model.setModelVersion("4.0.0");

        model.setGroupId(this.project.getGroupId());
        model.setArtifactId(this.project.getArtifactId());
        model.setVersion(this.project.getVersion());
        model.setPackaging("pom");
        model.setDescription("BOM");
        model.setRepositories(getProject().getRepositories());
        model.setPluginRepositories(getProject().getPluginRepositories());


        return model;
    }

    /**
     * @return repository session
     */
    protected RepositorySystemSession getRepoSession() {
        return repoSession;
    }

    /**
     * @return artifact repositories
     */
    protected List<ArtifactRepository> getArtifactRepositories() {
        return artifactRepositories;
    }


    /**
     * @return the maven project object
     */
    protected MavenProject getProject() {
        return this.project;
    }


    /**
     * @return the maven project builder
     */
    protected ProjectBuilder getMavenProjectBuilder() {
        return mavenProjectBuilder;
    }

    /**
     * @return the target directory
     */
    protected File getTargetDirectory() {
        return targetDirectory;
    }

    /**
     * @return the repository system
     */
    protected RepositorySystem getRepoSystem() {
        return repoSystem;
    }


}
