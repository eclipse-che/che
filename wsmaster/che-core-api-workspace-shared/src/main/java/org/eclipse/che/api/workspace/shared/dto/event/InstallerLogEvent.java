/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
