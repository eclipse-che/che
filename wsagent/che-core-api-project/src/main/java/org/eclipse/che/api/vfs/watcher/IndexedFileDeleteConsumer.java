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
package org.eclipse.che.api.vfs.watcher;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

@Singleton
public class IndexedFileDeleteConsumer implements Consumer<Path> {
    private static final Logger LOG = LoggerFactory.getLogger(IndexedFileDeleteConsumer.class);

    private File                      root;
    private VirtualFileSystemProvider vfsProvider;

    @Inject
    public IndexedFileDeleteConsumer(@Named("che.user.workspaces.storage") File root, VirtualFileSystemProvider vfsProvider) {
        this.root = root;
        this.vfsProvider = vfsProvider;
    }

    @Override
    public void accept(Path path) {
        try {
            VirtualFileSystem virtualFileSystem = vfsProvider.getVirtualFileSystem();
            SearcherProvider searcherProvider = virtualFileSystem.getSearcherProvider();
            Searcher searcher = searcherProvider.getSearcher(virtualFileSystem);
            Path innerPath = root.toPath().relativize(path);
            searcher.delete("/" + innerPath.toString(), true);
        } catch (ServerException e) {
            LOG.error("Issue happened during removing deleted file from index", e);
        }
    }
}
