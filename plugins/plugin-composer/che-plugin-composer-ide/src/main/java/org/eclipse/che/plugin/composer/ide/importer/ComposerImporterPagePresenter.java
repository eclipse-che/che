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
package org.eclipse.che.plugin.composer.ide.importer;

import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.util.NameUtils;
import org.eclipse.che.plugin.composer.ide.ComposerLocalizationConstants;

import javax.validation.constraints.NotNull;

/**
 * @author Kaloyan Raev
 */
public class ComposerImporterPagePresenter extends AbstractWizardPage<MutableProjectConfig> implements ComposerImporterPageView.ActionDelegate {

    // the package name
    private static final RegExp PACKAGE_NAME = RegExp.compile("^[\\w.\\-]+\\/[\\w.\\-]+$");
    // start with white space
    private static final RegExp WHITE_SPACE  = RegExp.compile("^\\s");

    private ComposerLocalizationConstants locale;
    private ComposerImporterPageView      view;

    private boolean                       ignoreChanges;

    @Inject
    public ComposerImporterPagePresenter(ComposerImporterPageView view, ComposerLocalizationConstants locale) {
        this.view = view;
        this.view.setDelegate(this);
        this.locale = locale;
    }

    @Override
    public boolean isCompleted() {
        return isPackageNameCorrect(dataObject.getSource().getLocation());
    }

    @Override
    public void projectNameChanged(@NotNull String name) {
        if (ignoreChanges) {
            return;
        }

        dataObject.setName(name);
        updateDelegate.updateControls();

        validateProjectName();
    }

    private void validateProjectName() {
        if (NameUtils.checkProjectName(view.getProjectName())) {
            view.markNameValid();
        } else {
            view.markNameInvalid();
        }
    }

    @Override
    public void packageNameChanged(@NotNull String name) {
        if (ignoreChanges) {
            return;
        }

        dataObject.getSource().setLocation(name);
        isPackageNameCorrect(name);

        String projectName = extractProjectNameFromPackageName(name);
    
        dataObject.setName(projectName);
        view.setProjectName(projectName);
        validateProjectName();

        updateDelegate.updateControls();
    }

    @Override
    public void projectDescriptionChanged(@NotNull String projectDescription) {
        dataObject.setDescription(projectDescription);
        updateDelegate.updateControls();
    }

    @Override
    public void go(@NotNull AcceptsOneWidget container) {
        container.setWidget(view);

        if (Strings.isNullOrEmpty(dataObject.getName())
                && Strings.isNullOrEmpty(dataObject.getSource().getLocation())) {
            ignoreChanges = true;

            view.unmarkPackageName();
            view.unmarkName();
            view.setPackageNameErrorMessage(null);
        }

        view.setProjectName(dataObject.getName());
        view.setProjectDescription(dataObject.getDescription());
        view.setPackageName(dataObject.getSource().getLocation());

        view.setInputsEnableState(true);
        view.focusInPackageNameInput();

        ignoreChanges = false;
    }

    /** Gets project name from package name. */
    private String extractProjectNameFromPackageName(@NotNull String packageName) {
        int indexStartProjectName = packageName.lastIndexOf("/") + 1;

        if (indexStartProjectName != 0) {
            return packageName.substring(indexStartProjectName);
        }

        return packageName;
    }

    /**
     * Validate package name
     *
     * @param name
     *            package name for validate
     * @return <code>true</code> if package name is correct
     */
    private boolean isPackageNameCorrect(@NotNull String name) {
        if (WHITE_SPACE.test(name)) {
            view.markPackageNameInvalid();
            view.setPackageNameErrorMessage(locale.projectImporterPackageNameStartWithWhiteSpace());
            return false;
        }

        if (!(PACKAGE_NAME.test(name))) {
            view.markPackageNameInvalid();
            view.setPackageNameErrorMessage(locale.projectImporterPackageNameInvalid());
            return false;
        }

        view.markPackageNameValid();
        view.setPackageNameErrorMessage(null);
        return true;
    }
}
