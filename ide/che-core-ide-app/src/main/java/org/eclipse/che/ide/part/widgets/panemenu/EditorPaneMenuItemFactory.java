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
package org.eclipse.che.ide.part.widgets.panemenu;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;

/**
 * The factory creates instances of {@link EditorPaneMenuItem} to display items of editor pane menu.
 *
 * @author Roman Nikitenko
 */
public class EditorPaneMenuItemFactory {

  /**
   * Creates implementation of {@link EditorPaneMenuItem} to display some {@code action} as item of
   * editor pane menu.
   *
   * @param action action to display as item of editor pane menu.
   * @return an instance of {@link EditorPaneMenuItem}
   */
  public EditorPaneMenuItem<Action> createMenuItem(@NotNull Action action) {
    return new PaneMenuActionItemWidget(action);
  }

  /**
   * Creates implementation of {@link EditorPaneMenuItem} to display some {@code tabItem} as item of
   * editor pane menu.
   *
   * @param tabItem item of opened file to display as item of editor pane menu.
   * @return an instance of {@link EditorPaneMenuItem}
   */
  public EditorPaneMenuItem<TabItem> createMenuItem(@NotNull TabItem tabItem) {
    return new PaneMenuTabItemWidget(tabItem);
  }
}
