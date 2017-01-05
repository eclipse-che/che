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
package org.eclipse.che.api.vfs.search;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.VirtualFileSystem;

/**
 * Manages instances of Searcher.
 *
 * @author andrew00x
 */
public interface SearcherProvider {
    /**
     * Get Searcher for specified VirtualFileSystem.
     *
     * @param virtualFileSystem
     *         VirtualFileSystem
     * @param create
     *         {@code true} to create new Searcher if necessary; {@code false} to return {@code null} if Searcher is not initialized yet
     * @return {@code Searcher} or {@code null} if {@code create} is {@code false} and the Searcher is not initialized yet
     * @see VirtualFileSystem
     */
    Searcher getSearcher(VirtualFileSystem virtualFileSystem, boolean create) throws ServerException;

    /**
     * Get Searcher for specified VirtualFileSystem. This method is shortcut for {@code getSearcher(VirtualFileSystem, true)}.
     *
     * @param virtualFileSystem
     *         VirtualFileSystem
     * @return {@code Searcher}
     * @see VirtualFileSystem
     */
    Searcher getSearcher(VirtualFileSystem virtualFileSystem) throws ServerException;

    /** Closes all Searcher related to this SearcherProvider. */
    void close() throws ServerException;
}
