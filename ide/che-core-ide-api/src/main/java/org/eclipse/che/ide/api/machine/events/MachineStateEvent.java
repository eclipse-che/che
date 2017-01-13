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

import org.eclipse.che.ide.api.machine.MachineEntity;

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

    private final MachineEntity machine;

    private final MachineAction machineAction;

    /**
     * Create new {@link MachineStateEvent}.
     *
     * @param machine
     *         machine
     * @param machineAction
     *         the type of action
     */
    public MachineStateEvent(MachineEntity machine, MachineAction machineAction) {
        this.machine = machine;
        this.machineAction = machineAction;
    }

    @Override
    public Type<MachineStateEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public MachineEntity getMachine() {
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
