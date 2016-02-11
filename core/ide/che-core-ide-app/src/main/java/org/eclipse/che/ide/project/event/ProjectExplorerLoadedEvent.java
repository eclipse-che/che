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
package org.eclipse.che.ide.project.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.project.node.AbstractProjectBasedNode;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Event fires when projects have been loaded into ide.
 * This event contains root project nodes which ones initialized.
 * These nodes are always instance of {@link AbstractProjectBasedNode}.
 *
 * @author Vlad Zhukovskiy
 */
public class ProjectExplorerLoadedEvent extends GwtEvent<ProjectExplorerLoadedEvent.ProjectExplorerLoadedHandler> {

    public interface ProjectExplorerLoadedHandler extends EventHandler {
        void onProjectsLoaded(ProjectExplorerLoadedEvent event);
    }

    private static Type<ProjectExplorerLoadedHandler> TYPE;

    public static Type<ProjectExplorerLoadedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private final List<Node> nodes;

    public ProjectExplorerLoadedEvent(@NotNull List<Node> nodes) {
        this.nodes = nodes;
    }

    @NotNull
    public List<Node> getNodes() {
        return nodes;
    }

    /** {@inheritDoc} */
    @Override
    public Type<ProjectExplorerLoadedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ProjectExplorerLoadedHandler handler) {
        handler.onProjectsLoaded(this);
    }
}
