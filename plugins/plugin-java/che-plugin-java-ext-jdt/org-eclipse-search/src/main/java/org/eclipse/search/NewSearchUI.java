/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.search;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;

/**
 * A facade for access to the new search UI facilities.
 *
 * @since 3.0
 */
public class NewSearchUI {
  //	/**
  //	 * Activates a search result view in the current workbench window page. If a
  //	 * search view is already open in the current workbench window page, it is
  //	 * activated. Otherwise a new search view is opened and activated.
  //	 *
  //	 * @return the activate search result view or <code>null</code> if the
  //	 *         search result view couldn't be activated
  //	 */
  //	public static ISearchResultViewPart activateSearchResultView() {
  //		return InternalSearchUI.getInstance().getSearchViewManager().activateSearchView(false);
  //	}
  //	/**
  //	 * Gets the search result view shown in the current workbench window.
  //	 *
  //	 * @return the search result view or <code>null</code>, if none is open
  //	 *         in the current workbench window page
  //	 */
  //	public static ISearchResultViewPart getSearchResultView() {
  //		return InternalSearchUI.getInstance().getSearchViewManager().getActiveSearchView();
  //	}
  //	/**
  //	 * Runs the given search query. This method may run the given query in a
  //	 * separate thread if <code>ISearchQuery#canRunInBackground()</code>
  //	 * returns <code>true</code>. Running a query adds it to the set of known
  //	 * queries and notifies any registered <code>IQueryListener</code>s about
  //	 * the addition.
  //	 *
  //	 * @param query
  //	 *            the query to execute
  //	 * @deprecated deprecated in 3.1.
  //	 * Use {@link #runQueryInBackground(ISearchQuery)} to run a query in background
  //	 * or {@link #runQueryInForeground(IRunnableContext, ISearchQuery)} to run it in foreground
  //	 */
  //	public static void runQuery(ISearchQuery query) {
  //		if (query == null) {
  //			throw new IllegalArgumentException("query must not be null"); //$NON-NLS-1$
  //		}
  //		if (query.canRunInBackground())
  //			runQueryInBackground(query);
  //		else {
  //			IStatus status= runQueryInForeground(null, query);
  //			if (status != null) {
  //				if (!status.isOK())
  //					SearchPlugin.log(status);
  //				if (status.getSeverity() == IStatus.ERROR) {
  //					ErrorDialog.openError(SearchPlugin.getActiveWorkbenchShell(),
  // SearchMessages.NewSearchUI_error_title, SearchMessages
  //							.NewSearchUI_error_label, status);
  //				}
  //			}
  //		}
  //	}

  /**
   * Runs the given search query. This method will execute the query in a background thread and not
   * block until the search is finished. Running a query adds it to the set of known queries and
   * notifies any registered {@link IQueryListener}s about the addition.
   *
   * <p>The search view that shows the result will be activated. That means a call to {@link
   * #activateSearchResultView} is not required.
   *
   * @param query the query to execute. The query must be able to run in background, that means
   *     {@link ISearchQuery#canRunInBackground()} must be <code>true</code>
   * @throws IllegalArgumentException Thrown when the passed query is not able to run in background
   * @since 3.1
   */
  public static void runQueryInBackground(ISearchQuery query) throws IllegalArgumentException {
    if (query == null) {
      throw new IllegalArgumentException("query must not be null"); // $NON-NLS-1$
    }
    runQueryInBackground(query, null);
  }

  /**
   * Runs the given search query. This method will execute the query in a background thread and not
   * block until the search is finished. Running a query adds it to the set of known queries and
   * notifies any registered {@link IQueryListener}s about the addition.
   *
   * <p>The result will be shown in the given search result view which will be activated. A call to
   * to {@link #activateSearchResultView} is not required.
   *
   * @param query the query to execute. The query must be able to run in background, that means
   *     {@link ISearchQuery#canRunInBackground()} must be <code>true</code>
   * @param view the search result view to show the result in. If <code>null</code> is passed in,
   *     the default activation mechanism is used to open a new result view or to select the view to
   *     be reused.
   * @throws IllegalArgumentException Thrown when the passed query is not able to run in background
   * @since 3.2
   */
  public static void runQueryInBackground(ISearchQuery query, Object view)
      throws IllegalArgumentException {
    if (query == null) {
      throw new IllegalArgumentException("query must not be null"); // $NON-NLS-1$
    }
    if (query.canRunInBackground())
      InternalSearchUI.getInstance().runSearchInBackground(query, view);
    else throw new IllegalArgumentException("Query can not be run in background"); // $NON-NLS-1$
  }

  /**
   * Runs the given search query. This method will execute the query in the same thread as the
   * caller. This method blocks until the query is finished. Running a query adds it to the set of
   * known queries and notifies any registered {@link IQueryListener}s about the addition.
   *
   * <p>The result will be shown in a search view that will be activated. That means a call to
   * {@link #activateSearchResultView} is not required.
   *
   * @param context the runnable context to run the query in
   * @param query the query to execute
   * @return a status indicating whether the query ran correctly, including {@link IStatus#CANCEL}
   *     to signal that the query was canceled.
   */
  public static IStatus runQueryInForeground(IRunnableContext context, ISearchQuery query) {
    if (query == null) {
      throw new IllegalArgumentException("query must not be null"); // $NON-NLS-1$
    }
    return runQueryInForeground(context, query, null);
  }

