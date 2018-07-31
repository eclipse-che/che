/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.multisplitpanel;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ui.multisplitpanel.panel.SubPanelView;

/**
 * A panel that represents a tabbed set of pages, each of which contains another widget. Its child
 * widgets are shown as the user selects the various tabs associated with them.
 *
 * <p>A panel may be split on two sub-panels vertically or horizontally. Each sub-panel may be
 * closed.
 *
 * <p>{@link SubPanelFactory#newPanel()} should be used in order to create new {@link SubPanel}.
 *
 * @author Artem Zatsarynnyi
 */
public interface SubPanel {

  /** Returns the panel's view. */
  SubPanelView getView();

  /** Split this panel horizontally on two sub-panels. */
  void splitHorizontally();

  /** Split this panel vertically on two sub-panels. */
  void splitVertically();

  /**
   * Add the given {@code widget} to this panel.
   *
   * @param widget widget to add
   * @param removable whether the {@code widget} may be removed by user from UI
   * @param widgetRemovingListener listener to be notified when the specified {@code widget} is
   *     going to be removed from the panel
   */
  void addWidget(
      WidgetToShow widget,
      boolean removable,
      @Nullable WidgetRemovingListener widgetRemovingListener);

  /** Show (activate) the {@code widget} if it exists on this panel. */
  void activateWidget(WidgetToShow widget);

  /** Returns list of all widgets added to the panel. */
  List<WidgetToShow> getAllWidgets();

  /**
   * Remove the given {@code widget} from this panel.
   *
   * @param widget widget to remove
   */
  void removeWidget(WidgetToShow widget);

  /** Close this panel. Note that each widget will be removed from the panel before it close. */
  void closePane();

  /**
   * Set the listener to be notified when some widget on this panel or on any child sub-panel gains
   * the focus.
   */
  void setFocusListener(FocusListener listener);

  /**
   * Set the listener to be notified when some widget on this panel or on any child sub-panel has
   * been double clicked.
   */
  void setDoubleClickListener(DoubleClickListener listener);

  /** Set the listener to be notified when Add Tab button has been clicked. */
  void setAddTabButtonClickListener(AddTabButtonClickListener listener);

  interface WidgetRemovingListener {
    /** Invoked when a widget is going to be removed. */
    void onWidgetRemoving(RemoveCallback removeCallback);
  }

  /** Callback that may be used for actual removing widget. */
  interface RemoveCallback {
    /** Tells panel to remove widget. */
    void remove();
  }

  interface FocusListener {
    /** Invoked when a {@code widget} on a {@code panel} gains the focus. */
    void focusGained(SubPanel panel, IsWidget widget);
  }

  interface DoubleClickListener {
    /** Invoked when a {@code widget} on a {@code panel} has been double clicked. */
    void onDoubleClicked(SubPanel panel, IsWidget widget);
  }

  interface AddTabButtonClickListener {
    /** Invoked when `Add Tab` button has been clicked. */
    void onAddTabButtonClicked(int mouseX, int mouseY);
  }
}
