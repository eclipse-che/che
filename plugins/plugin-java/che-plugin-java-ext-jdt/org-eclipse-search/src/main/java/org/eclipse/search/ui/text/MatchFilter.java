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

/**
 * A match filter is used to evaluate the filter state of a match ({@link Match#isFiltered()}.
 * Filters are managed by the ({@link AbstractTextSearchResult}.
 *
 * @since 3.3
 */
public abstract class MatchFilter {

  /**
   * Returns whether the given match is filtered by this filter.
   *
   * @param match the match to look at
   * @return returns <code>true</code> if the given match should be filtered or <code>false</code>
   *     if not.
   */
  public abstract boolean filters(Match match);

  /**
   * Returns the name of the filter as shown in the match filter selection dialog.
   *
   * @return the name of the filter as shown in the match filter selection dialog.
   */
  public abstract String getName();

  /**
   * Returns the description of the filter as shown in the match filter selection dialog.
   *
   * @return the description of the filter as shown in the match filter selection dialog.
   */
  public abstract String getDescription();

  /**
   * Returns the label of the filter as shown by the filter action.
   *
   * @return the label of the filter as shown by the filter action.
   */
  public abstract String getActionLabel();

  /**
   * Returns an ID of this filter.
   *
   * @return the id of the filter to be used when persisting this filter.
   */
  public abstract String getID();
}
