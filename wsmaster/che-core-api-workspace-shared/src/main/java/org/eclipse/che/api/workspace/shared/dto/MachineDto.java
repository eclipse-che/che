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
package org.eclipse.che.api.workspace.shared.dto;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Garagatyi */
@DTO
public interface MachineDto extends Machine {

  @Override
  Map<String, String> getAttributes();

  MachineDto withAttributes(Map<String, String> attributes);

  @Override
  Map<String, ServerDto> getServers();

  MachineDto withServers(Map<String, ServerDto> servers);

  @Override
  MachineStatus getStatus();

  MachineDto withStatus(MachineStatus status);
}
