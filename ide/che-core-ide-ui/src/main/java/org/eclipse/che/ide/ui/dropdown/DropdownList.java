/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui.dropdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.google.gwt.user.client.ui.PopupPanel.AnimationType.ROLL_DOWN;

/** Dropdown list widget. */
public class DropdownList extends Composite {

    private static final DropdownListUiBinder  UI_BINDER = GWT.create(DropdownListUiBinder.class);
    private static final DropdownListResources RESOURCES = GWT.create(DropdownListResources.class);

    /** Maximum amount of items that should visible in dropdown list without scrolling. */
    private static final int MAX_VISIBLE_ITEMS  = 7;
    /** Amount of pixels reserved for displaying one item in the dropdown list. */
    private static final int ITEM_WIDGET_HEIGHT = 22;

    private static final int DEFAULT_WIDGET_WIDTH_PX = 200;

    private final PopupPanel dropdownPopupPanel;
    private final FlowPanel  dropdownContentPanel;
    private final Widget     emptyStateWidget;

    private final Map<DropdownListItem, Widget> itemListWidgets;
    private final Map<DropdownListItem, Widget> itemHeaderWidgets;

    @UiField
    SimplePanel selectedItemPanel;
    @UiField
    SimplePanel dropButtonPanel;

    private SelectionHandler selectionHandler;
    private DropdownListItem selectedItem;

    /** Stores true if dropdown panels's width should be always synchronized with the list header's width. */
    private boolean widthsSynced;

    /** Creates new dropdown widget. */
    public DropdownList() {
        this(new Label("---"));
    }

    /**
     * Creates new dropdown widget.
     * Uses the given {@code emptyStateText} for displaying an empty list's state.
     */
    public DropdownList(String emptyStateText) {
        this(new Label(emptyStateText));
    }

    /**
     * Creates new dropdown widget.
     * Uses the given {@code emptyStateWidget} for displaying an empty list's state.
     */
    public DropdownList(Widget emptyStateWidget) {
        itemListWidgets = new HashMap<>();
        itemHeaderWidgets = new HashMap<>();

        this.emptyStateWidget = emptyStateWidget;

        initWidget(UI_BINDER.createAndBindUi(this));

        dropButtonPanel.getElement().appendChild(RESOURCES.expansionImage().getSvg().getElement());

        dropdownContentPanel = new FlowPanel();
        dropdownContentPanel.ensureDebugId("dropdown-list-content-panel");

        dropdownPopupPanel = new PopupPanel(true);
        dropdownPopupPanel.removeStyleName("gwt-PopupPanel");
        dropdownPopupPanel.addStyleName(RESOURCES.dropdownListCss().itemsPanel());
        dropdownPopupPanel.setAnimationEnabled(true);
        dropdownPopupPanel.addAutoHidePartner(getElement());
        dropdownPopupPanel.setAnimationType(ROLL_DOWN);
        dropdownPopupPanel.add(new ScrollPanel(dropdownContentPanel));

        attachEventHandlers();
        setSelectedItem(null);

        setWidth(DEFAULT_WIDGET_WIDTH_PX + "px");
    }

    /**
     * {@inheritDoc}
     * <p><b>Note:</b> this method sets the list header's width only.
     * Use {@link #setDropdownPanelWidth(String)} to set the dropdown panels's width.
     *
     * @see #setDropdownPanelWidth(String)
     */
    @Override
    public void setWidth(String width) {
        super.setWidth(width);
    }

    /**
     * Sets the dropdown panels's width.
     * If it's not set explicitly then it will be calculated depending on the children width.
     *
     * @param width
     *         the dropdown panels's new width, in CSS units (e.g. "10px", "1em")
     * @see #setWidth(String)
     */
    public void setDropdownPanelWidth(String width) {
        dropdownPopupPanel.setWidth(width);
    }

    /** Set the dropdown panels's width should be always synchronized with the list header's width. */
    public void syncWidths() {
        widthsSynced = true;
        Window.addResizeHandler(e -> setDropdownPanelWidth(getElement().getClientWidth() + "px"));
    }

