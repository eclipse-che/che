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

import org.eclipse.che.ide.ui.smartTree.event.StoreSortEvent.StoreSortHandler;

/**
 * Indicates that the store sort properties have changed.
 *
 * @author Vlad Zhukovskiy
 */
public final class StoreSortEvent extends GwtEvent<StoreSortHandler> {

    public interface HasStoreSortHandler extends HasHandlers {
        HandlerRegistration addStoreSortHandler(StoreSortHandler handler);
    }

    public interface StoreSortHandler extends EventHandler {
        void onSort(StoreSortEvent event);
    }

    private static Type<StoreSortHandler> TYPE;

    public static Type<StoreSortHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    @Override
    public Type<StoreSortHandler> getAssociatedType() {
        return getType();
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(StoreSortHandler handler) {
        handler.onSort(this);
    }
}
