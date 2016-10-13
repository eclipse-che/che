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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.TextBox;
import org.eclipse.che.plugin.composer.ide.ComposerResources;

import javax.validation.constraints.NotNull;

/**
 * @author Kaloyan Raev
 */
public class ComposerImporterPageViewImpl extends Composite implements ComposerImporterPageView {

    @UiField(provided = true)
    Style       style;

    @UiField
    TextBox     packageName;
    
    @UiField
    Label       labelPackageNameError;

    @UiField
    TextBox     projectName;

    @UiField
    TextArea    projectDescription;

    private ActionDelegate delegate;

    @Inject
    public ComposerImporterPageViewImpl(ComposerResources resources,
            ComposerImporterPageViewImplUiBinder uiBinder) {
        style = resources.composerImporterPageStyle();
        style.ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        projectName.getElement().setAttribute("maxlength", "32");
        projectDescription.getElement().setAttribute("maxlength", "256");
    }

    @UiHandler("projectName")
    void onProjectNameChanged(KeyUpEvent event) {
        String projectNameValue = projectName.getValue();

        if (projectNameValue != null && projectNameValue.contains(" ")) {
            projectNameValue = projectNameValue.replace(" ", "-");
            projectName.setValue(projectNameValue);
        }

        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }

        delegate.projectNameChanged(projectName.getValue());
    }

    @UiHandler("packageName")
    void onPackageNameChanged(KeyUpEvent event) {
        delegate.packageNameChanged(packageName.getValue());
    }

    @UiHandler("projectDescription")
    void onProjectDescriptionChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }
        delegate.projectDescriptionChanged(projectDescription.getValue());
    }

    @Override
    public void setPackageName(@NotNull String name) {
        packageName.setText(name);
        delegate.packageNameChanged(name);
    }

    @Override
    public void markPackageNameValid() {
        packageName.markValid();
    }

    @Override
    public void markPackageNameInvalid() {
        packageName.markInvalid();
    }

    @Override
    public void unmarkPackageName() {
        packageName.unmark();
    }

    @Override
    public void setPackageNameErrorMessage(@NotNull String message) {
        labelPackageNameError.setText(message != null ? message : "");
    }

    @Override
    public void markNameValid() {
        projectName.markValid();
    }

    @Override
    public void markNameInvalid() {
        projectName.markInvalid();
    }

    @Override
    public void unmarkName() {
        projectName.unmark();
    }

    @NotNull
    @Override
    public String getProjectName() {
        return projectName.getValue();
    }

    @Override
    public void setProjectName(@NotNull String projectName) {
        this.projectName.setValue(projectName);
        delegate.projectNameChanged(projectName);
    }

    @Override
    public void focusInPackageNameInput() {
        packageName.setFocus(true);
    }

    @Override
    public void setInputsEnableState(boolean isEnabled) {
        projectName.setEnabled(isEnabled);
        projectDescription.setEnabled(isEnabled);
        packageName.setEnabled(isEnabled);

        if (isEnabled) {
            focusInPackageNameInput();
        }
    }

    @Override
    public void setProjectDescription(@NotNull String projectDescription) {
        this.projectDescription.setValue(projectDescription);
    }

    @Override
    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface ComposerImporterPageViewImplUiBinder extends UiBinder<DockLayoutPanel, ComposerImporterPageViewImpl> {
    }

    public interface Style extends CssResource {
        String mainPanel();

        String namePanel();

        String labelPosition();

        String alignRight();

        String alignLeft();

        String labelErrorPosition();

        String description();

        String label();

        String horizontalLine();

        String inputField();

        String inputError();
    }
}
