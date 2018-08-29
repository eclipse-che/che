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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;

/**
 * An abstract base implementation for text-match based search results. This search result
 * implementation consists of a list of {@link Match matches}. No assumptions are made about the
 * kind of elements these matches are reported against.
 *
 * @since 3.0
 */
public abstract class AbstractTextSearchResult implements ISearchResult {

  private static final Match[] EMPTY_ARRAY = new Match[0];

  private final Map fElementsToMatches;
  private final List fListeners;
  private final MatchEvent fMatchEvent;

  private MatchFilter[] fMatchFilters;

  /** Constructs a new <code>AbstractTextSearchResult</code> */
  protected AbstractTextSearchResult() {
    fElementsToMatches = new HashMap();
    fListeners = new ArrayList();
    fMatchEvent = new MatchEvent(this);

    fMatchFilters = null; // filtering disabled by default
  }

  /**
   * Returns an array with all matches reported against the given element. Note that all matches of
   * the given element are returned. The filter state of the matches is not relevant.
   *
   * @param element the element to report matches for
   * @return all matches reported for this element
   * @see Match#getElement()
   */
  public Match[] getMatches(Object element) {
    synchronized (fElementsToMatches) {
      List matches = (List) fElementsToMatches.get(element);
      if (matches != null) return (Match[]) matches.toArray(new Match[matches.size()]);
      return EMPTY_ARRAY;
    }
  }

  /**
   * Adds a <code>Match</code> to this search result. This method does nothing if the match is
   * already present.
   *
   * <p>Subclasses may extend this method.
   *
   * @param match the match to add
   */
  public void addMatch(Match match) {
    boolean hasAdded = false;
    synchronized (fElementsToMatches) {
      hasAdded = doAddMatch(match);
    }
    if (hasAdded) fireChange(getSearchResultEvent(match, MatchEvent.ADDED));
  }

  /**
   * Adds a number of Matches to this search result. This method does nothing for matches that are
   * already present.
   *
   * <p>Subclasses may extend this method.
   *
   * @param matches the matches to add
   */
  public void addMatches(Match[] matches) {
    Collection reallyAdded = new ArrayList();
    synchronized (fElementsToMatches) {
      for (int i = 0; i < matches.length; i++) {
        if (doAddMatch(matches[i])) reallyAdded.add(matches[i]);
      }
    }
    if (!reallyAdded.isEmpty()) fireChange(getSearchResultEvent(reallyAdded, MatchEvent.ADDED));
  }

  private MatchEvent getSearchResultEvent(Match match, int eventKind) {
    fMatchEvent.setKind(eventKind);
    fMatchEvent.setMatch(match);
    return fMatchEvent;
  }

  private MatchEvent getSearchResultEvent(Collection matches, int eventKind) {
    fMatchEvent.setKind(eventKind);
    Match[] matchArray = (Match[]) matches.toArray(new Match[matches.size()]);
    fMatchEvent.setMatches(matchArray);
    return fMatchEvent;
  }

  private boolean doAddMatch(Match match) {
    updateFilterState(match);

    List matches = (List) fElementsToMatches.get(match.getElement());
    if (matches == null) {
      matches = new ArrayList();
      fElementsToMatches.put(match.getElement(), matches);
      matches.add(match);
      return true;
    }
    if (!matches.contains(match)) {
      insertSorted(matches, match);
      return true;
    }
    return false;
  }

  private static void insertSorted(List matches, Match match) {
    int insertIndex = getInsertIndex(matches, match);
    matches.add(insertIndex, match);
  }

  private static int getInsertIndex(List matches, Match match) {
    int count = matches.size();
    int min = 0, max = count - 1;
    while (min <= max) {
      int mid = (min + max) / 2;
      Match data = (Match) matches.get(mid);
      int compare = compare(match, data);
      if (compare > 0) max = mid - 1;
      else min = mid + 1;
    }
    return min;
  }

  private static int compare(Match match1, Match match2) {
    int diff = match2.getOffset() - match1.getOffset();
    if (diff != 0) return diff;
    return match2.getLength() - match1.getLength();
  }

  /**
   * Removes all matches from this search result.
   *
   * <p>Subclasses may extend this method.
   */
  public void removeAll() {
    synchronized (fElementsToMatches) {
      doRemoveAll();
    }
    fireChange(new RemoveAllEvent(this));
  }

  private void doRemoveAll() {
    fElementsToMatches.clear();
  }

  /**
   * Removes the given match from this search result. This method has no effect if the match is not
   * found.
   *
   * <p>Subclasses may extend this method.
   *
   * @param match the match to remove
   */
  public void removeMatch(Match match) {
    boolean existed = false;
    synchronized (fElementsToMatches) {
      existed = doRemoveMatch(match);
    }
    if (existed) fireChange(getSearchResultEvent(match, MatchEvent.REMOVED));
  }

  /**
   * Removes the given matches from this search result. This method has no effect for matches that
   * are not found
   *
   * <p>Subclasses may extend this method.
   *
   * @param matches the matches to remove
   */
  public void removeMatches(Match[] matches) {
    Collection existing = new ArrayList();
    synchronized (fElementsToMatches) {
      for (int i = 0; i < matches.length; i++) {
        if (doRemoveMatch(matches[i]))
          existing.add(matches[i]); // no duplicate matches at this point
      }
    }
    if (!existing.isEmpty()) fireChange(getSearchResultEvent(existing, MatchEvent.REMOVED));
  }

  private boolean doRemoveMatch(Match match) {
    boolean existed = false;
    List matches = (List) fElementsToMatches.get(match.getElement());
    if (matches != null) {
      existed = matches.remove(match);
      if (matches.isEmpty()) fElementsToMatches.remove(match.getElement());
    }
    return existed;
  }

