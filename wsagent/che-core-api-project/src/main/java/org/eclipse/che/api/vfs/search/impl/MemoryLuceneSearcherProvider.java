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
package org.eclipse.che.api.vfs.search.impl;

import org.eclipse.che.api.vfs.VirtualFileFilter;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class MemoryLuceneSearcherProvider extends AbstractLuceneSearcherProvider {
    /**
     * @param excludeFileIndexFilters
     *         set filter for files that should not be indexed
     */
    public MemoryLuceneSearcherProvider(@Named("vfs.index_filter") Set<VirtualFileFilter> excludeFileIndexFilters) {
        super(excludeFileIndexFilters);
    }

    @Override
    protected LuceneSearcher createLuceneSearcher(CloseCallback closeCallback) {
        return new MemoryLuceneSearcher(excludeFileIndexFilters, closeCallback);
    }
}
