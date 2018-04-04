/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.resources;

import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.che.core.internal.preferences.ChePreferences;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * Object representing the project scope in the Eclipse preferences hierarchy. Can be used as a
 * context for searching for preference values (in the <code>
 * org.eclipse.core.runtime.IPreferencesService</code> APIs) or for determining the correct
 * preference node to set values in the store.
 *
 * <p>Project preferences are stored on a per project basis in the project's content area as
 * specified by <code>IProject#getLocation</code>.
 *
 * <p>The path for preferences defined in the project scope hierarchy is as follows: <code>
 * /project/&lt;projectName&gt;/&lt;qualifier&gt;</code>
 *
 * <p>This class is not intended to be subclassed. This class may be instantiated.
 *
 * @see IProject#getLocation()
 * @since 3.0
 */
public final class ProjectScope implements IScopeContext {

  /**
   * String constant (value of <code>"project"</code>) used for the scope name for this preference
   * scope.
   */
  public static final String SCOPE = "project"; // $NON-NLS-1$

  private static ConcurrentHashMap<String, IEclipsePreferences> cache = new ConcurrentHashMap<>();

  private IProject context;

  /**
   * Create and return a new project scope for the given project. The given project must not be
   * <code>null</code>.
   *
   * @param context the project
   * @exception IllegalArgumentException if the project is <code>null</code>
   */
  public ProjectScope(IProject context) {
    super();
    if (context == null) throw new IllegalArgumentException();
    this.context = context;
  }

  /*
   * @see org.eclipse.core.runtime.IScopeContext#getNode(java.lang.String)
   */
  public IEclipsePreferences getNode(String qualifier) {
    if (qualifier == null) throw new IllegalArgumentException();

    if (!cache.containsKey(context.getName())) {
      synchronized (ProjectScope.class) {
        if (!cache.containsKey(context.getName())) {
          String pathToWorkspace = ResourcesPlugin.getPathToWorkspace();
          IPath fullPath = context.getFullPath();
          cache.putIfAbsent(
              context.getName(),
              new ChePreferences(pathToWorkspace + fullPath + "/.che/project.preferences"));
        }
      }
    }
    return cache.get(context.getName());
  }

  /*
   * @see org.eclipse.core.runtime.preferences.IScopeContext#getLocation()
   */
  public IPath getLocation() {
    IProject project = ((IResource) context).getProject();
    IPath location = project.getLocation();
    return location == null
        ? null
        : location.append(EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME);
  }

  /*
   * @see org.eclipse.core.runtime.preferences.IScopeContext#getName()
   */
  public String getName() {
    return SCOPE;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof ProjectScope)) return false;
    ProjectScope other = (ProjectScope) obj;
    return context.equals(other.context);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + context.getFullPath().hashCode();
  }
}
