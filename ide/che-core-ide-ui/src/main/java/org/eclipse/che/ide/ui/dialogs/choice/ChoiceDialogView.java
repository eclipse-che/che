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
package org.eclipse.che.ide.ui.dialogs.choice;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * The view interface for the choice dialog component.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface ChoiceDialogView {

  /** Sets the action delegate. */
  void setDelegate(ActionDelegate delegate);

  /** Displays the dialog window. Sets "first-choice" button in the focus. */
  void showDialog();

  /** Closes the dialog window. */
  void closeDialog();

  /** Fill the window with its content. */
  void setContent(IsWidget content);

  /** Sets the window title. */
  void setTitleCaption(String title);

  /** Sets the text of the first choice. */
  void setFirstChoiceLabel(String firstChoiceLabel);

  /** Sets the text of the second choice. */
  void setSecondChoiceLabel(String secondChoiceLabel);

  /** Sets the text of the third choice. */
  void setThirdChoiceLabel(String thirdChoiceLabel);

  /** Returns {@code true} if first button is in the focus and {@code false} - otherwise. */
  boolean isFirstButtonInFocus();

  /** Returns {@code true} if second button is in the focus and {@code false} - otherwise. */
  boolean isSecondButtonInFocus();

  /** Returns {@code true} if third button is in the focus and {@code false} - otherwise. */
  boolean isThirdButtonInFocus();

  /** The interface for the action delegate. */
  public interface ActionDelegate {

    /** Defines what's done when the user clicks the first choice. */
    void firstChoiceClicked();

    /** Defines what's done when the user clicks the second choice. */
    void secondChoiceClicked();

    /** Defines what's done when the user clicks the third choice. */
    void thirdChoiceClicked();

    /** Performs any actions appropriate in response to the user having clicked the Enter key. */
    void onEnterClicked();
  }
}
