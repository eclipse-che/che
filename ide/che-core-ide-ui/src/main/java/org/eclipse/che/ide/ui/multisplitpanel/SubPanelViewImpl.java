/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 ******************************************************************************/
package org.eclipse.che.ide.ui.multisplitpanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.multisplitpanel.CloseCallback;
import org.eclipse.che.ide.api.multisplitpanel.TabItem;
import org.eclipse.che.ide.api.multisplitpanel.WidgetToShow;

import java.util.HashMap;
import java.util.Map;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class SubPanelViewImpl extends Composite implements SubPanelView,
                                                           ListButton.ActionDelegate,
                                                           TabItem.ActionDelegate {

    private final TabItemFactory tabItemFactory;
    private final ListButton     listButton;

    private final Map<TabItem, WidgetToShow>        tabs2Widgets;
    private final Map<WidgetToShow, TabItem>        widgets2Tabs;
    private final Map<WidgetToShow, ListItemWidget> widgets2ListItems;

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    DockLayoutPanel mainPanel;

    @UiField
    FlowPanel tabsPanel;

    @UiField
    DeckLayoutPanel widgetsPanel;

    private ActionDelegate delegate;

    private SubPanelView parentPanel;

    @Inject
    public SubPanelViewImpl(SubPanelViewImplUiBinder uiBinder,
                            TabItemFactory tabItemFactory,
                            ListButton listButton,
                            @Assisted ClosePaneAction closePaneAction,
                            @Assisted CloseAllTabsInPaneAction closeAllTabsInPaneAction,
                            @Assisted SplitHorizontallyAction splitHorizontallyAction,
                            @Assisted SplitVerticallyAction splitVerticallyAction) {
        this.tabItemFactory = tabItemFactory;
        this.listButton = listButton;

        listButton.addListItem(new ListItemActionWidget(closePaneAction));
        listButton.addListItem(new ListItemActionWidget(closeAllTabsInPaneAction));
        listButton.addListItem(new ListItemActionWidget(splitHorizontallyAction));
        listButton.addListItem(new ListItemActionWidget(splitVerticallyAction));

        listButton.setDelegate(this);

        tabs2Widgets = new HashMap<>();
        widgets2Tabs = new HashMap<>();
        widgets2ListItems = new HashMap<>();

        splitLayoutPanel = new SplitLayoutPanel(5);

        initWidget(uiBinder.createAndBindUi(this));

        tabsPanel.add(listButton);

        widgetsPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onWidgetFocused(widgetsPanel.getVisibleWidget());
            }
        }, ClickEvent.getType());
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void splitHorizontally(IsWidget subPanelView) {
        int height = splitLayoutPanel.getOffsetHeight() / 2;

        splitLayoutPanel.remove(mainPanel);
        splitLayoutPanel.addSouth(subPanelView, height);
        splitLayoutPanel.add(mainPanel);
    }

    @Override
    public void splitVertically(IsWidget subPanelView) {
        int width = splitLayoutPanel.getOffsetWidth() / 2;

        splitLayoutPanel.remove(mainPanel);
        splitLayoutPanel.addEast(subPanelView, width);
        splitLayoutPanel.add(mainPanel);
    }

    @Override
    public void addWidget(WidgetToShow widget) {
        TabItem tabItem = tabItemFactory.createTabItem(widget.getTitle(), widget.getIcon());
        tabItem.setDelegate(this);

        tabs2Widgets.put(tabItem, widget);
        widgets2Tabs.put(widget, tabItem);

        tabsPanel.add(tabItem);
        widgetsPanel.setWidget(widget.getWidget());

        // add item to drop-down menu
        final ListItemWidget listItemWidget = new ListItemWidget(tabItem);
        listButton.addListItem(listItemWidget);
        widgets2ListItems.put(widget, listItemWidget);
    }

    @Override
    public void activateWidget(WidgetToShow widget) {
        final TabItem tab = widgets2Tabs.get(widget);
        if (tab != null) {
            selectTab(tab);
        }

        widgetsPanel.showWidget(widget.getWidget().asWidget());
    }

    @Override
    public void removeWidget(WidgetToShow widget) {
        final TabItem tabItem = widgets2Tabs.get(widget);
        if (tabItem != null) {
            closeTab(tabItem);
        }
    }

    private void removeWidgetFromUI(WidgetToShow widget) {
        final TabItem tabItem = widgets2Tabs.remove(widget);
        if (tabItem != null) {
            tabsPanel.remove(tabItem);
            widgetsPanel.remove(widget.getWidget());

            tabs2Widgets.remove(tabItem);

            // remove item from drop-down menu
            final ListItemWidget listItemWidget = widgets2ListItems.remove(widget);
            if (listItemWidget != null) {
                listButton.removeListItem(listItemWidget);
            }
        }
    }

    @Override
    public void removeCentralPanel() {
        if (splitLayoutPanel.getWidgetCount() == 1) {
            parentPanel.removeChildSubPanel(this);
            return;
        }

        splitLayoutPanel.remove(mainPanel);

        // move widget from east/south part to the center
        final Widget lastWidget = splitLayoutPanel.getWidget(0);
        splitLayoutPanel.setWidgetSize(lastWidget, 0);
        splitLayoutPanel.remove(lastWidget);
        splitLayoutPanel.add(lastWidget);
    }

    @Override
    public void removeChildSubPanel(Widget w) {
        if (splitLayoutPanel.getWidgetDirection(w) == DockLayoutPanel.Direction.CENTER) {
            // this is the only widget on the panel
            // don't allow to remove it
            return;
        }

        splitLayoutPanel.setWidgetSize(w, 0);
        splitLayoutPanel.remove(w);
    }

    @Override
    public void setParentPanel(SubPanelView parentPanel) {
        this.parentPanel = parentPanel;
    }

    @Override
    public void onListButtonClicked(ListItem tab) {
        final Object data = tab.getTabItem();
        if (data instanceof TabItem) {
            selectTab((TabItem)data);

            WidgetToShow widget = tabs2Widgets.get(data);
            if (widget != null) {
                activateWidget(widget);
                delegate.onWidgetFocused(widget.getWidget());
            }
        } else if (data instanceof Action) {
            ((Action)data).actionPerformed(null);
        }
    }

    @Override
    public void onListButtonClosing(ListItem tab) {
        Object data = tab.getTabItem();
        if (data instanceof TabItem) {
            closeTab((TabItem)data);
        }
    }

    @Override
    public void onTabClicked(TabItem tab) {
        selectTab(tab);

        // TODO: extract code to the separate method
        WidgetToShow widget = tabs2Widgets.get(tab);
        if (widget != null) {
            activateWidget(widget);
            delegate.onWidgetFocused(widget.getWidget());
        }
    }

    private void selectTab(TabItem tab) {
        for (TabItem tabItem : tabs2Widgets.keySet()) {
            tabItem.unSelect();
        }

        tab.select();
    }

    @Override

    public void onTabClosing(TabItem tab) {
        closeTab(tab);
    }

    private void closeTab(TabItem tab) {
        final WidgetToShow widget = tabs2Widgets.get(tab);

        if (widget != null) {
            delegate.onWidgetRemoving(widget.getWidget(), new CloseCallback() {
                @Override
                public void close() {
                    removeWidgetFromUI(widget);
                }
            });
        }
    }

    interface SubPanelViewImplUiBinder extends UiBinder<Widget, SubPanelViewImpl> {
    }
}
