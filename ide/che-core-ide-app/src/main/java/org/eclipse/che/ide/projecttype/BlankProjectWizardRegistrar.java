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
package org.eclipse.che.ide.projecttype;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import com.google.inject.Provider;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides information for registering Blank project type into project wizard.
 *
 * @author Artem Zatsarynnyi
 */
public class BlankProjectWizardRegistrar implements ProjectWizardRegistrar {

    public static final String BLANK_CATEGORY = "Blank";
    public static final String BLANK_ID       = "blank";

    private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

    public BlankProjectWizardRegistrar() {
        wizardPages = new ArrayList<>();
    }

    @NotNull
    public String getProjectTypeId() {
        return BLANK_ID;
    }

    @NotNull
    public String getCategory() {
        return BLANK_CATEGORY;
    }

    @NotNull
    public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
        return wizardPages;
    }
}
