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
package org.eclipse.che.ide.api.event.project;

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * An event that should be fired in order to open a project.
 *
 * don't use this event anymore
 * @author Artem Zatsarynnyi
 * @deprecated since project explorer have all projects opened
 */
@Deprecated
public class OpenProjectEvent extends GwtEvent<OpenProjectHandler> {

    /** Type class used to register this event. */
    public static Type<OpenProjectHandler> TYPE = new Type<>();

    private final ProjectConfigDto projectConfig;

    /**
     * Creates an event to initiate opening the specified project.
     *
     * @param projectConfig
     *         name of the project to open
     */
    public OpenProjectEvent(ProjectConfigDto projectConfig) {
        this.projectConfig = projectConfig;
    }

    @Override
    public Type<OpenProjectHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * Returns descriptor of the project to open.
     *
     * @return descriptor of the project to open
     */
    public ProjectConfigDto getProjectConfig() {
        return projectConfig;
    }

    @Override
    protected void dispatch(OpenProjectHandler handler) {
        handler.onProjectOpened(this);
    }

}
