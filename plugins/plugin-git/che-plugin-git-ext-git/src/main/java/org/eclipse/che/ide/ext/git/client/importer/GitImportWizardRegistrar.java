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
package org.eclipse.che.ide.ext.git.client.importer;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import org.eclipse.che.ide.ext.git.client.importer.page.GitImporterPagePresenter;

import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides information for registering GIT importer into import wizard.
 *
 * @author Artem Zatsarynnyi
 */
public class GitImportWizardRegistrar implements ImportWizardRegistrar {
    private final static String ID = "git";
    private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

    @Inject
    public GitImportWizardRegistrar(Provider<GitImporterPagePresenter> provider) {
        wizardPages = new ArrayList<>();
        wizardPages.add(provider);
    }

    @NotNull
    public String getImporterId() {
        return ID;
    }

    @NotNull
    public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
        return wizardPages;
    }
}
