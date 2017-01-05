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
package org.eclipse.che.ide.api.project.type.wizard;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Picks-up all bounded {@link ProjectWizardRegistrar}s to be able to return it for the particular project type ID.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProjectWizardRegistry {
    /**
     * Get a {@link ProjectWizardRegistrar} for the specified project type or {@code null} if none.
     *
     * @param projectTypeId
     *         the ID of the project type to get an appropriate {@link ProjectWizardRegistrar}
     * @return {@link ProjectWizardRegistrar} for the specified project type ID or {@code null} if none
     */
    @Nullable
    ProjectWizardRegistrar getWizardRegistrar(@NotNull String projectTypeId);

    /**
     * Returns wizard category of the specified {@code projectTypeId} or {@code null} if none.
     *
     * @param projectTypeId
     *         the ID of the project type to get it's wizard category
     * @return wizard category of the specified {@code projectTypeId} or {@code null}
     */
    @Nullable
    String getWizardCategory(@NotNull String projectTypeId);
}
