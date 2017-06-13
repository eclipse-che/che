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
package org.eclipse.che.ide.api.machine.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.ide.api.workspace.model.MachineImpl;

public class MachineFailedEvent extends GwtEvent<MachineFailedEvent.Handler> {

    public static final Type<MachineFailedEvent.Handler> TYPE = new Type<>();

    private final MachineImpl machine;
    private final String      error;

    public MachineFailedEvent(MachineImpl machine, String error) {
        this.machine = machine;
        this.error = error;
    }

    public MachineImpl getMachine() {
        return machine;
    }

    public String getError() {
        return error;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onMachineFailed(this);
    }

    public interface Handler extends EventHandler {
        void onMachineFailed(MachineFailedEvent event);
    }
}
