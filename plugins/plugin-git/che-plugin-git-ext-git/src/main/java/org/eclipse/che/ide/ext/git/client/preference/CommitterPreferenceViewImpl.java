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
package org.eclipse.che.ide.ext.git.client.preference;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Singleton;

/**
 * @author Valeriy Svydenko
 */
@Singleton
public class CommitterPreferenceViewImpl implements CommitterPreferenceView {
    private static CommitterPreferenceViewImplUiBinder ourUiBinder = GWT.create(CommitterPreferenceViewImplUiBinder.class);
    private final FlowPanel rootElement;
    @UiField
    TextBox email;
    @UiField
    TextBox name;
    private ActionDelegate delegate;

    public CommitterPreferenceViewImpl() {
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return rootElement;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        this.name.setText(name);
    }

    /** {@inheritDoc} */
    @Override
    public void setEmail(String email) {
        this.email.setText(email);
    }

    @UiHandler("name")
    void handleNameChanged(KeyUpEvent event) {
        delegate.nameChanged(name.getText());
    }

    @UiHandler("email")
    void handleEmailChanged(KeyUpEvent event) {
        delegate.emailChanged(email.getText());
    }

    interface CommitterPreferenceViewImplUiBinder
            extends UiBinder<FlowPanel, CommitterPreferenceViewImpl> {
    }
}