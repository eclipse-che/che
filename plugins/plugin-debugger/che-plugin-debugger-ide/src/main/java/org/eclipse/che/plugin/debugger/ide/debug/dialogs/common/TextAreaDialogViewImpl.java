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
package org.eclipse.che.plugin.debugger.ide.debug.dialogs.common;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link TextAreaDialogView}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 * @author Oleksandr Andriienko
 */
public class TextAreaDialogViewImpl extends Window implements TextAreaDialogView {
  interface ChangeValueViewImplUiBinder extends UiBinder<Widget, TextAreaDialogViewImpl> {}

  @UiField TextArea value;
  @UiField Label textAreaLabel;

  private ActionDelegate delegate;
  private Button agreeButton;

  /** Create view. */
  @Inject
  public TextAreaDialogViewImpl(
      ChangeValueViewImplUiBinder uiBinder,
      @NotNull @Assisted("title") String title,
      @NotNull @Assisted("agreeBtnLabel") String agreeBtnLabel,
      @NotNull @Assisted("cancelBtnLabel") String cancelBtnLabel) {
    Widget widget = uiBinder.createAndBindUi(this);

    this.setTitle(title);
    this.setWidget(widget);
    this.ensureDebugId("debugger-textarea-dialog");

    addFooterButton(
        cancelBtnLabel,
        "debugger-textarea-dialog-cancel-btn",
        clickEvent -> delegate.onCancelClicked());

    agreeButton =
        addFooterButton(
            agreeBtnLabel,
            "debugger-textarea-dialog-agree-btn",
            clickEvent -> delegate.onAgreeClicked(),
            true);
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getValue() {
    return value.getText();
  }

  /** {@inheritDoc} */
  @Override
  public void setValue(@NotNull String value) {
    this.value.setText(value);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableChangeButton(boolean isEnable) {
    agreeButton.setEnabled(isEnable);
  }

  /** {@inheritDoc} */
  @Override
  public void focusInValueField() {
    new Timer() {
      @Override
      public void run() {
        value.setFocus(true);
      }
    }.schedule(300);
  }

  /** {@inheritDoc} */
  @Override
  public void selectAllText() {
    value.selectAll();
  }

  /** {@inheritDoc} */
  @Override
  public void setValueTitle(@NotNull String title) {
    textAreaLabel.getElement().setInnerHTML(title);
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    this.hide();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    show();
  }

  @Override
  protected void onShow() {
    if (!value.getText().isEmpty()) {
      value.selectAll();
      setEnableChangeButton(true);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @UiHandler("value")
  public void onValueChanged(KeyUpEvent event) {
    delegate.onValueChanged();
  }
}
