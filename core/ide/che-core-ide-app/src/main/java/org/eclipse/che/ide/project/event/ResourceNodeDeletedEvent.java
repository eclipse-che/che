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

import org.eclipse.che.ide.project.event.ResourceNodeDeletedEvent.ResourceNodeDeletedHandler;
import org.eclipse.che.ide.project.node.ResourceBasedNode;

import javax.validation.constraints.NotNull;

/**
 * @author Vlad Zhukovskiy
 */
public class ResourceNodeDeletedEvent extends GwtEvent<ResourceNodeDeletedHandler> {

    public interface ResourceNodeDeletedHandler extends EventHandler {
        void onResourceEvent(ResourceNodeDeletedEvent event);
    }

    private static Type<ResourceNodeDeletedHandler> TYPE;

    public static Type<ResourceNodeDeletedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private final ResourceBasedNode node;

    public ResourceNodeDeletedEvent(@NotNull ResourceBasedNode node) {
        this.node = node;
    }

    @NotNull
    public ResourceBasedNode getNode() {
        return node;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Type<ResourceNodeDeletedHandler> getAssociatedType() {
        return (Type)TYPE;
    }

    @Override
    protected void dispatch(ResourceNodeDeletedHandler handler) {
        handler.onResourceEvent(this);
    }
}
