/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.shared.NewProjectConfig;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * Data transfer object (DTO) for creating of project.
 *
 * @author Roman Nikitenko
 */
@DTO
public interface NewProjectConfigDto extends ProjectConfigDto, NewProjectConfig {
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getName();

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getType();

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  SourceStorageDto getSource();

  @FactoryParameter(obligation = OPTIONAL)
  Map<String, String> getOptions();

  NewProjectConfigDto withName(String name);

  NewProjectConfigDto withPath(String path);

  NewProjectConfigDto withDescription(String description);

  NewProjectConfigDto withType(String type);

  NewProjectConfigDto withMixins(List<String> mixins);

  NewProjectConfigDto withAttributes(Map<String, List<String>> attributes);

  NewProjectConfigDto withSource(SourceStorageDto source);

  NewProjectConfigDto withLinks(List<Link> links);

  @Override
  List<ProjectProblemDto> getProblems();

  NewProjectConfigDto withProblems(List<ProjectProblemDto> problems);

  NewProjectConfigDto withOptions(Map<String, String> options);
}
