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

import org.eclipse.che.api.core.model.project.Project;

/**
 * This event should be fired when we select different projects.
 * @deprecated since 4.6.0 replaced by {@link org.eclipse.che.ide.api.event.SelectionChangedEvent}
 *
 * @author Dmitry Shnurenko
 */
@Deprecated
public class CurrentProjectChangedEvent extends GwtEvent<CurrentProjectChangedHandler> {

    /** Type class used to register this event. */
    public static Type<CurrentProjectChangedHandler> TYPE = new Type<>();

    private final Project project;

    /**
     * Creates an event to initiate changing of current project.
     *
     * @param project
     *         selected project
     */
    public CurrentProjectChangedEvent(Project project) {
        this.project = project;
    }

    @Override
    public Type<CurrentProjectChangedHandler> getAssociatedType() {
        return TYPE;
    }

    /** Returns descriptor of the project. */
    public Project getProject() {
        return project;
    }

    @Override
    protected void dispatch(CurrentProjectChangedHandler handler) {
        handler.onCurrentProjectChanged(this);
    }
}
