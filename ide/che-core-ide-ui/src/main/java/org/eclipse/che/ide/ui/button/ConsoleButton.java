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
