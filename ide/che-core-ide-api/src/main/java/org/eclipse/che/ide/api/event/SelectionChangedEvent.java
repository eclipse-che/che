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

import com.google.gwt.event.shared.EventHandler;
import org.eclipse.che.ide.api.selection.Selection;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that notifies of changed Selection
 *
 * @author Nikolay Zamosenchuk
 */
public class SelectionChangedEvent extends GwtEvent<SelectionChangedEvent.Handler> {

    public interface Handler extends EventHandler {
        /**
         * Selection Changed
         *
         * @param event
         */
        void onSelectionChanged(SelectionChangedEvent event);

    }

    public static Type<Handler> TYPE = new Type<>();

    private final Selection<?> selection;

    /**
     * @param selection
     *         new selection
     */
    public SelectionChangedEvent(Selection<?> selection) {
        this.selection = selection;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    /** @return current selection */
    public Selection<?> getSelection() {
        return selection;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onSelectionChanged(this);
    }

}
