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
package org.eclipse.che.plugin.languageserver.ide.window.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.lsp4j.MessageActionItem;

/**
 * Implementation of the confirmation dialog view.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class MessageDialogViewImpl extends Window implements MessageDialogView {

  /** The UI binder instance. */
  private static ConfirmWindowUiBinder uiBinder = GWT.create(ConfirmWindowUiBinder.class);
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

  @Override
  public void setDelegate(final ActionDelegate delegate) {
    this.delegate = delegate;
    this.footer.setDelegate(delegate);
  }

  @Override
  public void showDialog() {
    this.show();
  }

  @Override
  public void closeDialog() {
    this.hide();
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    delegate.onEnterClicked();
  }

  @Override
  public void setTitleCaption(String title) {
    setTitle(title);
  }

  @Override
  public void setContent(String content) {
    this.content.clear();
    this.content.getElement().setInnerText(content);
  }

  @Override
  public void setActions(List<MessageActionItem> actions) {
    footer.setActions(actions);
  }

  /** The UI binder interface for this components. */
  interface ConfirmWindowUiBinder extends UiBinder<Widget, MessageDialogViewImpl> {}
}
