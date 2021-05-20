/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import java.util.Map;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Garagatyi */
@DTO
public interface MachineConfigDto extends MachineConfig {

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Map<String, ServerConfigDto> getServers();

  void setServers(Map<String, ServerConfigDto> servers);

  MachineConfigDto withServers(Map<String, ServerConfigDto> servers);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Map<String, String> getEnv();

  void setEnv(Map<String, String> env);

  MachineConfigDto withEnv(Map<String, String> env);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  MachineConfigDto withAttributes(Map<String, String> attributes);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Map<String, VolumeDto> getVolumes();

  void setVolumes(Map<String, VolumeDto> volumes);

  MachineConfigDto withVolumes(Map<String, VolumeDto> volumes);
}
