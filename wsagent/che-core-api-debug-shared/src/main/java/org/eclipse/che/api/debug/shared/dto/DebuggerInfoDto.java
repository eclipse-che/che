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
package org.eclipse.che.api.debug.shared.dto;

import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Anatoliy Bazko
 */
@DTO
public interface DebuggerInfoDto extends DebuggerInfo {
    String getHost();

    void setHost(String host);

    DebuggerInfoDto withHost(String host);

    int getPort();

    void setPort(int port);

    DebuggerInfoDto withPort(int port);

    String getName();

    void setName(String name);

    DebuggerInfoDto withName(String name);

    String getVersion();

    void setVersion(String version);

    DebuggerInfoDto withVersion(String version);

    int getPid();

    void setPid(int pid);

    DebuggerInfoDto withPid(int pid);

    String getFile();

    void setFile(String file);

    DebuggerInfoDto withFile(String file);
}
