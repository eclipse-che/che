/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs;

import org.eclipse.che.api.core.ServerException;

public interface VirtualFileSystemProvider {
    /**
     * Get VirtualFileSystem.
     *
     * @param create
     *         {@code true} to create new VirtualFileSystem if necessary; {@code false} to return {@code null} if VirtualFileSystem is not
     *         initialized yet
     * @return {@code VirtualFileSystem} or {@code null} if {@code create} is {@code false} and the VirtualFileSystem is not initialized yet
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
