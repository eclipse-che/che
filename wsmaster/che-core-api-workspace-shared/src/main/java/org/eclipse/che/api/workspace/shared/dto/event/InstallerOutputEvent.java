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
public interface InstallerOutputEvent {

    enum Stream {
        STDOUT,
        STDERR
    }

    String getText();

    void setText(String text);

    InstallerOutputEvent withText(String text);


    InstallerOutputEvent.Stream getStream();

    void setStream(InstallerOutputEvent.Stream stream);

    InstallerOutputEvent withStream(InstallerOutputEvent.Stream stream);


    String getInstaller();

    void setInstaller(String installer);

    InstallerOutputEvent withInstaller(String installer);


    String getMachineName();

    void setMachineName(String machineName);

    InstallerOutputEvent withMachineName(String machineName);


    RuntimeId getRuntimeId();

    void setRuntimeId(RuntimeId runtimeId);

    InstallerOutputEvent withRuntimeId(RuntimeId runtimeId);


    String getTime();

    void setTime(String time);

    InstallerOutputEvent withTime(String time);
}
