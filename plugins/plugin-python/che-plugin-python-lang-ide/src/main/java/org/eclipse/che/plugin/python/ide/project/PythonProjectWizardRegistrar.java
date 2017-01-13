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
package org.eclipse.che.plugin.python.ide.project;

import com.google.inject.Provider;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.plugin.python.shared.ProjectAttributes.PYTHON_CATEGORY;
import static org.eclipse.che.plugin.python.shared.ProjectAttributes.PYTHON_ID;

/**
 * Provides information for registering Python project type into project wizard.
 *
 * @author Valeriy Svydenko
 */
public class PythonProjectWizardRegistrar implements ProjectWizardRegistrar {
    private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

    public PythonProjectWizardRegistrar() {
        wizardPages = new ArrayList<>();
    }

    @NotNull
    public String getProjectTypeId() {
        return PYTHON_ID;
    }

    @NotNull
    public String getCategory() {
        return PYTHON_CATEGORY;
    }

    @NotNull
    public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
        return wizardPages;
    }

}
