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
 * An event that should be fired in order to configure the currently opened project.
 *
 * @author Artem Zatsarynnyi
 */
public class ConfigureProjectEvent extends GwtEvent<ConfigureProjectEvent.Handler> {

    public interface Handler extends EventHandler {
        /**
         * Called when someone wants to configure the currently opened project.
         *
         * @param event
         *         the fired {@link ConfigureProjectEvent}
         */
        void onConfigureProject(ConfigureProjectEvent event);
    }

    public static Type<Handler> TYPE = new Type<>();

    private final ProjectConfigDto projectConfig;

    public ConfigureProjectEvent(ProjectConfigDto projectConfig) {
        this.projectConfig = projectConfig;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onConfigureProject(this);
    }

    public ProjectConfigDto getProject() {
        return projectConfig;
    }

}
