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

/** Drop down list widget. */
public class DropDownList extends Composite {

    private static final DropDownListUiBinder  UI_BINDER = GWT.create(DropDownListUiBinder.class);
    private static final DropDownListResources RESOURCES = GWT.create(DropDownListResources.class);

    /** Maximum amount of items that should visible in drop down list without scrolling. */
    private static final int MAX_VISIBLE_ITEMS  = 7;
    /** Amount of pixels reserved for displaying one item in the drop down list. */
    private static final int ITEM_WIDGET_HEIGHT = 22;

    private static final int DEFAULT_WIDGET_WIDTH_PX = 200;

    private final PopupPanel dropDownPopupPanel;
    private final FlowPanel  dropDownContentPanel;
    private final Widget     emptyStateWidget;

    private final Map<DropDownListItem, Widget>                   itemsWidgets;
    private final Map<DropDownListItem, DropDownListItemRenderer> itemsRenderers;

    @UiField
    SimplePanel selectedItemPanel;
    @UiField
    SimplePanel dropButtonPanel;

    private SelectionHandler selectionHandler;
    private DropDownListItem selectedItem;

    /** Creates new drop down widget. */
    public DropDownList() {
        this(new Label("---"));
    }

    /**
     * Creates new drop down widget.
     * Uses the given {@code emptyStateText} for displaying an empty list's state.
     */
    public DropDownList(String emptyStateText) {
        this(new Label(emptyStateText));
    }

    /**
     * Creates new drop down widget.
     * Uses the given {@code emptyStateWidget} for displaying an empty list's state.
     */
    public DropDownList(Widget emptyStateWidget) {
        itemsWidgets = new HashMap<>();
        itemsRenderers = new HashMap<>();

        this.emptyStateWidget = emptyStateWidget;

        initWidget(UI_BINDER.createAndBindUi(this));

        dropButtonPanel.getElement().appendChild(RESOURCES.expansionImage().getSvg().getElement());
        dropButtonPanel.addStyleName(RESOURCES.dropDownListCss().dropButton());

        dropDownContentPanel = new FlowPanel();

        dropDownPopupPanel = new PopupPanel(true);
        dropDownPopupPanel.removeStyleName("gwt-PopupPanel");
        dropDownPopupPanel.addStyleName(RESOURCES.dropDownListCss().itemsPanel());
        dropDownPopupPanel.setAnimationEnabled(true);
        dropDownPopupPanel.setAnimationType(ROLL_DOWN);
        dropDownPopupPanel.add(new ScrollPanel(dropDownContentPanel));

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
     *
     * @param width
     *         the dropdown panels's new width, in CSS units (e.g. "10px", "1em")
     * @see #setWidth(String)
     */
    public void setDropdownPanelWidth(String width) {
        dropDownPopupPanel.setWidth(width);
    }

    /** Adapts drop down panel's height depending on the amount of child items. */
    private void adaptDropDownPanelHeight() {
        final int visibleRowsCount = Math.min(MAX_VISIBLE_ITEMS, itemsWidgets.size());
        final int dropDownPanelHeight = ITEM_WIDGET_HEIGHT * visibleRowsCount;

        dropDownPopupPanel.setHeight(dropDownPanelHeight + "px");
    }

    private void attachEventHandlers() {
        selectedItemPanel.addDomHandler(event -> dropDownPopupPanel.showRelativeTo(DropDownList.this), ClickEvent.getType());
        emptyStateWidget.addDomHandler(event -> dropDownPopupPanel.showRelativeTo(DropDownList.this), ClickEvent.getType());
        dropButtonPanel.addDomHandler(event -> dropDownPopupPanel.showRelativeTo(DropDownList.this), ClickEvent.getType());
    }

    private void checkListEmptiness() {
        if (itemsWidgets.isEmpty()) {
            setSelectedItem(null);
        }
    }

    /** Sets the given {@code handler} to notify it about changing selected item. */
    public void setSelectionHandler(SelectionHandler handler) {
        selectionHandler = handler;
    }

    /** Returns the currently selected item or {@code null} if none. */
    @Nullable
    public DropDownListItem getSelectedItem() {
        return selectedItem;
    }

    /** Set the given item as currently selected. Sets empty state widget if {@code null} were provided. */
    private void setSelectedItem(@Nullable DropDownListItem item) {
        final Widget headerWidget;

        if (item != null) {
            headerWidget = itemsRenderers.get(item).renderHeaderWidget();
            headerWidget.addDomHandler(event -> dropDownPopupPanel.showRelativeTo(DropDownList.this), ClickEvent.getType());
        } else {
            headerWidget = emptyStateWidget;
        }

        selectedItem = item;
        selectedItemPanel.setWidget(headerWidget);
    }

    /**
     * Add the given {@code item} to the top of the list.
     *
     * @param item
     *         item to add to the list
     * @param renderer
     *         renderer provides widgets for representing the given {@code item} in the list
     */
    public void addItem(DropDownListItem item, DropDownListItemRenderer renderer) {
        final Widget itemWidget = new SimplePanel(renderer.renderListWidget());

        itemsWidgets.put(item, itemWidget);
        itemsRenderers.put(item, renderer);

        itemWidget.addStyleName(RESOURCES.dropDownListCss().listItem());
        itemWidget.addDomHandler(event -> {
            setSelectedItem(item);
            dropDownPopupPanel.hide();

            if (selectionHandler != null) {
                selectionHandler.onItemSelected(item);
            }
        }, ClickEvent.getType());

        dropDownContentPanel.insert(itemWidget, 0);
        adaptDropDownPanelHeight();
        setSelectedItem(item);
    }

    /**
     * Shorthand for quick adding text value to the list.
     *
     * @param value
     *         text value to add to the list
     * @return added item which wraps the given {@code value}
     */
    public BaseListItem<String> addItem(String value) {
        final BaseListItem<String> item = new BaseListItem<>(value);
        final StringItemRenderer renderer = new StringItemRenderer(item);

        addItem(item, renderer);

        return item;
    }

    /** Remove item from the list. */
    public void removeItem(DropDownListItem item) {
        final Widget widget = itemsWidgets.remove(item);

        if (widget != null) {
            dropDownContentPanel.remove(widget);
        }

        itemsRenderers.remove(item);

        if (!itemsWidgets.isEmpty()) {
            // set any available item as currently selected
            setSelectedItem(itemsWidgets.entrySet().iterator().next().getKey());
        } else {
            checkListEmptiness();
        }

        adaptDropDownPanelHeight();
    }

    /** Clear the list. */
    public void clear() {
        itemsWidgets.clear();
        itemsRenderers.clear();
        dropDownContentPanel.clear();

        adaptDropDownPanelHeight();
        checkListEmptiness();
    }

    interface DropDownListUiBinder extends UiBinder<Widget, DropDownList> {
    }

    public interface SelectionHandler {
        /**
         * Called when currently selected item has been changed.
         *
         * @param item
         *         currently selected item
         */
        void onItemSelected(DropDownListItem item);
    }

    static {
        RESOURCES.dropDownListCss().ensureInjected();
    }
}
