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
package org.eclipse.che.api.project.server;

import static org.eclipse.che.api.project.server.impl.ProjectDtoConverter.asDto;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_PROJECT_TYPES;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.project.server.impl.ProjectDtoConverter;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;

/**
 * ProjectTypeService
 *
 * @author gazarenkov
 */
@Api(value = "/project-type", description = "Project type REST API")
@Path("project-type")
public class ProjectTypeService extends Service {

  private final ProjectTypeRegistry registry;

  @Inject
  public ProjectTypeService(ProjectTypeRegistry registry) {
    this.registry = registry;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @GenerateLink(rel = LINK_REL_PROJECT_TYPES)
  @ApiOperation(
    value = "Get project types",
    responseContainer = "List",
    response = ProjectTypeDto.class
  )
  @ApiResponses(@ApiResponse(code = 200, message = "Project types successfully fetched"))
  public List<ProjectTypeDto> getProjectTypes() {
    return registry
        .getProjectTypes()
        .stream()
        .map(ProjectDtoConverter::asDto)
        .collect(Collectors.toList());
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Get the project type by the id")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains requested project type entity"),
    @ApiResponse(code = 404, message = "The project type with such id doesn't exist")
  })
  public ProjectTypeDto getProjectType(@ApiParam("Project type id") @PathParam("id") String id)
      throws NotFoundException {
    return asDto(registry.getProjectType(id));
  }
}
