/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeHierarchyChangedListener;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A thread-safe cache for super type hierarchies.
 *
 * @see JDTUIHelperClasses
 */
public class SuperTypeHierarchyCache {

  private static class HierarchyCacheEntry implements ITypeHierarchyChangedListener {

    private ITypeHierarchy fTypeHierarchy;
    private long fLastAccess;

    public HierarchyCacheEntry(ITypeHierarchy hierarchy) {
      fTypeHierarchy = hierarchy;
      fTypeHierarchy.addTypeHierarchyChangedListener(this);
      markAsAccessed();
    }

    public void typeHierarchyChanged(ITypeHierarchy typeHierarchy) {
      removeHierarchyEntryFromCache(this);
    }

    public ITypeHierarchy getTypeHierarchy() {
      return fTypeHierarchy;
    }

    public void markAsAccessed() {
      fLastAccess = System.currentTimeMillis();
    }

    public long getLastAccess() {
      return fLastAccess;
    }

    public void dispose() {
      if (fTypeHierarchy != null) {
        fTypeHierarchy.removeTypeHierarchyChangedListener(this);
        fTypeHierarchy = null;
      }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "Super hierarchy of: " + fTypeHierarchy.getType().getElementName(); // $NON-NLS-1$
    }
  }

  private static final int CACHE_SIZE = 8;

  private static ArrayList<HierarchyCacheEntry> fgHierarchyCache =
      new ArrayList<HierarchyCacheEntry>(CACHE_SIZE);
  private static Map<IType, MethodOverrideTester> fgMethodOverrideTesterCache =
      new LRUMap<IType, MethodOverrideTester>(CACHE_SIZE);

  private static int fgCacheHits = 0;
  private static int fgCacheMisses = 0;

  /**
   * Returns a super type hierarchy that contains the given type. The returned hierarchy may
   * actually be based on a subtype of the requested type. Therefore, queries such as {@link
   * ITypeHierarchy#getAllClasses()} or {@link ITypeHierarchy#getRootInterfaces()} may return more
   * types than the same queries on a type hierarchy for just the given type.
   *
   * @param type the focus type
   * @return a supertype hierarchy that contains <code>type</code>
   * @throws JavaModelException if a problem occurs
   */
  public static ITypeHierarchy getTypeHierarchy(IType type) throws JavaModelException {
    return getTypeHierarchy(type, null);
  }

  public static MethodOverrideTester getMethodOverrideTester(IType type) throws JavaModelException {
    MethodOverrideTester test = null;
    synchronized (fgMethodOverrideTesterCache) {
      test = fgMethodOverrideTesterCache.get(type);
    }
    if (test == null) {
      ITypeHierarchy hierarchy = getTypeHierarchy(type); // don't nest the locks
      synchronized (fgMethodOverrideTesterCache) {
        test =
            fgMethodOverrideTesterCache.get(
                type); // test again after waiting a long time for 'getTypeHierarchy'
        if (test == null) {
          test = new MethodOverrideTester(type, hierarchy);
          fgMethodOverrideTesterCache.put(type, test);
        }
      }
    }
    return test;
  }

  private static void removeMethodOverrideTester(ITypeHierarchy hierarchy) {
    synchronized (fgMethodOverrideTesterCache) {
      for (Iterator<MethodOverrideTester> iter = fgMethodOverrideTesterCache.values().iterator();
          iter.hasNext(); ) {
        MethodOverrideTester curr = iter.next();
        if (curr.getTypeHierarchy().equals(hierarchy)) {
          iter.remove();
        }
      }
    }
  }

  /**
   * Returns a super type hierarchy that contains the given type. The returned hierarchy may
   * actually be based on a subtype of the requested type. Therefore, queries such as {@link
   * ITypeHierarchy#getAllClasses()} or {@link ITypeHierarchy#getRootInterfaces()} may return more
   * types than the same queries on a type hierarchy for just the given type.
   *
   * @param type the focus type
   * @param progressMonitor progress monitor
   * @return a supertype hierarchy that contains <code>type</code>
   * @throws JavaModelException if a problem occurs
   */
  public static ITypeHierarchy getTypeHierarchy(IType type, IProgressMonitor progressMonitor)
      throws JavaModelException {
    ITypeHierarchy hierarchy = findTypeHierarchyInCache(type);
    if (hierarchy == null) {
      fgCacheMisses++;
      hierarchy = type.newSupertypeHierarchy(progressMonitor);
      addTypeHierarchyToCache(hierarchy);
    } else {
      fgCacheHits++;
    }
    return hierarchy;
  }

  private static void addTypeHierarchyToCache(ITypeHierarchy hierarchy) {
    synchronized (fgHierarchyCache) {
      int nEntries = fgHierarchyCache.size();
      if (nEntries >= CACHE_SIZE) {
        // find obsolete entries or remove entry that was least recently accessed
        HierarchyCacheEntry oldest = null;
        ArrayList<HierarchyCacheEntry> obsoleteHierarchies =
            new ArrayList<HierarchyCacheEntry>(CACHE_SIZE);
        for (int i = 0; i < nEntries; i++) {
          HierarchyCacheEntry entry = fgHierarchyCache.get(i);
          ITypeHierarchy curr = entry.getTypeHierarchy();
          if (!curr.exists() || hierarchy.contains(curr.getType())) {
            obsoleteHierarchies.add(entry);
          } else {
            if (oldest == null || entry.getLastAccess() < oldest.getLastAccess()) {
              oldest = entry;
            }
          }
        }
        if (!obsoleteHierarchies.isEmpty()) {
          for (int i = 0; i < obsoleteHierarchies.size(); i++) {
            removeHierarchyEntryFromCache(obsoleteHierarchies.get(i));
          }
        } else if (oldest != null) {
          removeHierarchyEntryFromCache(oldest);
        }
      }
      HierarchyCacheEntry newEntry = new HierarchyCacheEntry(hierarchy);
      fgHierarchyCache.add(newEntry);
    }
  }

  /**
   * Check if the given type is in the hierarchy cache.
   *
   * @param type a type
   * @return <code>true</code> if a hierarchy for the given type is cached
   */
  public static boolean hasInCache(IType type) {
    return findTypeHierarchyInCache(type) != null;
  }

  private static ITypeHierarchy findTypeHierarchyInCache(IType type) {
    synchronized (fgHierarchyCache) {
      for (int i = fgHierarchyCache.size() - 1; i >= 0; i--) {
        HierarchyCacheEntry curr = fgHierarchyCache.get(i);
        ITypeHierarchy hierarchy = curr.getTypeHierarchy();
        if (!hierarchy.exists()) {
          removeHierarchyEntryFromCache(curr);
        } else {
          if (hierarchy.contains(type)) {
            curr.markAsAccessed();
            return hierarchy;
          }
        }
      }
    }
    return null;
  }

  private static void removeHierarchyEntryFromCache(HierarchyCacheEntry entry) {
    synchronized (fgHierarchyCache) {
      removeMethodOverrideTester(entry.getTypeHierarchy());
      entry.dispose();
      fgHierarchyCache.remove(entry);
    }
  }

  /**
   * Gets the number of times the hierarchy could be taken from the hierarchy.
   *
   * @return Returns a int
   */
  public static int getCacheHits() {
    return fgCacheHits;
  }

  /**
   * Gets the number of times the hierarchy was build. Used for testing.
   *
   * @return Returns a int
   */
  public static int getCacheMisses() {
    return fgCacheMisses;
  }
}
