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

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.project.node.ResourceBasedNode;

import javax.validation.constraints.NotNull;

/**
 * @author Vlad Zhukovskiy
 */
public class ResourceNodeRenamedEvent<DataObject> extends GwtEvent<ResourceNodeRenamedEvent.ResourceNodeRenamedHandler> {

    public interface ResourceNodeRenamedHandler extends EventHandler {
        void onResourceRenamedEvent(ResourceNodeRenamedEvent event);
    }

    private static Type<ResourceNodeRenamedHandler> TYPE;

    public static Type<ResourceNodeRenamedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private final ResourceBasedNode node;
    private final DataObject        newDataObject;

    public ResourceNodeRenamedEvent(@Nullable ResourceBasedNode node, @NotNull DataObject newDataObject) {
        this.node = node;
        this.newDataObject = newDataObject;
    }

    @NotNull
    public ResourceBasedNode getNode() {
        return node;
    }

    public DataObject getNewDataObject() {
        return newDataObject;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Type<ResourceNodeRenamedHandler> getAssociatedType() {
        return (Type)TYPE;
    }

    @Override
    protected void dispatch(ResourceNodeRenamedHandler handler) {
        handler.onResourceRenamedEvent(this);
    }
}
