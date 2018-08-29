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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;

class QueryManager {
  private List fQueries;
  private List fListeners;

  public QueryManager() {
    super();
    // an ArrayList should be plenty fast enough (few searches).
    fListeners = new ArrayList();
    fQueries = new LinkedList();
  }

  public boolean hasQueries() {
    synchronized (this) {
      return !fQueries.isEmpty();
    }
  }

  public int getSize() {
    synchronized (this) {
      return fQueries.size();
    }
  }

  /**
   * Returns the queries in LRU order. Smaller index means more recently used.
   *
   * @return all queries
   */
  public ISearchQuery[] getQueries() {
    synchronized (this) {
      return (ISearchQuery[]) fQueries.toArray(new ISearchQuery[fQueries.size()]);
    }
  }

  public void removeQuery(ISearchQuery query) {
    synchronized (this) {
      fQueries.remove(query);
    }
    fireRemoved(query);
  }

  public void addQuery(ISearchQuery query) {
    synchronized (this) {
      if (fQueries.contains(query)) return;
      fQueries.add(0, query);
    }
    fireAdded(query);
  }

  public void addQueryListener(IQueryListener l) {
    synchronized (fListeners) {
      fListeners.add(l);
    }
  }

  public void removeQueryListener(IQueryListener l) {
    synchronized (fListeners) {
      fListeners.remove(l);
    }
  }

  public void fireAdded(ISearchQuery query) {
    Set copiedListeners = new HashSet();
    synchronized (fListeners) {
      copiedListeners.addAll(fListeners);
    }
    Iterator listeners = copiedListeners.iterator();
    while (listeners.hasNext()) {
      IQueryListener l = (IQueryListener) listeners.next();
      l.queryAdded(query);
    }
  }

  public void fireRemoved(ISearchQuery query) {
    Set copiedListeners = new HashSet();
    synchronized (fListeners) {
      copiedListeners.addAll(fListeners);
    }
    Iterator listeners = copiedListeners.iterator();
    while (listeners.hasNext()) {
      IQueryListener l = (IQueryListener) listeners.next();
      l.queryRemoved(query);
    }
  }

  public void fireStarting(ISearchQuery query) {
    Set copiedListeners = new HashSet();
    synchronized (fListeners) {
      copiedListeners.addAll(fListeners);
    }
    Iterator listeners = copiedListeners.iterator();
    while (listeners.hasNext()) {
      IQueryListener l = (IQueryListener) listeners.next();
      l.queryStarting(query);
    }
  }

  public void fireFinished(ISearchQuery query) {
    Set copiedListeners = new HashSet();
    synchronized (fListeners) {
      copiedListeners.addAll(fListeners);
    }
    Iterator listeners = copiedListeners.iterator();
    while (listeners.hasNext()) {
      IQueryListener l = (IQueryListener) listeners.next();
      l.queryFinished(query);
    }
  }

  public void removeAll() {
    synchronized (this) {
      List old = fQueries;
      fQueries = new LinkedList();
      Iterator iter = old.iterator();
      while (iter.hasNext()) {
        ISearchQuery element = (ISearchQuery) iter.next();
        fireRemoved(element);
      }
    }
  }

  public void queryFinished(ISearchQuery query) {
    fireFinished(query);
  }

  public void queryStarting(ISearchQuery query) {
    fireStarting(query);
  }

  public void touch(ISearchQuery query) {
    synchronized (this) {
      if (fQueries.contains(query)) {
        fQueries.remove(query);
        fQueries.add(0, query);
      }
    }
  }
}
