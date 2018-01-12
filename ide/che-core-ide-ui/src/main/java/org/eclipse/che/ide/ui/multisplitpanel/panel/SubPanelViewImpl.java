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
package org.eclipse.che.ide.ui.multisplitpanel.panel;

import static com.google.gwt.user.client.ui.DockLayoutPanel.Direction.CENTER;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.BaseAction;
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

/**
 * Implementation of {@link SubPanelView}.
 *
 * @author Artem Zatsarynnyi
 */
public class SubPanelViewImpl extends Composite
    implements SubPanelView, Menu.ActionDelegate, Tab.ActionDelegate, RequiresResize {

  interface SubPanelViewImplUiBinder extends UiBinder<Widget, SubPanelViewImpl> {}

  private static final int POPUP_OFFSET = 15;

  private final TabItemFactory tabItemFactory;
  private final Menu menu;
  private final Map<Tab, WidgetToShow> tabs2Widgets;
  private final Map<WidgetToShow, Tab> widgets2Tabs;
  private final Map<WidgetToShow, MenuItemWidget> widgets2ListItems;
  private final MenuItem closePaneMenuItem;

  @UiField(provided = true)
  SplitLayoutPanel splitLayoutPanel;

  @UiField DockLayoutPanel mainPanel;

  @UiField FlowPanel tabsPanel;

  @UiField FlowPanel plusPanel;

  @UiField FlowPanel menuPanel;

  @UiField DeckLayoutPanel widgetsPanel;

  private ActionDelegate delegate;
  private SubPanelView parentPanel;
  private List<SubPanelView> eastSubPanels;
  private List<SubPanelView> southSubPanels;

  private Tab selectedTab;
  private int tabsPanelWidth = 0;

  @Inject
  public SubPanelViewImpl(
      SubPanelViewImplUiBinder uiBinder,
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

    menuPanel.add(menu);

    plusPanel.getElement().setInnerHTML(FontAwesome.PLUS);
    plusPanel.addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            delegate.onAddTabButtonClicked(
                getAbsoluteLeft(plusPanel.getElement()) + POPUP_OFFSET,
                getAbsoluteTop(plusPanel.getElement()) + POPUP_OFFSET);
          }
        },
        ClickEvent.getType());

    widgetsPanel.ensureDebugId("process-output-panel-holder");
    widgetsPanel.addDomHandler(
        event -> delegate.onWidgetFocused(widgetsPanel.getVisibleWidget()), ClickEvent.getType());
  }

  /**
   * Returns absolute left position of the element.
   *
   * @param element element
   * @return element left position
   */
  private native int getAbsoluteLeft(JavaScriptObject element) /*-{
      return element.getBoundingClientRect().left;
  }-*/;

  /**
   * Returns absolute top position of the element.
   *
   * @param element element
   * @return element top position
   */
  private native int getAbsoluteTop(JavaScriptObject element) /*-{
      return element.getBoundingClientRect().top;
  }-*/;

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

    onResize();
  }

  @Override
  public void splitVertically(SubPanelView subPanelView) {
    eastSubPanels.add(0, subPanelView);

    final int width = mainPanel.getOffsetWidth() / 2;

    splitLayoutPanel.remove(mainPanel);
    splitLayoutPanel.addEast(subPanelView, width);
    splitLayoutPanel.add(mainPanel);

    onResize();
  }

  @Override
  public void addWidget(WidgetToShow widget, boolean removable) {
    final Tab tab = tabItemFactory.createTabItem(widget.getTitle(), widget.getIcon(), removable);
    tab.setDelegate(this);

    tabs2Widgets.put(tab, widget);
    widgets2Tabs.put(widget, tab);

    tabsPanel.insert(tab, tabsPanel.getWidgetIndex(plusPanel));

    Widget visibleWidget = widgetsPanel.getVisibleWidget();
    widgetsPanel.setWidget(widget.getWidget());
    if (visibleWidget != null) {
      widgetsPanel.showWidget(visibleWidget);
    }

    // add item to drop-down menu
    final MenuItemWidget listItemWidget = new MenuItemWidget(tab, removable);
    menu.addListItem(listItemWidget);
    widgets2ListItems.put(widget, listItemWidget);
  }

  @Override
  public void activateWidget(WidgetToShow widgetToActivate) {
    final Tab tab = widgets2Tabs.get(widgetToActivate);
    if (tab != null) {
      selectTab(tab);
    }

    IsWidget widget = widgetToActivate.getWidget();
    widgetsPanel.showWidget(widget.asWidget());
    if (widget instanceof Focusable) {
      ((Focusable) widget).setFocus(true);
    }

    // add 'active' attribute for active widget for testing purpose
    for (WidgetToShow widgetToShow : widgets2Tabs.keySet()) {
      widgetToShow.getWidget().asWidget().getElement().removeAttribute("active");
    }
    widget.asWidget().getElement().setAttribute("active", "");
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
      delegate.onWidgetRemoving(
          widget.getWidget(),
          new SubPanel.RemoveCallback() {
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
      ((SubPanelViewImpl) parentPanel).removeWidgetFromSplitPanel(this);
      return;
    }

    if (isInTheCenterOfTheParent()) {
      ((SubPanelViewImpl) parentPanel).removeChildSubPanel(this);
    } else {
      removeChildSubPanel(mainPanel);
    }
  }

  /** Checks whether this panel is in the central part of it's parent. */
  private boolean isInTheCenterOfTheParent() {
    return ((SubPanelViewImpl) parentPanel).splitLayoutPanel.getWidgetDirection(this) == CENTER;
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
      ((SubPanelViewImpl) parentPanel).removeWidgetFromSplitPanel(this);
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
    } else if (data instanceof BaseAction) {
      ((Action) data).actionPerformed(null);
    }
  }

  @Override
  public void onMenuItemClosing(MenuItem menuItem) {
    Object data = menuItem.getData();
    if (data instanceof Tab) {
      closeTab((Tab) data);
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

  @Override
  public void onTabDoubleClicked(Tab tab) {
    final WidgetToShow widget = tabs2Widgets.get(tab);
    if (widget != null) {
      activateWidget(widget);
      delegate.onWidgetDoubleClicked(widget.getWidget());
    }
  }

  private void selectTab(Tab tab) {
    for (Tab tabItem : tabs2Widgets.keySet()) {
      tabItem.unSelect();
    }

    tab.select();

    selectedTab = tab;
    ensureActiveTabVisible();
  }

  @Override
  public void onTabClosing(Tab tab) {
    closeTab(tab);
  }

  @Override
  public void onResize() {
    for (WidgetToShow widgetToShow : widgets2Tabs.keySet()) {
      if (widgetToShow.getWidget() instanceof RequiresResize) {
        ((RequiresResize) widgetToShow.getWidget()).onResize();
      }
    }

    // reset timer and schedule it again
    ensureActiveTabVisibleTimer.cancel();
    ensureActiveTabVisibleTimer.schedule(200);
  }

  /**
   * Timer to prevent updating tabs visibility while resizing. It needs to update tabs once when
   * resizing has stopped.
   */
  private Timer ensureActiveTabVisibleTimer =
      new Timer() {
        @Override
        public void run() {
          ensureActiveTabVisible();
        }
      };

  /** Ensures active tab and plus button are visible */
  private void ensureActiveTabVisible() {
    // do nothing if selected tab is null
    if (selectedTab == null) {
      return;
    }

    // do nothing if selected tab is visible and plus button is visible
    if (selectedTab.asWidget().getElement().getAbsoluteTop()
            == tabsPanel.getElement().getAbsoluteTop()
        && plusPanel.getElement().getAbsoluteTop() == tabsPanel.getElement().getAbsoluteTop()
        && tabsPanelWidth == tabsPanel.getOffsetWidth()) {
      return;
    }

    tabsPanelWidth = tabsPanel.getOffsetWidth();

    // hide all widgets except plus button
    for (int i = 0; i < tabsPanel.getWidgetCount(); i++) {
      Widget w = tabsPanel.getWidget(i);
      if (plusPanel == w) {
        continue;
      }

      w.setVisible(false);
    }

    // determine selected tab index
    int selectedTabIndex = tabsPanel.getWidgetIndex(selectedTab.asWidget());

    // show all possible tabs before selected tab
    for (int i = selectedTabIndex; i >= 0; i--) {
      Widget w = tabsPanel.getWidget(i);

      // skip for plus button
      if (plusPanel == w) {
        continue;
      }

      // set tab visible
      w.setVisible(true);

      // continue cycle if plus button visible
      if (plusPanel.getElement().getAbsoluteTop() == tabsPanel.getElement().getAbsoluteTop()) {
        continue;
      }

      // otherwise hide tab and break
      w.setVisible(false);
      break;
    }

    // show all possible tabs after selected tab
    for (int i = selectedTabIndex + 1; i < tabsPanel.getWidgetCount(); i++) {
      Widget w = tabsPanel.getWidget(i);

      // skip for plus button
      if (plusPanel == w) {
        continue;
      }

      // set tab visible
      w.setVisible(true);

      // continue cycle if plus button visible
      if (plusPanel.getElement().getAbsoluteTop() == tabsPanel.getElement().getAbsoluteTop()) {
        continue;
      }

      // otherwise hide tab and break
      w.setVisible(false);
      break;
    }
  }
}
