/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
