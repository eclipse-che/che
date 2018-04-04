/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.shared.dto.event;

import org.eclipse.che.api.project.shared.FileChange;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ProjectTreeStateUpdateDto extends FileChange {
  String getPath();

  ProjectTreeStateUpdateDto withPath(String path);

  FileWatcherEventType getType();

  ProjectTreeStateUpdateDto withType(FileWatcherEventType type);
}
