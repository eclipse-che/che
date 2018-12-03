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

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Garagatyi */
@DTO
public interface RuntimeDto extends Runtime, Hyperlinks {

  @Override
  String getActiveEnv();

  void setActiveEnv(String activeEnv);

  RuntimeDto withActiveEnv(String activeEnvName);

  @Override
  Map<String, MachineDto> getMachines();

  void setMachines(Map<String, MachineDto> machines);

  RuntimeDto withMachines(Map<String, MachineDto> machines);

  @Override
  String getOwner();

  RuntimeDto withOwner(String owner);

  String getMachineToken();

  RuntimeDto withMachineToken(String machineToken);

  void setMachineToken(String machineToken);

  @Override
  List<WarningDto> getWarnings();

  void setWarnings(List<WarningDto> warnings);

  RuntimeDto withWarnings(List<WarningDto> warnings);

  @Override
  List<CommandDto> getCommands();

  void setCommands(List<CommandDto> commands);

  RuntimeDto withCommands(List<CommandDto> commands);

  @Override
  RuntimeDto withLinks(List<Link> links);
}
