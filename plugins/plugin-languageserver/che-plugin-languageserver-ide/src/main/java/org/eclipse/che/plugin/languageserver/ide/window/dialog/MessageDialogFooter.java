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
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.UILocalizationConstant;
import org.eclipse.lsp4j.MessageActionItem;

/**
 * The footer show on confirmation dialogs.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class MessageDialogFooter implements IsWidget {

  //  private static final Window.Resources resources = GWT.create(Window.Resources.class);
  /** The UI binder instance. */
  private static ConfirmDialogFooterUiBinder uiBinder =
      GWT.create(ConfirmDialogFooterUiBinder.class);
  /** The i18n messages. */
  @UiField(provided = true)
  UILocalizationConstant messages;

  @UiField MessageFooterStyle style;

  HTMLPanel rootPanel;

  /** The action delegate. */
  private MessageDialogView.ActionDelegate actionDelegate;

  @Inject
  public MessageDialogFooter(final @NotNull UILocalizationConstant messages) {
    this.messages = messages;
    rootPanel = uiBinder.createAndBindUi(this);
  }

  public void setActions(List<MessageActionItem> actions) {
    actions.forEach(
        action -> {
          Button button = new Button();
          button.setText(action.getTitle());
          button.addClickHandler(clickEvent -> actionDelegate.onAction(action));
          button.addStyleName(style.actionButton());
          rootPanel.add(button);
        });
  }

  /**
   * Sets the action delegate.
   *
   * @param delegate the new value
   */
  public void setDelegate(final MessageDialogView.ActionDelegate delegate) {
    this.actionDelegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootPanel;
  }

  /** The UI binder interface for this component. */
  interface ConfirmDialogFooterUiBinder extends UiBinder<HTMLPanel, MessageDialogFooter> {}

  interface MessageFooterStyle extends CssResource {

    @ClassName("button-block")
    String buttonBlock();

    @ClassName("action-button")
    String actionButton();
  }
}
