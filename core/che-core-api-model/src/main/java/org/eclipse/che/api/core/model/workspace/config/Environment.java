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
package org.eclipse.che.api.core.model.workspace.config;

import org.eclipse.che.api.core.model.workspace.Warning;

import java.util.List;
import java.util.Map;

/**
 * Defines environment for machines network.
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 */
public interface Environment {
    /**
     * Returns the recipe (the main script) to define this environment (compose, kubernetes pod).
     * Type of this recipe defines engine for composing machines network runtime.
     */
    Recipe getRecipe();

    /**
     * Returns mapping of machine name to additional configuration of machine.
     */
    Map<String, ? extends MachineConfig> getMachines();

    /**
     * Returns the list of the warnings, indicating that the environment
     * violates some non-critical constraints or some preferable configuration is missing
     * so defaults are used.
     */
    List<? extends Warning> getWarnings();
}
