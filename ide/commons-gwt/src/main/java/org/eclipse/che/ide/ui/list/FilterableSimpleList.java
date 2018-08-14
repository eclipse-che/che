/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.list;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * A focus panel witch contain {@link SimpleList} widget. Supports filtering items according to
 * typed from keyboard symbols.
 */
public class FilterableSimpleList<M> extends SimpleList<M> {
  private final FilterChangedHandler filterChangedHandler;
  private Map<String, M> filterableItems;
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
    super(view, view, view, css, itemRenderer, eventDelegate);
    this.filterChangedHandler = filterChangedHandler;
    this.filter = new StringBuilder();
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

  /** Clear the filter. */
  public void clearFilter() {
    filter.delete(0, filter.length() + 1);
  }

  /** Clear the filter and show all items. */
  public void resetFilter() {
    clearFilter();
    filterChangedHandler.onFilterChanged("");
    doFilter();
  }

  /** Remove last character from the filter and update items according to new filter value. */
  public void removeLastCharacter() {
    filter.deleteCharAt(filter.length() - 1);
    filterChangedHandler.onFilterChanged(filter.toString());
    doFilter();
  }

  /** Add character to the filter and update items according to new filter value. */
  public void addCharacterToFilter(String character) {
    filter.append(character);
    filterChangedHandler.onFilterChanged(filter.toString());
    doFilter();
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

  /** Returns the filter's value. */
  public String getFilter() {
    return filter.toString();
  }

  private void doFilter() {
    render(
        filterableItems
            .keySet()
            .stream()
            .filter(name -> name.toLowerCase().contains(filter.toString().toLowerCase()))
            .map(name -> filterableItems.get(name))
            .collect(Collectors.toList()));
  }
}
