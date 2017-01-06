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
package org.eclipse.che.ide.projectimport.zip;

import org.eclipse.che.api.project.shared.Constants;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides information for registering ZIP importer into import wizard.
 *
 * @author Artem Zatsarynnyi
 */
public class ZipImportWizardRegistrar implements ImportWizardRegistrar {
    private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

    @Inject
    public ZipImportWizardRegistrar(Provider<ZipImporterPagePresenter> provider) {
        wizardPages = new ArrayList<>();
        wizardPages.add(provider);
    }

    @NotNull
    public String getImporterId() {
        return Constants.ZIP_IMPORTER_ID;
    }

    @NotNull
    public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
        return wizardPages;
    }
}
