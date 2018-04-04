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
package org.eclipse.che.ide.command.toolbar.selector;

import org.eclipse.che.ide.api.mvp.View;

/** Interface for panel selector menu button. */
public interface PanelSelectorView extends View<PanelSelectorView.ActionDelegate> {

  /** State of the button. Icon on the menu button depends directly on the state. */
  enum State {
    LEFT,
    LEFT_BOTTOM,
    FULL_EDITOR,
    BOTTOM,
    RIGHT,
    LEFT_RIGHT_BOTTOM
  }

  /**
   * Sets new state for the button and updates the icon.
   *
   * @param state new state
   */
  void setState(State state);

  /** Shows selector popup. */
  void showPopup();

  /** Hides selector popup. */
  void hidePopup();

  interface ActionDelegate {

    /** Handle clicking on selector button. */
    void onButtonClicked();

    /** Selected button displaying left panel. */
    void onSelectorLeftClicked();

    /** Selected button displaying left and bottom panels. */
    void onSelectorLeftBottomClicked();

    /** Selected button displaying maximized editor. */
    void onSelectorFullEditorClicked();

    /** Selected button displaying only bottom panel. */
    void onSelectorBottomClicked();

    /** Selected button displaying only right panel. */
    void onSelectorRightClicked();

    /** Selected button displaying left, bottom and right panels. */
    void onSelectorLeftRightBottomClicked();
  }
}
