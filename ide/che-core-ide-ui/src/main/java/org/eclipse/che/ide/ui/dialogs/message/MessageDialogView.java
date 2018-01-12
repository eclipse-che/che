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

import com.google.gwt.user.client.ui.IsWidget;
import javax.validation.constraints.NotNull;

/**
 * The view interface for the message dialog component.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface MessageDialogView {

  /** Sets the action delegate. */
  void setDelegate(@NotNull ActionDelegate delegate);

  /** Displays the dialog window. */
  void showDialog();

  /** Closes the dialog window. */
  void closeDialog();

  /** Fill the window with its content. */
  void setContent(@NotNull IsWidget content);

  /** Sets the window title. */
  void setTitle(@NotNull String title);

  /** Sets the Confirm button text. */
  void setConfirmButtonText(@NotNull String text);

  /** The interface for the action delegate. */
  public interface ActionDelegate {

    /** Defines what's done when the user clicks OK. */
    void accepted();
  }
}
