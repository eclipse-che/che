/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.shared.dto.AttributeDto;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.project.shared.dto.ValueDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.project.server.Constants.LINK_REL_CHILDREN;
import static org.eclipse.che.api.project.server.Constants.LINK_REL_DELETE;
import static org.eclipse.che.api.project.server.Constants.LINK_REL_GET_CONTENT;
import static org.eclipse.che.api.project.server.Constants.LINK_REL_MODULES;
import static org.eclipse.che.api.project.server.Constants.LINK_REL_TREE;
import static org.eclipse.che.api.project.server.Constants.LINK_REL_UPDATE_CONTENT;
import static org.eclipse.che.api.project.server.Constants.LINK_REL_UPDATE_PROJECT;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Helper methods for convert server essentials to DTO and back.
 *
 * @author andrew00x
 */
public class DtoConverter {

    private DtoConverter() {
    }

    public static ProjectTypeDto toTypeDefinition(ProjectTypeDef projectType) {
        final ProjectTypeDto definition = newDto(ProjectTypeDto.class).withId(projectType.getId())
                                                                      .withDisplayName(projectType.getDisplayName())
                                                                      .withPrimaryable(projectType.isPrimaryable())
                                                                      .withMixable(projectType.isMixable())
                                                                      .withAncestors(projectType.getAncestors());

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
        definition.withAttributes(typeAttributes).withParents(projectType.getParents());

        return definition;
    }

    public static ProjectImporterDescriptor toImporterDescriptor(ProjectImporter importer) {
        return newDto(ProjectImporterDescriptor.class).withId(importer.getId())
                                                      .withInternal(importer.isInternal())
                                                      .withDescription(importer.getDescription() != null ? importer.getDescription()
                                                                                                         : "description not found")
                                                      .withCategory(importer.getCategory().getValue());
    }

    public static ItemReference toItemReference(FileEntry file, String workspace, UriBuilder uriBuilder)
            throws ServerException {
        return newDto(ItemReference.class).withName(file.getName())
                                          .withPath(file.getPath().toString())
                                          .withType("file")
                                          .withAttributes(file.getAttributes())
                                          .withModified(file.getModified())
                                          .withContentLength(file.getVirtualFile().getLength())
                                          .withLinks(generateFileLinks(file, workspace, uriBuilder));
    }

    public static ItemReference toItemReference(FolderEntry folder, String workspace,
                                                UriBuilder uriBuilder) throws ServerException {
        return newDto(ItemReference.class).withName(folder.getName())
                                          .withPath(folder.getPath().toString())
                                          .withType(folder.isProject() ? "project" : "folder")
                                          .withAttributes(folder.getAttributes())
                                          .withModified(folder.getModified())
                                          .withLinks(generateFolderLinks(folder, workspace, uriBuilder));
    }

    /**
     * The method tries to provide as much as possible information about project.If get error then save information about error
     * with 'problems' field in ProjectConfigDto.
     *
     * @param project
     *         project from which we need get information
     * @param serviceUriBuilder
     *         service for building URI
     * @return an instance of {@link ProjectConfigDto}
     * @throws InvalidValueException
     */
    public static ProjectConfigDto toProjectConfig(ProjectImpl project, String workspace,
                                                   UriBuilder serviceUriBuilder) throws ForbiddenException,
                                                                                        ServerException,
                                                                                        NotFoundException,
                                                                                        ValueStorageException {
        ProjectConfigDto projectConfigDto = newDto(ProjectConfigDto.class);

        projectConfigDto.withName(project.getName())
                        .withPath(project.getPath())
                        .withDescription(project.getDescription());


        List <String> mixins = project.getMixinTypes().keySet().stream().collect(Collectors.toList());
        projectConfigDto.withMixins(mixins);

        projectConfigDto.withAttributes(project.getPersistableAttributes());

        List<ProjectConfigDto> modules = new ArrayList<>();
        for(String projPath : project.getModulePaths()) {
            modules.add(toProjectConfig(project.getManager().getProject(projPath), workspace, serviceUriBuilder));
        }
        projectConfigDto.withModules(modules);

        projectConfigDto.withType(project.getProjectType().getId());
        projectConfigDto.withSource(toSourceDto(project.getSource()));


        for (ProjectImpl.Problem p : project.getProblems()) {
            ProjectProblemDto projectProblem = newDto(ProjectProblemDto.class).withCode(p.code).withMessage(p.message);
            projectConfigDto.getProblems().add(projectProblem);
        }


        if (serviceUriBuilder != null) {
            projectConfigDto.withLinks(DtoConverter.generateProjectLinks(project, workspace, serviceUriBuilder));
        }

        return projectConfigDto;
    }


