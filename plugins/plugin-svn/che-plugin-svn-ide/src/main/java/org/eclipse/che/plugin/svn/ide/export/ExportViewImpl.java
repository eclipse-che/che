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
package org.eclipse.che.plugin.svn.ide.export;


import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of {@link ExportView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class ExportViewImpl extends Window implements ExportView {
    interface ExportViewImplUiBinder extends UiBinder<Widget, ExportViewImpl> {
    }

    private static ExportViewImplUiBinder uiBinder = GWT.create(ExportViewImplUiBinder.class);

    Button btnExport;
    Button btnCancel;

    @UiField
    CheckBox revisionCheckBox;

    @UiField
    TextBox revisionTextBox;

    @UiField(provided = true)
    SubversionExtensionResources             resources;
    @UiField(provided = true)
    SubversionExtensionLocalizationConstants constants;

    private ExportView.ActionDelegate delegate;

    private static final String PLACEHOLDER       = "placeholder";
    private static final String PLACEHOLDER_DUMMY = "revision to export";

    @Inject
    public ExportViewImpl(SubversionExtensionResources resources,
                          SubversionExtensionLocalizationConstants constants) {
        this.resources = resources;
        this.constants = constants;

        this.ensureDebugId("svn-export-window");

        this.setTitle("Export");

        this.setWidget(uiBinder.createAndBindUi(this));

        btnCancel = createButton(constants.buttonCancel(), "svn-export-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        btnExport = createButton("Export", "svn-export-export", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onExportClicked();
            }
        });
        addButtonToFooter(btnExport);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(final ExportView.ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRevisionSpecified() {
        return revisionCheckBox.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public String getRevision() {
        return revisionTextBox.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void onClose() {
        hide();
    }

    /** {@inheritDoc} */
    @Override
    public void onShow() {
        revisionCheckBox.setValue(false, true);
        revisionTextBox.setText(null);
        revisionTextBox.setEnabled(false);
        revisionTextBox.getElement().setAttribute(PLACEHOLDER, PLACEHOLDER_DUMMY);
        btnExport.setEnabled(true);

        show();
    }

    @UiHandler("revisionCheckBox")
    @SuppressWarnings("unused")
    public void onRevisionCheckBoxChanged(ClickEvent event) {
        revisionTextBox.setEnabled(revisionCheckBox.getValue());
        revisionTextBox.setText(null);
        btnExport.setEnabled(!revisionCheckBox.getValue());
    }

    @UiHandler("revisionTextBox")
    @SuppressWarnings("unused")
    public void onRevisionTextBoxChanged(KeyUpEvent event) {
        btnExport.setEnabled(revisionCheckBox.getValue() && !Strings.isNullOrEmpty(revisionTextBox.getText()));
    }
}
