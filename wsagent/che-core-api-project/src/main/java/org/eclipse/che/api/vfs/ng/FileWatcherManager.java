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
package org.eclipse.che.api.vfs.ng;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.PathMatcher;
import java.util.function.Consumer;

import static org.eclipse.che.api.vfs.ng.FileWatcherUtils.toNormalPath;

@Singleton
public class FileWatcherManager {
    public final static  Consumer<String> EMPTY_CONSUMER = it -> {
    };

    private static final Logger           LOG            = LoggerFactory.getLogger(FileWatcherManager.class);

    private final FileWatcherByPathValue   fileWatcherByPathValue;
    private final FileWatcherByPathMatcher fileWatcherByPathMatcher;
    private final FileWatcherService       service;
    private final File                     root;

    @Inject
    public FileWatcherManager(@Named("che.user.workspaces.storage") File root, FileWatcherByPathValue watcherByPathValue,
                              FileWatcherByPathMatcher watcherByPathMatcher, FileWatcherService service) {
        this.fileWatcherByPathMatcher = watcherByPathMatcher;
        this.fileWatcherByPathValue = watcherByPathValue;
        this.service = service;
        this.root = root;
    }

    public void suspend() {
        service.suspend();
    }

    public void resume() {
        service.resume();
    }

    public int startWatchingByPath(String path, Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
        return fileWatcherByPathValue.watch(toNormalPath(root.toPath(), path), create, modify, delete);
    }

    public void stopWatchingByPath(int id) {
        fileWatcherByPathValue.unwatch(id);
    }

    public int startWatchingByMatcher(PathMatcher matcher, Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
        return fileWatcherByPathMatcher.watch(matcher, create, modify, delete);
    }

    public void stopWatchingByMatcher(int id) {
        fileWatcherByPathMatcher.unwatch(id);
    }
}
