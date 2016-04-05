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
package org.eclipse.che.ide.ext.debugger.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Summary of debugger information.
 *
 * @author andrew00x
 */
@DTO
public interface DebuggerInfo {
    String getHost();

    void setHost(String host);

    DebuggerInfo withHost(String host);

    int getPort();

    void setPort(int port);

    DebuggerInfo withPort(int port);

    String getId();

    void setId(String id);

    DebuggerInfo withId(String id);

    String getName();

    void setName(String name);

    DebuggerInfo withName(String name);

    String getVersion();

    void setVersion(String version);

    DebuggerInfo withVersion(String version);

    int getPid();

    void setPid(int pid);

    DebuggerInfo withPid(int pid);

    String getFile();

    void setFile(String file);

    DebuggerInfo withFile(String file);
}
