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
package org.eclipse.che.api.git.shared;

import java.util.List;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * Dto object that contains information about git index changed event.
 *
 * @author Igor Vinokur
 */
@DTO
public interface StatusChangedEventDto {

  /** Name of project in which git status was changed. */
  String getProjectName();

  void setProjectName(String projectName);

  StatusChangedEventDto withProjectName(String projectName);

  /** Status of the repository. */
  Status getStatus();

  void setStatus(Status status);

  StatusChangedEventDto withStatus(Status status);

  /** Map of modified files and their edited regions. */
  Map<String, List<EditedRegion>> getModifiedFiles();

  void setModifiedFiles(Map<String, List<EditedRegion>> modifiedFiles);

  StatusChangedEventDto withModifiedFiles(Map<String, List<EditedRegion>> modifiedFiles);
}