    /** Adapts dropdown panel's height depending on the amount of child items. */
    private void adaptDropDownPanelHeight() {
        final int visibleRowsCount = Math.min(MAX_VISIBLE_ITEMS, itemListWidgets.size());
        final int dropdownPanelHeight = ITEM_WIDGET_HEIGHT * visibleRowsCount;

        dropdownPopupPanel.setHeight(dropdownPanelHeight + "px");
    }

    private void attachEventHandlers() {
        selectedItemPanel.addDomHandler(e -> toggleListVisibility(), ClickEvent.getType());
        emptyStateWidget.addDomHandler(e -> toggleListVisibility(), ClickEvent.getType());
        dropButtonPanel.addDomHandler(e -> toggleListVisibility(), ClickEvent.getType());
    }

    private void toggleListVisibility() {
        if (dropdownPopupPanel.isShowing()) {
            dropdownPopupPanel.hide();
        } else {
            dropdownPopupPanel.showRelativeTo(this);

            if (widthsSynced) {
                setDropdownPanelWidth(getElement().getClientWidth() + "px");
            }
        }
    }

    private void checkListEmptiness() {
        if (itemListWidgets.isEmpty()) {
            setSelectedItem(null);
        }
    }

    /** Sets the given {@code handler} to notify it about changing selected item. */
    public void setSelectionHandler(SelectionHandler handler) {
        selectionHandler = handler;
    }

    /** Returns the currently selected item or {@code null} if none. */
    @Nullable
    public DropdownListItem getSelectedItem() {
        return selectedItem;
    }

    /** Set the given item as currently selected. Sets empty state widget if {@code null} were provided. */
    private void setSelectedItem(@Nullable DropdownListItem item) {
        selectedItem = item;
        selectedItemPanel.setWidget(item != null ? itemHeaderWidgets.get(item) : emptyStateWidget);
    }

    /**
     * Add the given {@code item} to the top of the list.
     *
     * @param item
     *         item to add to the list
     * @param renderer
     *         renderer provides widgets for representing the given {@code item} in the list
     */
    public void addItem(DropdownListItem item, DropdownListItemRenderer renderer) {
        final Widget headerWidget = renderer.renderHeaderWidget();
        final Widget listWidget = new SimplePanel(renderer.renderListWidget());

        itemHeaderWidgets.put(item, headerWidget);
        itemListWidgets.put(item, listWidget);

        headerWidget.addHandler(e -> toggleListVisibility(), ClickEvent.getType());

        listWidget.addStyleName(RESOURCES.dropdownListCss().listItem());
        listWidget.addDomHandler(e -> {
            setSelectedItem(item);
            dropdownPopupPanel.hide();

            if (selectionHandler != null) {
                selectionHandler.onItemSelected(item);
            }
        }, ClickEvent.getType());

        dropdownContentPanel.insert(listWidget, 0);
        adaptDropDownPanelHeight();
        setSelectedItem(item);
    }

    /**
     * Short for quick adding text value to the list.
     *
     * @param value
     *         text value to add to the list
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

        if (widget != null) {
            dropdownContentPanel.remove(widget);
        }

        itemHeaderWidgets.remove(item);

        if (!itemListWidgets.isEmpty()) {
            // set any available item as currently selected
            setSelectedItem(itemListWidgets.entrySet().iterator().next().getKey());
        } else {
            checkListEmptiness();
        }

        adaptDropDownPanelHeight();
    }

    /** Clear the list. */
    public void clear() {
        itemListWidgets.clear();
        itemHeaderWidgets.clear();
        dropdownContentPanel.clear();

        adaptDropDownPanelHeight();
        checkListEmptiness();
    }

    interface DropdownListUiBinder extends UiBinder<Widget, DropdownList> {
    }

    public interface SelectionHandler {
        /**
         * Called when currently selected item has been changed.
         *
         * @param item
         *         currently selected item
         */
        void onItemSelected(DropdownListItem item);
    }

    static {
        RESOURCES.dropdownListCss().ensureInjected();
    }
}
