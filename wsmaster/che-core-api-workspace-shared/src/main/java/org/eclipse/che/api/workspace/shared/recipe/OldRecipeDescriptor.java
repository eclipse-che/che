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
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes recipe
 *
 * @author Eugene Voevodin
 */
@DTO
public interface OldRecipeDescriptor extends Hyperlinks, ManagedOldRecipe {

  void setId(String id);

  OldRecipeDescriptor withId(String id);

  void setName(String name);

  OldRecipeDescriptor withName(String name);

  void setType(String type);

  OldRecipeDescriptor withType(String type);

  void setScript(String script);

  OldRecipeDescriptor withScript(String script);

  void setCreator(String creator);

  OldRecipeDescriptor withCreator(String creator);

  void setTags(List<String> tags);

  OldRecipeDescriptor withTags(List<String> tags);

  void setDescription(String description);

  OldRecipeDescriptor withDescription(String description);
}
