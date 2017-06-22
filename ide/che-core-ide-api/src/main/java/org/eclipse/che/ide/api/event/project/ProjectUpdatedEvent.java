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
package org.eclipse.che.ide.api.event.project;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * Event fires when project is updated or configured.
 *
 * @author Vlad Zhukovskiy
 */
public class ProjectUpdatedEvent extends GwtEvent<ProjectUpdatedEvent.ProjectUpdatedHandler> {
    public interface ProjectUpdatedHandler extends EventHandler {
        void onProjectUpdated(ProjectUpdatedEvent event);
    }

    private static Type<ProjectUpdatedHandler> TYPE;

    public static Type<ProjectUpdatedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private ProjectConfigDto updatedProjectConfig;
    private String           path;

    public ProjectUpdatedEvent(String path, ProjectConfigDto updatedProjectConfig) {
        this.path = path;
        this.updatedProjectConfig = updatedProjectConfig;
    }

    public ProjectConfigDto getUpdatedProjectDescriptor() {
        return updatedProjectConfig;
    }

    public String getPath() {
        return path;
    }

    /** {@inheritDoc} */
    @Override
    public Type<ProjectUpdatedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ProjectUpdatedHandler handler) {
        handler.onProjectUpdated(this);
    }
}
