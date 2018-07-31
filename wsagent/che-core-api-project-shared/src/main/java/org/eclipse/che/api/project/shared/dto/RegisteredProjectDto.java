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
package org.eclipse.che.api.project.shared.dto;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * Project data transfer object
 *
 * @author gazarenkov
 */
@DTO
public interface RegisteredProjectDto extends RegisteredProject, ProjectConfigDto {

  boolean isSynced();

  boolean isDetected();

  String getBaseFolder();

  List<ProjectProblemDto> getProblems();

  SourceStorageDto getSource();

  RegisteredProjectDto withName(String name);

  RegisteredProjectDto withPath(String path);

  RegisteredProjectDto withDescription(String description);

  RegisteredProjectDto withType(String type);

  RegisteredProjectDto withMixins(List<String> mixins);

  RegisteredProjectDto withAttributes(Map<String, List<String>> attributes);

  RegisteredProjectDto withSource(SourceStorageDto source);

  RegisteredProjectDto withProblems(List<ProjectProblemDto> problems);

  RegisteredProjectDto withSynced(boolean synced);

  RegisteredProjectDto withDetected(boolean detected);

  RegisteredProjectDto withBaseFolder(String baseFolder);

  RegisteredProjectDto withPersistableAttributes(Map<String, List<String>> attributes);
}
