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

import org.eclipse.che.dto.shared.DTO;

/**
 * Describes machine recipe.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface MachineOldRecipe extends OldRecipe {

  void setType(String type);

  MachineOldRecipe withType(String type);

  void setScript(String script);

  MachineOldRecipe withScript(String script);
}
