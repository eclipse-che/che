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
package org.eclipse.che.ide.api.parts;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * Part Stack is tabbed layout element, containing Parts.
 *
 * @author Nikolay Zamosenchuk
 * @author Vitaliy Guliy
 */
public interface PartStack extends Presenter {

  /** State of the part stack. */
  enum State {

    /**
     * The default state when the part stack is visible. Part stack can be maximized, collapsed or
     * hidden.
     */
    NORMAL,

    /**
     * Hidden part stack can not be maximized or collapsed. It only can be displayed using show()
     * methd.
     */
    HIDDEN,

    /** Part stack is maximized. In this state it can be restored or become hidden. */
    MAXIMIZED,

    /**
     * Part stack is minimized while another one is maximized. The state will be changed to NORMAL
     * after restoring the perspective.
     */
    MINIMIZED
  }

  /**
   * Change the focused state of the PartStack to desired value
   *
   * @param focused
   */
  void setFocus(boolean focused);

  /**
   * Add part to the PartStack. To immediately show part, you must call <code>setActivePart()</code>
   * .
   *
   * @param part
   */
  void addPart(PartPresenter part);

  /**
   * Add part to the PartStack with position constraint.
   *
   * @param part
   * @param constraint
   */
  void addPart(PartPresenter part, Constraints constraint);

  /**
   * Ask if PartStack contains given Part.
   *
   * @param part
   * @return
   */
  boolean containsPart(PartPresenter part);

  /**
   * Get active Part. Active is the part that is currently displayed on the screen
   *
   * @return
   */
  PartPresenter getActivePart();

  /**
   * Activate given part (force show it on the screen). If part wasn't previously added to the
   * PartStack or has been removed, that method has no effect.
   *
   * @param part
   */
  void setActivePart(@NotNull PartPresenter part);

  /**
   * Returns the state of the perspective.
   *
   * @return perspective state
   */
  State getPartStackState();

  /** Shows the part stack. */
  void show();

  /** Hides the part stack. */
  void hide();

  /** Maximizes the part stack. */
  void maximize();

  /**
   * Minimizes the part stack. The part state can be restored to NORMAL state when restoring the
   * perspective.
   */
  void minimize();

  /** Restores the part stack and the perspective to the default state. */
  void restore();

  /**
   * Remove given part from PartStack.
   *
   * @param part
   */
  void removePart(PartPresenter part);

  void openPreviousActivePart();

  /** Update part stack reference */
  void updateStack();

  /**
   * Get all parts, opened in this stack.
   *
   * @return the parts list
   */
  List<? extends PartPresenter> getParts();

  /**
   * Sets action delegate.
   *
   * @param delegate action delegate
   */
  void setDelegate(ActionDelegate delegate);

  interface ActionDelegate {

    /**
     * Requests the delegate to maximize the part stack.
     *
     * @param partStack part stack
     */
    void onMaximize(PartStack partStack);

    /**
     * Requests the delegate to restore part stack state.
     *
     * @param partStack part stack
     */
    void onRestore(PartStack partStack);
  }
}
