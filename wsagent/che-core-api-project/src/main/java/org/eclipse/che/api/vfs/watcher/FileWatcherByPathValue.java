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

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.function.Consumer;

import static java.nio.file.Files.isDirectory;

@Singleton
public class FileWatcherByPathValue {
    private static final Logger LOG = LoggerFactory.getLogger(FileWatcherByPathValue.class);

    private final FileWatcherEventHandler handler;
    private final FileWatcherService      service;

    @Inject
    public FileWatcherByPathValue(FileWatcherService service, FileWatcherEventHandler handler) {
        this.handler = handler;
        this.service = service;
    }

    int watch(Path path, Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
        LOG.debug("Watching path '{}'", path);
        service.register(isDirectory(path) ? path : path.getParent());
        int operationId = handler.register(path, create, modify, delete);
        LOG.debug("Registered an operation set with id '{}'", operationId);
        return operationId;
    }

    void unwatch(int operationId) {
        LOG.debug("Unregisterng an operation set with id '{}'", operationId);
        Path dir = handler.unRegister(operationId);
        LOG.debug("Unwatching path '{}'");
        service.unRegister(dir);
    }
}