  /**
   * Runs the given search query. This method will execute the query in the same thread as the
   * caller. This method blocks until the query is finished. Running a query adds it to the set of
   * known queries and notifies any registered {@link IQueryListener}s about the addition.
   *
   * <p>The result will be shown in the given search result view which will be activated. A call to
   * to {@link #activateSearchResultView} is not required.
   *
   * @param context the runnable context to run the query in
   * @param query the query to execute
   * @param view the search result view to show the result in. If <code>null</code> is passed in,
   *     the default activation mechanism is used to open a new result view or to select the view to
   *     be reused.
   * @return a status indicating whether the query ran correctly, including {@link IStatus#CANCEL}
   *     to signal that the query was canceled.
   * @since 3.2
   */
  public static IStatus runQueryInForeground(
      IRunnableContext context, ISearchQuery query, Object view) {
    if (query == null) {
      throw new IllegalArgumentException("query must not be null"); // $NON-NLS-1$
    }
    return InternalSearchUI.getInstance().runSearchInForeground(context, query, view);
  }

  /**
   * Registers the given listener to receive notification of changes to queries. The listener will
   * be notified whenever a query has been added, removed, is starting or has finished. Has no
   * effect if an identical listener is already registered.
   *
   * @param l the listener to be added
   */
  public static void addQueryListener(IQueryListener l) {
    InternalSearchUI.getInstance().addQueryListener(l);
  }

  /**
   * Removes the given query listener. Does nothing if the listener is not present.
   *
   * @param l the listener to be removed.
   */
  public static void removeQueryListener(IQueryListener l) {
    InternalSearchUI.getInstance().removeQueryListener(l);
  }

  /**
   * Returns all search queries know to the search UI (i.e. registered via <code>runQuery()</code>
   * or <code>runQueryInForeground())</code>.
   *
   * @return all search results
   */
  public static ISearchQuery[] getQueries() {
    return InternalSearchUI.getInstance().getQueries();
  }

  /**
   * Returns whether the given query is currently running. Queries may be run by client request or
   * by actions in the search UI.
   *
   * @param query the query
   * @return whether the given query is currently running
   * @see org.eclipse.search.ui.NewSearchUI#runQueryInBackground(ISearchQuery)
   * @see org.eclipse.search.ui.NewSearchUI#runQueryInForeground(IRunnableContext, ISearchQuery)
   */
  public static boolean isQueryRunning(ISearchQuery query) {
    if (query == null) {
      throw new IllegalArgumentException("query must not be null"); // $NON-NLS-1$
    }
    return InternalSearchUI.getInstance().isQueryRunning(query);
  }

  /**
   * Sends a 'cancel' command to the given query running in background. The call has no effect if
   * the query is not running, not in background or is not cancelable.
   *
   * @param query the query
   * @since 3.1
   */
  public static void cancelQuery(ISearchQuery query) {
    if (query == null) {
      throw new IllegalArgumentException("query must not be null"); // $NON-NLS-1$
    }
    InternalSearchUI.getInstance().cancelSearch(query);
  }

  /**
   * Removes the given search query.
   *
   * @param query the query to be removed
   * @since 3.5
   */
  public static void removeQuery(ISearchQuery query) {
    InternalSearchUI.getInstance().removeQuery(query);
  }

  /** Search Plug-in Id (value <code>"org.eclipse.search"</code>). */
  public static final String PLUGIN_ID = "org.eclipse.search"; // $NON-NLS-1$

  /**
   * Search marker type (value <code>"org.eclipse.search.searchmarker"</code>).
   *
   * @see org.eclipse.core.resources.IMarker
   */
  public static final String SEARCH_MARKER = PLUGIN_ID + ".searchmarker"; // $NON-NLS-1$

  /** Id of the new Search view (value <code>"org.eclipse.search.ui.views.SearchView"</code>). */
  public static final String SEARCH_VIEW_ID =
      "org.eclipse.search.ui.views.SearchView"; // $NON-NLS-1$

  /** Id of the Search action set (value <code>"org.eclipse.search.searchActionSet"</code>). */
  public static final String ACTION_SET_ID = PLUGIN_ID + ".searchActionSet"; // $NON-NLS-1$

  //
  //	/**
  //	 * Opens the search dialog.
  //	 * If <code>pageId</code> is specified and a corresponding page
  //	 * is found then it is brought to top.
  //	 * @param window 	the parent window
  //	 *
  //	 * @param pageId	the page to select or <code>null</code>
  //	 * 					if the best fitting page should be selected
  //	 */
  //	public static void openSearchDialog(IWorkbenchWindow window, String pageId) {
  //		new OpenSearchDialogAction(window, pageId).run();
  //	}
  //
  //	/**
  //	 * Returns the preference whether editors should be reused
  //	 * when showing search results.
  //	 *
  //	 * The goto action can decide to use or ignore this preference.
  //	 *
  //	 * @return <code>true</code> if editors should be reused for showing search results
  //	 */
  //	public static boolean reuseEditor() {
  //		return SearchPreferencePage.isEditorReused();
  //	}

  /**
   * Returns the preference whether a search engine is allowed to report potential matches or not.
   *
   * <p>Search engines which can report inexact matches must respect this preference i.e. they
   * should not report inexact matches if this method returns <code>true</code>
   *
   * @return <code>true</code> if search engine must not report inexact matches
   */
  public static boolean arePotentialMatchesIgnored() {
    //		return SearchPreferencePage.arePotentialMatchesIgnored();
    return true;
  }

  //	/**
  //	 * Returns the ID of the default perspective.
  //	 * <p>
  //	 * The perspective with this ID will be used to show the Search view.
  //	 * If no default perspective is set then the Search view will
  //	 * appear in the current perspective.
  //	 * </p>
  //	 * @return the ID of the default perspective <code>null</code> if no default perspective is set
  //	 */
  //	public static String getDefaultPerspectiveId() {
  //		return SearchPreferencePage.getDefaultPerspectiveId();
  //	}

}
