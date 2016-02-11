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
package org.eclipse.che.api.machine.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;

/**
 * Event that describes the fact that dev machine state has been changed.
 *
 * @author Roman Nikitenko
 */
public class DevMachineStateEvent extends GwtEvent<DevMachineStateHandler> {

    /** Type class used to register this event. */
    public static Type<DevMachineStateHandler> TYPE = new Type<>();
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
    public Type<DevMachineStateHandler> getAssociatedType() {
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
    protected void dispatch(DevMachineStateHandler handler) {
        switch (status) {
            case RUNNING:
                handler.onMachineStarted(this);
                break;
            case DESTROYED:
                handler.onMachineDestroyed(this);
                break;
            default:
                break;
        }
    }
}