    private static SourceStorageDto toSourceDto(SourceStorage sourceStorage) {
        SourceStorageDto storageDto = newDto(SourceStorageDto.class);

        if (sourceStorage != null) {
            storageDto.withType(sourceStorage.getType())
                      .withLocation(sourceStorage.getLocation())
                      .withParameters(sourceStorage.getParameters());
        }

        return storageDto;
    }

    private static List<Link> generateProjectLinks(ProjectImpl project, String workspace, UriBuilder uriBuilder) {
        final List<Link> links = generateFolderLinks(project.getBaseFolder(), workspace, uriBuilder);
        final String relPath = project.getPath().substring(1);
        links.add(LinksHelper.createLink(PUT,
                                         uriBuilder.clone()
                                                   .path(ProjectService.class, "updateProject")
                                                   .build(workspace, relPath)
                                                   .toString(),
                                         APPLICATION_JSON,
                                         APPLICATION_JSON,
                                         LINK_REL_UPDATE_PROJECT
        ));
        return links;
    }

    private static List<Link> generateFolderLinks(FolderEntry folder, String workspace,  UriBuilder uriBuilder) {
        final List<Link> links = new LinkedList<>();
        final String relPath = folder.getPath().toString().substring(1);

//        links.add(LinksHelper.createLink(GET,
//                                         uriBuilder.clone().path(ProjectService.class, "exportZip").build(workspace, relPath).toString(),
//                                         ExtMediaType.APPLICATION_ZIP, LINK_REL_EXPORT_ZIP));
        links.add(LinksHelper.createLink(GET,
                                         uriBuilder.clone().path(ProjectService.class, "getChildren").build(workspace, relPath).toString(),
                                         APPLICATION_JSON, LINK_REL_CHILDREN));
        links.add(
                LinksHelper.createLink(GET, uriBuilder.clone().path(ProjectService.class, "getTree").build(workspace, relPath).toString(),
                                       null, APPLICATION_JSON, LINK_REL_TREE)
        );
        links.add(LinksHelper.createLink(GET,
                                         uriBuilder.clone().path(ProjectService.class, "getModules").build(workspace, relPath).toString(),
                                         APPLICATION_JSON, LINK_REL_MODULES));
        links.add(LinksHelper.createLink(DELETE,
                                         uriBuilder.clone().path(ProjectService.class, "delete").build(workspace, relPath).toString(),
                                         LINK_REL_DELETE));
        return links;
    }

    private static List<Link> generateFileLinks(FileEntry file, String workspace, UriBuilder uriBuilder) throws ServerException {
        final List<Link> links = new LinkedList<>();
        final String relPath = file.getPath().toString().substring(1);

        links.add(LinksHelper.createLink(GET, uriBuilder.clone().path(ProjectService.class, "getFile").build(workspace, relPath).toString(),
                                         null, null, LINK_REL_GET_CONTENT));
        links.add(LinksHelper.createLink(PUT,
                                         uriBuilder.clone().path(ProjectService.class, "updateFile").build(workspace, relPath).toString(),
                                         MediaType.WILDCARD, null, LINK_REL_UPDATE_CONTENT));
        links.add(LinksHelper.createLink(DELETE,
                                         uriBuilder.clone().path(ProjectService.class, "delete").build(workspace, relPath).toString(),
                                         LINK_REL_DELETE));
        return links;
    }


}
