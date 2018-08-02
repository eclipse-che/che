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
package org.eclipse.che.ide.part.editor;

import static com.google.gwt.dom.client.Style.Display.BLOCK;
import static com.google.gwt.dom.client.Style.Unit.PCT;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.part.widgets.panemenu.EditorPaneMenu;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 */
public class EditorPartStackView extends ResizeComposite
    implements PartStackView, MouseDownHandler {

  interface PartStackUiBinder extends UiBinder<Widget, EditorPartStackView> {}

  /** Listener to handle clicking on Add tab button. */
  interface AddTabButtonClickListener {

    /**
     * Called when clicking on Add tab button.
     *
     * @param mouseX absolute mouse left
     * @param mouseY absolute mouse top
     */
    void onAddTabButtonClicked(int mouseX, int mouseY);
  }

  private static final PartStackUiBinder UI_BINDER = GWT.create(PartStackUiBinder.class);

  private static final int POPUP_OFFSET = 15;

  @UiField DockLayoutPanel parent;

  @UiField FlowPanel tabsPanel;

  @UiField FlowPanel plusPanel;

  @UiField DeckLayoutPanel contentPanel;

  @UiField FlowPanel menuPanel;

  private final Map<PartPresenter, TabItem> tabs;
  private final AcceptsOneWidget partViewContainer;
  private final LinkedList<PartPresenter> contents;

  private int tabsPanelWidth = 0;

  private ActionDelegate delegate;
  private EditorPaneMenu editorPaneMenu;
  private TabItem activeTab;
  private AddTabButtonClickListener addTabButtonClickListener;

  public EditorPartStackView() {
    this.tabs = new HashMap<>();
    this.contents = new LinkedList<>();

    initWidget(UI_BINDER.createAndBindUi(this));

    plusPanel.getElement().setInnerHTML(FontAwesome.PLUS);
    plusPanel.addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            if (addTabButtonClickListener != null) {
              addTabButtonClickListener.onAddTabButtonClicked(
                  getAbsoluteLeft(plusPanel.getElement()) + POPUP_OFFSET,
                  getAbsoluteTop(plusPanel.getElement()) + POPUP_OFFSET);
            }
          }
        },
        ClickEvent.getType());

    partViewContainer =
        new AcceptsOneWidget() {
          @Override
          public void setWidget(IsWidget widget) {
            contentPanel.add(widget);
          }
        };

    addDomHandler(this, MouseDownEvent.getType());

    setMaximized(false);
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

  public void setAddTabButtonClickListener(AddTabButtonClickListener listener) {
    addTabButtonClickListener = listener;
  }

  /** {@inheritDoc} */
  @Override
  protected void onAttach() {
    super.onAttach();

    Style style = getElement().getParentElement().getStyle();
    style.setHeight(100, PCT);
    style.setWidth(100, PCT);
  }

  /**
   * Adds editor pane menu button in special place on view.
   *
   * @param editorPaneMenu button which will be added
   */
  public void addPaneMenuButton(@NotNull EditorPaneMenu editorPaneMenu) {
    this.editorPaneMenu = editorPaneMenu;
    menuPanel.add(editorPaneMenu);
  }

  /** {@inheritDoc} */
  @Override
  public void onMouseDown(@NotNull MouseDownEvent event) {
    delegate.onRequestFocus();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void addTab(@NotNull TabItem tabItem, @NotNull PartPresenter partPresenter) {
    /** Show editor area if it is empty and hidden */
    if (contents.isEmpty()) {
      getElement().getParentElement().getStyle().setDisplay(BLOCK);
    }

    /** Add editor tab to tab panel */
    tabsPanel.insert(tabItem.getView(), tabsPanel.getWidgetIndex(plusPanel));

    /** Process added editor tab */
    tabs.put(partPresenter, tabItem);
    contents.add(partPresenter);
    partPresenter.go(partViewContainer);
  }

  /** Ensures active tab and plus button are visible. */
  private void ensureActiveTabVisible() {
    // do nothing if selected tab is null
    if (activeTab == null) {
      return;
    }

    // do nothing if selected tab is visible and plus button is visible
    if (getAbsoluteTop(activeTab.getView().asWidget().getElement())
            == getAbsoluteTop(tabsPanel.getElement())
        && getAbsoluteTop(plusPanel.getElement()) == getAbsoluteTop(tabsPanel.getElement())
        && tabsPanelWidth == tabsPanel.getOffsetWidth()) {
      return;
    }

    tabsPanelWidth = tabsPanel.getOffsetWidth();

    // determine whether all widgets are visible
    boolean allWidgetVisible = true;
    for (int i = 0; i < tabsPanel.getWidgetCount(); i++) {
      Widget w = tabsPanel.getWidget(i);
      if (plusPanel == w) {
        continue;
      }

      if (!w.isVisible()) {
        allWidgetVisible = false;
        break;
      }
    }

    // do nothing if all widgets are visible and sum of children width less then panel width
    if (allWidgetVisible) {
      int childrenWidth = 0;
      for (int i = 0; i < tabsPanel.getWidgetCount(); i++) {
        Widget w = tabsPanel.getWidget(i);
        childrenWidth += w.getOffsetWidth();
      }

      if (childrenWidth < tabsPanelWidth) {
        return;
      }
    }

    // hide all widgets except plus button
    for (int i = 0; i < tabsPanel.getWidgetCount(); i++) {
      Widget w = tabsPanel.getWidget(i);
      if (plusPanel == w) {
        continue;
      }

      w.setVisible(false);
    }

    // determine selected tab index
    int selectedTabIndex = tabsPanel.getWidgetIndex(activeTab.getView().asWidget());

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
      if (getAbsoluteTop(plusPanel.getElement()) == getAbsoluteTop(tabsPanel.getElement())) {
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
      if (getAbsoluteTop(plusPanel.getElement()) == getAbsoluteTop(tabsPanel.getElement())) {
        continue;
      }

      // otherwise hide tab and break
      w.setVisible(false);
      break;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void removeTab(@NotNull PartPresenter presenter) {
    TabItem tab = tabs.get(presenter);
    tabsPanel.remove(tab.getView());
    contentPanel.remove(presenter.getView());

    tabs.remove(presenter);
    contents.remove(presenter);

    if (!contents.isEmpty()) {
      selectTab(contents.getLast());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void selectTab(@NotNull PartPresenter partPresenter) {
    IsWidget view = partPresenter.getView();

    // set/remove attribute 'active' for Selenium tests
    for (int i = 0; i < contentPanel.getWidgetCount(); i++) {
      contentPanel.getWidget(i).getElement().removeAttribute("active");
    }
    view.asWidget().getElement().setAttribute("active", "");

    int viewIndex = contentPanel.getWidgetIndex(view);
    if (viewIndex < 0) {
      partPresenter.go(partViewContainer);
      viewIndex = contentPanel.getWidgetIndex(view);
    }

    contentPanel.showWidget(viewIndex);
    setActiveTab(partPresenter);

    if (partPresenter instanceof TextEditor) {
      ((TextEditor) partPresenter).activate();
    }
  }

  /**
   * Switches to specified tab.
   *
   * @param part tab part
   */
  private void setActiveTab(@NotNull PartPresenter part) {
    for (TabItem tab : tabs.values()) {
      tab.unSelect();
      tab.getView().asWidget().getElement().removeAttribute("active");
    }

    activeTab = tabs.get(part);
    activeTab.select();

    activeTab.getView().asWidget().getElement().setAttribute("active", "");

    delegate.onRequestFocus();

    // reset timer and schedule it again
    ensureActiveTabVisibleTimer.cancel();
    ensureActiveTabVisibleTimer.schedule(200);
  }

  /** {@inheritDoc} */
  @Override
  public void setTabPositions(List<PartPresenter> partPositions) {
    throw new UnsupportedOperationException(
        "The method doesn't allowed in this class " + getClass());
  }

  /** {@inheritDoc} */
  @Override
  public void setFocus(boolean focused) {
    if (activeTab == null) {
      return;
    }

    if (focused) {
      activeTab.select();
    } else {
      activeTab.unSelect();
    }
  }

  @Override
  public void setMaximized(boolean maximized) {
    getElement().setAttribute("maximized", Boolean.toString(maximized));
  }

  /** {@inheritDoc} */
  @Override
  public void updateTabItem(@NotNull PartPresenter partPresenter) {
    TabItem tab = tabs.get(partPresenter);
    tab.update(partPresenter);
  }

  @Override
  public void onResize() {
    super.onResize();

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
}
