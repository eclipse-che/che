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
package org.eclipse.che.api.project.server;

import static java.io.File.separator;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.project.server.DtoConverter.asDto;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_PROGRESS;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_CREATE_BATCH_PROJECTS;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_CREATE_PROJECT;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_GET_PROJECTS;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.fileupload.FileItem;
import org.apache.tika.Tika;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.fs.api.FsDtoConverter;
import org.eclipse.che.api.fs.api.FsManager;
import org.eclipse.che.api.fs.api.PathResolver;
import org.eclipse.che.api.fs.search.LuceneSearcher;
import org.eclipse.che.api.fs.search.QueryExpression;
import org.eclipse.che.api.fs.search.SearchResult;
import org.eclipse.che.api.fs.search.SearchResultEntry;
import org.eclipse.che.api.fs.search.Searcher;
import org.eclipse.che.api.project.server.api.ProjectManager;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.project.shared.dto.ProjectSearchRequestDto;
import org.eclipse.che.api.project.shared.dto.ProjectSearchResponseDto;
import org.eclipse.che.api.project.shared.dto.SearchOccurrenceDto;
import org.eclipse.che.api.project.shared.dto.SearchResultDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ProjectService extends Service {

  private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);
  private static Tika TIKA;

  private final ProjectManager projectManager;
  private final FsManager fsManager;
  private final FsDtoConverter fsDtoConverter;
  private final Searcher searcher;
  private final EventService eventService;
  private final ProjectServiceLinksInjector projectServiceLinksInjector;
  private final ProjectServiceVcsStatusInjector vcsStatusInjector;
  private final RequestTransmitter transmitter;
  private final PathResolver pathResolver;
  private final String workspace;

  @Inject
  public ProjectService(
      Searcher searcher,
      ProjectManager projectManager,
      FsManager fsManager,
      FsDtoConverter fsDtoConverter,
      EventService eventService,
      ProjectServiceLinksInjector projectServiceLinksInjector,
      ProjectServiceVcsStatusInjector vcsStatusInjector,
      RequestTransmitter transmitter,
      PathResolver pathResolver) {
    this.projectManager = projectManager;
    this.fsManager = fsManager;
    this.fsDtoConverter = fsDtoConverter;
    this.searcher = searcher;
    this.eventService = eventService;
    this.projectServiceLinksInjector = projectServiceLinksInjector;
    this.vcsStatusInjector = vcsStatusInjector;
    this.transmitter = transmitter;
    this.pathResolver = pathResolver;
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

    return projectManager
        .getAll()
        .stream()
        .map(DtoConverter::asDto)
        .map(this::injectProjectLinks)
        .collect(Collectors.toList());
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);
    return projectManager
        .get(wsPath)
        .map(DtoConverter::asDto)
        .map(this::injectProjectLinks)
        .orElseThrow(() -> new NotFoundException("Project is not found"));
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
  /** NOTE: parentPath is added to make a module */
  public ProjectConfigDto createProject(
      @ApiParam(value = "Add to this project as module", required = false) @Context UriInfo uriInfo,
      @Description("descriptor of project") ProjectConfigDto projectConfig)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
      BadRequestException {

    Map<String, String> options =
        uriInfo
            .getQueryParameters()
            .entrySet()
            .stream()
            .collect(toMap(Entry::getKey, it -> it.getValue().get(0)));

    RegisteredProject project = projectManager.create(projectConfig, options);
    ProjectConfigDto asDto = asDto(project);
    ProjectConfigDto injectedLinks = injectProjectLinks(asDto);

    eventService.publish(new ProjectCreatedEvent(workspace, project.getPath()));

    return injectedLinks;
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

    Set<RegisteredProject> registeredProjects =
        projectManager.doImport(
            new HashSet<>(projectConfigs), rewrite, jsonRpcImportConsumer(clientId));

    Set<ProjectConfigDto> result =
        registeredProjects
            .stream()
            .map(DtoConverter::asDto)
            .map(this::injectProjectLinks)
            .collect(toSet());

    registeredProjects
        .stream()
        .map(RegisteredProject::getPath)
        .map(path -> new ProjectCreatedEvent(workspace, path))
        .forEach(eventService::publish);

    return new ArrayList<>(result);
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
    if (wsPath != null) {
      wsPath = pathResolver.toAbsoluteWsPath(wsPath);
      projectConfigDto.setPath(wsPath);
    }

    RegisteredProject updated = projectManager.update(projectConfigDto);
    return asDto(updated);
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);
    projectManager.delete(wsPath);
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    ProjectTypeResolution resolution = projectManager.qualify(wsPath, projectType);

    Map<String, List<String>> attributes =
        resolution
            .getProvidedAttributes()
            .entrySet()
            .stream()
            .collect(toMap(Entry::getKey, it -> it.getValue().getList()));

    return DtoFactory.newDto(SourceEstimation.class)
        .withType(projectType)
        .withMatched(resolution.matched())
        .withResolution(resolution.getResolution())
        .withAttributes(attributes);
  }

  @GET
  @Path("/resolve/{path:.*}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SourceEstimation> resolveSources(
      @ApiParam(value = "Path to requested project", required = true) @PathParam("path")
          String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    return projectManager
        .qualify(wsPath)
        .stream()
        .filter(ProjectTypeResolution::matched)
        .map(
            resolution -> {
              Map<String, List<String>> attributes =
                  resolution
                      .getProvidedAttributes()
                      .entrySet()
                      .stream()
                      .collect(toMap(Entry::getKey, it -> it.getValue().getList()));

              return newDto(SourceEstimation.class)
                  .withType(resolution.getType())
                  .withMatched(resolution.matched())
                  .withAttributes(attributes);
            })
        .collect(toList());
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

    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    projectManager.doImport(wsPath, sourceStorage, force, jsonRpcImportConsumer(clientId));
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
    parentWsPath = pathResolver.toAbsoluteWsPath(parentWsPath);
    String wsPath = pathResolver.resolve(parentWsPath, fileName);

    fsManager.createFile(wsPath, content);
    String project =
        projectManager
            .getClosest(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find parent project for file"))
            .getName();

    eventService.publish(
        new ProjectItemModifiedEvent(
            ProjectItemModifiedEvent.EventType.CREATED, workspace, project, wsPath, false));

    final URI location =
        getServiceContext()
            .getServiceUriBuilder()
            .clone()
            .path(getClass(), "getFile")
            .build(new String[]{wsPath.substring(1)}, false);
    return Response.created(location)
        .entity(injectFileLinks(vcsStatusInjector.injectVcsStatus(fsDtoConverter.asDto(wsPath))))
        .build();
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);
    fsManager.createDirectory(wsPath);

    final URI location =
        getServiceContext()
            .getServiceUriBuilder()
            .clone()
            .path(getClass(), "getChildren")
            .build(new String[]{wsPath.substring(1)}, false);

    String project =
        projectManager
            .getClosest(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find parent project"))
            .getName();

    eventService.publish(
        new ProjectItemModifiedEvent(
            ProjectItemModifiedEvent.EventType.CREATED, workspace, project, wsPath, true));

    return Response.created(location)
        .entity(injectFolderLinks(fsDtoConverter.asDto(wsPath)))
        .build();
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
    parentWsPath = pathResolver.toAbsoluteWsPath(parentWsPath);

    fsManager.createFile(parentWsPath, formData);

    return Response.ok("", MediaType.TEXT_HTML).build();
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    fsManager.createDirectory(wsPath, formData);

    return Response.ok("", MediaType.TEXT_HTML).build();
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
      throws IOException, NotFoundException, ForbiddenException, ServerException {
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    InputStream inputStream = fsManager.readFileAsInputStream(wsPath);
    String name = wsPath.substring(wsPath.lastIndexOf(separator));
    String type = getTIKA().detect(name);

    return Response.ok().entity(inputStream).type(type).build();
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    fsManager.updateFile(wsPath, content);

    String project =
        projectManager
            .getClosest(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find parent project for file"))
            .getName();

    eventService.publish(
        new ProjectItemModifiedEvent(
            ProjectItemModifiedEvent.EventType.UPDATED, workspace, project, wsPath, false));

    return Response.ok().build();
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);
    newParentWsPath = pathResolver.toAbsoluteWsPath(newParentWsPath);
    String name = getNameValue(copyOptions, wsPath);
    boolean overwrite = getOverwriteValue(copyOptions);

    pathResolver.resolve(newParentWsPath, name);
    String dstWsPath = newParentWsPath + separator + name;

    boolean isProject = projectManager.isRegistered(wsPath);
    boolean isDirectory = fsManager.existsAsDirectory(wsPath);
    boolean isFile = fsManager.existsAsFile(wsPath);

    if (isProject) {
      projectManager.copy(wsPath, dstWsPath, overwrite);
    } else if (isDirectory) {
      fsManager.copyDirectory(wsPath, dstWsPath);
    } else {
      fsManager.copyFile(wsPath, dstWsPath);
    }

    URI location =
        getServiceContext()
            .getServiceUriBuilder()
            .path(getClass(), isFile ? "getFile" : "getChildren")
            .build(new String[]{dstWsPath.substring(1)}, false);

    return Response.created(location).build();
  }

  private String getNameValue(CopyOptions copyOptions, String wsPath) {
    if (copyOptions != null && copyOptions.getName() != null) {
      return copyOptions.getName();
    } else {
      return pathResolver.getName(wsPath);
    }
  }

  private boolean getOverwriteValue(CopyOptions copyOptions) {
    if (copyOptions != null && copyOptions.getOverWrite() != null) {
      return copyOptions.getOverWrite();
    } else {
      return false;
    }
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);
    newParentWsPath = pathResolver.toAbsoluteWsPath(newParentWsPath);
    String name = pathResolver.getName(wsPath);

    boolean overwrite = false;
    if (moveOptions != null) {
      if (moveOptions.getOverWrite() != null) {
        overwrite = moveOptions.getOverWrite();
      }
      if (moveOptions.getName() != null) {
        name = moveOptions.getName();
      }
    }

    String dstWsPath = pathResolver.resolve(newParentWsPath, name);

    boolean isProject = projectManager.isRegistered(wsPath);
    boolean isDirectory = fsManager.existsAsDirectory(wsPath);
    boolean isFile = fsManager.existsAsFile(wsPath);

    if (isProject) {
      projectManager.move(wsPath, dstWsPath, overwrite);
    } else if (isDirectory) {
      fsManager.moveDirectory(wsPath, dstWsPath);
    } else {
      fsManager.moveFile(wsPath, dstWsPath);
    }

    final URI location =
        getServiceContext()
            .getServiceUriBuilder()
            .path(getClass(), isFile ? "getFile" : "getChildren")
            .build(new String[]{dstWsPath.substring(1)}, false);

    return Response.created(location).build();
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    fsManager.createDirectory(wsPath, formData);

    return resolveSources(wsPath);
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    fsManager.unzipDirectory(wsPath, zip, skipFirstLevel);

    eventService.publish(new ProjectCreatedEvent(workspace, wsPath));

    return Response.created(
        getServiceContext()
            .getServiceUriBuilder()
            .path(getClass(), "getChildren")
            .build(new String[]{wsPath.substring(1)}, false))
        .build();
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
      throws NotFoundException, ForbiddenException, ServerException {
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    return fsManager.existsAsFile(wsPath)
        ? fsManager.zipFileToInputStream(wsPath)
        : fsManager.zipDirectoryToInputStream(wsPath);
  }

  @GET
  @Path("/export/file/{path:.*}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response exportFile(
      @ApiParam(value = "Path to resource to be imported") @PathParam("path") String wsPath)
      throws NotFoundException, ForbiddenException, ServerException {
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    InputStream inputStream = fsManager.readFileAsInputStream(wsPath);
    long length = fsManager.length(wsPath);
    long lastModified = fsManager.lastModified(wsPath);
    String name = pathResolver.getName(wsPath);

    return Response.ok(inputStream, getTIKA().detect(name))
        .lastModified(new Date(lastModified))
        .header(HttpHeaders.CONTENT_LENGTH, Long.toString(length))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + '"')
        .build();
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    Set<String> wsPaths = fsManager.getAllChildrenWsPaths(wsPath);
    Set<ItemReference> itemReferences = fsDtoConverter.asDto(wsPaths);

    List<ItemReference> result =
        itemReferences
            .stream()
            .map(it -> "file".equals(it.getType()) ? injectFileLinks(it) : injectFolderLinks(it))
            .collect(Collectors.toList());

    return vcsStatusInjector.injectVcsStatus(result);
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    ItemReference asDto = fsDtoConverter.asDto(wsPath);
    ItemReference asLinkedDto = injectFolderLinks(asDto);
    return newDto(TreeElement.class)
        .withNode(asLinkedDto)
        .withChildren(getTreeRecursively(wsPath, depth, includeFiles));
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
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    ItemReference asDto = fsDtoConverter.asDto(wsPath);
    return fsManager.isFile(wsPath)
        ? injectFileLinks(vcsStatusInjector.injectVcsStatus(asDto))
        : injectFolderLinks(asDto);
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
      throws NotFoundException, ForbiddenException, ConflictException, ServerException {
    if (skipCount < 0) {
      throw new ConflictException(String.format("Invalid 'skipCount' parameter: %d.", skipCount));
    }
    wsPath = pathResolver.toAbsoluteWsPath(wsPath);

    QueryExpression expr =
        new QueryExpression()
            .setPath(wsPath.startsWith("/") ? wsPath : ('/' + wsPath))
            .setName(name)
            .setText(text)
            .setMaxItems(maxItems)
            .setSkipCount(skipCount)
            .setIncludePositions(true);

    SearchResult result = searcher.search(expr);
    List<SearchResultEntry> searchResultEntries = result.getResults();
    return DtoFactory.newDto(ProjectSearchResponseDto.class)
        .withTotalHits(result.getTotalHits())
        .withItemReferences(prepareResults(searchResultEntries));
  }

  /**
   * Prepare result for client, add additional information like line number and line content where
   * found given text
   */
  private List<SearchResultDto> prepareResults(List<SearchResultEntry> searchResultEntries)
      throws ServerException, NotFoundException {
    List<SearchResultDto> results = new ArrayList<>(searchResultEntries.size());
    for (SearchResultEntry searchResultEntry : searchResultEntries) {
      String path = searchResultEntry.getFilePath();
      if (fsManager.existsAsDirectory(path)) {
        ItemReference asDto = fsDtoConverter.asDto(path);
        ItemReference itemReference = injectFileLinks(asDto);
        List<LuceneSearcher.OffsetData> datas = searchResultEntry.getData();
        List<SearchOccurrenceDto> searchOccurrences = new ArrayList<>(datas.size());
        for (LuceneSearcher.OffsetData data : datas) {
          SearchOccurrenceDto searchOccurrenceDto =
              DtoFactory.getInstance()
                  .createDto(SearchOccurrenceDto.class)
                  .withPhrase(data.phrase)
                  .withScore(data.score)
                  .withStartOffset(data.startOffset)
                  .withEndOffset(data.endOffset)
                  .withLineNumber(data.lineNum)
                  .withLineContent(data.line);
          searchOccurrences.add(searchOccurrenceDto);
        }
        SearchResultDto searchResultDto = DtoFactory.getInstance().createDto(SearchResultDto.class);
        results.add(
            searchResultDto
                .withItemReference(itemReference)
                .withSearchOccurrences(searchOccurrences));
      }
    }
    return results;
  }

  @Inject
  private void configureProjectSearchRequestHandler(
      RequestHandlerConfigurator requestHandlerConfigurator) {
    requestHandlerConfigurator
        .newConfiguration()
        .methodName("project/search")
        .paramsAsDto(ProjectSearchRequestDto.class)
        .resultAsDto(ProjectSearchResponseDto.class)
        .withFunction(this::search);
  }

  public ProjectSearchResponseDto search(ProjectSearchRequestDto request) {
    String path = request.getPath();
    String name = request.getName();
    String text = request.getText();
    int maxItems = request.getMaxItems();
    int skipCount = request.getSkipCount();

    try {
      return search(path, name, text, maxItems, skipCount);
    } catch (ServerException | ConflictException | NotFoundException | ForbiddenException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private BiConsumer<String, String> jsonRpcImportConsumer(String clientId) {
    return (projectName, message) -> {
      ImportProgressRecordDto progressRecord =
          newDto(ImportProgressRecordDto.class).withProjectName(projectName).withLine(message);

      transmitter
          .newRequest()
          .endpointId(clientId)
          .methodName(EVENT_IMPORT_OUTPUT_PROGRESS)
          .paramsAsDto(progressRecord)
          .sendAndSkipResult();
    };
  }

  private List<TreeElement> getTreeRecursively(String wsPath, int depth, boolean includeFiles)
      throws ServerException, NotFoundException {
    if (depth == 0) {
      return null;
    }

    Set<String> childrenWsPaths = includeFiles
        ? fsManager.getAllChildrenWsPaths(wsPath)
        : fsManager.getDirectoryWsPaths(wsPath);

    List<TreeElement> nodes = new ArrayList<>(childrenWsPaths.size());
    for (String childWsPath : childrenWsPaths) {
      ItemReference asDto = fsDtoConverter.asDto(childWsPath);
      ItemReference asLinkedDto =
          fsManager.isDirectory(childWsPath) ? injectFolderLinks(asDto) : injectFileLinks(asDto);
      TreeElement treeElement = newDto(TreeElement.class).withNode(asLinkedDto);
      nodes.add(treeElement);

      if (fsManager.isDirectory(childWsPath)) {
        List<TreeElement> treeElements = getTreeRecursively(childWsPath, depth - 1, includeFiles);
        if (treeElements != null) {
          treeElement.setChildren(treeElements);
        }
      }
    }

    return vcsStatusInjector.injectVcsStatusTreeElements(nodes);
  }

  private ItemReference injectFileLinks(ItemReference itemReference) {
    return projectServiceLinksInjector.injectFileLinks(itemReference, getServiceContext());
  }

  private ItemReference injectFolderLinks(ItemReference itemReference) {
    return projectServiceLinksInjector.injectFolderLinks(itemReference, getServiceContext());
  }

  private ProjectConfigDto injectProjectLinks(ProjectConfigDto projectConfig) {
    return projectServiceLinksInjector.injectProjectLinks(projectConfig, getServiceContext());
  }

  /**
   * Lazy init of Tika.
   */
  private synchronized Tika getTIKA() {
    if (TIKA == null) {
      TIKA = new Tika();
    }
    return TIKA;
  }
}
