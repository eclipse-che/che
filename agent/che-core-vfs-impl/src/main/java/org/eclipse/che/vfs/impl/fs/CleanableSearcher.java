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
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;

/**
 * Filesystem based LuceneSearcher which cleans index directory after call method {@link #close()}.
 *
 * @author andrew00x
 */
public class CleanableSearcher extends FSIndexSearcher {
    private static final Logger LOG = LoggerFactory.getLogger(CleanableSearcher.class);
    private final CleanableSearcherProvider searcherService;

    private final AtomicBoolean              initFlag;
    private final AtomicReference<Exception> initError;

    CleanableSearcher(CleanableSearcherProvider searcherService, java.io.File indexDir, VirtualFileFilter filter) {
        super(indexDir, filter);
        this.searcherService = searcherService;
        initFlag = new AtomicBoolean();
        initError = new AtomicReference<>();
    }

    @Override
    public void init(final MountPoint mountPoint) throws ServerException {
        doInit();
        final ExecutorService executor = searcherService.getExecutor();
        if (!executor.isShutdown()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        CleanableSearcher.this.addTree(mountPoint.getRoot());
                        initFlag.set(true);
                    } catch (ServerException e) {
                        initError.set(e);
                        LOG.error(e.getMessage());
                    }
                }
            });
        }
    }

    // for test
    Exception initializationError() {
        return initError.get();
    }

    // for test
    boolean initialized() {
        return initFlag.get();
    }

    @Override
    public void close() {
        searcherService.close(this);
    }

    void doClose() {
        super.close();
        final java.io.File dir = getIndexDir();
        if (!deleteRecursive(dir)) {
            LOG.warn("Unable delete index directory '{}'", dir);
        }
    }
}
