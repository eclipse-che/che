/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.impl;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.project.ProjectProblem;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.project.server.ProjectImporter;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.project.shared.dto.AttributeDto;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.project.shared.dto.RegisteredProjectDto;
import org.eclipse.che.api.project.shared.dto.ValueDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;

/**
 * Helper methods for convert server essentials to DTO and back.
 *
 * @author andrew00x
 */
public class ProjectDtoConverter {

  private ProjectDtoConverter() {}

  /** Converts {@link ProjectTypeDef} to {@link ProjectTypeDto}. */
  public static ProjectTypeDto asDto(ProjectTypeDef projectType) {
    final List<AttributeDto> typeAttributes = new ArrayList<>();
    for (Attribute attr : projectType.getAttributes()) {
      ValueDto valueDto = newDto(ValueDto.class);

      if (attr.getValue() != null) {
        valueDto.withList(attr.getValue().getList());
      }

      typeAttributes.add(
          newDto(AttributeDto.class)
              .withName(attr.getName())
              .withDescription(attr.getDescription())
              .withRequired(attr.isRequired())
              .withVariable(attr.isVariable())
              .withValue(valueDto));
    }

    return newDto(ProjectTypeDto.class)
        .withId(projectType.getId())
        .withDisplayName(projectType.getDisplayName())
        .withPrimaryable(projectType.isPrimaryable())
        .withMixable(projectType.isMixable())
        .withParents(projectType.getParents())
        .withAncestors(projectType.getAncestors())
        .withAttributes(typeAttributes);
  }

  /** Converts {@link ProjectImporter} to {@link ProjectImporterDescriptor}. */
  public static ProjectImporterDescriptor asDto(ProjectImporter importer) {
    return newDto(ProjectImporterDescriptor.class)
        .withId(importer.getId())
        .withInternal(importer.isInternal())
        .withCategory(importer.getSourceCategory().getValue())
        .withDescription(
            importer.getDescription() != null
                ? importer.getDescription()
                : "description not found");
  }

  /**
   * The method tries to provide as much as possible information about project. If get error then
   * save information about error with 'problems' field in ProjectConfigDto.
   *
   * @param project project from which we need get information
   * @return an instance of {@link ProjectConfigDto}
   */
  public static ProjectConfigDto asDto(RegisteredProject project) {
    return newDto(ProjectConfigDto.class)
        .withName(project.getName())
        .withPath(project.getPath())
        .withDescription(project.getDescription())
        .withSource(asDto(project.getSource()))
        .withAttributes(project.getAttributes())
        .withType(project.getType())
        .withMixins(project.getMixins())
        .withProblems(
            project
                .getProblems()
                .stream()
                .map(ProjectDtoConverter::asDto)
                .collect(Collectors.toList()));
  }

  /**
   * Converts abstract RegisteredProject to RegisteredProjectDto
   *
   * @param project RegisteredProject
   * @return RegisteredProjectDto
   */
  public static RegisteredProjectDto asRegisteredProjectDto(RegisteredProject project) {
    return newDto(RegisteredProjectDto.class)
        .withName(project.getName())
        .withPath(project.getPath())
        .withDescription(project.getDescription())
        .withSource(asDto(project.getSource()))
        .withAttributes(project.getAttributes())
        .withType(project.getType())
        .withMixins(project.getMixins())
        .withBaseFolder(project.getBaseFolder())
        .withDetected(project.isDetected())
        .withSynced(project.isSynced())
        .withPersistableAttributes(project.getPersistableAttributes())
        .withProblems(
            project
                .getProblems()
                .stream()
                .map(ProjectDtoConverter::asDto)
                .collect(Collectors.toList()));
  }

  /**
   * Converts abstract ProjectConfig to ProjectConfigDto
   *
   * @param project ProjectConfig
   * @return ProjectConfigDto
   */
  public static ProjectConfigDto asDto(ProjectConfig project) {
    return newDto(ProjectConfigDto.class)
        .withName(project.getName())
        .withPath(project.getPath())
        .withDescription(project.getDescription())
        .withSource(asDto(project.getSource()))
        .withAttributes(project.getAttributes())
        .withType(project.getType())
        .withMixins(project.getMixins());
  }

  public static SourceStorageDto asDto(SourceStorage sourceStorage) {
    SourceStorageDto storageDto = newDto(SourceStorageDto.class);

    if (sourceStorage != null) {
      storageDto
          .withType(sourceStorage.getType())
          .withLocation(sourceStorage.getLocation())
          .withParameters(sourceStorage.getParameters());
    }

    return storageDto;
  }

  public static ProjectProblemDto asDto(ProjectProblem problem) {
    return newDto(ProjectProblemDto.class)
        .withCode(problem.getCode())
        .withMessage(problem.getMessage());
  }
}
