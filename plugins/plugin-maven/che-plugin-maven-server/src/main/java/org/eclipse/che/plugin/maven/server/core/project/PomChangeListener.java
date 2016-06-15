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
package org.eclipse.che.plugin.maven.server.core.project;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
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

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class PomChangeListener {

    private final MavenWorkspace           mavenWorkspace;
    private final EclipseWorkspaceProvider eclipseWorkspaceProvider;
    private final String                   workspacePath;
    private CopyOnWriteArraySet<String> projectToUpdate = new CopyOnWriteArraySet<>();

    @Inject
    public PomChangeListener(EventService eventService,
                             MavenWorkspace mavenWorkspace,
                             EclipseWorkspaceProvider eclipseWorkspaceProvider,
                             ThreadPullLauncher launcher,
                             @Named("che.user.workspaces.storage") String workspacePath) {
        this.mavenWorkspace = mavenWorkspace;
        this.eclipseWorkspaceProvider = eclipseWorkspaceProvider;
        this.workspacePath = workspacePath;

        launcher.scheduleWithFixedDelay(this::updateProms, 20, 3, TimeUnit.SECONDS);

        eventService.subscribe(new EventSubscriber<ProjectItemModifiedEvent>() {
            @Override
            public void onEvent(ProjectItemModifiedEvent event) {
                String eventPath = event.getPath();
                if (!event.isFolder() && eventPath.endsWith("pom.xml")) {
                    //TODO update only pom file that in root of project
//                    if(event.getProject().equals(eventPath.substring(0, eventPath.lastIndexOf("pom.xml") - 1))) {
                    if (pomIsValid(eventPath)) {
                        projectToUpdate.add(new Path(eventPath).removeLastSegments(1).toOSString());
                    }
//                    }
                }
            }
        });

        eventService.subscribe(new EventSubscriber<PomModifiedEventDto>() {
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
            Model.readFrom(new File(workspacePath, path));
        } catch (Exception e) {
            JavaPlugin.log(e);
            return false;
        }
        return true;
    }

    //    @ScheduleDelay(initialDelay = 30, delay = 3)
    protected void updateProms() {
        try {
            if (projectToUpdate.size() == 0) {
                return;
            }
            Set<String> projects = new HashSet<>(projectToUpdate);
            projectToUpdate.clear();
            IWorkspace workspace = eclipseWorkspaceProvider.get();
            List<IProject> projectsList =
                    projects.stream().map(project -> workspace.getRoot().getProject(project)).collect(Collectors.toList());
            mavenWorkspace.update(projectsList);
        } catch (Throwable t) {
            JavaPlugin.log(t);
        }
    }
}
