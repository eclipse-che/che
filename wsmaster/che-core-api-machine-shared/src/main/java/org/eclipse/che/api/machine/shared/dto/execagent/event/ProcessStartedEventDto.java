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
package org.eclipse.che.api.machine.shared.dto.execagent.event;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ProcessStartedEventDto extends DtoWithPid {
    ProcessStartedEventDto withPid(int pid);

    String getTime();

    ProcessStartedEventDto withTime(String time);

    int getNativePid();

    ProcessStartedEventDto withNativePid(int nativePid);

    String getName();

    ProcessStartedEventDto withName(String name);

    String getCommandLine();

    ProcessStartedEventDto withCommandLine(String commandLine);
}
