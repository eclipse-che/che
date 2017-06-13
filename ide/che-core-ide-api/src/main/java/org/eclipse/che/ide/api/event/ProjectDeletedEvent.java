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
package org.eclipse.che.ide.api.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * The class store information about deleted project. This event should be fired when we delete project.
 *
 * @author Dmitry Shnurenko
 */
public class ProjectDeletedEvent extends GwtEvent<ProjectDeletedEvent.Handler> {

    public interface Handler extends EventHandler {

        /**
         * Performs some actions when user removes a project.
         *
         * @param event
         *         contains information about deleted project
         */
        void onProjectDeleted(ProjectDeletedEvent event);

    }

    public static Type<Handler> TYPE = new Type<>();

    private final ProjectConfigDto configDto;

    public ProjectDeletedEvent(ProjectConfigDto configDto) {
        this.configDto = configDto;
    }

    /** Returns project descriptor. It contains information about project. */
    public ProjectConfigDto getProjectConfig() {
        return configDto;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onProjectDeleted(this);
    }

}
