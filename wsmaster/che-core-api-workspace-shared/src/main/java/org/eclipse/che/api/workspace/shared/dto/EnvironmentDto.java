/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;

/**
 * @author Alexander Garagatyi
 */
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
