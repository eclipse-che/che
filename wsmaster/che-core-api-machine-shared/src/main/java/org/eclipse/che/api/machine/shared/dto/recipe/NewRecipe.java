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

import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Describes new recipe
 *
 * @author Eugene Voevodin
 */
@Deprecated
@DTO
public interface NewRecipe extends ManagedRecipe {

    void setType(String type);

    NewRecipe withType(String type);

    void setName(String name);

    NewRecipe withName(String name);

    void setScript(String script);

    NewRecipe withScript(String script);

    void setTags(List<String> tags);

    NewRecipe withTags(List<String> tags);

    void setDescription(String description);

    NewRecipe withDescription(String description);
}
