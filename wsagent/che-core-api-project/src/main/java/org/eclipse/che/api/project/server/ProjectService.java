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
package org.eclipse.che.api.project.server;

import static org.eclipse.che.api.project.shared.Constants.LINK_REL_CREATE_BATCH_PROJECTS;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_CREATE_PROJECT;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_GET_PROJECTS;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.project.server.impl.ProjectServiceApi;
import org.eclipse.che.api.project.server.impl.ProjectServiceApiFactory;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.project.shared.dto.ProjectSearchResponseDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.search.server.SearchResult;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;

/**
 * Project API.
 *
 * @author andrew00x
 * @author Eugene Voevodin
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
@Api(value = "/project", description = "Project REST API")
@Path("/project")
@Singleton
public class ProjectService {

  @Context protected UriInfo uriInfo;

  private final ProjectServiceApiFactory projectServiceApiFactory;
  private ProjectServiceApi projectServiceApi;

  @Inject
  public ProjectService(ProjectServiceApiFactory projectServiceApiFactory) {
    this.projectServiceApiFactory = projectServiceApiFactory;
  }

  private ProjectServiceApi getProjectServiceApi() {
    if (projectServiceApi == null) {
      UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();

      projectServiceApi =
          projectServiceApiFactory.create(
              new ServiceContext() {
                @Override
                public UriBuilder getServiceUriBuilder() {
                  return baseUriBuilder.clone().path(getClass());
                }

                @Override
                public UriBuilder getBaseUriBuilder() {
                  return uriInfo.getBaseUriBuilder().clone();
                }
              });
    }

    return projectServiceApi;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Gets list of projects in root folder",
    response = ProjectConfigDto.class,
    responseContainer = "List"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 500, message = "Server error")
  })
  @GenerateLink(rel = LINK_REL_GET_PROJECTS)
  public List<ProjectConfigDto> getProjects()
      throws IOException, ServerException, ConflictException, ForbiddenException {

    return getProjectServiceApi().getProjects();
  }

  @GET
  @Path("/{path:.*}")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Gets project by ID of workspace and project's path",
    response = ProjectConfigDto.class
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Project with specified path doesn't exist in workspace"),
    @ApiResponse(code = 403, message = "Access to requested project is forbidden"),
    @ApiResponse(code = 500, message = "Server error")
  })
  public ProjectConfigDto getProject(
      @ApiParam(value = "Path to requested project", required = true) @PathParam("path")
          String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {

    return getProjectServiceApi().getProject(wsPath);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Creates new project", response = ProjectConfigDto.class)
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "Operation is forbidden"),
    @ApiResponse(code = 409, message = "Project with specified name already exist in workspace"),
    @ApiResponse(code = 500, message = "Server error")
  })
  @GenerateLink(rel = LINK_REL_CREATE_PROJECT)
  public ProjectConfigDto createProject(
      @ApiParam(value = "Add to this project as module") @Context UriInfo uriInfo,
      @Description("descriptor of project") ProjectConfigDto projectConfig)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {

    return getProjectServiceApi().createProject(uriInfo, projectConfig);
  }

  @POST
  @Path("/batch")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Creates batch of projects according to their configurations",
    notes =
        "A project will be created by importing when project configuration contains source object. "
            + "For creating a project by generator options should be specified.",
    response = ProjectConfigDto.class
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "Path for new project should be defined"),
    @ApiResponse(code = 403, message = "Operation is forbidden"),
    @ApiResponse(code = 409, message = "Project with specified name already exist in workspace"),
    @ApiResponse(code = 500, message = "Server error")
  })
  @GenerateLink(rel = LINK_REL_CREATE_BATCH_PROJECTS)
  public List<ProjectConfigDto> createBatchProjects(
      @Description("list of descriptors for projects") List<NewProjectConfigDto> projectConfigs,
      @ApiParam(value = "Force rewrite existing project", allowableValues = "true,false")
          @QueryParam("force")
          boolean rewrite,
      @QueryParam("clientId") String clientId)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException, IOException,
          UnauthorizedException, BadRequestException {

    return getProjectServiceApi().createBatchProjects(projectConfigs, rewrite, clientId);
  }

  @PUT
  @Path("/{path:.*}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Updates existing project", response = ProjectConfigDto.class)
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Project with specified path doesn't exist in workspace"),
    @ApiResponse(code = 403, message = "Operation is forbidden"),
    @ApiResponse(code = 409, message = "Update operation causes conflicts"),
    @ApiResponse(code = 500, message = "Server error")
  })
  public ProjectConfigDto updateProject(
      @ApiParam(value = "Path to updated project", required = true) @PathParam("path")
          String wsPath,
      ProjectConfigDto projectConfigDto)
      throws NotFoundException, ConflictException, ForbiddenException, ServerException, IOException,
          BadRequestException {

    return getProjectServiceApi().updateProject(wsPath, projectConfigDto);
  }

  @DELETE
  @Path("/{path:.*}")
  @ApiOperation(
    value = "Delete a resource",
    notes =
        "Delete resources. If you want to delete a single project, specify project name. If a folder or file needs to "
            + "be deleted a path to the requested resource needs to be specified"
  )
  @ApiResponses({
    @ApiResponse(code = 204, message = ""),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public void delete(@ApiParam("Path to a resource to be deleted") @PathParam("path") String wsPath)
      throws NotFoundException, ForbiddenException, ConflictException, ServerException {

    getProjectServiceApi().delete(wsPath);
  }

  @GET
  @Path("/estimate/{path:.*}")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Estimates if the folder supposed to be project of certain type",
    response = Map.class
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Project with specified path doesn't exist in workspace"),
    @ApiResponse(code = 403, message = "Access to requested project is forbidden"),
    @ApiResponse(code = 500, message = "Server error")
  })
  public SourceEstimation estimateProject(
      @ApiParam(value = "Path to requested project", required = true) @PathParam("path")
          String wsPath,
      @ApiParam(value = "Project Type ID to estimate against", required = true) @QueryParam("type")
          String projectType)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {

    return getProjectServiceApi().estimateProject(wsPath, projectType);
  }

  @GET
  @Path("/resolve/{path:.*}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SourceEstimation> resolveSources(
      @ApiParam(value = "Path to requested project", required = true) @PathParam("path")
          String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {

    return getProjectServiceApi().resolveSources(wsPath);
  }

  @POST
  @Path("/import/{path:.*}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Import resource",
    notes =
        "Import resource. JSON with a designated importer and project location is sent. It is possible to import from "
            + "VCS or ZIP"
  )
  @ApiResponses({
    @ApiResponse(code = 204, message = ""),
    @ApiResponse(code = 401, message = "User not authorized to call this operation"),
    @ApiResponse(code = 403, message = "Forbidden operation"),
    @ApiResponse(code = 409, message = "Resource already exists"),
    @ApiResponse(code = 500, message = "Unsupported source type")
  })
  public void importProject(
      @ApiParam(value = "Path in the project", required = true) @PathParam("path") String wsPath,
      @ApiParam(value = "Force rewrite existing project", allowableValues = "true,false")
          @QueryParam("force")
          boolean force,
      @QueryParam("clientId") String clientId,
      SourceStorageDto sourceStorage)
      throws ConflictException, ForbiddenException, UnauthorizedException, IOException,
          ServerException, NotFoundException, BadRequestException {

    getProjectServiceApi().importProject(wsPath, force, clientId, sourceStorage);
  }

  @POST
  @Path("/file/{parent:.*}")
  @Consumes({MediaType.MEDIA_TYPE_WILDCARD})
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(
    value = "Create file",
    notes =
        "Create a new file in a project. If file type isn't specified the server will resolve its type."
  )
  @ApiResponses({
    @ApiResponse(code = 201, message = ""),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 409, message = "File already exists"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public Response createFile(
      @ApiParam(value = "Path to a target directory", required = true) @PathParam("parent")
          String parentWsPath,
      @ApiParam(value = "New file name", required = true) @QueryParam("name") String fileName,
      InputStream content)
      throws NotFoundException, ConflictException, ForbiddenException, ServerException {

    return getProjectServiceApi().createFile(parentWsPath, fileName, content);
  }

  @POST
  @Path("/folder/{path:.*}")
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "Create a folder", notes = "Create a folder is a specified project")
  @ApiResponses({
    @ApiResponse(code = 201, message = ""),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 409, message = "File already exists"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public Response createFolder(
      @ApiParam(value = "Path to a new folder destination", required = true) @PathParam("path")
          String wsPath)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException {

    return getProjectServiceApi().createFolder(wsPath);
  }

  @POST
  @Path("/uploadfile/{parent:.*}")
  @Consumes({MediaType.MULTIPART_FORM_DATA})
  @Produces({MediaType.TEXT_HTML})
  @ApiOperation(value = "Upload a file", notes = "Upload a new file")
  @ApiResponses({
    @ApiResponse(code = 201, message = ""),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 409, message = "File already exists"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public Response uploadFile(
      @ApiParam(value = "Destination path", required = true) @PathParam("parent")
          String parentWsPath,
      Iterator<FileItem> formData)
      throws NotFoundException, ConflictException, ForbiddenException, ServerException {

    return getProjectServiceApi().uploadFile(parentWsPath, formData);
  }

  @POST
  @Path("/upload/zipfolder/{path:.*}")
  @Consumes({MediaType.MULTIPART_FORM_DATA})
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Upload zip folder",
    notes = "Upload folder from local zip",
    response = Response.class
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = ""),
    @ApiResponse(code = 401, message = "User not authorized to call this operation"),
    @ApiResponse(code = 403, message = "Forbidden operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 409, message = "Resource already exists"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public Response uploadFolderFromZip(
      @ApiParam(value = "Path in the project", required = true) @PathParam("path") String wsPath,
      Iterator<FileItem> formData)
      throws ServerException, ConflictException, ForbiddenException, NotFoundException {

    return getProjectServiceApi().uploadFolderFromZip(wsPath, formData);
  }

  @ApiOperation(value = "Get file content", notes = "Get file content by its name")
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  @GET
  @Path("/file/{path:.*}")
  public Response getFile(
      @ApiParam(value = "Path to a file", required = true) @PathParam("path") String wsPath)
      throws IOException, NotFoundException, ForbiddenException, ServerException,
          ConflictException {

    return getProjectServiceApi().getFile(wsPath);
  }

  @PUT
  @Path("/file/{path:.*}")
  @Consumes({MediaType.MEDIA_TYPE_WILDCARD})
  @ApiOperation(value = "Update file", notes = "Update an existing file with new content")
  @ApiResponses({
    @ApiResponse(code = 200, message = ""),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public Response updateFile(
      @ApiParam(value = "Full path to a file", required = true) @PathParam("path") String wsPath,
      InputStream content)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {

    return getProjectServiceApi().updateFile(wsPath, content);
  }

  @POST
  @Path("/copy/{path:.*}")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Copy resource",
    notes = "Copy resource to a new location which is specified in a query parameter"
  )
  @ApiResponses({
    @ApiResponse(code = 201, message = ""),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 409, message = "Resource already exists"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public Response copy(
      @ApiParam("Path to a resource") @PathParam("path") String wsPath,
      @ApiParam(value = "Path to a new location", required = true) @QueryParam("to")
          String newParentWsPath,
      CopyOptions copyOptions)
      throws NotFoundException, ForbiddenException, ConflictException, ServerException {

    return getProjectServiceApi().copy(wsPath, newParentWsPath, copyOptions);
  }

  @POST
  @Path("/move/{path:.*}")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Move resource",
    notes = "Move resource to a new location which is specified in a query parameter"
  )
  @ApiResponses({
    @ApiResponse(code = 201, message = ""),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 409, message = "Resource already exists"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public Response move(
      @ApiParam("Path to a resource to be moved") @PathParam("path") String wsPath,
      @ApiParam("Path to a new location") @QueryParam("to") String newParentWsPath,
      MoveOptions moveOptions)
      throws NotFoundException, ForbiddenException, ConflictException, ServerException {

    return getProjectServiceApi().move(wsPath, newParentWsPath, moveOptions);
  }

  @POST
  @Path("/upload/zipproject/{path:.*}")
  @Consumes({MediaType.MULTIPART_FORM_DATA})
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Upload zip project",
    notes = "Upload project from local zip",
    response = ProjectConfigDto.class
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = ""),
    @ApiResponse(code = 401, message = "User not authorized to call this operation"),
    @ApiResponse(code = 403, message = "Forbidden operation"),
    @ApiResponse(code = 409, message = "Resource already exists"),
    @ApiResponse(code = 500, message = "Unsupported source type")
  })
  public List<SourceEstimation> uploadProjectFromZip(
      @ApiParam(value = "Path in the project", required = true) @PathParam("path") String wsPath,
      @ApiParam(value = "Force rewrite existing project", allowableValues = "true,false")
          @QueryParam("force")
          boolean force,
      Iterator<FileItem> formData)
      throws ServerException, ConflictException, ForbiddenException, NotFoundException,
          BadRequestException {

    return getProjectServiceApi().uploadProjectFromZip(wsPath, force, formData);
  }

  @POST
  @Path("/import/{path:.*}")
  @Consumes(ExtMediaType.APPLICATION_ZIP)
  @ApiOperation(value = "Import zip", notes = "Import resources as zip")
  @ApiResponses({
    @ApiResponse(code = 201, message = ""),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 409, message = "Resource already exists"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public Response importZip(
      @ApiParam(value = "Path to a location (where import to?)") @PathParam("path") String wsPath,
      InputStream zip,
      @DefaultValue("false") @QueryParam("skipFirstLevel") Boolean skipFirstLevel)
      throws NotFoundException, ConflictException, ForbiddenException, ServerException {

    return getProjectServiceApi().importZip(wsPath, zip, skipFirstLevel);
  }

  @GET
  @Path("/export/{path:.*}")
  @Produces(ExtMediaType.APPLICATION_ZIP)
  @ApiOperation(
    value = "Download ZIP",
    notes = "Export resource as zip. It can be an entire project or folder"
  )
  @ApiResponses({
    @ApiResponse(code = 201, message = ""),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public InputStream exportZip(
      @ApiParam(value = "Path to resource to be exported") @PathParam("path") String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {

    return getProjectServiceApi().exportZip(wsPath);
  }

  @GET
  @Path("/export/file/{path:.*}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response exportFile(
      @ApiParam(value = "Path to resource to be imported") @PathParam("path") String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {

    return getProjectServiceApi().exportFile(wsPath);
  }

  @GET
  @Path("/children/{parent:.*}")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Get project children items",
    notes = "Request all children items for a project, such as files and folders",
    response = ItemReference.class,
    responseContainer = "List"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public List<ItemReference> getChildren(
      @ApiParam(value = "Path to a project", required = true) @PathParam("parent") String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, IOException {

    return getProjectServiceApi().getChildren(wsPath);
  }

  @GET
  @Path("/tree/{parent:.*}")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Get project tree",
    notes = "Get project tree. Depth is specified in a query parameter",
    response = TreeElement.class
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public TreeElement getTree(
      @ApiParam(value = "Path to resource. Can be project or its folders", required = true)
          @PathParam("parent")
          String wsPath,
      @ApiParam(
            value =
                "Tree depth. This parameter can be dropped. If not specified ?depth=1 is used by default"
          )
          @DefaultValue("1")
          @QueryParam("depth")
          int depth,
      @ApiParam(
            value =
                "include children files (in addition to children folders). This parameter can be dropped"
                    + ". If not specified ?includeFiles=false is used by default"
          )
          @DefaultValue("false")
          @QueryParam("includeFiles")
          boolean includeFiles)
      throws NotFoundException, ForbiddenException, ServerException {

    return getProjectServiceApi().getTree(wsPath, depth, includeFiles);
  }

  @GET
  @Path("/item/{path:.*}")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get file or folder", response = ItemReference.class)
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public ItemReference getItem(
      @ApiParam(value = "Path to resource. Can be project or its folders", required = true)
          @PathParam("path")
          String wsPath)
      throws NotFoundException, ForbiddenException, ServerException {

    return getProjectServiceApi().getItem(wsPath);
  }

  @GET
  @Path("/search/{path:.*}")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Search for resources",
    notes = "Search for resources applying a number of search filters as query parameters",
    response = SearchResult.class,
    responseContainer = "List"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Not found"),
    @ApiResponse(code = 409, message = "Conflict error"),
    @ApiResponse(code = 500, message = "Internal Server Error")
  })
  public ProjectSearchResponseDto search(
      @ApiParam(value = "Path to resource, i.e. where to search?", required = true)
          @PathParam("path")
          String wsPath,
      @ApiParam(value = "Resource name") @QueryParam("name") String name,
      @ApiParam(value = "Search keywords") @QueryParam("text") String text,
      @ApiParam(
            value = "Maximum items to display. If this parameter is dropped, there are no limits"
          )
          @QueryParam("maxItems")
          @DefaultValue("-1")
          int maxItems,
      @ApiParam(value = "Skip count") @QueryParam("skipCount") int skipCount)
      throws NotFoundException, ServerException, BadRequestException {

    return getProjectServiceApi().search(wsPath, name, text, maxItems, skipCount);
  }
}
