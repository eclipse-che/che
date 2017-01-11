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

import java.nio.file.WatchEvent;
import java.util.Optional;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Simple class to keep all consumers in one place with ability to identify them
 */
class FileWatcherOperation {
    private final int              id;
    private final Consumer<String> create;
    private final Consumer<String> modify;
    private final Consumer<String> delete;

    FileWatcherOperation(int id, Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
        this.id = id;
        this.create = create;
        this.modify = modify;
        this.delete = delete;
    }

    int getId() {
        return id;
    }

    Optional<Consumer<String>> get(WatchEvent.Kind<?> kind) {
        Consumer<String> result = null;
        if (ENTRY_CREATE.name().equals(kind.name())) {
            result = create;
        } else if (ENTRY_MODIFY.name().equals(kind.name())) {
            result = modify;
        } else if (ENTRY_DELETE.name().equals(kind.name())) {
            result = delete;
        }
        return Optional.ofNullable(result);
    }
}
