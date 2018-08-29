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
 * A listener for changes to the set of search queries. Queries are added by running them via {@link
 * org.eclipse.search.ui.NewSearchUI#runQueryInBackground(ISearchQuery)
 * NewSearchUI#runQueryInBackground(ISearchQuery)} or {@link
 * org.eclipse.search.ui.NewSearchUI#runQueryInForeground(org.eclipse.jface.operation.IRunnableContext,ISearchQuery)
 * NewSearchUI#runQueryInForeground(IRunnableContext,ISearchQuery)}
 *
 * <p>The search UI determines when queries are rerun, stopped or deleted (and will notify
 * interested parties via this interface). Listeners can be added and removed in the {@link
 * org.eclipse.search.ui.NewSearchUI NewSearchUI} class.
 *
 * <p>Clients may implement this interface.
 *
 * @since 3.0
 */
public interface IQueryListener {
  /**
   * Called when an query has been added to the system.
   *
   * @param query the query that has been added
   */
  void queryAdded(ISearchQuery query);
  /**
   * Called when a query has been removed.
   *
   * @param query the query that has been removed
   */
  void queryRemoved(ISearchQuery query);

  /**
   * Called before an <code>ISearchQuery</code> is starting.
   *
   * @param query the query about to start
   */
  void queryStarting(ISearchQuery query);

  /**
   * Called after an <code>ISearchQuery</code> has finished.
   *
   * @param query the query that has finished
   */
  void queryFinished(ISearchQuery query);
}
