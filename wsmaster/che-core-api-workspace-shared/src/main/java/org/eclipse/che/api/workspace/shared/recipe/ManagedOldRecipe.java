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

/**
 * Serves as base model for OldRecipe API.
 *
 * @author Eugene Voevodin
 */
public interface ManagedOldRecipe extends OldRecipe {

  /** Returns recipe identifier. */
  String getId();

  /** Returns recipe name. */
  String getName();

  /** Returns identifier of user who is the recipe creator. */
  String getCreator();

  /** Returns recipe tags (i.e. 'java'). Tags used for recipes search. */
  List<String> getTags();

  /** Returns recipe description. */
  String getDescription();
}
