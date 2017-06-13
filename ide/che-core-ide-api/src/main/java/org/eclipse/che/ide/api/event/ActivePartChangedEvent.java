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
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.ide.api.parts.PartPresenter;

/**
 * Event that notifies of changing active PartPresenter
 *
 * @author Nikolay Zamosenchuk
 */
public class ActivePartChangedEvent extends GwtEvent<ActivePartChangedEvent.Handler> {

    public interface Handler extends EventHandler {
        /**
         * Active part have changed
         *
         * @param event
         */
        void onActivePartChanged(ActivePartChangedEvent event);

    }

    public static Type<Handler> TYPE = new Type<>();

    private final PartPresenter activePart;

    public ActivePartChangedEvent(PartPresenter activePart) {
        this.activePart = activePart;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    /** @return instance of Active Part */
    public PartPresenter getActivePart() {
        return activePart;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onActivePartChanged(this);
    }

}
