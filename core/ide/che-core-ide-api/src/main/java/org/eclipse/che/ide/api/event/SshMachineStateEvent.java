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
package org.eclipse.che.ide.api.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.machine.shared.dto.MachineDto;

import static org.eclipse.che.ide.api.event.SshMachineStateEvent.SshMachineState.CONNECTED;
import static org.eclipse.che.ide.api.event.SshMachineStateEvent.SshMachineState.DISCONNECTED;

/**
 * The class represents event which will be fired when user connect or disconnect to ssh machine.
 *
 * @author Dmitry Shnurenko
 */
public class SshMachineStateEvent extends GwtEvent<SshMachineStateEvent.SshMachineStateHandler> {

    public static Type<SshMachineStateHandler> TYPE = new Type<>();

    private final SshMachineState sshMachineState;
    private final MachineDto      machine;

    private SshMachineStateEvent(SshMachineState sshMachineState, MachineDto machine) {
        this.sshMachineState = sshMachineState;
        this.machine = machine;
    }

    public static SshMachineStateEvent createSshMachineConnectedEvent(MachineDto machine) {
        return new SshMachineStateEvent(CONNECTED, machine);
    }

    public static SshMachineStateEvent createSshMachineDisConnectedEvent(MachineDto machine) {
        return new SshMachineStateEvent(DISCONNECTED, machine);
    }

    public MachineDto getMachine() {
        return machine;
    }

    @Override
    public Type<SshMachineStateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SshMachineStateHandler handler) {
        switch (sshMachineState) {
            case CONNECTED:
                handler.onSshMachineConnected(this);
                break;
            case DISCONNECTED:
                handler.onSshMachineDisconnected(this);
                break;
            default:
                break;
        }
    }

    public enum SshMachineState {
        CONNECTED, DISCONNECTED
    }

    public interface SshMachineStateHandler extends EventHandler {
        /**
         * Performs some actions when user connects to ssh machine.
         *
         * @param event
         *         event which contains information about machine
         */
        void onSshMachineConnected(SshMachineStateEvent event);

        /**
         * Performs some actions when user disconnects from ssh machine.
         *
         * @param event
         *         event which contains information about machine
         */
        void onSshMachineDisconnected(SshMachineStateEvent event);
    }
}
