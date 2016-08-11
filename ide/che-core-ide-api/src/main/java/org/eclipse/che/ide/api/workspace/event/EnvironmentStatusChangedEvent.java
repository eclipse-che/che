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
 * Event informing about changing of environments status.
 *
 * @author Vitalii Parfonov
 */
public class EnvironmentStatusChangedEvent extends GwtEvent<EnvironmentStatusChangedEvent.Handler> {

    private final boolean                      dev;
    private final String                       error;
    private final EventType                    eventType;
    private final String                       machineId;
    private final String                       machineName;
    private final String                       workspaceId;

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
        void onEnvironmentStatusChanged(EnvironmentStatusChangedEvent event);
    }

    public static final Type<EnvironmentStatusChangedEvent.Handler> TYPE = new Type<>();

    public EnvironmentStatusChangedEvent(MachineStatusEvent machineStatusEvent) {
        dev = machineStatusEvent.isDev();
        error = machineStatusEvent.getError();
        eventType = machineStatusEvent.getEventType();
        machineId = machineStatusEvent.getMachineId();
        machineName = machineStatusEvent.getMachineName();
        workspaceId = machineStatusEvent.getWorkspaceId();
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onEnvironmentStatusChanged(this);
    }

    public boolean isDev() {
        return dev;
    }

    public String getError() {
        return error;
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
