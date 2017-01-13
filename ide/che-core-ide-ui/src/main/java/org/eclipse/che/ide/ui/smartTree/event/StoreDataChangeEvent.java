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
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.event.StoreDataChangeEvent.StoreDataChangeHandler;

/**
 * Indicates that the items in the store have been replaced.
 *
 * @author Vlad Zhukovskiy
 */
public final class StoreDataChangeEvent extends GwtEvent<StoreDataChangeHandler> {

    public interface HasStoreDataChangeHandlers extends HasHandlers {
        HandlerRegistration addStoreDataChangeHandler(StoreDataChangeHandler handler);
    }

    public interface StoreDataChangeHandler extends EventHandler {
        void onDataChange(StoreDataChangeEvent event);
    }

    private static Type<StoreDataChangeHandler> TYPE;

    public static Type<StoreDataChangeHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private Node parent;

    public StoreDataChangeEvent(Node parent) {
        this.parent = parent;
    }

    @Override
    public Type<StoreDataChangeHandler> getAssociatedType() {
        return getType();
    }

    public Node getParent() {
        return parent;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(StoreDataChangeHandler handler) {
        handler.onDataChange(this);
    }
}