/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.ide.project;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.php.shared.Constants.PHP_CATEGORY;

import javax.validation.constraints.NotNull;

/**
 * Provides information for registering Composer project type into project wizard.
 *
 * @author Kaloyan Raev
 */
public class ComposerProjectWizardRegistrar implements ProjectWizardRegistrar {

    private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

    @Inject
    public ComposerProjectWizardRegistrar(Provider<ComposerPagePresenter> pagePresenter) {
        wizardPages = new ArrayList<>();
        wizardPages.add(pagePresenter);
    }

    @Override
    @NotNull
    public String getProjectTypeId() {
        return COMPOSER_PROJECT_TYPE_ID;
    }

    @Override
    @NotNull
    public String getCategory() {
        return PHP_CATEGORY;
    }

    @Override
    @NotNull
    public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
        return wizardPages;
    }
}
