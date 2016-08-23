/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui.multisplitpanel.panel;

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
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.ui.multisplitpanel.WidgetToShow;
import org.eclipse.che.ide.ui.multisplitpanel.actions.ClosePaneAction;
import org.eclipse.che.ide.ui.multisplitpanel.actions.RemoveAllWidgetsInPaneAction;
import org.eclipse.che.ide.ui.multisplitpanel.actions.SplitHorizontallyAction;
import org.eclipse.che.ide.ui.multisplitpanel.actions.SplitVerticallyAction;
import org.eclipse.che.ide.ui.multisplitpanel.menu.Menu;
import org.eclipse.che.ide.ui.multisplitpanel.menu.MenuItem;
import org.eclipse.che.ide.ui.multisplitpanel.menu.MenuItemActionWidget;
import org.eclipse.che.ide.ui.multisplitpanel.menu.MenuItemWidget;
import org.eclipse.che.ide.ui.multisplitpanel.tab.Tab;
import org.eclipse.che.ide.ui.multisplitpanel.tab.TabItemFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link SubPanelView}.
 *
 * @author Artem Zatsarynnyi
 */
public class SubPanelViewImpl extends Composite implements SubPanelView,
                                                           Menu.ActionDelegate,
                                                           Tab.ActionDelegate {

    private final TabItemFactory                    tabItemFactory;
    private final Menu                              menu;
    private final Map<Tab, WidgetToShow>            tabs2Widgets;
    private final Map<WidgetToShow, Tab>            widgets2Tabs;
    private final Map<WidgetToShow, MenuItemWidget> widgets2ListItems;

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
                            Menu menu,
                            @Assisted ClosePaneAction closePaneAction,
                            @Assisted RemoveAllWidgetsInPaneAction removeAllWidgetsInPaneAction,
                            @Assisted SplitHorizontallyAction splitHorizontallyAction,
                            @Assisted SplitVerticallyAction splitVerticallyAction) {
        this.tabItemFactory = tabItemFactory;
        this.menu = menu;

        menu.addListItem(new MenuItemActionWidget(closePaneAction));
        menu.addListItem(new MenuItemActionWidget(removeAllWidgetsInPaneAction));
        menu.addListItem(new MenuItemActionWidget(splitHorizontallyAction));
        menu.addListItem(new MenuItemActionWidget(splitVerticallyAction));

        menu.setDelegate(this);

        tabs2Widgets = new HashMap<>();
        widgets2Tabs = new HashMap<>();
        widgets2ListItems = new HashMap<>();

        splitLayoutPanel = new SplitLayoutPanel(3);

        initWidget(uiBinder.createAndBindUi(this));

        tabsPanel.add(menu);

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
    public void addWidget(WidgetToShow widget, boolean removable) {
        final Tab tab = tabItemFactory.createTabItem(widget.getTitle(), widget.getIcon(), removable);
        tab.setDelegate(this);

        tabs2Widgets.put(tab, widget);
        widgets2Tabs.put(widget, tab);

        tabsPanel.add(tab);
        widgetsPanel.setWidget(widget.getWidget());

        // add item to drop-down menu
        final MenuItemWidget listItemWidget = new MenuItemWidget(tab, removable);
        menu.addListItem(listItemWidget);
        widgets2ListItems.put(widget, listItemWidget);
    }

    @Override
    public void activateWidget(WidgetToShow widget) {
        final Tab tab = widgets2Tabs.get(widget);
        if (tab != null) {
            selectTab(tab);
        }

        widgetsPanel.showWidget(widget.getWidget().asWidget());

        // add 'active' attribute for active widget for testing purpose
        for (WidgetToShow widgetToShow : widgets2Tabs.keySet()) {
            widgetToShow.getWidget().asWidget().getElement().removeAttribute("active");
        }
        widget.getWidget().asWidget().getElement().setAttribute("active", "");
    }

    @Override
    public void removeWidget(WidgetToShow widget) {
        final Tab tab = widgets2Tabs.get(widget);
        if (tab != null) {
            closeTab(tab);
        }
    }

    private void removeWidgetFromUI(WidgetToShow widget) {
        final Tab tab = widgets2Tabs.remove(widget);
        if (tab != null) {
            tabsPanel.remove(tab);
            widgetsPanel.remove(widget.getWidget());

            tabs2Widgets.remove(tab);

            // remove item from drop-down menu
            final MenuItemWidget listItemWidget = widgets2ListItems.remove(widget);
            if (listItemWidget != null) {
                menu.removeListItem(listItemWidget);
            }
        }
    }

    @Override
    public void closePanel() {
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
    public void removeChildSubPanel(Widget widget) {
        if (splitLayoutPanel.getWidgetDirection(widget) == DockLayoutPanel.Direction.CENTER) {
            // this is the only widget on the panel
            // don't allow to remove it
            return;
        }

        splitLayoutPanel.setWidgetSize(widget, 0);
        splitLayoutPanel.remove(widget);
    }

    @Override
    public void setParentPanel(SubPanelView parentPanel) {
        this.parentPanel = parentPanel;
    }

    @Override
    public void onMenuItemSelected(MenuItem menuItem) {
        final Object data = menuItem.getData();
        if (data instanceof Tab) {
            selectTab((Tab)data);

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
    public void onMenuItemClosing(MenuItem menuItem) {
        Object data = menuItem.getData();
        if (data instanceof Tab) {
            closeTab((Tab)data);
        }
    }

    @Override
    public void onTabClicked(Tab tab) {
        selectTab(tab);

        // TODO: extract code to the separate method
        WidgetToShow widget = tabs2Widgets.get(tab);
        if (widget != null) {
            activateWidget(widget);
            delegate.onWidgetFocused(widget.getWidget());
        }
    }

    private void selectTab(Tab tab) {
        for (Tab tabItem : tabs2Widgets.keySet()) {
            tabItem.unSelect();
        }

        tab.select();
    }

    @Override

    public void onTabClosing(Tab tab) {
        closeTab(tab);
    }

    private void closeTab(Tab tab) {
        final WidgetToShow widget = tabs2Widgets.get(tab);

        if (widget != null) {
            delegate.onWidgetRemoving(widget.getWidget(), new SubPanel.RemoveCallback() {
                @Override
                public void remove() {
                    removeWidgetFromUI(widget);
                }
            });
        }
    }

    interface SubPanelViewImplUiBinder extends UiBinder<Widget, SubPanelViewImpl> {
    }
}
