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
 * Event that describes the fact that Project Action (opened/closing/closed) has been performed.
 * Deprecated for now don't use it any more never fire
 *
 * @author Nikolay Zamosenchuk
 */
@Deprecated
public class ProjectReadyEvent extends GwtEvent<ProjectReadyHandler> {

    /** Type class used to register this event. */
    public static Type<ProjectReadyHandler> TYPE = new Type<>();

    private final ProjectConfigDto project;

    /**
     * Create new {@link ProjectReadyEvent}.
     *
     * @param project
     *         an instance of affected project
     */
    protected ProjectReadyEvent(ProjectConfigDto project) {
        this.project = project;
    }

    /**
     * Creates a Project Opened Event.
     *
     * @param project
     *         opened project
     */
    public static ProjectReadyEvent createReadyEvent(ProjectConfigDto project) {
        return new ProjectReadyEvent(project);
    }

    @Override
    public Type<ProjectReadyHandler> getAssociatedType() {
        return TYPE;
    }

    /** @return the instance of affected project */
    public ProjectConfigDto getProjectConfig() {
        return project;
    }

    @Override
    protected void dispatch(ProjectReadyHandler handler) {
        handler.onProjectReady(this);
    }
}
