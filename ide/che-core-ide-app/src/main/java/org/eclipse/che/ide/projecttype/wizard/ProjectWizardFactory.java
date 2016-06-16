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
package org.eclipse.che.ide.projecttype.wizard;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;

import javax.validation.constraints.NotNull;

/**
 * Helps to create new instances of {@link ProjectWizard}.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProjectWizardFactory {
    ProjectWizard newWizard(@NotNull ProjectConfigDto dataObject,
                            @NotNull ProjectWizardMode mode,
                            @NotNull String projectPath);
}
