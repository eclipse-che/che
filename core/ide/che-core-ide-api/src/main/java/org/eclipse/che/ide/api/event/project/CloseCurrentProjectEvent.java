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
 * An event that should be fired in order to close the currently opened project.
 *
 * @author Artem Zatsarynnyi
 * @deprecated @deprecated since project explorer has all projects opened
 */
@Deprecated
public class CloseCurrentProjectEvent extends GwtEvent<CloseCurrentProjectHandler> {

    /** Type class used to register this event. */
    public static Type<CloseCurrentProjectHandler> TYPE = new Type<>();

    private final ProjectConfigDto projectConfig;

    public CloseCurrentProjectEvent(ProjectConfigDto projectConfig) {
        this.projectConfig = projectConfig;
    }

    public ProjectConfigDto getProjectConfig() {
        return projectConfig;
    }

    @Override
    public Type<CloseCurrentProjectHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CloseCurrentProjectHandler handler) {
        handler.onCloseCurrentProject(this);
    }
}
