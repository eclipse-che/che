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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.search.NewSearchUI;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.Match;

public class SearchResultUpdater implements IElementChangedListener, IQueryListener {

  JavaSearchResult fResult;
  private static final int REMOVED_FLAGS =
      IJavaElementDelta.F_MOVED_TO
          | IJavaElementDelta.F_REMOVED_FROM_CLASSPATH
          | IJavaElementDelta.F_CLOSED
          | IJavaElementDelta.F_CONTENT;

  public SearchResultUpdater(JavaSearchResult result) {
    fResult = result;
    NewSearchUI.addQueryListener(this);
    JavaCore.addElementChangedListener(this);
    // TODO make this work with resources
  }

  public void elementChanged(ElementChangedEvent event) {
    // long t0= System.currentTimeMillis();
    IJavaElementDelta delta = event.getDelta();
    Set<IAdaptable> removedElements = new HashSet<IAdaptable>();
    Set<IAdaptable> potentiallyRemovedElements = new HashSet<IAdaptable>();
    collectRemoved(potentiallyRemovedElements, removedElements, delta);
    if (removedElements.size() > 0) handleRemoved(removedElements);
    if (potentiallyRemovedElements.size() > 0) handleRemoved(potentiallyRemovedElements);
    // System.out.println(this+"handled delta in: "+(System.currentTimeMillis()-t0));
  }

  private void handleRemoved(Set<IAdaptable> removedElements) {
    Object[] elements = fResult.getElements();
    for (int i = 0; i < elements.length; i++) {
      if (isContainedInRemoved(removedElements, elements[i])) {
        if (elements[i] instanceof IJavaElement) {
          IJavaElement je = (IJavaElement) elements[i];
          if (!je.exists()) {
            // System.out.println("removing: "+je+" in "+fResult.getUserData());
            Match[] matches = fResult.getMatches(elements[i]);
            for (int j = 0; j < matches.length; j++) {
              fResult.removeMatch(matches[j]);
            }
          }
        } else if (elements[i] instanceof IResource) {
          IResource resource = (IResource) elements[i];
          if (!resource.exists()) {
            // System.out.println("removing: "+resource+" in "+fResult.getUserData());
            Match[] matches = fResult.getMatches(elements[i]);
            for (int j = 0; j < matches.length; j++) {
              fResult.removeMatch(matches[j]);
            }
          }
        }
      }
    }
  }

  private boolean isContainedInRemoved(Set<IAdaptable> removedElements, Object object) {
    for (Iterator<IAdaptable> elements = removedElements.iterator(); elements.hasNext(); ) {
      if (isParentOf(elements.next(), object)) return true;
    }
    return false;
  }

  private boolean isParentOf(Object ancestor, Object descendant) {
    while (descendant != null && !ancestor.equals(descendant)) descendant = getParent(descendant);
    return descendant != null;
  }

  private Object getParent(Object object) {
    if (object instanceof IJavaElement) return ((IJavaElement) object).getParent();
    else if (object instanceof IResource) return ((IResource) object).getParent();
    return null;
  }

  private void collectRemoved(
      Set<IAdaptable> potentiallyRemovedSet,
      Set<IAdaptable> removedElements,
      IJavaElementDelta delta) {
    if (delta.getKind() == IJavaElementDelta.REMOVED) removedElements.add(delta.getElement());
    else if (delta.getKind() == IJavaElementDelta.CHANGED) {
      int flags = delta.getFlags();
      if ((flags & REMOVED_FLAGS) != 0) {
        potentiallyRemovedSet.add(delta.getElement());
      } else {
        IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
        for (int i = 0; i < childDeltas.length; i++) {
          collectRemoved(potentiallyRemovedSet, removedElements, childDeltas[i]);
        }
      }
    }
    IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
    if (resourceDeltas != null) {
      for (int i = 0; i < resourceDeltas.length; i++) {
        collectRemovals(removedElements, resourceDeltas[i]);
      }
    }
  }

  public void queryAdded(ISearchQuery query) {
    // don't care
  }

  public void queryRemoved(ISearchQuery query) {
    if (fResult.equals(query.getSearchResult())) {
      JavaCore.removeElementChangedListener(this);
      NewSearchUI.removeQueryListener(this);
    }
  }

  private void collectRemovals(Set<IAdaptable> removals, IResourceDelta delta) {
    if (delta.getKind() == IResourceDelta.REMOVED) removals.add(delta.getResource());
    else {
      IResourceDelta[] children = delta.getAffectedChildren();
      for (int i = 0; i < children.length; i++) {
        collectRemovals(removals, children[i]);
      }
    }
  }

  public void queryStarting(ISearchQuery query) {
    // not interested
  }

  public void queryFinished(ISearchQuery query) {
    // not interested
  }
}
