/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.search;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.search.IMatchPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

public class JavaSearchResult extends AbstractJavaSearchResult {

  private final JavaSearchQuery fQuery;
  private final Map<Object, IMatchPresentation> fElementsToParticipants;

  public JavaSearchResult(JavaSearchQuery query) {
    fQuery = query;
    fElementsToParticipants = new HashMap<Object, IMatchPresentation>();
    setActiveMatchFilters(JavaMatchFilter.getLastUsedFilters());
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor()
   */
  public ImageDescriptor getImageDescriptor() {
    return fQuery.getImageDescriptor();
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.ISearchResult#getLabel()
   */
  public String getLabel() {
    return fQuery.getResultLabel(getMatchCount());
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.ISearchResult#getTooltip()
   */
  public String getTooltip() {
    return getLabel();
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.text.AbstractTextSearchResult#setMatchFilters(org.eclipse.search.ui.text.MatchFilter[])
   */
  @Override
  public void setActiveMatchFilters(MatchFilter[] filters) {
    super.setActiveMatchFilters(filters);
    JavaMatchFilter.setLastUsedFilters(filters);
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getAllMatchFilters()
   */
  @Override
  public MatchFilter[] getAllMatchFilters() {
    return JavaMatchFilter.allFilters(fQuery);
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.ISearchResult#getQuery()
   */
  public ISearchQuery getQuery() {
    return fQuery;
  }

  synchronized IMatchPresentation getSearchParticpant(Object element) {
    return fElementsToParticipants.get(element);
  }

  boolean addMatch(Match match, IMatchPresentation participant) {
    Object element = match.getElement();
    if (fElementsToParticipants.get(element) != null) {
      // TODO must access the participant id / label to properly report the error.
      JavaPlugin.log(
          new Status(
              IStatus.WARNING,
              JavaPlugin.getPluginId(),
              0,
              "A second search participant was found for an element",
              null)); // $NON-NLS-1$
      return false;
    }
    fElementsToParticipants.put(element, participant);
    addMatch(match);
    return true;
  }

  @Override
  public void removeAll() {
    synchronized (this) {
      fElementsToParticipants.clear();
    }
    super.removeAll();
  }

  @Override
  public void removeMatch(Match match) {
    synchronized (this) {
      if (getMatchCount(match.getElement()) == 1)
        fElementsToParticipants.remove(match.getElement());
    }
    super.removeMatch(match);
  }
}
