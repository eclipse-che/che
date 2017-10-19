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

import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * Installer log event DTO.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@DTO
public interface InstallerLogEvent {

  enum Stream {
    STDOUT,
    STDERR
  }

  String getText();

  void setText(String text);

  InstallerLogEvent withText(String text);

  InstallerLogEvent.Stream getStream();

  void setStream(InstallerLogEvent.Stream stream);

  InstallerLogEvent withStream(InstallerLogEvent.Stream stream);

  String getInstaller();

  void setInstaller(String installer);

  InstallerLogEvent withInstaller(String installer);

  String getMachineName();

  void setMachineName(String machineName);

  InstallerLogEvent withMachineName(String machineName);

  RuntimeIdentityDto getRuntimeId();

  void setRuntimeId(RuntimeIdentityDto runtimeId);

  InstallerLogEvent withRuntimeId(RuntimeIdentityDto runtimeId);

  String getTime();

  void setTime(String time);

  InstallerLogEvent withTime(String time);
}
