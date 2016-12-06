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
package org.eclipse.che.ide.projectimport.zip;


import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.util.NameUtils;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Nikitenko
 */
public class ZipImporterPagePresenter extends AbstractWizardPage<MutableProjectConfig> implements ZipImporterPageView.ActionDelegate {

    private static final String SKIP_FIRST_LEVEL_PARAM_NAME = "skipFirstLevel";

    private static final RegExp URL_REGEX  = RegExp.compile("(https?|ftp)://(-\\.)?([^\\s/?\\.#-]+\\.?)+(/[^\\s]*)?");
    private static final RegExp WHITESPACE = RegExp.compile("^\\s");
    private static final RegExp END_URL    = RegExp.compile(".zip$");

    private CoreLocalizationConstant locale;
    private ZipImporterPageView      view;

    @Inject
    public ZipImporterPagePresenter(ZipImporterPageView view, CoreLocalizationConstant locale) {
        this.view = view;
        this.locale = locale;
        this.view.setDelegate(this);
    }

    @Override
    public void init(MutableProjectConfig dataObject) {
        super.init(dataObject);

        setImportParameterValue(SKIP_FIRST_LEVEL_PARAM_NAME, String.valueOf(false));
    }

    @Override
    public boolean isCompleted() {
        return isUrlCorrect(dataObject.getSource().getLocation());
    }

    @Override
    public void projectNameChanged(@NotNull String name) {
        dataObject.setName(name);
        updateDelegate.updateControls();

        validateProjectName();
    }

    private void validateProjectName() {
        if (NameUtils.checkProjectName(view.getProjectName())) {
            view.hideNameError();
        } else {
            view.showNameError();
        }
    }

    @Override
    public void projectUrlChanged(@NotNull String url) {
        dataObject.getSource().setLocation(url);
        isUrlCorrect(url);

        String projectName = view.getProjectName();
        if (projectName.isEmpty()) {
            projectName = extractProjectNameFromUri(url);

            dataObject.setName(projectName);
            view.setProjectName(projectName);
            validateProjectName();
        }

        updateDelegate.updateControls();
    }

    @Override
    public void projectDescriptionChanged(@NotNull String projectDescription) {
        dataObject.setDescription(projectDescription);
        updateDelegate.updateControls();
    }

    @Override
    public void skipFirstLevelChanged(boolean isSkipFirstLevel) {
        setImportParameterValue(SKIP_FIRST_LEVEL_PARAM_NAME, String.valueOf(isSkipFirstLevel));
        updateDelegate.updateControls();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
        updateView();

        view.setInputsEnableState(true);
        view.focusInUrlInput();
    }

    /** Updates view from data-object. */
    private void updateView() {
        view.setProjectName(dataObject.getName());
        view.setProjectDescription(dataObject.getDescription());
        view.setProjectUrl(dataObject.getSource().getLocation());

        final String value = getImportParameterValue(SKIP_FIRST_LEVEL_PARAM_NAME);
        if (value != null) {
            view.setSkipFirstLevel(Boolean.valueOf(value));
        }
    }

    @Nullable
    private String getImportParameterValue(String name) {
        Map<String, String> parameters = dataObject.getSource().getParameters();
        return parameters.get(name);
    }

    private void setImportParameterValue(String name, String value) {
        Map<String, String> parameters = dataObject.getSource().getParameters();
        parameters.put(name, value);
    }

    private String extractProjectNameFromUri(@NotNull String uri) {
        final String result;
        int indexStartProjectName = uri.lastIndexOf("/") + 1;
        int indexFinishProjectName = uri.indexOf(".", indexStartProjectName);
        if (indexStartProjectName != 0 && indexFinishProjectName != (-1)) {
            result = uri.substring(indexStartProjectName, indexFinishProjectName);
        } else if (indexStartProjectName != 0) {
            result = uri.substring(indexStartProjectName);
        } else {
            result = "";
        }
        return result;
    }

    /**
     * Validate url
     *
     * @param url
     *         url for validate
     * @return <code>true</code> if url is correct
     */
    private boolean isUrlCorrect(@NotNull String url) {
        if(isNullOrEmpty(url)) {
            view.showUrlError("");
            return false;
        }

        if (!END_URL.test(url)) {
            view.showUrlError(locale.importProjectMessageUrlInvalid());
            return false;
        }

        if (WHITESPACE.test(url)) {
            view.showUrlError(locale.importProjectMessageStartWithWhiteSpace());
            return false;
        }

        if (!URL_REGEX.test(url)) {
            view.showUrlError(locale.importProjectMessageUrlInvalid());
            return false;
        }

        view.hideUrlError();
        return true;
    }
}
