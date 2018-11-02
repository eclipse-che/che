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
package org.eclipse.che.ide.ui.multisplitpanel.panel;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanelFactory;
import org.eclipse.che.ide.ui.multisplitpanel.WidgetToShow;
import org.eclipse.che.ide.ui.multisplitpanel.actions.ClosePaneAction;
import org.eclipse.che.ide.ui.multisplitpanel.actions.RemoveAllWidgetsInPaneAction;
import org.eclipse.che.ide.ui.multisplitpanel.actions.SplitHorizontallyAction;
import org.eclipse.che.ide.ui.multisplitpanel.actions.SplitVerticallyAction;

/**
 * Presenter for {@link SubPanel}.
 *
 * @author Artem Zatsarynnyi
 */
public class SubPanelPresenter implements SubPanel, SubPanelView.ActionDelegate {

  private final SubPanelView view;
  private final SubPanelFactory subPanelFactory;
  private final List<WidgetToShow> widgets;
  private final Map<IsWidget, WidgetRemovingListener> removingListeners;

  private FocusListener focusListener;
  private DoubleClickListener doubleClickListener;
  private AddTabButtonClickListener addTabButtonClickListener;

  @AssistedInject
  public SubPanelPresenter(
      SubPanelFactory subPanelFactory, SubPanelViewFactory subPanelViewFactory) {
    this(subPanelFactory, subPanelViewFactory, null);
  }

  @AssistedInject
  public SubPanelPresenter(
      SubPanelFactory subPanelFactory,
      SubPanelViewFactory subPanelViewFactory,
      @Assisted @Nullable SubPanel parentPanel) {
    this.subPanelFactory = subPanelFactory;

    widgets = new ArrayList<>();

    this.view =
        subPanelViewFactory.createView(
            new ClosePaneAction(this),
            new RemoveAllWidgetsInPaneAction(this),
            new SplitHorizontallyAction(this),
            new SplitVerticallyAction(this));

    removingListeners = new HashMap<>();

    view.setDelegate(this);

    if (parentPanel != null) {
      view.setParentPanel(parentPanel.getView());
    } else {
      view.setParentPanel(null);
    }
  }

  @Override
  public SubPanelView getView() {
    return view;
  }

  @Override
  public void splitHorizontally() {
    final SubPanel subPanel = subPanelFactory.newPanel(this);
    subPanel.setFocusListener(focusListener);
    subPanel.setDoubleClickListener(doubleClickListener);
    subPanel.setAddTabButtonClickListener(addTabButtonClickListener);
    view.splitHorizontally(subPanel.getView());
  }

  @Override
  public void splitVertically() {
    final SubPanel subPanel = subPanelFactory.newPanel(this);
    subPanel.setFocusListener(focusListener);
    subPanel.setDoubleClickListener(doubleClickListener);
    subPanel.setAddTabButtonClickListener(addTabButtonClickListener);
    view.splitVertically(subPanel.getView());
  }

  @Override
  public void addWidget(
      WidgetToShow widget,
      boolean removable,
      @Nullable WidgetRemovingListener widgetRemovingListener) {
    // just activate the widget if it's already exists on the panel
    if (widgets.contains(widget)) {
      activateWidget(widget);
      return;
    }

    widgets.add(widget);

    if (widgetRemovingListener != null) {
      removingListeners.put(widget.getWidget(), widgetRemovingListener);
    }

    view.addWidget(widget, removable);
  }

  @Override
  public void activateWidget(WidgetToShow widget) {
    view.activateWidget(widget);
  }

  @Override
  public List<WidgetToShow> getAllWidgets() {
    return new ArrayList<>(widgets);
  }

  @Override
  public void removeWidget(WidgetToShow widget, ActiveTabClosedHandler handler) {
    view.removeWidget(widget, handler);
    widgets.remove(widget);
    removingListeners.remove(widget.getWidget());
  }

  @Override
  public void closePane() {
    view.closePanel();
  }

  @Override
  public void setFocusListener(FocusListener listener) {
    focusListener = listener;
  }

  @Override
  public void setDoubleClickListener(DoubleClickListener listener) {
    doubleClickListener = listener;
  }

  @Override
  public void setAddTabButtonClickListener(AddTabButtonClickListener listener) {
    addTabButtonClickListener = listener;
  }

  @Override
  public void onWidgetFocused(IsWidget widget) {
    focusListener.focusGained(this, widget);
  }

  @Override
  public void onWidgetDoubleClicked(IsWidget widget) {
    doubleClickListener.onDoubleClicked(this, widget);
  }

  @Override
  public void onWidgetRemoving(IsWidget widget, RemoveCallback removeCallback) {
    final WidgetRemovingListener listener = removingListeners.get(widget);
    if (listener != null) {
      listener.onWidgetRemoving(removeCallback);
    }

    widgets.removeIf(widgetToShow -> widgetToShow.getWidget().equals(widget));
  }

  @Override
  public void onAddTabButtonClicked(int mouseX, int mouseY) {
    if (addTabButtonClickListener != null) {
      addTabButtonClickListener.onAddTabButtonClicked(mouseX, mouseY);
    }
  }
}
