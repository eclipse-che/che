/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.core.project;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopy;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopyManager;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.api.project.shared.dto.event.PomModifiedEventDto;
import org.eclipse.che.commons.schedule.executor.ThreadPullLauncher;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.plugin.maven.server.core.EclipseWorkspaceProvider;
import org.eclipse.che.plugin.maven.server.core.MavenWorkspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.ui.JavaPlugin;

/** @author Evgen Vidolob */
@Singleton
public class PomChangeListener {

  private final MavenWorkspace mavenWorkspace;
  private final EclipseWorkspaceProvider eclipseWorkspaceProvider;
  private final EditorWorkingCopyManager editorWorkingCopyManager;
  private final String workspacePath;
  private CopyOnWriteArraySet<String> projectToUpdate = new CopyOnWriteArraySet<>();

  @Inject
  public PomChangeListener(
      EventService eventService,
      MavenWorkspace mavenWorkspace,
      EclipseWorkspaceProvider eclipseWorkspaceProvider,
      EditorWorkingCopyManager editorWorkingCopyManager,
      ThreadPullLauncher launcher,
      RootDirPathProvider pathProvider) {
    this.mavenWorkspace = mavenWorkspace;
    this.eclipseWorkspaceProvider = eclipseWorkspaceProvider;
    this.editorWorkingCopyManager = editorWorkingCopyManager;
    this.workspacePath = pathProvider.get();

    launcher.scheduleWithFixedDelay(this::updateProjects, 20, 3, TimeUnit.SECONDS);

    eventService.subscribe(
        new EventSubscriber<ProjectItemModifiedEvent>() {
          @Override
          public void onEvent(ProjectItemModifiedEvent event) {
            String eventPath = event.getPath();
            if (!event.isFolder() && eventPath.endsWith("pom.xml")) {
              // TODO update only pom file that in root of project
              //                    if(event.getProject().equals(eventPath.substring(0,
              // eventPath.lastIndexOf("pom.xml") - 1))) {
              if (pomIsValid(eventPath)) {
                projectToUpdate.add(new Path(eventPath).removeLastSegments(1).toOSString());
              }
              //                    }
            }
          }
        });

    eventService.subscribe(
        new EventSubscriber<PomModifiedEventDto>() {
          @Override
          public void onEvent(PomModifiedEventDto event) {
            String eventPath = event.getPath();
            if (pomIsValid(eventPath)) {
              projectToUpdate.add(new Path(eventPath).removeLastSegments(1).toOSString());
            }
          }
        });
  }

  private boolean pomIsValid(String path) {
    try {
      EditorWorkingCopy workingCopy = editorWorkingCopyManager.getWorkingCopy(path);
      if (workingCopy != null) {
        Model.readFrom(workingCopy.getContent());
      } else {
        Model.readFrom(new File(workspacePath, path));
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  protected void updateProjects() {
    try {
      if (projectToUpdate.size() == 0) {
        return;
      }
      Set<String> projects = new HashSet<>(projectToUpdate);
      projectToUpdate.clear();
      IWorkspace workspace = eclipseWorkspaceProvider.get();
      List<IProject> projectsList =
          projects
              .stream()
              .map(project -> workspace.getRoot().getProject(project))
              .collect(Collectors.toList());
      mavenWorkspace.update(projectsList);
    } catch (Throwable t) {
      JavaPlugin.log(t);
    }
  }
}
