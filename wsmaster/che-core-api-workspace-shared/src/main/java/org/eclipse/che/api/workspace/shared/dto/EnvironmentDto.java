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
import org.eclipse.che.dto.shared.DelegateRule;
import org.eclipse.che.dto.shared.DelegateTo;

import java.util.List;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface EnvironmentDto extends Environment {

    EnvironmentDto withName(String name);

    void setName(String name);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    RecipeDto getRecipe();

    void setRecipe(RecipeDto recipe);

    EnvironmentDto withRecipe(RecipeDto recipe);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    List<MachineConfigDto> getMachineConfigs();

    @DelegateTo(client = @DelegateRule(type = DevMachineResolver.class, method = "getDevMachine"),
                server = @DelegateRule(type = DevMachineResolver.class, method = "getDevMachine"))
    MachineConfigDto devMachine();

    EnvironmentDto withMachineConfigs(List<MachineConfigDto> machineConfigs);

    void setMachineConfigs(List<MachineConfigDto> machineConfigs);

    class DevMachineResolver {
        public static MachineConfigDto getDevMachine(EnvironmentDto environmentDto) {
            for (MachineConfigDto machineConfigDto : environmentDto.getMachineConfigs()) {
                if (machineConfigDto.isDev()) {
                    return machineConfigDto;
                }
            }
            return null;
        }
    }
}
