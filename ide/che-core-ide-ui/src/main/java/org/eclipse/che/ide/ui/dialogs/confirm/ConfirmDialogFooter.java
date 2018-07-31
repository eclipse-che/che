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
package org.eclipse.che.ide.ui.dialogs.confirm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.UILocalizationConstant;
import org.eclipse.che.ide.ui.window.WindowClientBundle;

/**
 * The footer show on confirmation dialogs.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class ConfirmDialogFooter implements IsWidget {

  /** The UI binder instance. */
  private static ConfirmDialogFooterUiBinder uiBinder =
      GWT.create(ConfirmDialogFooterUiBinder.class);
  /** The i18n messages. */
  @UiField(provided = true)
  UILocalizationConstant messages;

  @UiField Button okButton;
  @UiField Button cancelButton;
  HTMLPanel rootPanel;

  /** The action delegate. */
  private ConfirmDialogView.ActionDelegate actionDelegate;

  @Inject
  public ConfirmDialogFooter(
      final @NotNull UILocalizationConstant messages, WindowClientBundle resources) {
    this.messages = messages;
    rootPanel = uiBinder.createAndBindUi(this);

    okButton.addStyleName(resources.getStyle().windowFrameFooterButtonPrimary());
    okButton.getElement().setId("ask-dialog-ok");

    cancelButton.addStyleName(resources.getStyle().windowFrameFooterButton());
    cancelButton.getElement().setId("ask-dialog-cancel");
  }

  /**
   * Overwrites label of Ok button
   *
   * @param label new label
   */
  public void setOkButtonLabel(String label) {
    okButton.setText(label);
  }

  /**
   * Overwrites label of Cancel button
   *
   * @param label new label
   */
  public void setCancelButtonLabel(String label) {
    cancelButton.setText(label);
  }

  /**
   * Sets the action delegate.
   *
   * @param delegate the new value
   */
  public void setDelegate(final ConfirmDialogView.ActionDelegate delegate) {
    this.actionDelegate = delegate;
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

  /**
   * Handler set on the cancel button.
   *
   * @param event the event that triggers the handler call
   */
  @UiHandler("cancelButton")
  public void handleCancelClick(final ClickEvent event) {
    this.actionDelegate.cancelled();
  }

  @Override
  public Widget asWidget() {
    return rootPanel;
  }

  /** The UI binder interface for this component. */
  interface ConfirmDialogFooterUiBinder extends UiBinder<HTMLPanel, ConfirmDialogFooter> {}
}
