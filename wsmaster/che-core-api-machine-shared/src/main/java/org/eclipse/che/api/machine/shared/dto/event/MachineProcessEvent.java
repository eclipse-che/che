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
package org.eclipse.che.api.machine.shared.dto.event;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes event about state of process in machine machine
 *
 * @author Alexander Garagatyi
 */
@EventOrigin("machine")
@DTO
public interface MachineProcessEvent {
    public enum EventType {
        STARTED,
        STOPPED,
        ERROR
    }

    EventType getEventType();

    void setEventType(EventType eventType);

    MachineProcessEvent withEventType(EventType eventType);

    String getMachineId();

    void setMachineId(String machineId);

    MachineProcessEvent withMachineId(String machineId);

    int getProcessId();

    void setProcessId(int processId);

    MachineProcessEvent withProcessId(int processId);

    String getError();

    void setError(String error);

    MachineProcessEvent withError(String error);
}
