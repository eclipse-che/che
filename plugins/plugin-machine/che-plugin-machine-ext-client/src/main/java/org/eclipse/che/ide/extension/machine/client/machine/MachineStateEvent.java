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
package org.eclipse.che.ide.extension.machine.client.machine;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that describes the fact that machine state has been changed.
 *
 * @author Artem Zatsarynnyi
 */
public class MachineStateEvent extends GwtEvent<MachineStateEvent.Handler> {

    public interface Handler extends EventHandler {

        /**
         * Is called when creating a machine.
         *
         * @param event
         *         state event
         */
        void onMachineCreating(MachineStateEvent event);

        /**
         * Called when machine has been run.
         *
         * @param event
         *         the fired {@link MachineStateEvent}
         */
        void onMachineRunning(MachineStateEvent event);

        /**
         * Called when machine has been destroyed.
         *
         * @param event
         *         the fired {@link MachineStateEvent}
         */
        void onMachineDestroyed(MachineStateEvent event);

    }

    /**
     * Type class used to register this event.
     */
    public static final Type<MachineStateEvent.Handler> TYPE = new Type<>();

    private final org.eclipse.che.api.core.model.machine.Machine machine;

    private final MachineAction machineAction;

    /**
     * Create new {@link MachineStateEvent}.
     *
     * @param machine
     *         machine
     * @param machineAction
     *         the type of action
     */
    public MachineStateEvent(org.eclipse.che.api.core.model.machine.Machine machine, MachineAction machineAction) {
        this.machine = machine;
        this.machineAction = machineAction;
    }

    @Override
    public Type<MachineStateEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public org.eclipse.che.api.core.model.machine.Machine getMachine() {
        return machine;
    }

    public String getMachineId() {
        return machine.getId();
    }

    @Override
    protected void dispatch(MachineStateEvent.Handler handler) {
        switch (machineAction) {
            case CREATING:
                handler.onMachineCreating(this);
                break;
            case RUNNING:
                handler.onMachineRunning(this);
                break;
            case DESTROYED:
                handler.onMachineDestroyed(this);
                break;
        }
    }

    /**
     * Set of possible type of machine actions.
     */
    public enum MachineAction {
        CREATING,
        RUNNING,
        DESTROYED
    }

}
