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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;

import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;

/**
 * View implementation for the Subversion project importer.
 *
 * @author vzhukovskii@codenvy.com
 */
public class SubversionProjectImporterViewImpl extends Composite implements SubversionProjectImporterView {

    interface SubversionProjectImporterViewImplUiBinder
            extends UiBinder<DockLayoutPanel, SubversionProjectImporterViewImpl> {
    }

    private ActionDelegate delegate;

    @UiField(provided = true)
    Style       style;
    @UiField
    Label       labelUrlError;
    @UiField
    TextBox     projectUrl;
    @UiField
    TextBox     projectRelativePath;
    @UiField
    TextBox     projectName;
    @UiField
    TextArea    projectDescription;

    @Inject
    public SubversionProjectImporterViewImpl(SubversionExtensionResources resources,
                                             SubversionProjectImporterViewImplUiBinder uiBinder) {
        style = resources.svnProjectImporterPageStyle();
        style.ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        projectName.getElement().setAttribute("maxlength", "32");
        projectDescription.getElement().setAttribute("maxlength", "256");
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void setProjectUrl(@NotNull String url) {
        projectUrl.setText(url);
        delegate.onProjectUrlChanged(url);
    }

    /** {@inheritDoc} */
    @Override
    public String getProjectUrl() {
        return this.projectUrl.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public void setProjectUrlErrorHighlight(boolean visible) {
        if (visible) {
            projectUrl.addStyleName(style.inputError());
        } else {
            projectUrl.removeStyleName(style.inputError());
        }
    }

    @Override
    public void setURLErrorMessage(@NotNull String message) {
        labelUrlError.setText(message != null ? message : "");
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getProjectRelativePath() {
        return this.projectRelativePath.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public void setProjectNameErrorHighlight(boolean visible) {
        if (visible) {
            projectName.addStyleName(style.inputError());
        } else {
            projectName.removeStyleName(style.inputError());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setProjectDescription(@NotNull String text) {
        projectDescription.setText(text);
    }

    /** {@inheritDoc} */
    @Override
    public String getProjectDescription() {
        return projectDescription.getText();
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getProjectName() {
        return projectName.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public void setProjectName(@NotNull String projectName) {
        this.projectName.setValue(projectName);
        delegate.onProjectNameChanged(projectName);
    }

    /** {@inheritDoc} */
    @Override
    public void setUrlTextBoxFocused() {
        projectUrl.setFocus(true);
    }

    @Override
    public void setInputsEnableState(boolean isEnabled) {
        projectName.setEnabled(isEnabled);
        projectDescription.setEnabled(isEnabled);
        projectUrl.setEnabled(isEnabled);

        if (isEnabled) {
            setUrlTextBoxFocused();
        }
    }

    @UiHandler("projectUrl")
    void onProjectUrlChanged(KeyUpEvent event) {
        delegate.onProjectUrlChanged(projectUrl.getValue());
    }

    @UiHandler("projectRelativePath")
    void onProjectRelativePathChanged(final KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }
        delegate.onProjectRelativePathChanged(projectRelativePath.getValue());
    }

    @UiHandler("projectName")
    void onProjectNameChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }

        if (projectName.getValue() != null && projectName.getValue().contains(" ")) {
            String tmp = projectName.getValue();
            projectName.setValue(tmp.replaceAll(" ", "-"));
        }

        delegate.onProjectNameChanged(projectName.getValue());
    }

    @UiHandler("projectDescription")
    void onProjectDescriptionChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }
        delegate.onProjectDescriptionChanged(projectDescription.getValue());
    }

    public interface Style extends CssResource {
        String mainPanel();

        String namePanel();

        String labelPosition();

        String alignRight();

        String labelErrorPosition();

        String label();

        String horizontalLine();

        String inputField();

        String inputError();

        String passwordField();
    }

}
