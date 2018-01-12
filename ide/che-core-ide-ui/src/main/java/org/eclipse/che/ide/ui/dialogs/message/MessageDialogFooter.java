/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.dialogs.message;

import static org.eclipse.che.ide.ui.dialogs.message.MessageDialogView.ActionDelegate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.UILocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The footer show on message windows.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class MessageDialogFooter extends Composite {

  private static final Window.Resources resources = GWT.create(Window.Resources.class);
  /** The UI binder instance. */
  private static MessageWindowFooterUiBinder uiBinder =
      GWT.create(MessageWindowFooterUiBinder.class);
  /** The i18n messages. */
  @UiField(provided = true)
  UILocalizationConstant messages;

  @UiField Button okButton;
  /** The action delegate. */
  private ActionDelegate actionDelegate;

  @Inject
  public MessageDialogFooter(final @NotNull UILocalizationConstant messages) {
    this.messages = messages;
    initWidget(uiBinder.createAndBindUi(this));
    okButton.addStyleName(resources.windowCss().primaryButton());
    okButton.getElement().setId("info-window");
  }

  /**
   * Sets the action delegate.
   *
   * @param delegate the new value
   */
  public void setDelegate(@NotNull ActionDelegate delegate) {
    actionDelegate = delegate;
  }

  /**
   * Sets the confirm button text.
   *
   * @param text the text
   */
  public void setConfirmButtonText(@NotNull String text) {
    okButton.setText(text);
  }

  /**
   * Handler set on the OK button.
   *
   * @param event the event that triggers the handler call
   */
  @UiHandler("okButton")
  public void handleOkClick(final ClickEvent event) {
    this.actionDelegate.accepted();
  }

  /** The UI binder interface for this component. */
  interface MessageWindowFooterUiBinder extends UiBinder<Widget, MessageDialogFooter> {}
}
