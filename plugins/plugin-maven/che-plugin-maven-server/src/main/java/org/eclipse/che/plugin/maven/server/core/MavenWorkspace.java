/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.core;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.api.project.server.notification.ProjectDeletedEvent;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.che.plugin.maven.server.core.classpath.ClasspathHelper;
import org.eclipse.che.plugin.maven.server.core.classpath.ClasspathManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.server.core.project.MavenProjectModifications;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.eclipse.che.plugin.maven.shared.event.MavenOutputEvent;
import org.eclipse.che.plugin.maven.shared.impl.MavenUpdateEventImpl;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
@Singleton
public class MavenWorkspace {

  private static final Logger LOG = LoggerFactory.getLogger(MavenWorkspace.class);

  private final MavenProjectManager manager;
  private final Provider<ProjectManager> projectManagerProvider;
  private final ClasspathManager classpathManager;

  private MavenTaskExecutor resolveExecutor;
  private MavenTaskExecutor classPathExecutor;

  private Set<MavenProject> projectsToResolve = new CopyOnWriteArraySet<>();

  @Inject
  public MavenWorkspace(
      MavenProjectManager manager,
      MavenProgressNotifier notifier,
      MavenExecutorService executorService,
      Provider<ProjectManager> projectManagerProvider,
      ClasspathManager classpathManager,
      EventService eventService,
      EclipseWorkspaceProvider workspaceProvider) {
    this.projectManagerProvider = projectManagerProvider;
    this.classpathManager = classpathManager;
    this.manager = manager;
    resolveExecutor = new MavenTaskExecutor(executorService, notifier);
    eventService.subscribe(
        new EventSubscriber<ProjectDeletedEvent>() {
          @Override
          public void onEvent(ProjectDeletedEvent event) {
            IProject project = workspaceProvider.get().getRoot().getProject(event.getProjectPath());
            manager.delete(Collections.singletonList(project));
          }
        });
    manager.addListener(
        new MavenProjectListener() {
          @Override
          public void projectResolved(
              MavenProject project, MavenProjectModifications modifications) {
            //                communication.sendUpdateMassage(Collections.emptySet(),
            // Collections.emptyList());
          }

          @Override
          public void projectUpdated(
              Map<MavenProject, MavenProjectModifications> updated, List<MavenProject> removed) {
            removeProjects(removed);
            createNewProjects(updated.keySet());

            List<MavenProject> allChangedProjects =
                new ArrayList<>(updated.keySet().size() + removed.size());
            allChangedProjects.addAll(updated.keySet());
            allChangedProjects.addAll(removed);
            List<MavenProject> needResolve = manager.findDependentProjects(allChangedProjects);
            needResolve.addAll(updated.keySet());

            List<String> updatedPaths =
                updated
                    .keySet()
                    .stream()
                    .map(MavenProject::getProject)
                    .map(IResource::getFullPath)
                    .map(IPath::toOSString)
                    .collect(toList());
            List<String> removedPaths =
                removed
                    .stream()
                    .map(MavenProject::getProject)
                    .map(IResource::getFullPath)
                    .map(IPath::toOSString)
                    .collect(toList());

            addResolveProjects(needResolve);

            eventService.publish(
                new MavenUpdateEventImpl(updatedPaths, removedPaths, MavenOutputEvent.TYPE.UPDATE));
          }
        });
  }

  private void addResolveProjects(List<MavenProject> needResolve) {
    projectsToResolve.addAll(needResolve);
  }

  private void createNewProjects(Set<MavenProject> mavenProjects) {
    mavenProjects.forEach(
        project -> {
          try {
            String path = project.getProject().getFullPath().toOSString();
            projectManagerProvider.get().setType(path, MAVEN_ID, false);
          } catch (ConflictException
              | ServerException
              | NotFoundException
              | BadRequestException
              | ForbiddenException e) {
            LOG.error("Can't add new project: " + project.getProject().getFullPath(), e);
          }
        });
    mavenProjects.forEach(this::updateJavaProject);
  }

  private void removeProjects(List<MavenProject> removed) {
    removed.forEach(
        project -> {
          try {
            projectManagerProvider
                .get()
                .removeType(project.getProject().getFullPath().toOSString(), MAVEN_ID);
          } catch (ServerException
              | ForbiddenException
              | ConflictException
              | NotFoundException
              | BadRequestException e) {
            LOG.error(e.getMessage(), e);
          }
        });
  }