  /** {@inheritDoc} */
  public void addListener(ISearchResultListener l) {
    synchronized (fListeners) {
      fListeners.add(l);
    }
  }

  /** {@inheritDoc} */
  public void removeListener(ISearchResultListener l) {
    synchronized (fListeners) {
      fListeners.remove(l);
    }
  }

  /**
   * Send the given <code>SearchResultEvent</code> to all registered search result listeners.
   *
   * @param e the event to be sent
   * @see ISearchResultListener
   */
  protected void fireChange(SearchResultEvent e) {
    HashSet copiedListeners = new HashSet();
    synchronized (fListeners) {
      copiedListeners.addAll(fListeners);
    }
    Iterator listeners = copiedListeners.iterator();
    while (listeners.hasNext()) {
      ((ISearchResultListener) listeners.next()).searchResultChanged(e);
    }
  }

  private void updateFilterStateForAllMatches() {
    boolean disableFiltering = getActiveMatchFilters() == null;
    ArrayList changed = new ArrayList();
    Object[] elements = getElements();
    for (int i = 0; i < elements.length; i++) {
      Match[] matches = getMatches(elements[i]);
      for (int k = 0; k < matches.length; k++) {
        if (disableFiltering || updateFilterState(matches[k])) {
          changed.add(matches[k]);
        }
      }
    }
    Match[] allChanges = (Match[]) changed.toArray(new Match[changed.size()]);
    fireChange(new FilterUpdateEvent(this, allChanges, getActiveMatchFilters()));
  }

  /*
   * Evaluates the filter for the match and updates it. Return true if the filter changed.
   */
  private boolean updateFilterState(Match match) {
    MatchFilter[] matchFilters = getActiveMatchFilters();
    if (matchFilters == null) {
      return false; // do nothing, no change
    }

    boolean oldState = match.isFiltered();
    for (int i = 0; i < matchFilters.length; i++) {
      if (matchFilters[i].filters(match)) {
        match.setFiltered(true);
        return !oldState;
      }
    }
    match.setFiltered(false);
    return oldState;
  }

  /**
   * Returns the total number of matches contained in this search result. The filter state of the
   * matches is not relevant when counting matches. All matches are counted.
   *
   * @return total number of matches
   */
  public int getMatchCount() {
    int count = 0;
    synchronized (fElementsToMatches) {
      for (Iterator elements = fElementsToMatches.values().iterator(); elements.hasNext(); ) {
        List element = (List) elements.next();
        if (element != null) count += element.size();
      }
    }
    return count;
  }

  /**
   * Returns the number of matches reported against a given element. This is equivalent to calling
   * <code>getMatches(element).length</code> The filter state of the matches is not relevant when
   * counting matches. All matches are counted.
   *
   * @param element the element to get the match count for
   * @return the number of matches reported against the element
   */
  public int getMatchCount(Object element) {
    List matches = (List) fElementsToMatches.get(element);
    if (matches != null) return matches.size();
    return 0;
  }

  /**
   * Returns an array containing the set of all elements that matches are reported against in this
   * search result. Note that all elements that contain matches are returned. The filter state of
   * the matches is not relevant.
   *
   * @return the set of elements in this search result
   */
  public Object[] getElements() {
    synchronized (fElementsToMatches) {
      return fElementsToMatches.keySet().toArray();
    }
  }

  /**
   * Sets the active match filters for this result. If set to non-null, the match filters will be
   * used to update the filter state ({@link Match#isFiltered()} of matches and the {@link
   * AbstractTextSearchViewPage} will only show non-filtered matches. If <code>null</code> is set
   * the filter state of the match is ignored by the {@link AbstractTextSearchViewPage} and all
   * matches are shown. Note the model contains all matches, regardless if the filter state of a
   * match.
   *
   * @param filters the match filters to set or <code>null</code> if the filter state of the match
   *     should be ignored.
   * @since 3.3
   */
  public void setActiveMatchFilters(MatchFilter[] filters) {
    fMatchFilters = filters;
    updateFilterStateForAllMatches();
  }

  /**
   * Returns the active match filters for this result. If not null is returned, the match filters
   * will be used to update the filter state ({@link Match#isFiltered()} of matches and the {@link
   * AbstractTextSearchViewPage} will only show non-filtered matches. If <code>null</code> is set
   * the filter state of the match is ignored by the {@link AbstractTextSearchViewPage} and all
   * matches are shown.
   *
   * @return the match filters to be used or <code>null</code> if the filter state of the match
   *     should be ignored.
   * @since 3.3
   */
  public MatchFilter[] getActiveMatchFilters() {
    return fMatchFilters;
  }

  /**
   * Returns all applicable filters for this result or null if match filters are not supported. If
   * match filters are returned, the {@link AbstractTextSearchViewPage} will contain menu entries in
   * the view menu.
   *
   * @return all applicable filters for this result.
   * @since 3.3
   */
  public MatchFilter[] getAllMatchFilters() {
    return null;
  }

  /**
   * Returns an implementation of <code>IEditorMatchAdapter</code> appropriate for this search
   * result.
   *
   * @return an appropriate adapter or <code>null</code> if none has been implemented
   * @see IEditorMatchAdapter
   */
  public abstract IEditorMatchAdapter getEditorMatchAdapter();

  /**
   * Returns an implementation of <code>IFileMatchAdapter</code> appropriate for this search result.
   *
   * @return an appropriate adapter or <code>null</code> if none has been implemented
   * @see IFileMatchAdapter
   */
  public abstract IFileMatchAdapter getFileMatchAdapter();
}
