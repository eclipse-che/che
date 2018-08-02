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
package org.eclipse.che.multiuser.machine.authentication.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Representation of machine token, that needed for communication between workspace master and
 * workspace agents.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@DTO
public interface MachineTokenDto {

  String getUserId();

  void setUserId(String userId);

  MachineTokenDto withUserId(String userId);

  String getWorkspaceId();

  void setWorkspaceId(String workspaceId);

  MachineTokenDto withWorkspaceId(String workspaceId);

  String getMachineToken();

  void setMachineToken(String machineToken);

  MachineTokenDto withMachineToken(String machineToken);
}
