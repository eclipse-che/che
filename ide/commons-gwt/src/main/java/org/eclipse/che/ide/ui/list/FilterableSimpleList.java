/**
 * Copyright (c) 2012-2017 Red Hat, Inc. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.list;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_BACKSPACE;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ESCAPE;

import com.google.gwt.user.client.ui.FocusPanel;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A focus panel witch contain {@link SimpleList} widget. Supports filtering items according to
 * typed from keyboard symbols.
 */
public class FilterableSimpleList<M> extends FocusPanel {
  private Map<String, M> filterableItems;
  private SimpleList<M> simpleList;
  private StringBuilder filter;

  public interface FilterChangedHandler {
    /** Is called when search filter is changed */
    void onFilterChanged(String filter);
  }

  private FilterableSimpleList(
      SimpleList.View view,
      SimpleList.Css css,
      SimpleList.ListItemRenderer<M> itemRenderer,
      SimpleList.ListEventDelegate<M> eventDelegate,
      FilterChangedHandler filterChangedHandler) {
    super();
    simpleList = SimpleList.create(view, css, itemRenderer, eventDelegate);
    this.getElement().setAttribute("style", "outline: 0");

    addKeyDownHandler(
        keyDownEvent -> {
          int keyCode = keyDownEvent.getNativeEvent().getKeyCode();
          if (keyCode == KEY_BACKSPACE) {
            filter.deleteCharAt(filter.length() - 1);
            filterChangedHandler.onFilterChanged(filter.toString());
          } else if (keyCode == KEY_ESCAPE && !filter.toString().isEmpty()) {
            clearFilter();
            keyDownEvent.stopPropagation();
            filterChangedHandler.onFilterChanged("");
          } else {
            return;
          }
          doFilter();
        });

    addKeyPressHandler(
        keyPressEvent -> {
          filter.append(String.valueOf(keyPressEvent.getCharCode()));
          filterChangedHandler.onFilterChanged(filter.toString());
          doFilter();
        });

    add(simpleList);
    filter = new StringBuilder();
  }

  public static <M> FilterableSimpleList<M> create(
      SimpleList.View view,
      SimpleList.Css simpleListCss,
      SimpleList.ListItemRenderer<M> itemRenderer,
      SimpleList.ListEventDelegate<M> eventDelegate,
      FilterChangedHandler filterChangedHandler) {
    return new FilterableSimpleList<>(
        view, simpleListCss, itemRenderer, eventDelegate, filterChangedHandler);
  }

  /**
   * Render the list with given items.
   *
   * @param filterableItems map with filterable value as a key and the item as a value
   */
  public void render(Map<String, M> filterableItems) {
    this.filterableItems = filterableItems;
    doFilter();
  }

  /** Reset the filter. */
  public void clearFilter() {
    filter.delete(0, filter.length() + 1);
  }

  /** Returns the selection model from parent {@link SimpleList} widget. */
  public HasSelection<M> getSelectionModel() {
    return simpleList.getSelectionModel();
  }

  private void doFilter() {
    simpleList.render(
        filterableItems
            .keySet()
            .stream()
            .filter(name -> name.startsWith(filter.toString()))
            .map(name -> filterableItems.get(name))
            .collect(Collectors.toList()));
  }
}
