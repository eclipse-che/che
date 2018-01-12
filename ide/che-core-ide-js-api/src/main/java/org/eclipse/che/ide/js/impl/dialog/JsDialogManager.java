/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.js.impl.dialog;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.js.api.dialog.ChoiceDialogData;
import org.eclipse.che.ide.js.api.dialog.ClickButtonHandler;
import org.eclipse.che.ide.js.api.dialog.ConfirmDialogData;
import org.eclipse.che.ide.js.api.dialog.DialogManager;
import org.eclipse.che.ide.js.api.dialog.InputAcceptedHandler;
import org.eclipse.che.ide.js.api.dialog.InputDialogData;
import org.eclipse.che.ide.js.api.dialog.MessageDialogData;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/** @author Roman Nikitenko */
@Singleton
public class JsDialogManager implements DialogManager {

  private DialogFactory dialogFactory;

  @Inject
  public JsDialogManager(DialogFactory dialogFactory) {
    this.dialogFactory = dialogFactory;
  }

  @Override
  public void showMessageDialog(
      MessageDialogData dialogData, ClickButtonHandler confirmButtonClickedHandler) {
    dialogFactory
        .createMessageDialog(
            dialogData.getTitle(),
            dialogData.getContent(),
            dialogData.getConfirmButtonText(),
            confirmButtonClickedHandler::onButtonClicked)
        .show();
  }

  @Override
  public void showConfirmDialog(
      ConfirmDialogData dialogData,
      ClickButtonHandler confirmButtonClickedHandler,
      ClickButtonHandler cancelButtonClickedHandler) {
    dialogFactory
        .createConfirmDialog(
            dialogData.getTitle(),
            dialogData.getContent(),
            dialogData.getConfirmButtonText(),
            dialogData.getCancelButtonText(),
            confirmButtonClickedHandler::onButtonClicked,
            cancelButtonClickedHandler::onButtonClicked)
        .show();
  }

  @Override
  public void showInputDialog(
      InputDialogData dialogData,
      InputAcceptedHandler inputAcceptedHandler,
      ClickButtonHandler cancelButtonClickedHandler) {
    dialogFactory
        .createInputDialog(
            dialogData.getTitle(),
            dialogData.getContent(),
            dialogData.getInitialText(),
            dialogData.getSelectionStartIndex(),
            dialogData.getSelectionLength(),
            dialogData.getConfirmButtonText(),
            dialogData.getCancelButtonText(),
            inputAcceptedHandler::onInputAccepted,
            cancelButtonClickedHandler::onButtonClicked)
        .show();
  }

  @Override
  public void showChoiceDialog(
      ChoiceDialogData dialogData,
      ClickButtonHandler firstButtonClickedHandler,
      ClickButtonHandler secondButtonClickedHandler,
      ClickButtonHandler thirdButtonClickedHandler) {
    dialogFactory
        .createChoiceDialog(
            dialogData.getTitle(),
            dialogData.getContent(),
            dialogData.getFirstChoiceButtonText(),
            dialogData.getSecondChoiceButtonText(),
            dialogData.getThirdChoiceButtonText(),
            firstButtonClickedHandler::onButtonClicked,
            secondButtonClickedHandler::onButtonClicked,
            thirdButtonClickedHandler::onButtonClicked)
        .show();
  }
}
