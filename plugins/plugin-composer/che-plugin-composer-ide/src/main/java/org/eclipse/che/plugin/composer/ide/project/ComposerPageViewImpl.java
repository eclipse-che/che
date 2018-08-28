/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.composer.ide.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/** @author Kaloyan Raev */
public class ComposerPageViewImpl implements ComposerPageView {

  private static ComposerPageViewImplUiBinder ourUiBinder =
      GWT.create(ComposerPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField Style style;
  @UiField TextBox packageField;

  private ActionDelegate delegate;

  @Inject
  public ComposerPageViewImpl() {
    rootElement = ourUiBinder.createAndBindUi(this);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  @Override
  public String getPackage() {
    return packageField.getText();
  }

  @Override
  public void setPackage(String value) {
    packageField.setText(value);
  }

  @UiHandler({"packageField"})
  void onKeyUp(KeyUpEvent event) {
    if (delegate != null) {
      delegate.onAttributesChanged();
    }
  }

  @Override
  public void showPackageMissingIndicator(boolean doShow) {
    if (doShow) {
      packageField.addStyleName(style.inputError());
    } else {
      packageField.removeStyleName(style.inputError());
    }
  }

  @Override
  public void changePackageFieldState(boolean isEnable) {
    packageField.setEnabled(isEnable);
  }

  interface ComposerPageViewImplUiBinder extends UiBinder<FlowPanel, ComposerPageViewImpl> {}

  interface Style extends CssResource {
    String inputError();
  }
}
