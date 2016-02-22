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
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.ide.extension.maven.server.core.classpath.ClasspathHelper;
import org.eclipse.che.ide.extension.maven.server.core.classpath.ClasspathManager;
import org.eclipse.che.ide.extension.maven.server.core.project.MavenProject;
import org.eclipse.che.ide.extension.maven.server.core.project.MavenProjectModifications;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.che.maven.data.MavenProjectProblem;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
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
    private final String              wsId;
    private final ProjectRegistry     projectRegistry;
    private final MavenCommunication communication;

    private MavenTaskExecutor resolveExecutor;
    private MavenTaskExecutor classPathExecutor;


    @Inject
    public MavenWorkspace(MavenProjectManager manager,
                          MavenProgressNotifier notifier,
                          MavenExecutorService executorService,
                          ProjectRegistry projectRegistry,
                          MavenCommunication communication) {
        this.projectRegistry = projectRegistry;
        this.communication = communication;
        wsId = System.getenv("CHE_WORKSPACE_ID");
        this.manager = manager;
        resolveExecutor = new MavenTaskExecutor(executorService, notifier);
        manager.addListener(new MavenProjectListener() {
            @Override
            public void projectResolved(MavenProject project, MavenProjectModifications modifications) {
                communication.sendUpdateMassage(Collections.emptySet(), Collections.emptyList(), project.getProblems());
            }

            @Override
            public void projectUpdated(Map<MavenProject, MavenProjectModifications> updated,
                                       List<MavenProject> removed) {
//                List<MavenProject> updatedProjects = new ArrayList<>(updated.keySet());
//                Set<MavenProject> rootProjectsToUpdate = importProjects(added, removed);
//                updatedProjects.forEach(MavenWorkspace.this::updateJavaProject);
                removeProjects(removed);
                createNewProjects(updated.keySet());
                //TODO schedule resolving tasks
                List<MavenProject> updatedProjects = new ArrayList<>(updated.keySet());
                communication.sendUpdateMassage(updated.keySet(), removed, collectProblems(updatedProjects));
            }
        });
    }

    private void createNewProjects(Set<MavenProject> mavenProjects) {
        mavenProjects.stream()
                     .filter(project -> projectRegistry.getProject(project.getProject().getFullPath().toOSString()) == null)
                     .forEach(project -> {
                         try {
                             projectRegistry.initProject(project.getProject().getFullPath().toOSString(), MAVEN_ID);
                         } catch (ConflictException | ForbiddenException | ServerException | NotFoundException e) {
                             LOG.error("Can't add new project: " + project.getProject().getFullPath(), e);
                         }
                     });
        mavenProjects.forEach(this::updateJavaProject);
    }

    private void removeProjects(List<MavenProject> removed) {
        removed.forEach(project -> projectRegistry.removeProjects(project.getProject().getFullPath().toOSString()));
    }

    private List<MavenProjectProblem> collectProblems(List<MavenProject> updatedProjects) {

        List<MavenProjectProblem> result = new ArrayList<>();
        result.addAll(updatedProjects.stream().flatMap(project -> project.getProblems().stream()).collect(Collectors.toList()));
        return result;
    }

    public void update(List<IProject> projects) {
        manager.update(projects, true);
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

}
