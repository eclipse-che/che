/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.server.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.jdt.core.resources.ResourceChangedEvent;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class ProjectListeners {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectListeners.class);

    private final File                workspace;
    private final ProjectRegistry     projectRegistry;
    private final ProjectTypeRegistry projectTypeRegistry;

    @Inject
    public ProjectListeners(@Named("che.user.workspaces.storage") String workspacePath,
                            EventService eventService,
                            ProjectRegistry projectRegistry,
                            ProjectTypeRegistry projectTypeRegistry) {
        this.projectRegistry = projectRegistry;
        this.projectTypeRegistry = projectTypeRegistry;
        workspace = new File(workspacePath);
        eventService.subscribe(new ProjectCreated());
        eventService.subscribe(new EventSubscriber<ProjectItemModifiedEvent>() {
            @Override
            public void onEvent(ProjectItemModifiedEvent event) {
                handleEvent(event);
            }
        });
    }

    public void handleEvent(ProjectItemModifiedEvent event) {
        final String eventPath = event.getPath();
        if (!isJavaProject(event.getProject())) {
            return;
        }
        try {
            JavaModelManager.getJavaModelManager().deltaState.resourceChanged(
                    new ResourceChangedEvent(workspace, event));
        } catch (Throwable t) {
            //catch all exceptions that may be happened
            LOG.error("Can't update java model in " + eventPath, t);
        }
        if (event.getType() == ProjectItemModifiedEvent.EventType.UPDATED) {
            ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
            ITextFileBuffer fileBuffer = manager.getTextFileBuffer(new Path(eventPath), LocationKind.IFILE);
            if (fileBuffer != null) {
                try {
                    fileBuffer.revert(new NullProgressMonitor());
                } catch (CoreException e) {
                    LOG.error("Can't read file content: " + eventPath, e);
                }
            }
        }
    }

    private class ProjectCreated implements EventSubscriber<ProjectCreatedEvent> {
        @Override
        public void onEvent(ProjectCreatedEvent event) {
            if (!isJavaProject(event.getProjectPath())) {
                return;
            }
            try {
                JavaModelManager.getJavaModelManager().deltaState.resourceChanged(new ResourceChangedEvent(workspace, event));
            } catch (Throwable t) {
                //catch all exceptions that may be happened
                LOG.error("Can't update java model " + event.getProjectPath(), t);
            }
        }
    }

    private boolean isJavaProject(String projectPath) {
        ProjectConfig project = projectRegistry.getProject(projectPath);
        String type = project.getType();
        try {
            return projectTypeRegistry.getProjectType(type).isTypeOf("java");
        } catch (NotFoundException e) {
            LOG.error("Can't find project " + projectPath, e);
            return false;
        }
    }
}
