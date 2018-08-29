/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.search.ui.text;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.SearchResultEvent;

/**
 * An event object describing that the filter state of the given {@link Match matches} has been
 * updated or {@link MatchFilter match filters} have been reconfigured.
 *
 * <p>Clients may instantiate or subclass this class.
 *
 * @since 3.3
 */
public class FilterUpdateEvent extends SearchResultEvent {

  private static final long serialVersionUID = 6009335074727417443L;

  private final Match[] fMatches;
  private final MatchFilter[] fFilters;

  /**
   * Constructs a new {@link FilterUpdateEvent}.
   *
   * @param searchResult the search result concerned
   * @param matches the matches updated by the filter change
   * @param filters the currently activated filters
   */
  public FilterUpdateEvent(ISearchResult searchResult, Match[] matches, MatchFilter[] filters) {
    super(searchResult);
    fMatches = matches;
    fFilters = filters;
  }

  /**
   * Returns the matches updated by the filter update.
   *
   * @return the matches updated by the filter update
   */
  public Match[] getUpdatedMatches() {
    return fMatches;
  }

  /**
   * Returns the the filters currently set, or <code>null</code> if filters have been disabled.
   *
   * @return the filters currently set
   */
  public MatchFilter[] getActiveFilters() {
    return fFilters;
  }
}
