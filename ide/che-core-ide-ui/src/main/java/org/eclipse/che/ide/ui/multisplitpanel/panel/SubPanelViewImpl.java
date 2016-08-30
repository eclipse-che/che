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

import org.eclipse.che.commons.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.gwt.user.client.ui.DockLayoutPanel.Direction.CENTER;

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
    private final MenuItem                          closePaneMenuItem;


    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    DockLayoutPanel mainPanel;

    @UiField
    FlowPanel tabsPanel;

    @UiField
    DeckLayoutPanel widgetsPanel;

    private ActionDelegate     delegate;
    private SubPanelView       parentPanel;
    private List<SubPanelView> eastSubPanels;
    private List<SubPanelView> southSubPanels;

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

        closePaneMenuItem = new MenuItemActionWidget(closePaneAction);
        menu.addListItem(closePaneMenuItem);
        menu.addListItem(new MenuItemActionWidget(removeAllWidgetsInPaneAction));
        menu.addListItem(new MenuItemActionWidget(splitHorizontallyAction));
        menu.addListItem(new MenuItemActionWidget(splitVerticallyAction));

        menu.setDelegate(this);

        tabs2Widgets = new HashMap<>();
        widgets2Tabs = new HashMap<>();
        widgets2ListItems = new HashMap<>();
        eastSubPanels = new ArrayList<>();
        southSubPanels = new ArrayList<>();

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
    public void splitHorizontally(SubPanelView subPanelView) {
        southSubPanels.add(0, subPanelView);

        final int height = mainPanel.getOffsetHeight() / 2;

        splitLayoutPanel.remove(mainPanel);
        splitLayoutPanel.addSouth(subPanelView, height);
        splitLayoutPanel.add(mainPanel);
    }

    @Override
    public void splitVertically(SubPanelView subPanelView) {
        eastSubPanels.add(0, subPanelView);

        final int width = mainPanel.getOffsetWidth() / 2;

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
        if (parentPanel == null) {
            // do not allow to close root panel
            return;
        }

        if (splitLayoutPanel.getWidgetCount() == 1 && !isInTheCenterOfTheParent()) {
            // this panel doesn't have any child sub-panels
            // so just remove it from it's parent
            ((SubPanelViewImpl)parentPanel).removeWidgetFromSplitPanel(this);
            return;
        }

        if (isInTheCenterOfTheParent()) {
            ((SubPanelViewImpl)parentPanel).removeChildSubPanel(this);
        } else {
            removeChildSubPanel(mainPanel);
        }
    }

    /** Checks whether this panel is in the central part of it's parent. */
    private boolean isInTheCenterOfTheParent() {
        return ((SubPanelViewImpl)parentPanel).splitLayoutPanel.getWidgetDirection(this) == CENTER;
    }

    private void removeChildSubPanel(Widget widget) {
        removeWidgetFromSplitPanel(widget.asWidget());

        IsWidget lastWidget = null;
        if (!southSubPanels.isEmpty()) {
            lastWidget = southSubPanels.get(0);
        } else if (!eastSubPanels.isEmpty()) {
            lastWidget = eastSubPanels.get(0);
        }

        if (lastWidget != null) {
            removeWidgetFromSplitPanel(lastWidget.asWidget());
            splitLayoutPanel.add(lastWidget);
        } else {
            ((SubPanelViewImpl)parentPanel).removeWidgetFromSplitPanel(this);
        }
    }

    private void removeWidgetFromSplitPanel(Widget widget) {
        if (splitLayoutPanel.getWidgetDirection(widget) != CENTER) {
            // collapse east/south part in order to maximize central part
            splitLayoutPanel.setWidgetSize(widget, 0);
        }

        splitLayoutPanel.remove(widget);

        southSubPanels.remove(widget);
        eastSubPanels.remove(widget);
    }

    @Override
    public void setParentPanel(@Nullable SubPanelView parentPanel) {
        this.parentPanel = parentPanel;

        if (parentPanel == null) {
            // do not allow to remove root panel (if it doesn't have parent)
            menu.removeListItem(closePaneMenuItem);
        }
    }

    @Override
    public void onMenuItemSelected(MenuItem menuItem) {
        final Object data = menuItem.getData();
        if (data instanceof Tab) {
            final WidgetToShow widget = tabs2Widgets.get(data);
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
        final WidgetToShow widget = tabs2Widgets.get(tab);
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

    interface SubPanelViewImplUiBinder extends UiBinder<Widget, SubPanelViewImpl> {
    }
}
