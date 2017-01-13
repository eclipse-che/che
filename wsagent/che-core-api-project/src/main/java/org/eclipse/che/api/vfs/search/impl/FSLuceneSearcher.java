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
package org.eclipse.che.api.vfs.search.impl;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;

/**
 * Filesystem based LuceneSearcher which cleans index directory after call method {@link #close()}.
 *
 * @author andrew00x
 */
public class FSLuceneSearcher extends LuceneSearcher {
    private static final Logger LOG = LoggerFactory.getLogger(FSLuceneSearcher.class);

    private final File indexDirectory;

    FSLuceneSearcher(File indexDirectory, VirtualFileFilter filter) {
        this(indexDirectory, filter, null);
    }

    FSLuceneSearcher(File indexDirectory, VirtualFileFilter filter, AbstractLuceneSearcherProvider.CloseCallback closeCallback) {
        super(filter, closeCallback);
        this.indexDirectory = indexDirectory;
    }

    @Override
    protected Directory makeDirectory() throws ServerException {
        try {
            return FSDirectory.open(indexDirectory.toPath(), new SingleInstanceLockFactory());
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    @Override
    protected void afterClose() throws IOException {
        if (!deleteRecursive(indexDirectory)) {
            LOG.warn("Unable delete index directory '{}', add it in FileCleaner", indexDirectory);
            FileCleaner.addFile(indexDirectory);
        }
        super.afterClose();
    }
}
