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
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;

public interface Searcher {
    /**
     * Return paths of matched items on virtual filesystem.
     *
     * @param query
     *         query expression
     * @return results of search
     * @throws ServerException
     *         if an error occurs
     */
    SearchResult search(QueryExpression query) throws ServerException;

    /**
     * Add VirtualFile to index.
     *
     * @param virtualFile
     *         VirtualFile to add
     * @throws ServerException
     *         if an error occurs
     */
    void add(VirtualFile virtualFile) throws ServerException;

    /**
     * Delete VirtualFile from index.
     *
     * @param path
     *         path of VirtualFile
     * @throws ServerException
     *         if an error occurs
     */
    void delete(String path, boolean isFile) throws ServerException;

    /**
     * Updated indexed VirtualFile.
     *
     * @param virtualFile
     *         VirtualFile to add
     * @throws ServerException
     *         if an error occurs
     */
    void update(VirtualFile virtualFile) throws ServerException;

    /** Close Searcher. */
    void close();

    boolean isClosed();

    /**
     * Add filter to prevent adding files in index.
     *
     * @param indexFilter
     *         file filter
     * @return {@code true} if filter accepted and {@code false} otherwise, e.g. if filter already added
     */
    boolean addIndexFilter(VirtualFileFilter indexFilter);

    /**
     * Remove filter to prevent adding files in index.
     *
     * @param indexFilter
     *         file filter
     * @return {@code true} if filter successfully removed and {@code false} otherwise, e.g. if filter was not added before with method
     * {@link #addIndexFilter(VirtualFileFilter)}
     */
    boolean removeIndexFilter(VirtualFileFilter indexFilter);
}