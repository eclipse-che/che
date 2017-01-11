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

import org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent.StoreClearHandler;

/**
 * Indicates that the data in the Store has been cleared.
 *
 * @author Vlad Zhukovskiy
 */
public final class StoreClearEvent extends GwtEvent<StoreClearHandler> {

    public interface HasStoreClearHandler extends HasHandlers {
        HandlerRegistration addStoreClearHandler(StoreClearHandler handler);
    }

    public interface StoreClearHandler extends EventHandler {
        void onClear(StoreClearEvent event);
    }

    private static Type<StoreClearHandler> TYPE;

    public static Type<StoreClearHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    @Override
    public Type<StoreClearHandler> getAssociatedType() {
        return getType();
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(StoreClearHandler handler) {
        handler.onClear(this);
    }
}