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
package org.eclipse.che.api.machine.shared.dto.execagent;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Dmitry Kuleshov
 */
@DTO
public interface GetProcessResponseDto {
    Integer getPid();

    GetProcessResponseDto withPid(Integer pid);

    String getName();

    GetProcessResponseDto withName(String name);

    String getCommandLine();

    GetProcessResponseDto withCommandLine(String commandLine);

    String getType();

    GetProcessResponseDto withType(String type);

    Boolean isAlive();

    GetProcessResponseDto withAlive(Boolean alive);

    Integer getNativePid();

    GetProcessResponseDto withNativePid(Integer nativePid);
}
