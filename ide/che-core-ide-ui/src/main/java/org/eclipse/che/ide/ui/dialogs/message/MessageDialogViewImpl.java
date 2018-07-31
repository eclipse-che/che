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
package org.eclipse.che.ide.ui.dialogs.message;

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
 * Implementation of the message dialog view.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class MessageDialogViewImpl extends Window implements MessageDialogView {

  /** The UI binder instance. */
  private static MessageWindowUiBinder uiBinder = GWT.create(MessageWindowUiBinder.class);
  /** The window footer. */
  private final MessageDialogFooter footer;
  /** The container for the window content. */
  @UiField SimplePanel content;

  private ActionDelegate delegate;

  @Inject
  public MessageDialogViewImpl(final @NotNull MessageDialogFooter footer) {
    Widget widget = uiBinder.createAndBindUi(this);
    setWidget(widget);

    this.footer = footer;
    addFooterWidget(footer);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(@NotNull ActionDelegate delegate) {
    this.delegate = delegate;
    this.footer.setDelegate(this.delegate);
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    show(footer.okButton);
  }

  /** {@inheritDoc} */
  @Override
  public void closeDialog() {
    hide();
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    delegate.accepted();
  }

  /** {@inheritDoc} */
  @Override
  public void setContent(@NotNull IsWidget content) {
    this.content.clear();
    this.content.setWidget(content);
  }

  @Override
  public void setTitleCaption(String title) {
    setTitle(title);
  }

  /** {@inheritDoc} */
  @Override
  public void setConfirmButtonText(@NotNull String text) {
    footer.setConfirmButtonText(text);
  }

  /** The UI binder interface for this components. */
  interface MessageWindowUiBinder extends UiBinder<Widget, MessageDialogViewImpl> {}
}
