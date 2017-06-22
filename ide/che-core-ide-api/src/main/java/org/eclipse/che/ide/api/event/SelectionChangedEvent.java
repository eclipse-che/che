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

import org.eclipse.che.ide.api.selection.Selection;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that notifies of changed Selection
 *
 * @author Nikolay Zamosenchuk
 */
public class SelectionChangedEvent extends GwtEvent<SelectionChangedHandler> {
    public static Type<SelectionChangedHandler> TYPE = new Type<>();

    private final Selection<?> selection;

    /**
     * @param selection
     *         new selection
     */
    public SelectionChangedEvent(Selection<?> selection) {
        this.selection = selection;
    }

    @Override
    public Type<SelectionChangedHandler> getAssociatedType() {
        return TYPE;
    }

    /** @return current selection */
    public Selection<?> getSelection() {
        return selection;
    }

    @Override
    protected void dispatch(SelectionChangedHandler handler) {
        handler.onSelectionChanged(this);
    }
}
