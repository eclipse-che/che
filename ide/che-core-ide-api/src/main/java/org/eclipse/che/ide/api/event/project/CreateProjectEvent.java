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

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * The class store information about created project. This event should be fired when we create project.
 *
 * @author Dmitry Shnurenko
 */
public class CreateProjectEvent extends GwtEvent<CreateProjectHandler> {

    public static Type<CreateProjectHandler> TYPE = new Type<>();

    private final ProjectConfigDto projectConfig;

    public CreateProjectEvent(ProjectConfigDto projectConfig) {
        this.projectConfig = projectConfig;
    }

    /** Returns project descriptor. It contains information about project. */
    public ProjectConfigDto getProjectConfig() {
        return projectConfig;
    }

    @Override
    public Type<CreateProjectHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CreateProjectHandler handler) {
        handler.onProjectCreated(this);
    }
}
