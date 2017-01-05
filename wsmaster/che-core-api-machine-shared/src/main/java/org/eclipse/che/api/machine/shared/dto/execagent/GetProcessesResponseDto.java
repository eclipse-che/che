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
package org.eclipse.che.api.machine.shared.dto.execagent;

import org.eclipse.che.api.machine.shared.dto.execagent.event.DtoWithPid;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface GetProcessesResponseDto extends DtoWithPid {
    GetProcessesResponseDto withPid(int pid);

    String getName();

    GetProcessesResponseDto withName(String name);

    String getCommandLine();

    GetProcessesResponseDto withCommandLine(String commandLine);

    String getType();

    GetProcessesResponseDto withType(String type);

    boolean isAlive();

    GetProcessesResponseDto withAlive(boolean alive);

    int getNativePid();

    GetProcessesResponseDto withNativePid(int nativePid);
}
