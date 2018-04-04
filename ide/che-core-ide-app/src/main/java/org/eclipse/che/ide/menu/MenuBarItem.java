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
package org.eclipse.che.ide.menu;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;
import com.google.inject.Provider;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.ActionSelectedHandler;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.ui.toolbar.MenuLockLayer;
import org.eclipse.che.ide.ui.toolbar.PopupMenu;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.ui.toolbar.Utils;

/**
 * Menu bar is implementation of Menu interface and represents a visual component.
 *
 * @author Vitaliy Gulyy
 * @author Dmitry Shnurenko
 */
public class MenuBarItem implements ActionSelectedHandler {

  private final ActionGroup group;
  private final ActionManager actionManager;
  private final Provider<PerspectiveManager> managerProvider;
  private final PresentationFactory presentationFactory;
  /** Working variable: is need to store pressed state. */
  boolean pressed = false;
  /** Visual element which is table cell. */
  private Element element;
  /** Enabled or disabled state */
  private boolean enabled = true;

  private boolean hasVisibleItems = true;
  private ActionSelectedHandler actionSelectedHandler;
  private KeyBindingAgent keyBindingAgent;
  private MenuResources.Css css;
  /** Working variable: is needs to store opened Popup menu. */
  private PopupMenu popupMenu;

  /** Title of Menu Bar Item */
  private String title;

  public MenuBarItem(
      ActionGroup group,
      ActionManager actionManager,
      Provider<PerspectiveManager> managerProvider,
      PresentationFactory presentationFactory,
      Element element,
      ActionSelectedHandler handler,
      KeyBindingAgent keyBindingAgent,
      MenuResources.Css css) {
    this.group = group;
    this.actionManager = actionManager;
    this.managerProvider = managerProvider;
    this.presentationFactory = presentationFactory;
    this.element = element;
    this.actionSelectedHandler = handler;
    this.keyBindingAgent = keyBindingAgent;
    this.css = css;
    Presentation presentation = presentationFactory.getPresentation(group);
    title = presentation.getText();
    element.setInnerText(presentation.getText());
    setEnabled(Utils.hasVisibleChildren(group, presentationFactory, actionManager));
  }

  /** Close opened Popup Menu. */
  public void closePopupMenu() {
    popupMenu.closePopup();
  }

  /** {@inheritDoc} */
  public boolean isEnabled() {
    return enabled;
  }

  /** {@inheritDoc} */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    updateEnabledState();
  }

  /** Mouse Down handler */
  public boolean onMouseDown() {
    if (enabled && hasVisibleItems) {
      element.setClassName(css.menuBarItemSelected());
      pressed = true;
      actionSelectedHandler.onActionSelected(group);
      return true;
    }

    return false;
  }

  /** Mouse Out Handler */
  public void onMouseOut() {
    if (pressed) {
      return;
    }

    if (enabled && hasVisibleItems) {
      element.setClassName(css.menuBarItem());
    } else {
      element.setClassName(css.menuBarItemDisabled());
    }
  }

  /** Mouse Over Handler */
  public void onMouseOver() {
    if (pressed) {
      return;
    }

    if (enabled && hasVisibleItems) {
      element.setClassName(css.menuBarItemOver());
    }
  }

  /**
   * Open sub Popup Menu
   *
   * @param menuLockLayer - lock layer which will receive PopupMenu visual component and
   */
  public void openPopupMenu(MenuLockLayer menuLockLayer) {
    int x = element.getAbsoluteLeft();
    int y = 0;
    popupMenu =
        new PopupMenu(
            group,
            actionManager,
            managerProvider,
            presentationFactory,
            menuLockLayer,
            this,
            keyBindingAgent,
            "topmenu/" + title);
    menuLockLayer.add(popupMenu, x, y);
  }

  /** Reset visual state of Menu Bar Item to default. */
  public void setNormalState() {
    pressed = false;
    element.setClassName(css.menuBarItem());
  }

  private void updateEnabledState() {
    pressed = false;
    if (enabled && hasVisibleItems) {
      element.setClassName(css.menuBarItem());
    } else {
      element.setClassName(css.menuBarItemDisabled());
    }

    UIObject.ensureDebugId(element, "MenuItem/" + actionManager.getId(group) + "-" + enabled);
  }

  /** {@inheritDoc} */
  @Override
  public void onActionSelected(Action action) {
    setNormalState();
    actionSelectedHandler.onActionSelected(action);
  }

  public String getTitle() {
    return title;
  }

  public void update() {
    setEnabled(Utils.hasVisibleChildren(group, presentationFactory, actionManager));
  }
}
