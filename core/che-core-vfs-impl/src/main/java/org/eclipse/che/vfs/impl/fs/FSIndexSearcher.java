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
package org.eclipse.che.vfs.impl.fs;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.server.search.LuceneSearcher;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;

import java.io.IOException;

/**
 * Implementation of LuceneSearcher which stores index in the filesystem.
 *
 * @author andrew00x
 */
public class FSIndexSearcher extends LuceneSearcher {
    protected final java.io.File indexDir;

    public FSIndexSearcher(java.io.File indexDir, VirtualFileFilter filter) {
        super(filter);
        this.indexDir = indexDir;
    }

    @Override
    protected Directory makeDirectory() throws ServerException {
        try {
            return FSDirectory.open(indexDir.toPath(), new SingleInstanceLockFactory());
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    public java.io.File getIndexDir() {
        return indexDir;
    }
}
