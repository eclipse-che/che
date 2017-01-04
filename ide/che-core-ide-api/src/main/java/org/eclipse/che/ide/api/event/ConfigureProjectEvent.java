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

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * An event that should be fired in order to configure the currently opened project.
 *
 * @author Artem Zatsarynnyi
 */
public class ConfigureProjectEvent extends GwtEvent<ConfigureProjectHandler> {
    public static Type<ConfigureProjectHandler> TYPE = new Type<>();

    private final ProjectConfigDto projectConfig;

    public ConfigureProjectEvent(ProjectConfigDto projectConfig) {
        this.projectConfig = projectConfig;
    }

    @Override
    public Type<ConfigureProjectHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ConfigureProjectHandler handler) {
        handler.onConfigureProject(this);
    }

    public ProjectConfigDto getProject() {
        return projectConfig;
    }
}
