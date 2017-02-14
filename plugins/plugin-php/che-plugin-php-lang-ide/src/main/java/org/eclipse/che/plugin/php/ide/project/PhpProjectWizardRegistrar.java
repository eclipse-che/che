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
package org.eclipse.che.plugin.php.ide.project;

import com.google.inject.Provider;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import org.eclipse.che.plugin.php.shared.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;


/**
 * Provides information for registering PHP_PROJECT_TYPE_ID project type into project wizard.
 *
 * @author Kaloyan Raev
 */
public class PhpProjectWizardRegistrar implements ProjectWizardRegistrar {

    private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

    public PhpProjectWizardRegistrar() {
        wizardPages = new ArrayList<>();
    }

    @NotNull
    public String getProjectTypeId() {
        return Constants.PHP_PROJECT_TYPE_ID;
    }

    @NotNull
    public String getCategory() {
        return Constants.PHP_CATEGORY;
    }

    @NotNull
    public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
        return wizardPages;
    }
}
