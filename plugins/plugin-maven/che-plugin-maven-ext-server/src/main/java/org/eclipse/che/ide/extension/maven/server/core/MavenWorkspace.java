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
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.ide.extension.maven.server.core.classpath.ClasspathHelper;
import org.eclipse.che.ide.extension.maven.server.core.classpath.ClasspathManager;
import org.eclipse.che.ide.extension.maven.server.core.project.MavenProject;
import org.eclipse.che.ide.extension.maven.server.core.project.MavenProjectModifications;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenWorkspace {

    private static final Logger LOG = LoggerFactory.getLogger(MavenWorkspace.class);

    private final MavenProjectManager manager;
    private final ProjectManager projectManager;
    private final ProjectRegistry     projectRegistry;
    private final MavenCommunication  communication;
    private final ClasspathManager    classpathManager;

    private MavenTaskExecutor resolveExecutor;
    private MavenTaskExecutor classPathExecutor;

    private Set<MavenProject> projectsToResolve = new CopyOnWriteArraySet<>();


    @Inject
    public MavenWorkspace(MavenProjectManager manager,
                          MavenProgressNotifier notifier,
                          MavenExecutorService executorService,
                          ProjectRegistry projectRegistry,
                          MavenCommunication communication,
                          ClasspathManager classpathManager,
                          ProjectManager projectManager) {
        this.projectRegistry = projectRegistry;
        this.communication = communication;
        this.classpathManager = classpathManager;
        this.manager = manager;
        this.projectManager = projectManager;
        resolveExecutor = new MavenTaskExecutor(executorService, notifier);
        manager.addListener(new MavenProjectListener() {
            @Override
            public void projectResolved(MavenProject project, MavenProjectModifications modifications) {
//                communication.sendUpdateMassage(Collections.emptySet(), Collections.emptyList());
            }

            @Override
            public void projectUpdated(Map<MavenProject, MavenProjectModifications> updated,
                                       List<MavenProject> removed) {
                removeProjects(removed);
                createNewProjects(updated.keySet());

                List<MavenProject> allChangedProjects = new ArrayList<>(updated.keySet().size() + removed.size());
                allChangedProjects.addAll(updated.keySet());
                allChangedProjects.addAll(removed);
                List<MavenProject> needResolve = manager.findDependentProjects(allChangedProjects);
                needResolve.addAll(updated.keySet());

                addResolveProjects(needResolve);

                //TODO schedule resolving tasks
                List<MavenProject> updatedProjects = new ArrayList<>(updated.keySet());
                communication.sendUpdateMassage(updated.keySet(), removed);
            }
        });
    }

    private void addResolveProjects(List<MavenProject> needResolve) {
        projectsToResolve.addAll(needResolve);
    }

    private void createNewProjects(Set<MavenProject> mavenProjects) {
        mavenProjects.stream()
//                     .filter(project -> projectRegistry.getProject(project.getProject().getFullPath().toOSString()) == null)
                     .forEach(project -> {
                         try {
//                             ProjectConfigImpl config = new ProjectConfigImpl();
//                             config.setType(MAVEN_ID);
                             String path = project.getProject().getFullPath().toOSString();
//                             config.setPath(path);
//                             config.setName(project.getProject().getFullPath().lastSegment());

                             projectRegistry.setProjectType(path, MAVEN_ID, false);
                         } catch (ConflictException | ServerException | NotFoundException e) {
                             LOG.error("Can't add new project: " + project.getProject().getFullPath(), e);
                         }
                     });
        mavenProjects.forEach(this::updateJavaProject);
    }

    private void removeProjects(List<MavenProject> removed) {
        removed.forEach(project -> {
            try {
                projectRegistry.removeProjectType(project.getProject().getFullPath().toOSString(), MAVEN_ID);
            } catch (ServerException | ForbiddenException | ConflictException | NotFoundException e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

//    private List<MavenProjectProblem> collectProblems(List<MavenProject> updatedProjects) {
//
//        List<MavenProjectProblem> result = new ArrayList<>();
//        result.addAll(updatedProjects.stream().flatMap(project -> project.getProblems().stream()).collect(Collectors.toList()));
//        return result;
//    }

    public void update(List<IProject> projects) {
        manager.update(projects, true);
        runResolve();
    }

    private void runResolve() {
        //TODO synchronise on projectsToResolve change
        Set<MavenProject> needResolve = new HashSet<>(projectsToResolve);
        projectsToResolve.clear();

        for (MavenProject mavenProject : needResolve) {

            resolveExecutor.submitTask(new MavenProjectResolveTask(mavenProject, manager, () -> {
                classpathManager.updateClasspath(mavenProject);
            }));
        }

    }

    private void updateJavaProject(MavenProject project) {
        IJavaProject javaProject = JavaCore.create(project.getProject());
        try {
            ClasspathHelper helper = new ClasspathHelper(javaProject);
            project.getSources().stream().map(s -> project.getProject().getFullPath().append(s)).forEach(helper::addSourceEntry);
            project.getTestSources().stream().map(s -> project.getProject().getFullPath().append(s)).forEach(helper::addSourceEntry);
            //add maven classpath container
            helper.addContainerEntry(new Path(MavenClasspathContainer.CONTAINER_ID));
            //add JRE classpath container
            helper.addContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));

            javaProject.setRawClasspath(helper.getEntries(), null);
        } catch (JavaModelException e) {
            LOG.error("Can't update Java project classpath", e);
        }
    }

    /**
     * Waits for resolving tasks ends.
     * For test only.
     */
    public void waitForUpdate() {
        resolveExecutor.waitForEndAllTasks();
    }
}
