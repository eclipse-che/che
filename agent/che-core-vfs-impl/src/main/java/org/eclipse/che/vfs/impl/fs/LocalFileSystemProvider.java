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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.vfs.server.SystemPathsFilter;
import org.eclipse.che.api.vfs.server.VirtualFileSystem;
import org.eclipse.che.api.vfs.server.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.search.Searcher;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of VirtualFileSystemProvider for plain file system.
 *
 * @author andrew00x
 */
public class LocalFileSystemProvider extends VirtualFileSystemProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileSystemProvider.class);

    private final String                       workspaceId;
    private final LocalFSMountStrategy         mountStrategy;
    private final EventService                 eventService;
    private final SearcherProvider             searcherProvider;
    private final MountPointRef                mountRef;
    private final VirtualFileSystemUserContext userContext;
    private final SystemPathsFilter            systemFilter;
    private final VirtualFileSystemRegistry    vfsRegistry;

    /**
     * @param workspaceId
     *         virtual file system identifier
     * @param mountStrategy
     *         LocalFSMountStrategy
     * @param searcherProvider
     *         SearcherProvider or {@code null}
     * @see LocalFileSystemProvider
     */
    public LocalFileSystemProvider(String workspaceId,
                                   LocalFSMountStrategy mountStrategy,
                                   EventService eventService,
                                   SearcherProvider searcherProvider,
                                   SystemPathsFilter systemFilter,
                                   VirtualFileSystemRegistry vfsRegistry) {
        this(workspaceId, mountStrategy, eventService, searcherProvider, VirtualFileSystemUserContext.newInstance(), systemFilter, vfsRegistry);
    }


    /**
     * @param workspaceId
     *         virtual file system identifier
     * @param mountStrategy
     *         LocalFSMountStrategy
     * @param searcherProvider
     *         SearcherProvider
     * @see LocalFileSystemProvider
     */
    protected LocalFileSystemProvider(String workspaceId,
                                      LocalFSMountStrategy mountStrategy,
                                      EventService eventService,
                                      SearcherProvider searcherProvider,
                                      VirtualFileSystemUserContext userContext,
                                      SystemPathsFilter systemFilter,
                                      VirtualFileSystemRegistry vfsRegistry) {
        super(workspaceId);
        this.workspaceId = workspaceId;
        this.mountStrategy = mountStrategy;
        this.eventService = eventService;
        this.searcherProvider = searcherProvider;
        this.userContext = userContext;
        this.mountRef = new MountPointRef();
        this.systemFilter = systemFilter;
        this.vfsRegistry = vfsRegistry;
    }

    /** Get new instance of LocalFileSystem. If virtual file system is not mounted yet if mounted automatically when used first time. */
    @Override
    public VirtualFileSystem newInstance(URI baseUri) throws ServerException {
        return new LocalFileSystem(workspaceId,
                                   baseUri == null ? URI.create("") : baseUri,
                                   userContext,
                                   getMountPoint(true),
                                   searcherProvider,
                                   vfsRegistry);
    }

    @Override
    public void close() {
        final FSMountPoint mount = mountRef.remove();
        if (mount != null) {
            if (searcherProvider != null) {
                try {
                    final Searcher searcher = searcherProvider.getSearcher(mount, false);
                    if (searcher != null) {
                        searcher.close();
                    }
                } catch (ServerException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        super.close();
    }

    /**
     * Mount backing local filesystem.
     *
     * @param ioFile
     *         root point on the backing local filesystem
     * @throws ServerException
     *         if mount is failed, e.g. if specified <code>ioFile</code> already mounted
     * @see VirtualFileSystem
     */
    public void mount(java.io.File ioFile) throws ServerException {
        if (!mountRef.maybeSet(new FSMountPoint(getWorkspaceId(), ioFile, eventService, searcherProvider, systemFilter))) {
            throw new ServerException(String.format("Local filesystem '%s' already mounted. ", ioFile));
        }
    }

    public boolean isMounted() {
        return mountRef.get() != null;
    }

    @Override
    public FSMountPoint getMountPoint(boolean create) throws ServerException {
        FSMountPoint mount = mountRef.get();
        if (mount == null && create) {
            final java.io.File workspaceMountPoint = mountStrategy.getMountPath(workspaceId);
            FSMountPoint newMount = new FSMountPoint(workspaceId, workspaceMountPoint, eventService, searcherProvider, systemFilter);
            if (mountRef.maybeSet(newMount)) {
                if (!(workspaceMountPoint.exists() || workspaceMountPoint.mkdirs())) {
                    LOG.error("Unable create directory {}", workspaceMountPoint);
                    // critical error cannot continue
                    throw new ServerException(String.format("Virtual filesystem '%s' is not available. ", workspaceId));
                }
                mount = newMount;
            }
        }
        return mount;
    }

    private static class MountPointRef {
        final AtomicReference<FSMountPoint> ref;

        private MountPointRef() {
            ref = new AtomicReference<>();
        }

        boolean maybeSet(FSMountPoint mountPoint) {
            final boolean res = ref.compareAndSet(null, mountPoint);
            if (res) {
                MountPointCacheCleaner.add(mountPoint);
            }
            return res;
        }

        FSMountPoint get() {
            return ref.get();
        }

        FSMountPoint remove() {
            final FSMountPoint mountPoint = ref.getAndSet(null);
            if (mountPoint != null) {
                MountPointCacheCleaner.remove(mountPoint);
            }
            return mountPoint;
        }
    }
}
