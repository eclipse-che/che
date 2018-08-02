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

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;

/**
 * Dialog Window based on TextArea widget.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 * @author Oleksandr Andriienko
 */
public interface TextAreaDialogView extends View<TextAreaDialogView.ActionDelegate> {
  /** Needs for delegate some function into TextArea view. */
  interface ActionDelegate {

    /** Show text area dialog view. */
    void showDialog();

    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Change button.
     */
    void onAgreeClicked();

    /** Performs any actions appropriate in response to the user having changed value. */
    void onValueChanged();
  }

  /** @return changed value */
  @NotNull
  String getValue();

  /**
   * Set new value.
   *
   * @param value new value
   */
  void setValue(@NotNull String value);

  /**
   * Change the enable state of the evaluate button.
   *
   * @param isEnable <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableChangeButton(boolean isEnable);

  /** Give focus to expression field. */
  void focusInValueField();

  /** Select all text in expression field. */
  void selectAllText();

  /**
   * Set title for value field.
   *
   * @param title new title for value field
   */
  void setValueTitle(@NotNull String title);

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
