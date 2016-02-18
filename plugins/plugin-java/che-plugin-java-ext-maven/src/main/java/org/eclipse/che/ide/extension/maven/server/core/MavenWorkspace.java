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
package org.eclipse.che.ide.extension.maven.server.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.ide.extension.maven.server.core.classpath.ClasspathHelper;
import org.eclipse.che.ide.extension.maven.server.core.classpath.ClasspathManager;
import org.eclipse.che.ide.extension.maven.server.core.project.MavenProject;
import org.eclipse.che.ide.extension.maven.server.core.project.MavenProjectModifications;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.che.maven.data.MavenProjectProblem;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenWorkspace {

    private static final Logger LOG = LoggerFactory.getLogger(MavenWorkspace.class);

    private final MavenProjectManager manager;
    private final ProjectManager      projectManager;
    private final String              wsId;
    private final MavenCommunication communication;

    private MavenTaskExecutor resolveExecutor;
    private MavenTaskExecutor classPathExecutor;


    @Inject
    public MavenWorkspace(MavenProjectManager manager,
                          MavenProgressNotifier notifier,
                          MavenExecutorService executorService,
                          ProjectManager projectManager,
                          MavenCommunication communication) {
        this.communication = communication;
        wsId = System.getenv("CHE_WORKSPACE_ID");
        this.manager = manager;
        this.projectManager = projectManager;
        resolveExecutor = new MavenTaskExecutor(executorService, notifier);
        manager.addListener(new MavenProjectListener() {
            @Override
            public void projectResolved(MavenProject project, MavenProjectModifications modifications) {
                communication.sendUpdateMassage(Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), project.getProblems());
            }

            @Override
            public void projectUpdated(Map<MavenProject, MavenProjectModifications> updated, List<MavenProject> added,
                                       List<MavenProject> removed) {
                List<MavenProject> updatedProjects = new ArrayList<>(updated.keySet());
                Set<MavenProject> rootProjectsToUpdate = importProjects(added, removed);
                updatedProjects.forEach(MavenWorkspace.this::updateJavaProject);

                //TODO schedule resolving tasks

                communication.sendUpdateMassage(rootProjectsToUpdate, added, removed, collectProblems(updatedProjects, added, rootProjectsToUpdate));
            }
        });
    }

    private List<MavenProjectProblem> collectProblems(List<MavenProject> updatedProjects, List<MavenProject> added,
                                                      Set<MavenProject> rootProjectsToUpdate) {
        List<MavenProjectProblem> result = new ArrayList<>();

        result.addAll(updatedProjects.stream().flatMap(project -> project.getProblems().stream()).collect(Collectors.toList()));
        result.addAll(added.stream().flatMap(project -> project.getProblems().stream()).collect(Collectors.toList()));
        result.addAll(rootProjectsToUpdate.stream().flatMap(project -> project.getProblems().stream()).collect(Collectors.toList()));
        return result;
    }

    private static ProjectConfigImpl createConfig(IPath path) {
        ProjectConfigImpl projectConfig = new ProjectConfigImpl();
        projectConfig.setName(path.lastSegment());
        projectConfig.setPath(path.toOSString());
        projectConfig.setType(MAVEN_ID);

        return projectConfig;
    }

    public void update(List<IProject> projects) {
        manager.update(projects, true);
    }

    private Set<MavenProject> importProjects(List<MavenProject> addedProjects, List<MavenProject> removed) {
        Set<MavenProject> projectsToUpdateConfig = getRootProjects(addedProjects);
        projectsToUpdateConfig.addAll(getRootProjects(removed));
        for (MavenProject mavenProject : projectsToUpdateConfig) {
            try {
                if (getCheProject(mavenProject.getProject().getFullPath()) == null) {
                    createCheProject(mavenProject.getProject().getFullPath());
                }
                updateMavenProject(mavenProject);
            } catch (IOException | ForbiddenException | NotFoundException | ServerException | ConflictException e) {
                LOG.error("Can't convert folder to project: " + mavenProject.getProject().getFullPath().toString(), e);
            }

        }

        addedProjects.forEach(this::updateJavaProject);
        return projectsToUpdateConfig;
    }

    private void updateMavenProject(MavenProject mavenProject)
            throws ProjectTypeConstraintException, ForbiddenException, NotFoundException, ValueStorageException, ServerException {
        Project cheProject = getCheProject(mavenProject.getProject().getFullPath());
        if (cheProject != null) {
            ProjectConfig config = cheProject.getConfig();
            ProjectConfigImpl newConfig = new ProjectConfigImpl(config);
            List<ProjectConfig> modules = new ArrayList<>();
            addModules(mavenProject, modules);
            newConfig.setModules(modules);
            cheProject.updateConfig(newConfig);
        }
    }

    private void addModules(MavenProject mavenProject, List<ProjectConfig> modules) {
        List<MavenProject> mavenProjects = manager.findModules(mavenProject);
        for (MavenProject project : mavenProjects) {
            List<ProjectConfig> internalModules = new ArrayList<>();
            addModules(project, internalModules);

            ProjectConfigImpl config = createConfig(project.getProject().getFullPath());
            config.setModules(internalModules);
            modules.add(config);
        }

    }

    private Set<MavenProject> getRootProjects(List<MavenProject> addedProjects) {
        Set<MavenProject> rootProjects = new HashSet<>();
        for (MavenProject project : addedProjects) {
            MavenProject parentProject = manager.findParentProject(project);
            MavenProject rootProject = project;
            while (parentProject != null) {
                rootProject = parentProject;
                parentProject = manager.findParentProject(parentProject);
            }

            rootProjects.add(rootProject);
        }

        return rootProjects;
    }

    private void updateJavaProject(MavenProject project) {
        IJavaProject javaProject = JavaCore.create(project.getProject());
        try {
            ClasspathHelper helper = new ClasspathHelper(javaProject);
            project.getSources().stream().map(s -> project.getProject().getFullPath().append(s)).forEach(helper::addSourceEntry);
            project.getTestSources().stream().map(s -> project.getProject().getFullPath().append(s)).forEach(helper::addSourceEntry);
            //add maven classpath container
            helper.addContainerEntry(new Path(ClasspathManager.CONTAINER_ID));
            //add JRE classpath container
            helper.addContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));

            javaProject.setRawClasspath(helper.getEntries(), null);
        } catch (JavaModelException e) {
            LOG.error("Can't update Java project classpath", e);
        }
    }

    private void createCheProject(IPath path)
            throws IOException, ForbiddenException, ServerException, NotFoundException, ConflictException {
        ProjectConfigImpl config = createConfig(path);
        projectManager.convertFolderToProject(wsId, path.toOSString(), config);

    }

    private Project getCheProject(IPath path) {
        try {
            return projectManager.getProject(wsId, path.toOSString());

        } catch (ForbiddenException | ServerException | NotFoundException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

}
