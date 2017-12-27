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

package org.eclipse.che.ide.js.api.dialog;

import jsinterop.annotations.JsType;

/**
 * Manager for creating and displaying Message, Confirm, Input and Choice dialogs.
 *
 * @author Roman Nikitenko
 */
@JsType
public interface DialogManager {

  /**
   * Display a Message dialog. A dialog consists of a title, main part with text as content and
   * confirmation button. Confirmation button text is 'OK' by default, can be overridden.
   *
   * @param dialogData the information necessary to create a Message dialog window
   * @param confirmButtonClickedHandler the handler is used when user click on confirmation button
   */
  void displayMessageDialog(
      MessageDialogData dialogData, ClickButtonHandler confirmButtonClickedHandler);

  /**
   * Display a Confirmation dialog. A dialog consists of a title, main part with text as content,
   * confirmation and cancel buttons. Confirmation button text is 'OK' by default. Cancel button
   * text is 'Cancel' by default. Text for confirmation and cancel buttons can be overridden.
   *
   * @param dialogData the information necessary to create a Confirmation dialog window
   * @param confirmButtonClickedHandler the handler is used when user click on confirmation button
   * @param cancelButtonClickedHandler the handler is used when user click on cancel button
   */
  void displayConfirmDialog(
      ConfirmDialogData dialogData,
      ClickButtonHandler confirmButtonClickedHandler,
      ClickButtonHandler cancelButtonClickedHandler);

  /**
   * Display an Input dialog. A dialog consists of a title, main part with input field and label for
   * it, confirmation and cancel buttons. Input field can contains an initial text. The initial text
   * may be pre-selected. Confirmation button text is 'OK' by default. Cancel button text is
   * 'Cancel' by default. Text for confirmation and cancel buttons can be overridden.
   *
   * @param dialogData the information necessary to create an Input dialog window
   * @param inputAcceptedHandler the handler is used when user click on confirmation button
   * @param cancelButtonClickedHandler the handler is used when user click on cancel button
   */
  void displayInputDialog(
      InputDialogData dialogData,
      InputAcceptedHandler inputAcceptedHandler,
      ClickButtonHandler cancelButtonClickedHandler);

  /**
   * Display a Choice dialog. A dialog consists of a title, main part with text as content and three
   * buttons to confirm some choice.
   *
   * @param dialogData the information necessary to create a Choice dialog window
   * @param firstButtonClickedHandler the handler is used when user click on first button on the
   *     right
   * @param secondButtonClickedHandler the handler is used when user click on second button on the
   *     right
   * @param thirdButtonClickedHandler the handler is used when user click on third button on the
   *     right
   */
  void displayChoiceDialog(
      ChoiceDialogData dialogData,
      ClickButtonHandler firstButtonClickedHandler,
      ClickButtonHandler secondButtonClickedHandler,
      ClickButtonHandler thirdButtonClickedHandler);
}
