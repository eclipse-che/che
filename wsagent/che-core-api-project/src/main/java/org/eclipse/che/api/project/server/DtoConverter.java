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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.shared.dto.AttributeDto;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.project.shared.dto.ValueDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Helper methods for convert server essentials to DTO and back.
 *
 * @author andrew00x
 */
public class DtoConverter {

    private DtoConverter() {
    }

    /** Converts {@link ProjectTypeDef} to {@link ProjectTypeDto}. */
    public static ProjectTypeDto asDto(ProjectTypeDef projectType) {
        final List<AttributeDto> typeAttributes = new ArrayList<>();
        for (Attribute attr : projectType.getAttributes()) {
            ValueDto valueDto = newDto(ValueDto.class);

            if (attr.getValue() != null) {
                valueDto.withList(attr.getValue().getList());
            }

            typeAttributes.add(newDto(AttributeDto.class).withName(attr.getName())
                                                         .withDescription(attr.getDescription())
                                                         .withRequired(attr.isRequired())
                                                         .withVariable(attr.isVariable())
                                                         .withValue(valueDto));
        }

        return newDto(ProjectTypeDto.class).withId(projectType.getId())
                                           .withDisplayName(projectType.getDisplayName())
                                           .withPrimaryable(projectType.isPrimaryable())
                                           .withMixable(projectType.isMixable())
                                           .withParents(projectType.getParents())
                                           .withAncestors(projectType.getAncestors())
                                           .withAttributes(typeAttributes);
    }

    /** Converts {@link ProjectImporter} to {@link ProjectImporterDescriptor}. */
    public static ProjectImporterDescriptor asDto(ProjectImporter importer) {
        return newDto(ProjectImporterDescriptor.class).withId(importer.getId())
                                                      .withInternal(importer.isInternal())
                                                      .withCategory(importer.getCategory().getValue())
                                                      .withDescription(importer.getDescription() != null ? importer.getDescription()
                                                                                                         : "description not found");
    }

    /** Converts {@link FileEntry} to {@link ItemReference}. */
    public static ItemReference asDto(FileEntry file) throws ServerException {
        return newDto(ItemReference.class).withName(file.getName())
                                          .withPath(file.getPath().toString())
                                          .withType("file")
                                          .withAttributes(file.getAttributes())
                                          .withModified(file.getModified())
                                          .withContentLength(file.getVirtualFile().getLength());
    }

    /** Converts {@link FolderEntry} to {@link ItemReference}. */
    public static ItemReference asDto(FolderEntry folder) {
        return newDto(ItemReference.class).withName(folder.getName())
                                          .withPath(folder.getPath().toString())
                                          .withType(folder.isProject() ? "project" : "folder")
                                          .withAttributes(folder.getAttributes())
                                          .withModified(folder.getModified());
    }

    /**
     * The method tries to provide as much as possible information about project. If get error then save information about error
     * with 'problems' field in ProjectConfigDto.
     *
     * @param project
     *         project from which we need get information
     * @return an instance of {@link ProjectConfigDto}
     */
    public static ProjectConfigDto asDto(RegisteredProject project) {
        return newDto(ProjectConfigDto.class).withName(project.getName())
                                             .withPath(project.getPath())
                                             .withDescription(project.getDescription())
                                             .withSource(asDto(project.getSource()))
                                             .withAttributes(project.getAttributes())
                                             .withType(project.getProjectType().getId())
                                             .withMixins(project.getMixinTypes().keySet()
                                                                .stream()
                                                                .collect(Collectors.toList()))
                                             .withProblems(project.getProblems()
                                                                  .stream()
                                                                  .map(DtoConverter::asDto)
                                                                  .collect(Collectors.toList()));
    }

    public static ProjectConfigDto asDto(ProjectConfig project) {
        return newDto(ProjectConfigDto.class).withName(project.getName())
                                             .withPath(project.getPath())
                                             .withDescription(project.getDescription())
                                             .withSource(asDto(project.getSource()))
                                             .withAttributes(project.getAttributes())
                                             .withType(project.getType())
                                             .withMixins(project.getMixins())
                                             ;
    }


    public static SourceStorageDto asDto(SourceStorage sourceStorage) {
        SourceStorageDto storageDto = newDto(SourceStorageDto.class);

        if (sourceStorage != null) {
            storageDto.withType(sourceStorage.getType())
                      .withLocation(sourceStorage.getLocation())
                      .withParameters(sourceStorage.getParameters());
        }

        return storageDto;
    }

    public static ProjectProblemDto asDto(RegisteredProject.Problem problem) {
        return newDto(ProjectProblemDto.class).withCode(problem.code).withMessage(problem.message);
    }
}
