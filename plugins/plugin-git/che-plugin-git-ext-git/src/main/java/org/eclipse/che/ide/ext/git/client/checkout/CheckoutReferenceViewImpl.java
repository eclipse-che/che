/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.checkout;

import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

/** @author Roman Nikitenko */
public class CheckoutReferenceViewImpl extends Window implements CheckoutReferenceView {

  interface CheckoutReferenceViewImplUiBinder extends UiBinder<Widget, CheckoutReferenceViewImpl> {}

  private static CheckoutReferenceViewImplUiBinder ourUiBinder =
      GWT.create(CheckoutReferenceViewImplUiBinder.class);

  private GitLocalizationConstant locale;
  private ActionDelegate delegate;

  Button btnCheckout;
  Button btnCancel;
  @UiField TextBox reference;

  @Inject
  public CheckoutReferenceViewImpl(GitLocalizationConstant locale) {
    this.locale = locale;
    Widget widget = ourUiBinder.createAndBindUi(this);

    this.setTitle(locale.checkoutReferenceTitle());
    this.setWidget(widget);

    btnCancel =
        addFooterButton(
            locale.buttonCancel(),
            "git-checkoutReference-cancel",
            event -> delegate.onCancelClicked());

    btnCheckout =
        addFooterButton(
            locale.buttonCheckout(),
            "git-checkoutReference-checkout",
            event -> delegate.onCheckoutClicked(reference.getValue()),
            true);
  }

  @Override
  public void showDialog() {
    reference.setText("");
    show(reference);
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
  public void onEnterPress(NativeEvent evt) {
    if (isWidgetOrChildFocused(btnCancel)) {
      delegate.onCancelClicked();
    } else {
      delegate.onEnterClicked();
    }
  }
}
