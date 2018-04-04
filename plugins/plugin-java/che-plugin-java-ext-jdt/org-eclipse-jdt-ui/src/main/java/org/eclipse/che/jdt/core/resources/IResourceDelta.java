/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core.resources;

import java.io.File;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;

/**
 * A resource delta represents changes in the state of a resource tree between two discrete points
 * in time.
 *
 * <p>Resource deltas implement the <code>IAdaptable</code> interface; extensions are managed by the
 * platform's adapter manager.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @see org.eclipse.core.resources.IResource
 * @see Platform#getAdapterManager()
 */
public interface IResourceDelta extends org.eclipse.core.resources.IResourceDelta {

  /**
   * Returns a handle for the affected resource.
   *
   * <p>For additions (<code>ADDED</code>), this handle describes the newly-added resource; i.e.,
   * the one in the "after" state.
   *
   * <p>For changes (<code>CHANGED</code>), this handle also describes the resource in the "after"
   * state. When a file or folder resource has changed type, the former type of the handle can be
   * inferred.
   *
   * <p>For removals (<code>REMOVED</code>), this handle describes the resource in the "before"
   * state. Even though this resource would not normally exist in the current workspace, the type of
   * resource that was removed can be determined from the handle.
   *
   * <p>For phantom additions and removals (<code>ADDED_PHANTOM</code> and <code>REMOVED_PHANTOM
   * </code>), this is the handle of the phantom resource.
   *
   * @return the affected resource (handle)
   */
  @Deprecated
  public File getFile();

  @Override
  IResource getResource();
}
