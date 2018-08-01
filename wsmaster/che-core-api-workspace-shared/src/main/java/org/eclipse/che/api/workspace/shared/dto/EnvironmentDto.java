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

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;

import java.util.Map;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Garagatyi */
@DTO
public interface EnvironmentDto extends Environment {

  @Override
  @FactoryParameter(obligation = MANDATORY)
  RecipeDto getRecipe();

  void setRecipe(RecipeDto recipe);

  EnvironmentDto withRecipe(RecipeDto recipe);

  @Override
  @FactoryParameter(obligation = MANDATORY)
  Map<String, MachineConfigDto> getMachines();

  void setMachines(Map<String, MachineConfigDto> machines);

  EnvironmentDto withMachines(Map<String, MachineConfigDto> machines);
}
