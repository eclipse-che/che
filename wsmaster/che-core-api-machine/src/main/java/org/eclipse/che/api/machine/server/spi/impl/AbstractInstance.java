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
package org.eclipse.che.api.machine.server.spi.impl;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.spi.Instance;

/**
 * @author Alexander Garagatyi
 */
public abstract class AbstractInstance extends MachineImpl implements Instance {
    public AbstractInstance(Machine machine) {
        super(machine);
    }

    @Override
    public synchronized MachineStatus getStatus() {
        return super.getStatus();
    }

    @Override
    public synchronized void setStatus(MachineStatus status) {
        super.setStatus(status);
    }

    @Override
    public abstract MachineRuntimeInfoImpl getRuntime();
}
