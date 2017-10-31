/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.dto.event;

import org.eclipse.che.api.core.model.workspace.runtime.InstallerStatus;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * Installer status event DTO.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@DTO
public interface InstallerStatusEvent {

  InstallerStatus getStatus();

  void setStatus(InstallerStatus status);

  InstallerStatusEvent withStatus(InstallerStatus status);

  String getInstaller();

  void setInstaller(String installer);

  InstallerStatusEvent withInstaller(String installer);

  String getMachineName();

  void setMachineName(String machineName);

  InstallerStatusEvent withMachineName(String machineName);

  RuntimeIdentityDto getRuntimeId();

  void setRuntimeId(RuntimeIdentityDto runtimeId);

  InstallerStatusEvent withRuntimeId(RuntimeIdentityDto runtimeId);

  String getError();

  void setError(String error);

  InstallerStatusEvent withError(String error);

  String getTime();

  void setTime(String time);

  InstallerStatusEvent withTime(String time);
}
