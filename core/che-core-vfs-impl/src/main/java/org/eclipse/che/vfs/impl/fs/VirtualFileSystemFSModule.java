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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.vfs.server.SystemPathsFilter;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;

import javax.inject.Inject;

/**
 * @deprecated  VFS is deprecated in 4.0
 * @author andrew00x
 * */
@Deprecated
public class VirtualFileSystemFSModule extends AbstractModule {
    @Override
    protected void configure() {
        final Multibinder<VirtualFileFilter> multibinder =
                Multibinder.newSetBinder(binder(), VirtualFileFilter.class, Names.named("vfs.index_filter"));
        multibinder.addBinding().to(DefaultVirtualFileFilter.class);
        //bind(LocalFSMountStrategy.class).to(WorkspaceHashLocalFSMountStrategy.class);
        bind(SearcherProvider.class).to(CleanableSearcherProvider.class);
        bind(MountPointCacheCleaner.Finalizer.class).asEagerSingleton();
    }

    public static class DefaultVirtualFileFilter implements VirtualFileFilter {

        @Inject
        private SystemPathsFilter systemFilter;

        @Override
        public boolean accept(VirtualFile file) {
            return systemFilter.accept(file.getMountPoint().getWorkspaceId(), file.getVirtualFilePath());
        }

    }

}
