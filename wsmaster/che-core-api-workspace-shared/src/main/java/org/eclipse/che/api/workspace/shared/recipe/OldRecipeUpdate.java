/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.recipe;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes recipe update.
 *
 * @author Eugene Voevodin
 */
@DTO
public interface OldRecipeUpdate extends ManagedOldRecipe {

  void setId(String id);

  OldRecipeUpdate withId(String id);

  void setName(String name);

  OldRecipeUpdate withName(String name);

  void setType(String type);

  OldRecipeUpdate withType(String type);

  void setScript(String script);

  OldRecipeUpdate withScript(String script);

  void setTags(List<String> tags);

  OldRecipeUpdate withTags(List<String> tags);

  void setDescription(String description);

  OldRecipeUpdate withDescription(String description);
}
