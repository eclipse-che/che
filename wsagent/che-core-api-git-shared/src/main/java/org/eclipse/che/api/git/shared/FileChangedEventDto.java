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
package org.eclipse.che.api.git.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Dto object that contains information about git changed file event.
 *
 * @author Igor Vinokur
 */
@DTO
public interface FileChangedEventDto {

  /** Status of the file. */
  Status getStatus();

  FileChangedEventDto withStatus(Status type);

  /** Path of the file. */
  String getPath();

  FileChangedEventDto withPath(String path);

  /** List of edited regions of the file. */
  List<EditedRegion> getEditedRegions();

  void setEditedRegions(List<EditedRegion> editedRegions);

  FileChangedEventDto withEditedRegions(List<EditedRegion> editedRegions);

  enum Status {
    ADDED,
    MODIFIED,
    UNTRACKED,
    NOT_MODIFIED
  }
}
