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
package org.eclipse.che.plugin.svn.ide.importer;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.plugin.svn.shared.ImportParameterKeys;
import org.eclipse.che.ide.util.NameUtils;

/**
 * Handler for the Subversion Project Importer.
 *
 * @author vzhukovskii@codenvy.com
 */
public class SubversionProjectImporterPresenter extends AbstractWizardPage<ProjectConfigDto>
        implements SubversionProjectImporterView.ActionDelegate {

    private SubversionProjectImporterView view;

    @Inject
    public SubversionProjectImporterPresenter(SubversionProjectImporterView view) {
        this.view = view;
        this.view.setDelegate(this);
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {

        view.setProjectName(dataObject.getName());
        view.setProjectDescription(dataObject.getDescription());
        view.setProjectUrl(dataObject.getSource().getLocation());

        container.setWidget(view);

        view.setUrlTextBoxFocused();
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectNameChanged() {
        dataObject.setName(view.getProjectName());
        updateDelegate.updateControls();

        view.setNameErrorVisibility(!NameUtils.checkProjectName(view.getProjectName()));
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectUrlChanged() {
        if (Strings.isNullOrEmpty(view.getProjectUrl())) {
            view.setProjectName("");
            return;
        }

        String projectName = Iterables.getLast(Splitter.on("/").omitEmptyStrings().split(view.getProjectUrl()));
        String calcUrl = getUrl(view.getProjectUrl(), view.getProjectRelativePath());

        view.setProjectName(projectName);
        dataObject.getSource().setLocation(calcUrl);
        updateDelegate.updateControls();
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectRelativePathChanged() {
        String calcUrl = getUrl(view.getProjectUrl(), view.getProjectRelativePath());
        dataObject.getSource().setLocation(calcUrl);
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectDescriptionChanged() {
        dataObject.setDescription(view.getProjectDescription());
        updateDelegate.updateControls();
    }

    /** {@inheritDoc} */
    @Override
    public void onCredentialsChanged() {
        dataObject.getSource().getParameters().put(ImportParameterKeys.PARAMETER_USERNAME, view.getUserName());
        dataObject.getSource().getParameters().put(ImportParameterKeys.PARAMETER_PASSWORD, view.getPassword());
    }

    private String getUrl(String url, String relPath) {
        return (url.endsWith("/") ? url.substring(0, url.length() - 1) : url) + (relPath.startsWith("/") ? relPath : relPath.concat("/"));
    }
}
