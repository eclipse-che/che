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
package org.eclipse.che.ide.api.project.wizard;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Picks-up all bounded {@link ImportWizardRegistrar}s to be able to return it for the particular project importer ID.
 *
 * @author Artem Zatsarynnyi
 */
public interface ImportWizardRegistry {
    /**
     * Get an {@link ImportWizardRegistrar} for the specified project importer or {@code null} if none.
     *
     * @param importerId
     *         the ID of the project importer to get an appropriate {@link ImportWizardRegistrar}
     * @return {@link ImportWizardRegistrar} for the specified project importer ID or {@code null} if none
     */
    @Nullable
    ImportWizardRegistrar getWizardRegistrar(@NotNull String importerId);
}
