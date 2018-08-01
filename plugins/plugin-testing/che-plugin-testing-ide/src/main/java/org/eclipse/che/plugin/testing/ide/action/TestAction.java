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
package org.eclipse.che.plugin.testing.ide.action;

import org.eclipse.che.ide.api.action.DefaultActionGroup;

/**
 * Interface for defining test framework IDE actions for the test runner. All test framework
 * implementations should implement this interface in order to appear testing actions in menus
 *
 * @author Mirage Abeysekara
 */
public interface TestAction {

  /**
   * This method get called when the extension is loading and adding the menu items for the main
   * menu.
   *
   * @param testMainMenu Main menu item for test actions
   */
  void addMainMenuItems(DefaultActionGroup testMainMenu);

  /**
   * This method get called when the extension is loading and adding the menu items for the project
   * explorer context menu.
   *
   * @param testContextMenu Context menu item for test actions
   */
  void addContextMenuItems(DefaultActionGroup testContextMenu);
}