  public void update(List<IProject> projects) {
    manager.update(projects, true);
    runResolve();
  }

  private void runResolve() {
    // TODO synchronise on projectsToResolve change
    Set<MavenProject> needResolve = new HashSet<>(projectsToResolve);
    projectsToResolve.clear();

    for (MavenProject mavenProject : needResolve) {

      resolveExecutor.submitTask(
          new MavenProjectResolveTask(
              mavenProject,
              manager,
              () -> {
                addSourcesFromBuildHelperPlugin(mavenProject);
                classpathManager.updateClasspath(mavenProject);
              }));
    }
  }

  private void updateJavaProject(MavenProject project) {
    IJavaProject javaProject = JavaCore.create(project.getProject());
    try {
      ClasspathHelper helper = new ClasspathHelper(javaProject);
      IPath sourceOutputPath =
          project.getProject().getFullPath().append(project.getOutputDirectory());
      for (String source : project.getSources()) {
        IPath sourcePath = project.getProject().getFullPath().append(source);
        helper.addSourceEntry(sourcePath, sourceOutputPath);
      }

      IPath testOutputPath =
          project.getProject().getFullPath().append(project.getTestOutputDirectory());
      for (String testSource : project.getTestSources()) {
        IPath testSourcePath = project.getProject().getFullPath().append(testSource);
        helper.addSourceEntry(testSourcePath, testOutputPath);
      }
      // add maven classpath container
      helper.addContainerEntry(new Path(MavenClasspathContainer.CONTAINER_ID));
      // add JRE classpath container
      helper.addContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));

      javaProject.setRawClasspath(helper.getEntries(), null);
    } catch (JavaModelException e) {
      LOG.error("Can't update Java project classpath", e);
    }
  }

  private void addSourcesFromBuildHelperPlugin(MavenProject project) {
    IJavaProject javaProject = JavaCore.create(project.getProject());
    try {
      ClasspathHelper helper = new ClasspathHelper(javaProject);

      Element pluginConfigurationSource =
          project.getPluginConfiguration(
              "org.codehaus.mojo", "build-helper-maven-plugin", "add-source");

      Element pluginConfigurationTestSource =
          project.getPluginConfiguration(
              "org.codehaus.mojo", "build-helper-maven-plugin", "add-test-source");

      IPath projectPath = project.getProject().getFullPath();
      RegisteredProject registeredProject =
          projectManagerProvider
              .get()
              .get(projectPath.toOSString())
              .orElseThrow(
                  () ->
                      new JavaModelException(
                          new JavaModelStatus(
                              IJavaModelStatusConstants.CORE_EXCEPTION,
                              "Project " + projectPath.toOSString() + " doesn't exist")));

      List<String> sourceFolders = registeredProject.getAttributes().get(Constants.SOURCE_FOLDER);
      List<String> testSourceFolders =
          registeredProject.getAttributes().get(MavenAttributes.TEST_SOURCE_FOLDER);

      addSourcePathFromConfiguration(helper, project, pluginConfigurationSource, sourceFolders);
      addSourcePathFromConfiguration(
          helper, project, pluginConfigurationTestSource, testSourceFolders);
      javaProject.setRawClasspath(helper.getEntries(), null);
    } catch (JavaModelException e) {
      LOG.error(
          "Can't update Java project classpath with Maven build helper plugin configuration", e);
    }
  }

  private void addSourcePathFromConfiguration(
      ClasspathHelper helper,
      MavenProject project,
      Element configuration,
      List<String> attributes) {
    if (configuration != null) {
      Element sources = configuration.getChild("sources");
      if (sources != null) {
        IPath outputPath = project.getProject().getFullPath().append(project.getOutputDirectory());
        for (Object element : sources.getChildren()) {
          final String path = ((Element) element).getTextTrim();
          final IPath projectLocation = project.getProject().getLocation();
          final String projectPath = projectLocation.toOSString();
          final String sourceFolder =
              path.contains(projectPath) ? path.substring(projectPath.length() + 1) : path;

          helper.addSourceEntry(
              project.getProject().getFullPath().append(sourceFolder), outputPath);
          if (!attributes.contains(sourceFolder)) {
            attributes.add(sourceFolder);
          }
        }
      }
    }
  }

  /** Waits for resolving tasks ends. For test only. */
  public void waitForUpdate() {
    resolveExecutor.waitForEndAllTasks();
  }
}
