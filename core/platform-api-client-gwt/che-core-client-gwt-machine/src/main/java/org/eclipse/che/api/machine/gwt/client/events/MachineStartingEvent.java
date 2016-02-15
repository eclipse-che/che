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

import org.eclipse.che.api.machine.shared.dto.MachineDto;

/**
 * The class represents special event which is fired when machine just start running (click on create machine button). And contains
 * information about starting machine.
 *
 * @author Dmitry Shnurenko
 */
public class MachineStartingEvent extends GwtEvent<MachineStartingHandler> {

    public static Type<MachineStartingHandler> TYPE = new Type<>();

    private final MachineDto machine;

    public MachineStartingEvent(MachineDto machine) {
        this.machine = machine;
    }

    public MachineDto getMachine() {
        return machine;
    }

    @Override
    public Type<MachineStartingHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MachineStartingHandler handler) {
        handler.onMachineStarting(this);
    }
}
