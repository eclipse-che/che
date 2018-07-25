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
package org.eclipse.che.ide.ui.dropdown;

import static com.google.gwt.user.client.ui.PopupPanel.AnimationType.ROLL_DOWN;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

/** Menu for {@link DropdownList}. */
class DropdownMenu extends PopupPanel {

  private static final DropdownListResources RESOURCES = GWT.create(DropdownListResources.class);

  /** Maximum amount of widgets that should visible without scrolling. */
  private static final int MAX_VISIBLE_WIDGETS = 7;
  /** Amount of pixels reserved for displaying one widget. */
  private static final int CHILD_WIDGET_HEIGHT = 22;

  private final DropdownList dropdownList;
  private final FlowPanel contentPanel;

  /**
   * Stores true if dropdown menu's width should be always synchronized with the dropdown header's
   * width.
   */
  private boolean widthsSynced;

  /**
   * Creates new dropdown menu.
   *
   * @param dropdownList a dropdown list to which the menu should be bound
   * @param syncWidths specifies whether this dropdown menu's width always should be the same as the
   *     given {@code dropdownList}'s width
   */
  DropdownMenu(DropdownList dropdownList, boolean syncWidths) {
    super(true);

    this.dropdownList = dropdownList;
    widthsSynced = syncWidths;
    if (syncWidths) {
      Window.addResizeHandler(e -> setWidth(dropdownList.getElement().getClientWidth() + "px"));
    }

    removeStyleName("gwt-PopupPanel");
    addStyleName(RESOURCES.dropdownListCss().menu());

    addAutoHidePartner(dropdownList.getElement());
    setAnimationEnabled(true);
    setAnimationType(ROLL_DOWN);

    contentPanel = new FlowPanel();
    contentPanel.ensureDebugId("dropdown-list-content-panel");

    add(new ScrollPanel(contentPanel));
  }

  void toggleMenuVisibility() {
    if (isShowing()) {
      hide();
    } else {
      if (widthsSynced) {
        setWidth(dropdownList.getElement().getClientWidth() + "px");
      }

      showRelativeTo(dropdownList);
    }
  }

  void addWidget(IsWidget widget) {
    contentPanel.insert(widget, 0);
    adaptMenuHeight();
  }

  void removeWidget(IsWidget widget) {
    contentPanel.remove(widget);
    adaptMenuHeight();
  }

  void removeAllWidgets() {
    contentPanel.clear();
    adaptMenuHeight();
  }

  /** Adapts menu's height depending on the amount of the child widgets. */
  private void adaptMenuHeight() {
    final int visibleRowsCount = Math.min(MAX_VISIBLE_WIDGETS, contentPanel.getWidgetCount());
    final int dropdownPanelHeight = CHILD_WIDGET_HEIGHT * visibleRowsCount;

    setHeight(dropdownPanelHeight + "px");
  }
}
