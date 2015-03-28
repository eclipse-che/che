/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.tutorials.client.wizard;

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.collections.Collections;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.annotation.Nonnull;

import static org.eclipse.che.ide.Constants.CODENVY_CATEGORY;
import static org.eclipse.che.ide.ext.tutorials.shared.Constants.TUTORIAL_ID;

/**
 * Provides information for registering AngularJS project type into project wizard.
 *
 * @author Artem Zatsarynnyy
 */
public class TutorialProjectWizardRegistrar implements ProjectWizardRegistrar {
    private final Array<Provider<? extends WizardPage<ImportProject>>> wizardPages;

    @Inject
    public TutorialProjectWizardRegistrar(Provider<ExtensionPagePresenter> provider) {
        wizardPages = Collections.createArray();
        wizardPages.add(provider);
    }

    @Nonnull
    public String getProjectTypeId() {
        return TUTORIAL_ID;
    }

    @Nonnull
    public String getCategory() {
        return CODENVY_CATEGORY;
    }

    @Nonnull
    public Array<Provider<? extends WizardPage<ImportProject>>> getWizardPages() {
        return wizardPages;
    }
}
