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
package org.eclipse.che.plugin.java.plain.client.wizard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.plugin.java.plain.client.wizard.selector.SelectNodePresenter;
import org.eclipse.che.plugin.java.plain.client.wizard.selector.SelectionDelegate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.LIBRARY_FOLDER;

/**
 * Presenter of the wizard page which configures Plain Java project.
 *
 * @author Valeriy Svydenko
 */
@Singleton
class PlainJavaPagePresenter extends AbstractWizardPage<ProjectConfigDto> implements PlainJavaPageView.ActionDelegate,
                                                                                     SelectionDelegate {

    private final PlainJavaPageView   view;
    private final SelectNodePresenter selectNodePresenter;

    private boolean isSourceSelected;

    @Inject
    public PlainJavaPagePresenter(PlainJavaPageView view, SelectNodePresenter selectNodePresenter) {
        super();
        this.view = view;
        this.selectNodePresenter = selectNodePresenter;
        view.setDelegate(this);
    }

    @Override
    public void init(ProjectConfigDto dataObject) {
        super.init(dataObject);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        if (CREATE == wizardMode) {
            setAttribute(SOURCE_FOLDER, DEFAULT_SOURCE_FOLDER_VALUE);
        }
    }

    @Override
    public boolean isCompleted() {
        return isCoordinatesCompleted();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        final String projectName = dataObject.getName();

        if (CREATE == wizardMode && projectName != null) {
            updateDelegate.updateControls();
        }

        updateView();
        validateCoordinates();
    }

    @Override
    public void onCoordinatesChanged() {
        setAttribute(SOURCE_FOLDER, view.getSourceFolder());
        setAttribute(LIBRARY_FOLDER, view.getLibraryFolder());

        validateCoordinates();
        updateDelegate.updateControls();
    }

    @Override
    public void onBrowseSourceButtonClicked() {
        isSourceSelected = true;
        selectNodePresenter.show(this, dataObject.getName());
    }

    @Override
    public void onBrowseLibraryButtonClicked() {
        isSourceSelected = false;
        selectNodePresenter.show(this, dataObject.getName());
    }

    @Override
    public void onNodeSelected(String path) {
        int projectNameLength = dataObject.getName().length();
        if (isSourceSelected) {
            view.setSourceFolder(path.substring(projectNameLength + 1));
        } else {
            view.setLibraryFolder(path.substring(projectNameLength + 1));
        }

        onCoordinatesChanged();
    }

    private String getAttribute(String attrId) {
        Map<String, List<String>> attributes = dataObject.getAttributes();
        List<String> values = attributes.get(attrId);
        if (values == null || values.isEmpty()) {
            return "";
        }
        return firstNonNull(values.get(0), "");
    }

    private void setAttribute(String attrId, String value) {
        Map<String, List<String>> attributes = dataObject.getAttributes();
        attributes.put(attrId, Collections.singletonList(value));
    }

    private boolean isCoordinatesCompleted() {
        return !getAttribute(SOURCE_FOLDER).isEmpty();
    }

    private void updateView() {
        ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        boolean isCreateWizard = wizardMode == CREATE;

        view.changeBrowseBtnVisibleState(!isCreateWizard);
        view.changeSourceFolderFieldState(isCreateWizard);
        view.changeLibraryPanelVisibleState(!isCreateWizard);

        Map<String, List<String>> attributes = dataObject.getAttributes();

        view.setSourceFolder(attributes.get(SOURCE_FOLDER) == null ? "" : getAttribute(SOURCE_FOLDER));
        view.setLibraryFolder(getAttribute(LIBRARY_FOLDER));
    }

    private void validateCoordinates() {
        view.showSourceFolderMissingIndicator(view.getSourceFolder().isEmpty());
    }

}
