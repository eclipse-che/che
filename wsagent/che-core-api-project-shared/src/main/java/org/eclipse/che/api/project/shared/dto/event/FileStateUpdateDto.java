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

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface FileStateUpdateDto {
  FileWatcherEventType getType();

  FileStateUpdateDto withType(FileWatcherEventType type);

  String getPath();

  FileStateUpdateDto withPath(String path);

  String getHashCode();

  FileStateUpdateDto withHashCode(String hashCode);
}
