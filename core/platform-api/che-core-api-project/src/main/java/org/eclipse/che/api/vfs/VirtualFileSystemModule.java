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
package org.eclipse.che.api.vfs;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.search.MediaTypeFilter;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;

import java.nio.file.PathMatcher;

/**
 * Guice module contains configuration of VFS components used by Project API internally.
 *
 * @author Artem Zatsarynnyi
 */
public class VirtualFileSystemModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<VirtualFileFilter> filtersMultibinder =
                Multibinder.newSetBinder(binder(), VirtualFileFilter.class, Names.named("vfs.index_filter"));
        filtersMultibinder.addBinding().to(MediaTypeFilter.class);

        Multibinder<PathMatcher> pathMatcherMultibinder =
                Multibinder.newSetBinder(binder(), PathMatcher.class, Names.named("vfs.index_filter_matcher"));

        bind(SearcherProvider.class).to(FSLuceneSearcherProvider.class);
        bind(VirtualFileSystemProvider.class).to(LocalVirtualFileSystemProvider.class);

        bind(FileWatcherNotificationHandler.class).to(DefaultFileWatcherNotificationHandler.class);
    }
}
