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
import org.eclipse.che.api.vfs.server.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.che.commons.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * Implementation of VirtualFileSystemRegistry that is able to create VirtualFileSystemProvider automatically (even if it doesn't
 * registered) if required path on local filesystem exists.
 *
 * @author andrew00x
 */
@Singleton
public class AutoMountVirtualFileSystemRegistry extends VirtualFileSystemRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(AutoMountVirtualFileSystemRegistry.class);

    private final LocalFSMountStrategy mountStrategy;
    private final EventService         eventService;
    private final SearcherProvider     searcherProvider;
    private final SystemPathsFilter    systemFilter;

    @Inject
    public AutoMountVirtualFileSystemRegistry(LocalFSMountStrategy mountStrategy,
                                              EventService eventService,
                                              SystemPathsFilter systemFilter,
                                              @Nullable SearcherProvider searcherProvider) {
        this.mountStrategy = mountStrategy;
        this.eventService = eventService;
        this.searcherProvider = searcherProvider;
        this.systemFilter = systemFilter;
    }

    @Override
    protected VirtualFileSystemProvider loadProvider(String vfsId) throws ServerException {
        File wsPath = mountStrategy.getMountPath(vfsId);
        if (!wsPath.exists()) {
            return null;
        }
        LOG.debug("Using {} as mount point for workspace {} ", wsPath.getAbsolutePath(), vfsId);
        return new LocalFileSystemProvider(vfsId, mountStrategy, eventService, searcherProvider, systemFilter, this);
    }
}
