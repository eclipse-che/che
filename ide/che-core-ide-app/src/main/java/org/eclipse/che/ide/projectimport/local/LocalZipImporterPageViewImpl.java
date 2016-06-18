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
package org.eclipse.che.ide.projectimport.local;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.projectimport.ProjectImporterResource;
import org.eclipse.che.ide.ui.Styles;
import org.eclipse.che.ide.ui.window.Window;

import javax.validation.constraints.NotNull;

/**
 * @author Roman Nikitenko
 */
public class LocalZipImporterPageViewImpl extends Window implements LocalZipImporterPageView {
    @UiField(provided = true)
    Style       importerStyle;
    @UiField
    TextBox     projectName;
    @UiField
    TextArea    projectDescription;
    @UiField
    CheckBox    skipFirstLevel;
    @UiField
    FormPanel   submitForm;
    @UiField
    FlowPanel   uploadPanel;

    Button cancelButton;
    Button importButton;

    private ActionDelegate           delegate;
    private CoreLocalizationConstant locale;
    private FileUpload               file;

    @Inject
    public LocalZipImporterPageViewImpl(ProjectImporterResource resource,
                                        LocalZipImporterPageViewImplUiBinder uiBinder,
                                        CoreLocalizationConstant locale,
                                        org.eclipse.che.ide.Resources ideResources) {
        this.locale = locale;
        this.setTitle(locale.importLocalProjectDescription());

        importerStyle = resource.localZipImporterPageStyle();
        importerStyle.ensureInjected();

        setWidget(uiBinder.createAndBindUi(this));
        bind();

        projectName.getElement().setAttribute("maxlength", "32");
        projectDescription.getElement().setAttribute("maxlength", "256");
        importButton.addStyleName(ideResources.Css().buttonLoader());
        importButton.setEnabled(false);
        skipFirstLevel.setValue(true);
    }

    /** Bind handlers. */
    private void bind() {
        submitForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                delegate.onSubmitComplete(event.getResults());
            }
        });

        cancelButton = createButton(locale.cancel(), "file-importLocalProject-cancel", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(cancelButton);

        importButton = createPrimaryButton(locale.importProjectButton(), "file-importLocalProject-import", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onImportClicked();
            }
        });
        addButtonToFooter(importButton);
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

        delegate.projectNameChanged();
    }

    @Override
    public void showDialog() {
        addFile();
        this.show();
    }

    @Override
    public void closeDialog() {
        this.hide();
        this.onClose();
    }

    @Override
    public void setEncoding(@NotNull String encodingType) {
        submitForm.setEncoding(encodingType);
    }

    @Override
    public void setAction(@NotNull String url) {
        submitForm.setMethod(FormPanel.METHOD_POST);
        submitForm.setAction(url);
    }

    @Override
    public void submit() {
        skipFirstLevel.setFormValue(skipFirstLevel.getValue().toString());
        submitForm.submit();
    }

    @Override
    public void showNameError() {
        projectName.addStyleName(importerStyle.inputError());
    }

    @Override
    public void hideNameError() {
        projectName.removeStyleName(importerStyle.inputError());
    }

    @NotNull
    @Override
    public String getProjectName() {
        return projectName.getValue();
    }

    @NotNull
    @Override
    public String getFileName() {
        String name = file.getFilename();
        return name != null ? name : "";
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
    public void setInputsEnableState(boolean isEnabled) {
        file.setEnabled(isEnabled);
        skipFirstLevel.setEnabled(isEnabled);
        projectName.setEnabled(isEnabled);
        projectDescription.setEnabled(isEnabled);
        cancelButton.setEnabled(isEnabled);
    }

    @Override
    public void setEnabledImportButton(boolean enabled) {
        importButton.setEnabled(enabled);
    }

    @Override
    public void setSkipFirstLevel(boolean skip) {
        skipFirstLevel.setValue(skip);
    }

    @Override
    public void setLoaderVisibility(boolean isVisible) {
        if (isVisible) {
            importButton.setHTML("<i></i>");
            importButton.setEnabled(false);
        } else {
            importButton.setText(locale.importProjectButton());
            importButton.setEnabled(true);
        }
    }

    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    private void addFile() {
        file = new FileUpload();
        file.setHeight("22px");
        file.setWidth("100%");
        file.setName("file");
        file.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                delegate.fileNameChanged();
            }
        });
        file.ensureDebugId("file-importProject-chooseFile");
        uploadPanel.insert(file, 0);
    }

    @Override
    protected void onClose() {
        importButton.setEnabled(false);
        skipFirstLevel.setValue(true);
        uploadPanel.remove(file);
    }

    interface LocalZipImporterPageViewImplUiBinder extends UiBinder<DockLayoutPanel, LocalZipImporterPageViewImpl> {
    }

    public interface Style extends Styles {
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
    }
}
