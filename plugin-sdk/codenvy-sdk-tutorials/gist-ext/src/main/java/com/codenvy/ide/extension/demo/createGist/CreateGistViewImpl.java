/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.demo.createGist;

import com.codenvy.ide.extension.demo.GistExtensionLocalizationConstant;
import com.codenvy.ide.extension.demo.GistExtensionResources;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;

/** The implementation of {@link CreateGistView}. */
public class CreateGistViewImpl extends DialogBox implements CreateGistView {
    interface CommitViewImplUiBinder extends UiBinder<Widget, CreateGistViewImpl> {
    }

    @UiField
    CheckBox                  publicField;
    @UiField
    TextArea                  snippet;
    @UiField
    Button                    btnCreate;
    @UiField
    Button btnCancel;
    @UiField(provided = true)
    final   GistExtensionResources            res;
    @UiField(provided = true)
    final   GistExtensionLocalizationConstant locale;
    private ActionDelegate                    delegate;

    /**
     * Create view.
     *
     * @param resources
     * @param locale
     */
    @Inject
    protected CreateGistViewImpl(CommitViewImplUiBinder ourUiBinder,
                                 GistExtensionResources resources,
                                 GistExtensionLocalizationConstant locale) {
        this.res = resources;
        this.locale = locale;

        Widget widget = ourUiBinder.createAndBindUi(this);

        this.setText(locale.createViewTitle());
        this.setWidget(widget);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getSnippet() {
        return snippet.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setSnippet(@NotNull String snippet) {
        this.snippet.setText(snippet);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPublic() {
        return publicField.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public void setPublic(boolean isPublic) {
        publicField.setValue(isPublic);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableCreateButton(boolean enable) {
        btnCreate.setEnabled(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void focusInSnippetField() {
        snippet.setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.center();
        this.show();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler("btnCreate")
    public void onCommitClicked(ClickEvent event) {
        delegate.onCreateClicked();
    }

    @UiHandler("btnCancel")
    public void onCancelClicked(ClickEvent event) {
        delegate.onCancelClicked();
    }

    @UiHandler("snippet")
    public void onMessageChanged(KeyUpEvent event) {
        delegate.onValueChanged();
    }
}