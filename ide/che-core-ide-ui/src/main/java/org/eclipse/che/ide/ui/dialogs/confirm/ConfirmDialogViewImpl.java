/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.dialogs.confirm;

import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of the confirmation dialog view.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class ConfirmDialogViewImpl extends Window implements ConfirmDialogView {

  /** The UI binder instance. */
  private static ConfirmWindowUiBinder uiBinder = GWT.create(ConfirmWindowUiBinder.class);
  /** The window footer. */
  private final ConfirmDialogFooter footer;
  /** The container for the window content. */
  @UiField SimplePanel content;

  private ActionDelegate delegate;

  @Inject
  public ConfirmDialogViewImpl(final @NotNull ConfirmDialogFooter footer) {
    Widget widget = uiBinder.createAndBindUi(this);
    setWidget(widget);

    this.footer = footer;
    addFooterWidget(footer);
  }

  @Override
  public void setDelegate(final ActionDelegate delegate) {
    this.delegate = delegate;
    this.footer.setDelegate(delegate);
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    evt.preventDefault();
    delegate.onEnterClicked();
  }

  @Override
  public void showDialog() {
    this.show(footer.okButton);
  }

  @Override
  public void closeDialog() {
    this.hide();
  }

  @Override
  public void setContent(final IsWidget content) {
    this.content.clear();
    this.content.setWidget(content);
  }

  @Override
  public void setTitleCaption(String title) {
    setTitle(title);
  }

  public void setOkButtonLabel(String label) {
    footer.setOkButtonLabel(label);
  }

  public void setCancelButtonLabel(String label) {
    footer.setCancelButtonLabel(label);
  }

  @Override
  public boolean isOkButtonInFocus() {
    return isWidgetOrChildFocused(footer.okButton);
  }

  @Override
  public boolean isCancelButtonInFocus() {
    return isWidgetOrChildFocused(footer.cancelButton);
  }

  /** The UI binder interface for this components. */
  interface ConfirmWindowUiBinder extends UiBinder<Widget, ConfirmDialogViewImpl> {}
}
