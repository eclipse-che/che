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
import org.eclipse.che.api.vfs.search.SearcherProvider;

/**
 * Attaches any point on backend filesystem some VirtualFile (root folder).
 * Only children of root folder may be accessible through this API.
 *
 * @author andrew00x
 */
public interface VirtualFileSystem {
    /**
     * Get root folder of virtual file system. Any files in higher level than root are not accessible through virtual file system API.
     *
     * @return root folder of virtual file system
     */
    VirtualFile getRoot();

    /**
     * Get searcher provider associated with this VirtualFileSystem. Method may return {@code null} if implementation doesn't support
     * searching.
     */
    SearcherProvider getSearcherProvider();

    /** Release used resources, e.g. clear caches, searcher index, etc */
    void close() throws ServerException;

}
