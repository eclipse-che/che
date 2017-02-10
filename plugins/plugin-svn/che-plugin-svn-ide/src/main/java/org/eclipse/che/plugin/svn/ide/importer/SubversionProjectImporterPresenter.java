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
package org.eclipse.che.plugin.svn.ide.importer;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.ide.util.NameUtils;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Handler for the Subversion Project Importer.
 *
 * @author vzhukovskii@codenvy.com
 */
public class SubversionProjectImporterPresenter extends AbstractWizardPage<MutableProjectConfig>
        implements SubversionProjectImporterView.ActionDelegate {

    // start with white space
    private static final RegExp SUBVERSION_REPOSITORY_REGEX =
            RegExp.compile(
                    "^(http|https|svn|svn\\+ssh)://[A-Za-z0-9_\\-]+@?[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-:]+)+(/[A-Za-z0-9_.\\-]+)*/?$");

    private final SubversionExtensionLocalizationConstants constants;
    private final SubversionProjectImporterView            view;

    private boolean ignoreChanges;

    @Inject
    public SubversionProjectImporterPresenter(SubversionExtensionLocalizationConstants constants,
                                              SubversionProjectImporterView view) {
        this.constants = constants;
        this.view = view;
        this.view.setDelegate(this);
    }

    @Override
    public boolean isCompleted() {
        return isSubversionUrlCorrect(dataObject.getSource().getLocation())
               && NameUtils.checkProjectName(dataObject.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {

        if (Strings.isNullOrEmpty(dataObject.getName()) && Strings.isNullOrEmpty(dataObject.getSource().getLocation())) {
            ignoreChanges = true;

            view.setProjectUrlErrorHighlight(false);
            view.setProjectNameErrorHighlight(false);
            view.setURLErrorMessage(null);
        }

        view.setProjectName(dataObject.getName());
        view.setProjectDescription(dataObject.getDescription());
        view.setProjectUrl(dataObject.getSource().getLocation());

        container.setWidget(view);

        view.setInputsEnableState(true);

        ignoreChanges = false;
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectNameChanged(final String projectName) {
        if (ignoreChanges) {
            return;
        }

        dataObject.setName(projectName);
        updateDelegate.updateControls();

        view.setProjectNameErrorHighlight(!NameUtils.checkProjectName(projectName));
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectUrlChanged(final String projectUrl) {
        if (ignoreChanges) {
            return;
        }

        final String calcUrl = getUrl(projectUrl, view.getProjectRelativePath());
        dataObject.getSource().setLocation(calcUrl);

        if (isSubversionUrlCorrect(calcUrl)) {
            view.setProjectUrlErrorHighlight(false);
            view.setURLErrorMessage(null);


            if (isNullOrEmpty(view.getProjectName())) {
                final String projectName = Iterables.getLast(Splitter.on("/").omitEmptyStrings().split(projectUrl));
                if (!isNullOrEmpty(projectName)) {
                    view.setProjectName(projectName);
                    dataObject.setName(view.getProjectName());
                }
            }

        } else {
            view.setProjectUrlErrorHighlight(true);
            view.setURLErrorMessage(constants.importProjectUrlIncorrectMessage());
        }

        updateDelegate.updateControls();
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectRelativePathChanged(final String projectRelativePath) {
        String calcUrl = getUrl(view.getProjectUrl(), projectRelativePath);
        dataObject.getSource().setLocation(calcUrl);
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectDescriptionChanged(final String projectDescription) {
        dataObject.setDescription(projectDescription);
    }

    private boolean isSubversionUrlCorrect(final String url) {
        return !isNullOrEmpty(url) && SUBVERSION_REPOSITORY_REGEX.test(url);
    }

    private String getUrl(String url, String relPath) {
        return (url.endsWith("/") ? url.substring(0, url.length() - 1) : url) + (relPath.startsWith("/") ? relPath : relPath.concat("/"));
    }
}
