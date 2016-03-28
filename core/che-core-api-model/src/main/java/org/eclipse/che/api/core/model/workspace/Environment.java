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
package org.eclipse.che.api.core.model.workspace;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * Defines environment for machines network.
 *
 * @author gazarenkov
 */
public interface Environment {

    /**
     * Returns environment display name. It is mandatory and unique per workspace
     */
    String getName();

    /**
     * Returns the recipe (the main script) to define this environment (compose, kubernetes pod).
     * Type of this recipe defines engine for composing machines network runtime
     */
    @Nullable
    Recipe getRecipe();

    /**
     * Returns list of Machine configs defined by this environment
     * Note: it may happen that we are not able to provide this info for particular environment type
     * or for particular time (for example this information may be reasonable accessible only when we start network or so)
     * to investigate
     */
    List<? extends MachineConfig> getMachineConfigs();
}
