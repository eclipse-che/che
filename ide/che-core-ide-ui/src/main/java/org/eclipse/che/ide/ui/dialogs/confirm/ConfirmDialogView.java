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

import com.google.gwt.user.client.ui.IsWidget;

/**
 * The view interface for the confirmation dialog component.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface ConfirmDialogView {

  /** Sets the action delegate. */
  void setDelegate(ActionDelegate delegate);

  /** Displays the dialog window. Sets "accept" button in the focus. */
  void showDialog();

  /** Closes the dialog window. */
  void closeDialog();

  /** Fill the window with its content. */
  void setContent(IsWidget content);

  /** Sets the window title. */
  void setTitleCaption(String title);

  /** Overwrites label of Ok button */
  void setOkButtonLabel(String label);

  /** Overwrites label of Cancel button */
  void setCancelButtonLabel(String label);

  /** Returns {@code true} if OK button is in the focus and {@code false} - otherwise. */
  boolean isOkButtonInFocus();

  /** Returns {@code true} if Cancel button is in the focus and {@code false} - otherwise. */
  boolean isCancelButtonInFocus();

  /** The interface for the action delegate. */
  interface ActionDelegate {

    /** Defines what's done when the user clicks cancel. */
    void cancelled();

    /** Defines what's done when the user clicks OK. */
    void accepted();

    /** Performs any actions appropriate in response to the user having clicked the Enter key. */
    void onEnterClicked();
  }
}
