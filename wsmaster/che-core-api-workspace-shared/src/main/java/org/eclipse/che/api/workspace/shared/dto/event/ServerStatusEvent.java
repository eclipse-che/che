/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.dto.event;

import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.dto.shared.DTO;

/** @author gazarenkov */
@DTO
public interface ServerStatusEvent {

  ServerStatus getStatus();

  ServerStatusEvent withStatus(ServerStatus status);

  String getServerName();

  ServerStatusEvent withServerName(String serverName);

  String getServerUrl();

  ServerStatusEvent withServerUrl(String serverUrl);

  String getMachineName();

  ServerStatusEvent withMachineName(String machineName);

  /** @return runtime identity */
  RuntimeIdentityDto getIdentity();

  ServerStatusEvent withIdentity(RuntimeIdentityDto identity);
}
