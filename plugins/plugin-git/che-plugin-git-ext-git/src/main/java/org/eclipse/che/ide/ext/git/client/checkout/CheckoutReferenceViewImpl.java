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
package org.eclipse.che.ide.ext.git.client.checkout;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

/**
 * @author Roman Nikitenko
 */
public class CheckoutReferenceViewImpl extends Window implements CheckoutReferenceView {

    interface CheckoutReferenceViewImplUiBinder extends UiBinder<Widget, CheckoutReferenceViewImpl> {
    }

    private static CheckoutReferenceViewImplUiBinder ourUiBinder = GWT.create(CheckoutReferenceViewImplUiBinder.class);

    private GitLocalizationConstant locale;
    private ActionDelegate          delegate;

    Button btnCheckout;
    Button btnCancel;
    @UiField
    TextBox reference;

    @Inject
    public CheckoutReferenceViewImpl(GitLocalizationConstant locale) {
        this.locale = locale;
        Widget widget = ourUiBinder.createAndBindUi(this);

        this.setTitle(locale.checkoutReferenceTitle());
        this.setWidget(widget);

        btnCancel = createButton(locale.buttonCancel(), "git-checkoutReference-cancel", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        btnCheckout = createButton(locale.buttonCheckout(), "git-checkoutReference-checkout", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onCheckoutClicked(reference.getValue());
            }
        });
        addButtonToFooter(btnCheckout);
    }

    @Override
    public void showDialog() {
        reference.setText("");
        this.show();

        new Timer() {
            @Override
            public void run() {
                reference.setFocus(true);
            }
        }.schedule(300);
    }

    @Override
    public void close() {
        this.hide();
    }

    @Override
    public String getReference() {
        return this.reference.getValue();
    }

    @Override
    public void setCheckoutButEnableState(boolean isEnabled) {
        btnCheckout.setEnabled(isEnabled);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler("reference")
    void onKeyUp(KeyUpEvent event) {
        delegate.referenceValueChanged(reference.getValue());
    }

    @Override
    protected void onEnterClicked() {
        if (isWidgetFocused(btnCancel)) {
            delegate.onCancelClicked();
            return;
        }
        delegate.onEnterClicked();
    }
}
