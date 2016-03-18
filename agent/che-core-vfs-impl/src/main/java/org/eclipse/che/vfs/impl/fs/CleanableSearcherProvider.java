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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.server.search.LuceneSearcherProvider;
import org.eclipse.che.api.vfs.server.search.Searcher;
import org.eclipse.che.api.vfs.server.util.MediaTypeFilter;
import org.eclipse.che.api.vfs.server.util.VirtualFileFilters;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NOTE: This implementation always create new index in new directory. Index is not reused after call {@link
 * CleanableSearcher#close()}. Index directory is cleaned after close Searcher.
 *
 * @author andrew00x
 */
@Singleton
public class CleanableSearcherProvider extends LuceneSearcherProvider {
    private final ConcurrentMap<java.io.File, CleanableSearcher> instances;
    private final ExecutorService                                executor;
    private final java.io.File                                   indexRootDir;
    private final Set<VirtualFileFilter>                         filters;

    @Inject
    CleanableSearcherProvider(@Named("vfs.local.fs_index_root_dir") java.io.File indexRootDir,
                              @Named("vfs.index_filter") Set<VirtualFileFilter> filters) {
        this.indexRootDir = indexRootDir;
        this.filters = filters;
        executor = Executors.newFixedThreadPool(1 + Runtime.getRuntime().availableProcessors(),
                                                new ThreadFactoryBuilder().setNameFormat("LocalVirtualFileSystem-CleanableSearcher-%d")
                                                                          .setDaemon(true).build());
        instances = new ConcurrentHashMap<>();
    }

    @Override
    public Searcher getSearcher(final MountPoint mountPoint, boolean create) throws ServerException {
        final java.io.File vfsIoRoot = ((VirtualFileImpl)mountPoint.getRoot()).getIoFile();
        CleanableSearcher searcher = instances.get(vfsIoRoot);
        if (searcher == null && create) {
            final java.io.File myIndexDir;
            final CleanableSearcher newSearcher;
            try {
                Files.createDirectories(indexRootDir.toPath());
                myIndexDir = Files.createTempDirectory(indexRootDir.toPath(), null).toFile();
                final VirtualFileFilter filter;
                if (!filters.isEmpty()) {
                    final VirtualFileFilter[] myFilters = new VirtualFileFilter[filters.size() + 1];
                    final Iterator<VirtualFileFilter> iterator = filters.iterator();
                    for (int i = 1; i < myFilters.length; i++) {
                        myFilters[i] = iterator.next();
                    }
                    myFilters[0] = new MediaTypeFilter();
                    filter = VirtualFileFilters.createAndFilter(myFilters);
                } else {
                    filter = new MediaTypeFilter();
                }
                newSearcher = new CleanableSearcher(this, myIndexDir, filter);
            } catch (IOException e) {
                throw new ServerException("Unable create searcher. " + e.getMessage(), e);
            }
            searcher = instances.putIfAbsent(vfsIoRoot, newSearcher);
            if (searcher == null) {
                searcher = newSearcher;
                searcher.init(mountPoint);
            } else {
                // not need this directory
                myIndexDir.delete();
            }
        }
        return searcher;
    }

    void close(CleanableSearcher searcher) {
        instances.values().remove(searcher);
        searcher.doClose();
    }

    @PreDestroy
    private void stop() {
        executor.shutdownNow();
        for (CleanableSearcher searcher : instances.values()) {
            searcher.close();
        }
    }

    ExecutorService getExecutor() {
        return executor;
    }
}

