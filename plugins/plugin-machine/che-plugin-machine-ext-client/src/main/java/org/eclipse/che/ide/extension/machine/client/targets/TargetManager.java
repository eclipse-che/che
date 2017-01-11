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
package org.eclipse.che.ide.extension.machine.client.targets;

import javax.validation.constraints.NotNull;

/**
 * Target manager for {@link Target} instances.
 *
 * @author Oleksii Orel
 */
public interface TargetManager {

    /**
     * Creates a new target.
     *
     * @param name
     *          name of the target
     *
     * @return default target
     */
    Target createTarget(@NotNull String name);

    /**
     * Creates a default target.
     *
     * @return default target
     */
    Target createDefaultTarget();

    /**
     * Delete the target.
     *
     * @param target
     */
    void onDeleteClicked(Target target);

    /**
     * Restore target if it possible.
     *
     * @param target
     */
    void restoreTarget(Target target);
}
