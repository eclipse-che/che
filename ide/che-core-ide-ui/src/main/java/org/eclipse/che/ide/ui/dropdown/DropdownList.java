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
package org.eclipse.che.ide.ui.dropdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.commons.annotation.Nullable;

/** Dropdown list widget. */
public class DropdownList extends Composite {

  private static final DropdownListUiBinder UI_BINDER = GWT.create(DropdownListUiBinder.class);
  private static final DropdownListResources RESOURCES = GWT.create(DropdownListResources.class);

  /** Default width of this widget. */
  private static final int DEFAULT_WIDTH_PX = 200;

  private final DropdownMenu menu;
  private final Widget emptyStateWidget;

  private final Map<DropdownListItem, Widget> itemListWidgets;
  private final Map<DropdownListItem, Widget> itemHeaderWidgets;

  @UiField SimplePanel selectedItemPanel;
  @UiField SimplePanel dropdownMenuButton;

  private SelectionHandler selectionHandler;
  private DropdownListItem selectedItem;

  /**
   * Creates new dropdown list widget.
   *
   * @param syncWidths specifies whether the dropdown menu's width always should be the same as this
   *     dropdown list's width
   */
  public DropdownList(boolean syncWidths) {
    this(new Label("---"), syncWidths);
  }

  /**
   * Creates new dropdown list widget.
   *
   * @param emptyStateText text that should be used for displaying an empty list's state
   * @param syncWidths specifies whether the dropdown menu's width always should be the same as this
   *     dropdown list's width
   */
  public DropdownList(String emptyStateText, boolean syncWidths) {
    this(new Label(emptyStateText), syncWidths);
  }

  /**
   * Creates new dropdown list widget.
   *
   * @param emptyStateWidget widget that should be used for displaying an empty list's state
   * @param syncWidths specifies whether the dropdown menu's width always should be the same as this
   *     dropdown list's width
   */
  public DropdownList(Widget emptyStateWidget, boolean syncWidths) {
    this.emptyStateWidget = emptyStateWidget;
    itemListWidgets = new HashMap<>();
    itemHeaderWidgets = new HashMap<>();

    initWidget(UI_BINDER.createAndBindUi(this));

    setWidth(DEFAULT_WIDTH_PX + "px");

    menu = new DropdownMenu(this, syncWidths);
    dropdownMenuButton.getElement().appendChild(RESOURCES.expansionImage().getSvg().getElement());

    addDomHandler(e -> menu.toggleMenuVisibility(), ClickEvent.getType());

    setSelectedItem(null);
  }

  /**
   * {@inheritDoc}
   *
   * <p><b>Note:</b> this method sets the list header's width only. Use {@link
   * #setDropdownMenuWidth(String)} to set the dropdown menu's width.
   *
   * @see #setDropdownMenuWidth(String)
   */
  @Override
  public void setWidth(String width) {
    super.setWidth(width);
  }

  /**
   * Sets the dropdown menu's width. If it's not set explicitly then it will be calculated depending
   * on the content's width.
   *
   * @param width the dropdown menu's width, in CSS units (e.g. "10px", "1em")
   * @see #setWidth(String)
   */
  public void setDropdownMenuWidth(String width) {
    menu.setWidth(width);
  }

  private void checkListEmptiness() {
    if (itemListWidgets.isEmpty()) {
      setSelectedItem(null);
    }
  }

  /** Sets the given {@link SelectionHandler}. */
  public void setSelectionHandler(SelectionHandler handler) {
    selectionHandler = handler;
  }

  /** Returns the currently selected item or {@code null} if none. */
  @Nullable
  public DropdownListItem getSelectedItem() {
    return selectedItem;
  }

  /** Set the given item as currently selected. Sets empty state widget if {@code null} provided. */
  private void setSelectedItem(@Nullable DropdownListItem item) {
    selectedItem = item;
    selectedItemPanel.setWidget(item != null ? itemHeaderWidgets.get(item) : emptyStateWidget);
  }

  /**
   * Add the given {@code item} to the top of the list.
   *
   * @param item item to add to the list
   * @param renderer renderer provides widgets for representing the given {@code item} in the list
   */
  public void addItem(DropdownListItem item, DropdownListItemRenderer renderer) {
    final Widget headerWidget = renderer.renderHeaderWidget();
    final Widget listWidget = new SimplePanel(renderer.renderListWidget());

    itemHeaderWidgets.put(item, headerWidget);
    itemListWidgets.put(item, listWidget);

    headerWidget.addHandler(e -> menu.toggleMenuVisibility(), ClickEvent.getType());

    listWidget.addStyleName(RESOURCES.dropdownListCss().listItem());
    listWidget.addDomHandler(
        e -> {
          setSelectedItem(item);
          menu.hide();

          if (selectionHandler != null) {
            selectionHandler.onItemSelected(item);
          }
        },
        ClickEvent.getType());

    menu.addWidget(listWidget);

    setSelectedItem(item);
  }

  /**
   * Short for quick adding text value to the list.
   *
   * @param value text value to add to the list
   * @return added item which wraps the given {@code value}
   */
  public BaseListItem<String> addItem(String value) {
    BaseListItem<String> item = new BaseListItem<>(value);

    addItem(item, new StringItemRenderer(item));

    return item;
  }

  /** Remove item from the list. */
  public void removeItem(DropdownListItem item) {
    final Widget widget = itemListWidgets.remove(item);

    if (widget == null) {
      return;
    }

    menu.removeWidget(widget);

    itemHeaderWidgets.remove(item);

    if (itemListWidgets.isEmpty()) {
      checkListEmptiness();
    } else if (item.equals(getSelectedItem())) {
      setSelectedItem(itemListWidgets.keySet().iterator().next());
    }
  }

  /** Returns all list's items. */
  public Set<DropdownListItem> getItems() {
    return itemListWidgets.keySet();
  }

  /** Clear the list. */
  public void clear() {
    itemListWidgets.clear();
    itemHeaderWidgets.clear();

    menu.removeAllWidgets();

    checkListEmptiness();
  }

  interface DropdownListUiBinder extends UiBinder<Widget, DropdownList> {}

  public interface SelectionHandler {
    /**
     * Called when currently selected item has been changed.
     *
     * @param item currently selected item
     */
    void onItemSelected(DropdownListItem item);
  }

  static {
    RESOURCES.dropdownListCss().ensureInjected();
  }
}
