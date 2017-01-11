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

import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;

/**
 * Event that describes the fact that dev machine state has been changed.
 *
 * @author Roman Nikitenko
 */
public class DevMachineStateEvent extends GwtEvent<DevMachineStateEvent.Handler> {

    public interface Handler extends EventHandler {
        /**
         * Called when dev machine has been started.
         *
         * @param event
         *         the fired {@link DevMachineStateEvent}
         */
        void onDevMachineStarted(DevMachineStateEvent event);

        /**
         * Called when dev machine has been destroyed.
         *
         * @param event
         *         the fired {@link DevMachineStateEvent}
         */
        void onDevMachineDestroyed(DevMachineStateEvent event);
    }

    /** Type class used to register this event. */
    public static Type<DevMachineStateEvent.Handler> TYPE = new Type<>();
    private final MachineStatusEvent.EventType status;
    private final String                       machineId;
    private final String                       workspaceId;
    private final String                       machineName;
    private final String                       error;

    /**
     * Create new {@link DevMachineStateEvent}.
     *
     * @param event
     *         the type of action
     */
    public DevMachineStateEvent(MachineStatusEvent event) {
        this.status = event.getEventType();
        this.machineId = event.getMachineId();
        this.workspaceId = event.getWorkspaceId();
        this.machineName = event.getMachineName();
        this.error = event.getError();
    }

    @Override
    public Type<DevMachineStateEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    /** @return the status of the dev machine */
    public MachineStatusEvent.EventType getDevMachineStatus() {
        return status;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getMachineName() {
        return machineName;
    }

    public String getError() {
        return error;
    }

    @Override
    protected void dispatch(DevMachineStateEvent.Handler handler) {
        switch (status) {
            case RUNNING:
                handler.onDevMachineStarted(this);
                break;
            case DESTROYED:
                handler.onDevMachineDestroyed(this);
                break;
        }
    }

}
