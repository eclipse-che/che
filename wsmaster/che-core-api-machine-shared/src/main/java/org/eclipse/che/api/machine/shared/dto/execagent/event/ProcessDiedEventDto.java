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
public interface ProcessDiedEventDto extends DtoWithPid {
    ProcessDiedEventDto withPid(int pid);

    String getTime();

    ProcessDiedEventDto withTime(String time);

    int getNativePid();

    ProcessDiedEventDto withNativePid(int nativePid);

    String getName();

    ProcessDiedEventDto withName(String name);

    String getCommandLine();

    ProcessDiedEventDto withCommandLine(String commandLine);
}
