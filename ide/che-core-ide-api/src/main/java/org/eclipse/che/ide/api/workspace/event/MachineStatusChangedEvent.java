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
package org.eclipse.che.ide.api.workspace.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType;

/**
 * Event informing about changing the machine status.
 *
 * @author Vitalii Parfonov
 */
public class MachineStatusChangedEvent extends GwtEvent<MachineStatusChangedEvent.Handler> {

    /**
     * Implement this handler to handle the event.
     */
    public interface Handler extends EventHandler {
        /**
         * Performs some actions when environments status has been changed.
         *
         * @param event
         *         contains information about environments status
         */
        void onMachineStatusChanged(MachineStatusChangedEvent event);
    }

    public static final Type<MachineStatusChangedEvent.Handler> TYPE = new Type<>();

    private final String                       workspaceId;
    private final String                       machineId;
    private final String                       machineName;
    private final boolean                      dev;
    private final EventType                    eventType;
    private final String                       errorMessage;

    public MachineStatusChangedEvent(MachineStatusEvent machineStatusEvent) {
        workspaceId = machineStatusEvent.getWorkspaceId();
        machineId = machineStatusEvent.getMachineId();
        machineName = machineStatusEvent.getMachineName();
        dev = machineStatusEvent.isDev();
        eventType = machineStatusEvent.getEventType();
        errorMessage = machineStatusEvent.getError();
    }

    public MachineStatusChangedEvent(String workspaceId,
                                     String machineId,
                                     String machineName,
                                     boolean dev,
                                     EventType eventType,
                                     String errorMessage) {
        this.workspaceId = workspaceId;
        this.machineId = machineId;
        this.machineName = machineName;
        this.dev = dev;
        this.eventType = eventType;
        this.errorMessage = errorMessage;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onMachineStatusChanged(this);
    }

    public boolean isDev() {
        return dev;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getMachineName() {
        return machineName;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }
}
