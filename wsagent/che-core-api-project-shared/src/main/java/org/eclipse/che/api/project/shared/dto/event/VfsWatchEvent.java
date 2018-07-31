/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.shared.dto.event;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;

/** @author gazarenkov */
@EventOrigin("vfs")
@DTO
public interface VfsWatchEvent {

  String VFS_CHANNEL = "vfs";

  String getPath();

  VfsWatchEvent withPath(String path);

  FileWatcherEventType getType();

  VfsWatchEvent withType(FileWatcherEventType type);

  boolean isFile();

  VfsWatchEvent withFile(boolean isFile);
}
