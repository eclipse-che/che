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

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Describes recipe
 *
 * @author Eugene Voevodin
 */
@DTO
public interface RecipeDescriptor extends Hyperlinks, ManagedRecipe {

    void setId(String id);

    RecipeDescriptor withId(String id);

    void setName(String name);

    RecipeDescriptor withName(String name);

    void setType(String type);

    RecipeDescriptor withType(String type);

    void setScript(String script);

    RecipeDescriptor withScript(String script);

    void setCreator(String creator);

    RecipeDescriptor withCreator(String creator);

    void setTags(List<String> tags);

    RecipeDescriptor withTags(List<String> tags);

    void setDescription(String description);

    RecipeDescriptor withDescription(String description);
}
