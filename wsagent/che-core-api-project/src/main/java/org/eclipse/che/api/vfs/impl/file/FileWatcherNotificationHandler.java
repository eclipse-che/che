/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.impl.file;

import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;

import java.io.File;

/**
 * @deprecated VFS components are now considered deprecated and will be replaced by standard JDK routines.
 */
@Deprecated
public interface FileWatcherNotificationHandler {
    void handleFileWatcherEvent(FileWatcherEventType eventType, File watchRoot, String subPath, boolean isDir);

    void started(File watchRoot);

    void errorOccurred(File watchRoot, Throwable cause);

    boolean addNotificationListener(FileWatcherNotificationListener fileWatcherNotificationListener);

    boolean removeNotificationListener(FileWatcherNotificationListener fileWatcherNotificationListener);
}
