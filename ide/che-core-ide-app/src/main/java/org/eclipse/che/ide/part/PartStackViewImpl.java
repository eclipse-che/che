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
package org.eclipse.che.ide.part;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.base.ToolButton;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * PartStack view class. Implements UI that manages Parts organized in a Tab-like widget.
 *
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public class PartStackViewImpl extends Composite
    implements RequiresResize, PartStackView, MouseDownHandler, ContextMenuHandler {

  interface PartStackViewImplUiBinder extends UiBinder<Widget, PartStackViewImpl> {}

  private static final PartStackViewImplUiBinder UI_BINDER =
      GWT.create(PartStackViewImplUiBinder.class);

  private final Map<PartPresenter, TabItem> tabs;

  private final AcceptsOneWidget partViewContainer;

  private final PartStackUIResources resources;
  private final CoreLocalizationConstant localizationConstant;

  @UiField FlowPanel partButtons;

  @UiField FlowPanel partStackActions;

  @UiField FlowPanel maximizeButton;

  @UiField FlowPanel hideButton;

  @UiField FlowPanel menuButton;

  @UiField DeckLayoutPanel partStackContent;

  private ActionDelegate delegate;
  private Widget focusedWidget;

  @Inject
  public PartStackViewImpl(
      PartStackUIResources resources, CoreLocalizationConstant localizationConstant) {
    this.resources = resources;
    this.localizationConstant = localizationConstant;
    initWidget(UI_BINDER.createAndBindUi(this));

    partStackContent.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);

    tabs = new HashMap<>();

    partViewContainer =
        new AcceptsOneWidget() {
          @Override
          public void setWidget(IsWidget widget) {
            partStackContent.add(widget);
          }
        };

    addDomHandler(this, MouseDownEvent.getType());
    addDomHandler(this, ContextMenuEvent.getType());

    setMaximized(false);

    addMaximizeButton();
    addHideButton();
    addMenuButton();
  }

  /** Adds button to maximize part stack. */
  private void addMaximizeButton() {
    SVGImage maximize = new SVGImage(resources.maximizePart());
    maximize.getElement().setAttribute("name", "workBenchIconMaximize");
    ToolButton maximizeToolButton = new ToolButton(maximize);
    maximizeButton.add(maximizeToolButton);

    maximizeToolButton.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            delegate.onToggleMaximize();
          }
        });

    if (maximizeButton.getElement() instanceof elemental.dom.Element) {
      Tooltip.create(
          (elemental.dom.Element) maximizeButton.getElement(),
          PositionController.VerticalAlign.BOTTOM,
          PositionController.HorizontalAlign.MIDDLE,
          localizationConstant.maximizePartStackTitle());
    }
  }

  /** Adds button to hide part stack. */
  private void addHideButton() {
    ToolButton hideToolButton = new ToolButton(FontAwesome.CARET_SQUARE_O_LEFT);
    hideToolButton.getElement().setAttribute("name", "workBenchIconMinimize");
    hideButton.add(hideToolButton);

    hideToolButton.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            delegate.onHide();
          }
        });

    if (hideButton.getElement() instanceof elemental.dom.Element) {
      Tooltip.create(
          (elemental.dom.Element) hideButton.getElement(),
          PositionController.VerticalAlign.BOTTOM,
          PositionController.HorizontalAlign.MIDDLE,
          localizationConstant.minimizePartStackTitle());
    }
  }

  /** Adds part stack options button. */
  private void addMenuButton() {
    final ToolButton menuToolButton = new ToolButton(FontAwesome.COG);
    menuToolButton.getElement().setAttribute("name", "workBenchIconPartStackOptions");
    menuButton.add(menuToolButton);
    menuToolButton.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Scheduler.get()
                .scheduleDeferred(
                    () -> {
                      int left = getAbsoluteLeft(menuToolButton.getElement());
                      int top = getAbsoluteTop(menuToolButton.getElement());
                      delegate.onPartStackMenu(left + 10, top + 21 - 8);
                    });
          }
        });

    if (menuButton.getElement() instanceof elemental.dom.Element) {
      Tooltip.create(
          (elemental.dom.Element) menuButton.getElement(),
          PositionController.VerticalAlign.BOTTOM,
          PositionController.HorizontalAlign.MIDDLE,
          localizationConstant.partStackOptionsTitle());
    }
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

  /** {@inheritDoc} */
  @Override
  public void onMouseDown(@NotNull MouseDownEvent event) {
    delegate.onRequestFocus();
  }

  /** {@inheritDoc} */
  @Override
  public void onContextMenu(@NotNull ContextMenuEvent event) {
    delegate.onRequestFocus();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void addTab(@NotNull TabItem tabItem, @NotNull PartPresenter presenter) {
    partButtons.add(tabItem.getView());
    presenter.go(partViewContainer);
    tabs.put(presenter, tabItem);
  }

  /** {@inheritDoc} */
  @Override
  public void removeTab(@NotNull PartPresenter presenter) {
    TabItem tab = tabs.get(presenter);
    partButtons.remove(tab.getView());
    partStackContent.remove(presenter.getView());
    tabs.remove(presenter);
  }

  /** {@inheritDoc} */
  @Override
  public void setTabPositions(List<PartPresenter> presenters) {
    for (PartPresenter partPresenter : presenters) {
      int tabIndex = presenters.indexOf(partPresenter);
      TabItem tabItem = tabs.get(partPresenter);
      partButtons.insert(tabItem.getView(), tabIndex);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void selectTab(@NotNull PartPresenter partPresenter) {
    IsWidget view = partPresenter.getView();
    int viewIndex = partStackContent.getWidgetIndex(view);

    boolean isWidgetExist = viewIndex != -1;

    if (!isWidgetExist) {
      partPresenter.go(partViewContainer);

      viewIndex = partStackContent.getWidgetIndex(view);
    }

    partStackContent.showWidget(viewIndex);

    setActiveTab(partPresenter);
  }

  /**
   * Displays and sets part tab active.
   *
   * @param part
   */
  private void setActiveTab(@NotNull PartPresenter part) {
    for (TabItem tab : tabs.values()) {
      tab.unSelect();
    }

    tabs.get(part).select();

    delegate.onRequestFocus();
  }

  /** {@inheritDoc} */
  @Override
  public void setFocus(boolean focused) {
    if (focusedWidget != null) {
      focusedWidget.getElement().removeAttribute("focused");
    }

    focusedWidget = partStackContent.getVisibleWidget();

    if (focused && focusedWidget != null) {
      focusedWidget.getElement().setAttribute("focused", "");
    }
  }

  /** {@inheritDoc} */
  @Override
  public void updateTabItem(@NotNull PartPresenter partPresenter) {
    TabItem tabItem = tabs.get(partPresenter);

    tabItem.update(partPresenter);
  }

  @Override
  public void setMaximized(boolean maximized) {
    getElement().setAttribute("maximized", "" + maximized);
  }

  public void onResize() {
    if (partStackContent instanceof RequiresResize) {
      ((RequiresResize) partStackContent).onResize();
    }
  }
}
