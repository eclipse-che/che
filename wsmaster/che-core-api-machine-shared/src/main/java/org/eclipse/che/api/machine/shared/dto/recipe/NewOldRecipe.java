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

import org.eclipse.che.api.machine.shared.ManagedOldRecipe;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Describes new recipe
 *
 * @author Eugene Voevodin
 */
@DTO
public interface NewOldRecipe extends ManagedOldRecipe {

    void setType(String type);

    NewOldRecipe withType(String type);

    void setName(String name);

    NewOldRecipe withName(String name);

    void setScript(String script);

    NewOldRecipe withScript(String script);

    void setTags(List<String> tags);

    NewOldRecipe withTags(List<String> tags);

    void setDescription(String description);

    NewOldRecipe withDescription(String description);
}
