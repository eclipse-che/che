/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.server.search;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.MountPoint;

/**
 * Manages instances of Searcher.
 *
 * @author andrew00x
 */
public interface SearcherProvider {
    /**
     * Get LuceneSearcher for specified MountPoint.
     *
     * @param mountPoint
     *         MountPoint
     * @param create
     *         {@code true} to create new Searcher if necessary; {@code false} to return {@code null} if Searcher is not initialized yet
     * @return {@code Searcher} or {@code null} if {@code create} is {@code false} and the Searcher is not initialized yet
     * @see org.eclipse.che.api.vfs.server.MountPoint
     */
    Searcher getSearcher(MountPoint mountPoint, boolean create) throws ServerException;
}
