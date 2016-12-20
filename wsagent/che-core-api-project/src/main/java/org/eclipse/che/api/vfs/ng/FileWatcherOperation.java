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

import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class FileWatcherOperation {
    private final int id;
    private Map<String, Consumer<String>> consumers = new HashMap<>();

    public FileWatcherOperation(int id, Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
        this.id = id;
        consumers.put(ENTRY_CREATE.name(), create);
        consumers.put(ENTRY_MODIFY.name(), modify);
        consumers.put(ENTRY_DELETE.name(), delete);

    }

    public int getId() {
        return id;
    }

    Optional<Consumer<String>> get(WatchEvent.Kind<?> kind) {
        return Optional.ofNullable(consumers.get(kind.name()));
    }
}
