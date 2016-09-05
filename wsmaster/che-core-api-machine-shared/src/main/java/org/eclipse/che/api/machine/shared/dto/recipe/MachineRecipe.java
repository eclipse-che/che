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
package org.eclipse.che.api.machine.shared.dto.recipe;

import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes machine recipe.
 *
 * @author Valeriy Svydenko
 */
@Deprecated
@DTO
public interface MachineRecipe extends Recipe {

    void setType(String type);

    MachineRecipe withType(String type);

    void setScript(String script);

    MachineRecipe withScript(String script);
}
