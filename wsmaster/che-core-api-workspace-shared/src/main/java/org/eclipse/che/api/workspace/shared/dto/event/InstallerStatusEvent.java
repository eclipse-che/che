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

import org.eclipse.che.dto.shared.DTO;
/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@DTO
public interface InstallerStatusEvent {

    enum Status {
        STARTING,
        RUNNING,
        DONE,
        FAILED
    }


    InstallerStatusEvent.Status getStatus();

    void setStatus(InstallerStatusEvent.Status status);

    InstallerStatusEvent withStatus(InstallerStatusEvent.Status status);


    String getInstaller();

    void setInstaller(String installer);

    InstallerStatusEvent withInstaller(String installer);


    String getMachineName();

    void setMachineName(String machineName);

    InstallerStatusEvent withMachineName(String machineName);
    

    RuntimeId getRuntimeId();

    void setRuntimeId(RuntimeId runtimeId);

    InstallerStatusEvent withRuntimeId(RuntimeId runtimeId);
    

    String getError();

    void setError(String error);

    InstallerStatusEvent withError(String error);
    

    String getTime();

    void setTime(String time);

    InstallerStatusEvent withTime(String time);
}
