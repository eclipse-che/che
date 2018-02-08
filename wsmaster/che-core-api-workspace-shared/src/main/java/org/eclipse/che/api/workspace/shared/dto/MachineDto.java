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
