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
package org.eclipse.che.ide.api.parts.base;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;

/**
 * Fire this event to maximize part.
 *
 * @author Vitaliy Guliy
 */
public class MaximizePartEvent extends GwtEvent<MaximizePartEvent.Handler> {

    /**
     * Implement this handler to handle maximizing the part.
     */
    public interface Handler extends EventHandler {

        void onMaximizePart(MaximizePartEvent event);

    }

    public static final GwtEvent.Type<MaximizePartEvent.Handler> TYPE = new GwtEvent.Type<>();

    private PartPresenter part;

    public MaximizePartEvent(PartPresenter part) {
        this.part = part;
    }

    /**
     * Returns part to be maximized.
     *
     * @return part
     */
    public PartPresenter getPart() {
        return part;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onMaximizePart(this);
    }

}
