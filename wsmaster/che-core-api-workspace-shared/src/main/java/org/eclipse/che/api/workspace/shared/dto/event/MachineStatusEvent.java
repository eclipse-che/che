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
package org.eclipse.che.api.workspace.shared.dto.event;

import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes event about status of machine
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
//@EventOrigin("machine")
@DTO
public interface MachineStatusEvent {
    enum EventType {
        STARTING,
        RUNNING,
        STOPPED,
        FAILED
    }


    EventType getEventType();

    void setEventType(EventType eventType);

    MachineStatusEvent withEventType(EventType eventType);

    String getError();

    void setError(String error);

    MachineStatusEvent withError(String error);


    String getMachineName();

    MachineStatusEvent withMachineName(String machineName);

    /**
     * @return runtime identity
     */
    RuntimeIdentityDto getIdentity();

    MachineStatusEvent withIdentity(RuntimeIdentityDto identity);

}
