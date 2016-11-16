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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

import org.eclipse.che.ide.projectimport.ProjectImporterResource;

import javax.validation.constraints.NotNull;

/**
 * @author Roman Nikitenko
 */
public class ZipImporterPageViewImpl extends Composite implements ZipImporterPageView {
    @UiField(provided = true)
    Style       style;
    @UiField
    Label       labelUrlError;
    @UiField
    TextBox     projectName;
    @UiField
    TextArea    projectDescription;
    @UiField
    TextBox     projectUrl;
    @UiField
    CheckBox    skipFirstLevel;
    private ActionDelegate delegate;

    @Inject
    public ZipImporterPageViewImpl(ProjectImporterResource resource,
                                   ZipImporterPageViewImplUiBinder uiBinder) {
        style = resource.zipImporterPageStyle();
        style.ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        projectName.getElement().setAttribute("maxlength", "32");
        projectDescription.getElement().setAttribute("maxlength", "256");
        skipFirstLevel.setValue(false);
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

    @UiHandler("projectUrl")
    void onProjectUrlChanged(KeyUpEvent event) {
        delegate.projectUrlChanged(projectUrl.getValue());
    }

    @UiHandler("projectDescription")
    void onProjectDescriptionChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }
        delegate.projectDescriptionChanged(projectDescription.getValue());
    }

    @UiHandler({"skipFirstLevel"})
    void skipFirstLevelHandler(ValueChangeEvent<Boolean> event) {
        delegate.skipFirstLevelChanged(skipFirstLevel.getValue());
    }

    @Override
    public void setProjectUrl(@NotNull String url) {
        projectUrl.setText(url);
        delegate.projectUrlChanged(url);
    }

    @Override
    public void showNameError() {
        projectName.addStyleName(style.inputError());
    }

    @Override
    public void hideNameError() {
        projectName.removeStyleName(style.inputError());
    }

    @Override
    public void showUrlError(@NotNull String message) {
        projectUrl.addStyleName(style.inputError());
        labelUrlError.setText(message);
    }

    @Override
    public void hideUrlError() {
        projectUrl.removeStyleName(style.inputError());
        labelUrlError.setText("");
    }

    @NotNull
    @Override
    public String getProjectName() {
        return projectName.getValue();
    }

    @Override
    public void setProjectName(@NotNull String projectName) {
        this.projectName.setValue(projectName);
    }

    @Override
    public void setProjectDescription(@NotNull String projectDescription) {
        this.projectDescription.setValue(projectDescription);
    }

    @Override
    public void focusInUrlInput() {
        projectUrl.setFocus(true);
    }

    @Override
    public void setInputsEnableState(boolean isEnabled) {
        projectName.setEnabled(isEnabled);
        projectDescription.setEnabled(isEnabled);
        projectUrl.setEnabled(isEnabled);

        if (isEnabled) {
            focusInUrlInput();
        }
    }

    @Override
    public boolean isSkipFirstLevelSelected() {
        return skipFirstLevel.getValue();
    }

    @Override
    public void setSkipFirstLevel(boolean skip) {
        skipFirstLevel.setValue(skip);
    }

    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface ZipImporterPageViewImplUiBinder extends UiBinder<DockLayoutPanel, ZipImporterPageViewImpl> {
    }

    public interface Style extends CssResource {
        String mainPanel();

        String namePanel();

        String labelPosition();

        String marginTop();

        String alignRight();

        String alignLeft();

        String labelErrorPosition();

        String description();

        String label();

        String horizontalLine();

        String checkBoxPosition();

        String inputField();

        String inputError();
    }
}
