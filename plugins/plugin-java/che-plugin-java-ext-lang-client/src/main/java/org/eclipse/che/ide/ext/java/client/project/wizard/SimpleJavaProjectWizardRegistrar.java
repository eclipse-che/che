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
package org.eclipse.che.ide.ext.java.client.project.wizard;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVA_CATEGORY;
import static org.eclipse.che.ide.ext.java.shared.Constants.SIMPLE_JAVA_PROJECT_ID;

/**
 * Provides information for registering Simple Java project type into project wizard.
 *
 * @author Valeriy Svydenko
 */
public class SimpleJavaProjectWizardRegistrar implements ProjectWizardRegistrar {
    private final List<Provider<? extends WizardPage<ProjectConfigDto>>> wizardPages;

    @Inject
    public SimpleJavaProjectWizardRegistrar() {
        wizardPages = new ArrayList<>();
    }

    @NotNull
    public String getProjectTypeId() {
        return SIMPLE_JAVA_PROJECT_ID;
    }

    @NotNull
    public String getCategory() {
        return JAVA_CATEGORY;
    }

    @NotNull
    public List<Provider<? extends WizardPage<ProjectConfigDto>>> getWizardPages() {
        return wizardPages;
    }
}
