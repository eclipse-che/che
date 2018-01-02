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
 * An event indicating that all matches have been removed from a <code>AbstractTextSearchResult
 * </code>.
 *
 * <p>Clients may instantiate or subclass this class.
 *
 * @since 3.0
 */
public class RemoveAllEvent extends SearchResultEvent {
  private static final long serialVersionUID = 6009335074727417445L;
  /**
   * A constructor
   *
   * @param searchResult the search result this event is about
   */
  public RemoveAllEvent(ISearchResult searchResult) {
    super(searchResult);
  }
}
