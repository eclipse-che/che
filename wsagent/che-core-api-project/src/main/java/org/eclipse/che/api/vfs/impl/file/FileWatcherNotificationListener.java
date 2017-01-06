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
package org.eclipse.che.api.vfs.impl.file;

import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public abstract class FileWatcherNotificationListener {
    private final List<VirtualFileFilter> eventsFilters;

    public FileWatcherNotificationListener(VirtualFileFilter eventsFilter) {
        this.eventsFilters = newArrayList(eventsFilter);
    }

    public FileWatcherNotificationListener(VirtualFileFilter eventsFilter, VirtualFileFilter... eventsFilters) {
        this.eventsFilters = newArrayList(eventsFilter);
        Collections.addAll(this.eventsFilters, eventsFilters);
    }

    public FileWatcherNotificationListener(List<VirtualFileFilter> eventsFilters) {
        this.eventsFilters = eventsFilters;
    }

    public boolean shouldBeNotifiedFor(VirtualFile virtualFile) {
        for (VirtualFileFilter filter : eventsFilters) {
            if (!filter.accept(virtualFile)) {
                return false;
            }
        }
        return true;
    }

    public abstract void onFileWatcherEvent(VirtualFile virtualFile, FileWatcherEventType eventType);
}
