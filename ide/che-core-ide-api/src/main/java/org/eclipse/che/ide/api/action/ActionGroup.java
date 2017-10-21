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
package org.eclipse.che.ide.api.action;

import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Evgen Vidolob */
public abstract class ActionGroup extends BaseAction {

  private boolean popup;

  /**
   * Creates a new <code>ActionGroup</code> with shortName set to <code>null</code> and popup set to
   * false.
   */
  public ActionGroup() {
    this(null, false);
  }

  /**
   * Creates a new <code>ActionGroup</code> with the specified shortName and popup.
   *
   * @param shortName Text that represents a short name for this action group
   * @param popup <code>true</code> if this group is a popup, <code>false</code> otherwise
   */
  public ActionGroup(String shortName, boolean popup) {
    super(shortName);
    setPopup(popup);
  }

  public ActionGroup(String text, String description, SVGResource svgIcon) {
    super(text, description, svgIcon);
  }

  /** This method can be called in popup menus if {@link #canBePerformed()} is true */
  @Override
  public void actionPerformed(ActionEvent e) {}

  @Override
  public void update(ActionEvent e) {
    super.update(e);
  }

  /** @return true if {@link #actionPerformed(ActionEvent)} should be called */
  public boolean canBePerformed() {
    return false;
  }

  /**
   * Returns the type of the group.
   *
   * @return <code>true</code> if the group is a popup, <code>false</code> otherwise
   */
  public boolean isPopup() {
    return popup;
  }

  /**
   * Sets the type of the group.
   *
   * @param popup If <code>true</code> the group will be shown as a popup in menus
   */
  public final void setPopup(boolean popup) {
    this.popup = popup;
  }

  /**
   * Returns the children of the group.
   *
   * @return An array representing children of this group. All returned children must be not <code>
   *     null</code>.
   */
  public abstract Action[] getChildren(ActionEvent e);

  public boolean hideIfNoVisibleChildren() {
    return false;
  }

  public boolean disableIfNoVisibleChildren() {
    return true;
  }
}
