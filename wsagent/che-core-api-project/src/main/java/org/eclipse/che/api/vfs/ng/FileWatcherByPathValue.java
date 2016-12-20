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

    public int watch(Path path, Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
        service.register(isDirectory(path) ? path : path.getParent());
        return handler.register(path, create, modify, delete);
    }

    public void unwatch(int pathRegistrationId) {
        Path dir = handler.unRegister(pathRegistrationId);
        service.unRegister(dir);
    }
}
