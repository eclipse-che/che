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
package org.eclipse.che.api.machine.shared.dto.recipe;

import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Describes recipe update.
 *
 * @author Eugene Voevodin
 */
@DTO
public interface RecipeUpdate extends ManagedRecipe {

    void setId(String id);

    RecipeUpdate withId(String id);

    void setName(String name);

    RecipeUpdate withName(String name);

    void setType(String type);

    RecipeUpdate withType(String type);

    void setScript(String script);

    RecipeUpdate withScript(String script);

    void setTags(List<String> tags);

    RecipeUpdate withTags(List<String> tags);

    void setDescription(String description);

    RecipeUpdate withDescription(String description);
}
