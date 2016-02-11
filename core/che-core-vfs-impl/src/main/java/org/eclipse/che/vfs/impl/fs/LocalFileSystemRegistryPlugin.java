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
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;

import org.eclipse.che.commons.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author andrew00x
 */
@Singleton
public class LocalFileSystemRegistryPlugin {
    @Inject
    public LocalFileSystemRegistryPlugin(@Named("vfs.local.id") String[] ids,
                                         LocalFSMountStrategy mountStrategy,
                                         VirtualFileSystemRegistry registry,
                                         EventService eventService,
                                         SystemPathsFilter systemFilter,
                                         @Nullable SearcherProvider searcherProvider) throws ServerException {
        for (String id : ids) {
            registry.registerProvider(id, new LocalFileSystemProvider(id, mountStrategy, eventService, searcherProvider, systemFilter, registry));
        }
    }
}
