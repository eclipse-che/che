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
package org.eclipse.che.ide.ui.button;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The abstract representation of console button widget. It provides all needed method for this kind
 * of button.
 *
 * @author Andrey Plotnikov
 */
public interface ConsoleButton extends View<ConsoleButton.ActionDelegate> {
  /**
   * Changes check status of button.
   *
   * @param isChecked check status of button that needs to be applied
   */
  void setCheckedStatus(boolean isChecked);

  interface ActionDelegate {
    /** Performs some actions in response to user's clicking on the button panel. */
    void onButtonClicked();
  }
}
