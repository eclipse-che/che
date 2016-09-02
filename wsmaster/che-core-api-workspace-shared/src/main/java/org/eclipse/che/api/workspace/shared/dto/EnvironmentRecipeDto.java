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
import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;
import org.eclipse.che.dto.shared.DTO;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface EnvironmentRecipeDto extends EnvironmentRecipe {

    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getType();

    void setType(String type);

    EnvironmentRecipeDto withType(String type);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getContentType();

    void setContentType(String contentType);

    EnvironmentRecipeDto withContentType(String contentType);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getContent();

    void setContent(String content);

    EnvironmentRecipeDto withContent(String content);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getLocation();

    void setLocation(String location);

    EnvironmentRecipeDto withLocation(String location);
}
