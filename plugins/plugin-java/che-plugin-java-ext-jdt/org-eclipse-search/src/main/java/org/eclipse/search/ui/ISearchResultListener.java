/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.search.ui;
/**
 * Listener interface for changes to an <code>ISearchResult</code>. Implementers of <code>
 * ISearchResult</code> should define subclasses of <code>SearchResultEvent</code> and send those to
 * registered listeners. Implementers of <code>ISearchResultListener</code> will in general know the
 * concrete class of search result they are listening to, and therefore the kind of events they have
 * to handle.
 *
 * <p>Clients may implement this interface.
 *
 * @since 3.0
 */
public interface ISearchResultListener {
  /**
   * Called to notify listeners of changes in a <code>ISearchResult</code>. The event object <code>e
   * </code> is only guaranteed to be valid for the duration of the call.
   *
   * @param e the event object describing the change. Note that implementers of <code>ISearchResult
   *     </code> will be sending subclasses of <code>SearchResultEvent</code>
   */
  void searchResultChanged(SearchResultEvent e);
}
