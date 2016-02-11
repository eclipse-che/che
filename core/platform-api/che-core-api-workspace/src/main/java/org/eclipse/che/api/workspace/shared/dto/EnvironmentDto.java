/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface EnvironmentDto extends Environment {

    EnvironmentDto withName(String name);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    RecipeDto getRecipe();

    EnvironmentDto withRecipe(RecipeDto recipe);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    List<MachineConfigDto> getMachineConfigs();

    EnvironmentDto withMachineConfigs(List<MachineConfigDto> machineConfigs);
}
