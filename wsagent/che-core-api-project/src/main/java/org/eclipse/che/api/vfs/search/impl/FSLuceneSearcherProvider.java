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

import org.eclipse.che.api.vfs.VirtualFileFilters;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

@Singleton
public class FSLuceneSearcherProvider extends AbstractLuceneSearcherProvider {
    private final File indexRootDirectory;

    /**
     * @param indexRootDirectory
     *         root directory for creation index
     * @param excludePatterns
     *         set filter for files that should not be indexed
     * @see LuceneSearcher
     */
    @Inject
    public FSLuceneSearcherProvider(@Named("vfs.local.fs_index_root_dir") File indexRootDirectory,
                                    @Named("vfs.index_filter_matcher") Set<PathMatcher> excludePatterns) throws IOException {
        super(newHashSet(transform(excludePatterns, VirtualFileFilters::wrap)));
        this.indexRootDirectory = indexRootDirectory;
        Files.createDirectories(indexRootDirectory.toPath());
    }

    @Override
    protected LuceneSearcher createLuceneSearcher(CloseCallback closeCallback) {
        return new FSLuceneSearcher(indexRootDirectory, excludeFileIndexFilters, closeCallback);
    }
}
