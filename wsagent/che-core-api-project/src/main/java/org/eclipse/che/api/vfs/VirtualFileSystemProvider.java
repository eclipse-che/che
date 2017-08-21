/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.vfs;

import org.eclipse.che.api.core.ServerException;

/**
 * @deprecated VFS components are now considered deprecated and will be replaced by standard JDK
 *     routines.
 */
@Deprecated
public interface VirtualFileSystemProvider {
  /**
   * Get VirtualFileSystem.
   *
   * @param create {@code true} to create new VirtualFileSystem if necessary; {@code false} to
   *     return {@code null} if VirtualFileSystem is not initialized yet
   * @return {@code VirtualFileSystem} or {@code null} if {@code create} is {@code false} and the
   *     VirtualFileSystem is not initialized yet
   */
  VirtualFileSystem getVirtualFileSystem(boolean create) throws ServerException;

  /**
   * Get VirtualFileSystem. This method is shortcut for {@code getVirtualFileSystem(true)}.
   *
   * @return {@code VirtualFileSystem}
   */
  VirtualFileSystem getVirtualFileSystem() throws ServerException;

  /** Closes all VirtualFileSystem related to this VirtualFileSystemProvider. */
  void close() throws ServerException;
}
