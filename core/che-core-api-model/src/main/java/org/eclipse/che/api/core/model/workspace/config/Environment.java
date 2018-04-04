/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.model.workspace.config;

import java.util.Map;

/**
 * Defines environment for machines network.
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 */
public interface Environment {
  /**
   * Returns the recipe (the main script) to define this environment (compose, kubernetes pod). Type
   * of this recipe defines engine for composing machines network runtime.
   */
  Recipe getRecipe();

  /** Returns mapping of machine name to additional configuration of machine. */
  Map<String, ? extends MachineConfig> getMachines();
}
