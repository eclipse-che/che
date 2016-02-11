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
package org.eclipse.che.api.vfs.server.impl.memory;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.SystemPathsFilter;
import org.eclipse.che.api.vfs.server.VirtualFileSystem;
import org.eclipse.che.api.vfs.server.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.search.LuceneSearcherProvider;
import org.eclipse.che.api.vfs.server.search.Searcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/** @author andrew00x */
public class MemoryFileSystemProvider extends VirtualFileSystemProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryFileSystemProvider.class);

    public static class SimpleLuceneSearcherProvider extends LuceneSearcherProvider {
        MemoryLuceneSearcher searcher;

        @Override
        public Searcher getSearcher(MountPoint mountPoint, boolean create) throws ServerException {
            if (searcher == null) {
                searcher = new MemoryLuceneSearcher();
                searcher.init(mountPoint);
            }
            return searcher;
        }
    }

    private final String                       workspaceId;
    private final EventService                 eventService;
    private final VirtualFileSystemUserContext userContext;
    private final SimpleLuceneSearcherProvider searcherProvider;
    private final VirtualFileSystemRegistry    vfsRegistry;
    private final SystemPathsFilter            systemFilter;

    private MemoryMountPoint memoryMountPoint;

    public MemoryFileSystemProvider(String workspaceId, EventService eventService, VirtualFileSystemUserContext userContext,
                                    VirtualFileSystemRegistry vfsRegistry, SystemPathsFilter systemFilter) {
        super(workspaceId);
        this.workspaceId = workspaceId;
        this.eventService = eventService;
        this.userContext = userContext;
        searcherProvider = new SimpleLuceneSearcherProvider();
        this.vfsRegistry = vfsRegistry;
        this.systemFilter = systemFilter;
    }

    public MemoryFileSystemProvider(String workspaceId, EventService eventService, VirtualFileSystemRegistry vfsRegistry) {
        this(workspaceId, eventService, VirtualFileSystemUserContext.newInstance(), vfsRegistry, SystemPathsFilter.ANY);
    }

    @Override
    public VirtualFileSystem newInstance(URI baseUri) throws ServerException {
        final MemoryMountPoint memoryMountPoint = (MemoryMountPoint)getMountPoint(true);
        return new MemoryFileSystem(
                baseUri == null ? URI.create("") : baseUri,
                workspaceId,
                userContext,
                memoryMountPoint,
                searcherProvider,
                vfsRegistry);
    }

    @Override
    public MountPoint getMountPoint(boolean create) throws ServerException {
        if (memoryMountPoint == null && create) {
            memoryMountPoint = new MemoryMountPoint(workspaceId, eventService, searcherProvider, userContext, systemFilter);
        }
        return memoryMountPoint;
    }

    @Override
    public void close() {
        try {
            final MemoryMountPoint memoryMountPoint = (MemoryMountPoint)getMountPoint(false);
            if (memoryMountPoint != null) {
                final Searcher searcher = memoryMountPoint.getSearcherProvider().getSearcher(memoryMountPoint, false);
                if (searcher != null) {
                    searcher.close();
                }
            }
        } catch (ServerException e) {
            LOG.error(e.getMessage(), e);
        }
        super.close();
    }
}
