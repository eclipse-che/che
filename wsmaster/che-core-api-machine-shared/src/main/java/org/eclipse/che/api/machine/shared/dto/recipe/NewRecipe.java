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
package org.eclipse.che.api.machine.shared.dto.recipe;

import java.util.List;
import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes new recipe
 *
 * @author Eugene Voevodin
 */
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
