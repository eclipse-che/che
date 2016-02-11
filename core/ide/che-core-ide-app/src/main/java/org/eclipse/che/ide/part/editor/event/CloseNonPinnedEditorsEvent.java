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
package org.eclipse.che.ide.part.editor.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Close non pinned editor tabs event.
 *
 * @author Vlad Zhukovskiy
 */
public class CloseNonPinnedEditorsEvent extends GwtEvent<CloseNonPinnedEditorsEvent.CloseNonPinnedEditorsHandler> {

    public interface CloseNonPinnedEditorsHandler extends EventHandler {
        void onCloseNonPinnedEditors(CloseNonPinnedEditorsEvent event);
    }

    private static Type<CloseNonPinnedEditorsHandler> TYPE;

    public static Type<CloseNonPinnedEditorsHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    public Type<CloseNonPinnedEditorsHandler> getAssociatedType() {
        return getType();
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(CloseNonPinnedEditorsHandler handler) {
        handler.onCloseNonPinnedEditors(this);
    }
}