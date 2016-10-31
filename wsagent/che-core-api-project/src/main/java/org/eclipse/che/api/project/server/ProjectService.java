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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.commons.fileupload.FileItem;
import org.apache.tika.Tika;
import org.eclipse.che.WorkspaceIdProvider;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.project.type.Value;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.search.QueryExpression;
import org.eclipse.che.api.vfs.search.SearchResult;
import org.eclipse.che.api.vfs.search.SearchResultEntry;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.project.server.DtoConverter.asDto;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_CHILDREN;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_CREATE_PROJECT;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_DELETE;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_GET_CONTENT;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_GET_PROJECTS;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_TREE;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_UPDATE_CONTENT;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_UPDATE_PROJECT;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

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
    private static final Logger LOG  = LoggerFactory.getLogger(ProjectService.class);
    private static final Tika   TIKA = new Tika();

    private final ProjectManager projectManager;
    private final EventService   eventService;
    private final String         workspace;

    @Inject
    public ProjectService(ProjectManager projectManager, EventService eventService) {
        this.projectManager = projectManager;
        this.eventService = eventService;
        this.workspace = WorkspaceIdProvider.getWorkspaceId();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets list of projects in root folder",
                  response = ProjectConfigDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 500, message = "Server error")})
    @GenerateLink(rel = LINK_REL_GET_PROJECTS)
    public List<ProjectConfigDto> getProjects() throws IOException,
                                                       ServerException,
                                                       ConflictException,
                                                       ForbiddenException {
        return projectManager.getProjects()
                             .stream()
                             .map(p -> injectProjectLinks(asDto(p)))
                             .collect(Collectors.toList());
    }

    @GET
    @Path("/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets project by ID of workspace and project's path",
                  response = ProjectConfigDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Project with specified path doesn't exist in workspace"),
                   @ApiResponse(code = 403, message = "Access to requested project is forbidden"),
                   @ApiResponse(code = 500, message = "Server error")})
    public ProjectConfigDto getProject(@ApiParam(value = "Path to requested project", required = true)
                                       @PathParam("path") String path) throws NotFoundException,
                                                                              ForbiddenException,
                                                                              ServerException,
                                                                              ConflictException {
        return injectProjectLinks(asDto(projectManager.getProject(path)));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates new project",
                  response = ProjectConfigDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 403, message = "Operation is forbidden"),
                   @ApiResponse(code = 409, message = "Project with specified name already exist in workspace"),
                   @ApiResponse(code = 500, message = "Server error")})
    @GenerateLink(rel = LINK_REL_CREATE_PROJECT)
    /**
     * NOTE: parentPath is added to make a module
     */
    public ProjectConfigDto createProject(@ApiParam(value = "Add to this project as module", required = false)
                                          @Context UriInfo uriInfo,
                                          @Description("descriptor of project") ProjectConfigDto projectConfig) throws ConflictException,
                                                                                                                       ForbiddenException,
                                                                                                                       ServerException,
                                                                                                                       NotFoundException {
        Map<String, String> options = new HashMap<>();
        MultivaluedMap<String, String> map = uriInfo.getQueryParameters();
        for(String key: map.keySet()) {
            options.put(key, map.get(key).get(0));
        }
        String pathToProject = projectConfig.getPath();
        String pathToParent = pathToProject.substring(0, pathToProject.lastIndexOf("/"));

        if (!pathToParent.equals("/")) {
            VirtualFileEntry parentFileEntry = projectManager.getProjectsRoot().getChild(pathToParent);
            if (parentFileEntry == null) {
                throw new NotFoundException("The parent folder with path " + pathToParent + " does not exist.");
            }
        }

        final RegisteredProject project = projectManager.createProject(projectConfig, options);
        final ProjectConfigDto configDto = asDto(project);

        eventService.publish(new ProjectCreatedEvent(workspace, project.getPath()));

        // TODO this throws NPE
        //logProjectCreatedEvent(configDto.getName(), configDto.getProjectType());

        return injectProjectLinks(configDto);
    }

    @PUT
    @Path("/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates existing project",
                  response = ProjectConfigDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Project with specified path doesn't exist in workspace"),
                   @ApiResponse(code = 403, message = "Operation is forbidden"),
                   @ApiResponse(code = 409, message = "Update operation causes conflicts"),
                   @ApiResponse(code = 500, message = "Server error")})
    public ProjectConfigDto updateProject(@ApiParam(value = "Path to updated project", required = true)
                                          @PathParam("path") String path,
                                          ProjectConfigDto projectConfigDto) throws NotFoundException,
                                                                                    ConflictException,
                                                                                    ForbiddenException,
                                                                                    ServerException,
                                                                                    IOException {
        if (path != null) {
            projectConfigDto.setPath(path);
        }

        return asDto(projectManager.updateProject(projectConfigDto));
    }

    @DELETE
    @Path("/{path:.*}")
    @ApiOperation(value = "Delete a resource",
                  notes = "Delete resources. If you want to delete a single project, specify project name. If a folder or file needs to " +
                          "be deleted a path to the requested resource needs to be specified")
    @ApiResponses({@ApiResponse(code = 204, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public void delete(@ApiParam("Path to a resource to be deleted")
                       @PathParam("path") String path) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        projectManager.delete(path);
    }

    @GET
    @Path("/estimate/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Estimates if the folder supposed to be project of certain type",
                  response = Map.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Project with specified path doesn't exist in workspace"),
                   @ApiResponse(code = 403, message = "Access to requested project is forbidden"),
                   @ApiResponse(code = 500, message = "Server error")})
    public SourceEstimation estimateProject(@ApiParam(value = "Path to requested project", required = true)
                                            @PathParam("path") String path,
                                            @ApiParam(value = "Project Type ID to estimate against", required = true)
                                            @QueryParam("type") String projectType) throws NotFoundException,
                                                                                           ForbiddenException,
                                                                                           ServerException,
                                                                                           ConflictException {
        final ProjectTypeResolution resolution = projectManager.estimateProject(path, projectType);

        final HashMap<String, List<String>> attributes = new HashMap<>();
        for (Map.Entry<String, Value> attr : resolution.getProvidedAttributes().entrySet()) {
            attributes.put(attr.getKey(), attr.getValue().getList());
        }

        return DtoFactory.newDto(SourceEstimation.class)
                         .withType(projectType)
                         .withMatched(resolution.matched())
                         .withAttributes(attributes);
    }

    @GET
    @Path("/resolve/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SourceEstimation> resolveSources(@ApiParam(value = "Path to requested project", required = true)
                                                 @PathParam("path") String path) throws NotFoundException,
                                                                                        ForbiddenException,
                                                                                        ServerException,
                                                                                        ConflictException {
        List<SourceEstimation> estimations = new ArrayList<>();
        for (ProjectTypeResolution resolution : projectManager.resolveSources(path, false)) {
            if (resolution.matched()) {
                final HashMap<String, List<String>> attributes = new HashMap<>();
                for (Map.Entry<String, Value> attr : resolution.getProvidedAttributes().entrySet()) {
                    attributes.put(attr.getKey(), attr.getValue().getList());
                }
                estimations.add(DtoFactory.newDto(SourceEstimation.class)
                                          .withType(resolution.getType())
                                          .withMatched(resolution.matched())
                                          .withAttributes(attributes));
            }
        }

        return estimations;
    }

    @POST
    @Path("/import/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Import resource",
                  notes = "Import resource. JSON with a designated importer and project location is sent. It is possible to import from " +
                          "VCS or ZIP")
    @ApiResponses({@ApiResponse(code = 204, message = ""),
                   @ApiResponse(code = 401, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 403, message = "Forbidden operation"),
                   @ApiResponse(code = 409, message = "Resource already exists"),
                   @ApiResponse(code = 500, message = "Unsupported source type")})
    public void importProject(@ApiParam(value = "Path in the project", required = true)
                              @PathParam("path") String path,
                              @ApiParam(value = "Force rewrite existing project", allowableValues = "true,false")
                              @QueryParam("force") boolean force,
                              SourceStorageDto sourceStorage) throws ConflictException,
                                                                     ForbiddenException,
                                                                     UnauthorizedException,
                                                                     IOException,
                                                                     ServerException,
                                                                     NotFoundException,
                                                                     BadRequestException {
        projectManager.importProject(path, sourceStorage, force);
    }

    @POST
    @Path("/file/{parent:.*}")
    @Consumes({MediaType.MEDIA_TYPE_WILDCARD})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Create file",
                  notes = "Create a new file in a project. If file type isn't specified the server will resolve its type.")
    @ApiResponses({@ApiResponse(code = 201, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "File already exists"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response createFile(@ApiParam(value = "Path to a target directory", required = true)
                               @PathParam("parent") String parentPath,
                               @ApiParam(value = "New file name", required = true)
                               @QueryParam("name") String fileName,
                               InputStream content) throws NotFoundException, ConflictException, ForbiddenException, ServerException {
        final FolderEntry parent = projectManager.asFolder(parentPath);

        if (parent == null) {
            throw new NotFoundException("Parent not found for " + parentPath);
        }

        final FileEntry newFile = parent.createFile(fileName, content);

        eventService.publish(new ProjectItemModifiedEvent(ProjectItemModifiedEvent.EventType.CREATED,
                                                          workspace,
                                                          newFile.getProject(),
                                                          newFile.getPath().toString(),
                                                          false));

        final URI location = getServiceContext().getServiceUriBuilder().clone()
                                                .path(getClass(), "getFile")
                                                .build(new String[]{newFile.getPath().toString().substring(1)}, false);
        return Response.created(location)
                       .entity(injectFileLinks(asDto(newFile)))
                       .build();
    }

    @POST
    @Path("/folder/{path:.*}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Create a folder",
                  notes = "Create a folder is a specified project")
    @ApiResponses({@ApiResponse(code = 201, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "File already exists"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response createFolder(@ApiParam(value = "Path to a new folder destination", required = true)
                                 @PathParam("path") String path) throws ConflictException,
                                                                        ForbiddenException,
                                                                        ServerException,
                                                                        NotFoundException {
        final FolderEntry newFolder = projectManager.getProjectsRoot().createFolder(path);
        final URI location = getServiceContext().getServiceUriBuilder().clone()
                                                .path(getClass(), "getChildren")
                                                .build(new String[]{newFolder.getPath().toString().substring(1)}, false);

        eventService.publish(new ProjectItemModifiedEvent(ProjectItemModifiedEvent.EventType.CREATED,
                                                          workspace,
                                                          newFolder.getProject(),
                                                          newFolder.getPath().toString(),
                                                          true));

        return Response.created(location)
                       .entity(injectFolderLinks(asDto(newFolder)))
                       .build();
    }

    @POST
    @Path("/uploadfile/{parent:.*}")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.TEXT_HTML})
    @ApiOperation(value = "Upload a file",
                  notes = "Upload a new file")
    @ApiResponses({@ApiResponse(code = 201, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "File already exists"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response uploadFile(@ApiParam(value = "Destination path", required = true)
                               @PathParam("parent") String parentPath,
                               Iterator<FileItem> formData) throws NotFoundException,
                                                                   ConflictException,
                                                                   ForbiddenException,
                                                                   ServerException {
        final FolderEntry parent = projectManager.asFolder(parentPath);

        if (parent == null) {
            throw new NotFoundException("Parent not found for " + parentPath);
        }

        return uploadFile(parent.getVirtualFile(), formData);
    }

    @POST
    @Path("/upload/zipfolder/{path:.*}")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Upload zip folder",
                  notes = "Upload folder from local zip",
                  response = Response.class)
    @ApiResponses({@ApiResponse(code = 200, message = ""),
                   @ApiResponse(code = 401, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 403, message = "Forbidden operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "Resource already exists"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response uploadFolderFromZip(@ApiParam(value = "Path in the project", required = true)
                                        @PathParam("path") String path,
                                        Iterator<FileItem> formData) throws ServerException,
                                                                            ConflictException,
                                                                            ForbiddenException,
                                                                            NotFoundException {
        final FolderEntry parent = projectManager.asFolder(path);

        if (parent == null) {
            throw new NotFoundException("Parent not found for " + path);
        }

        return uploadZip(parent.getVirtualFile(), formData);
    }

    @ApiOperation(value = "Get file content",
                  notes = "Get file content by its name")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/file/{path:.*}")
    public Response getFile(@ApiParam(value = "Path to a file", required = true)
                            @PathParam("path") String path) throws IOException, NotFoundException, ForbiddenException, ServerException {
        final FileEntry file = projectManager.asFile(path);
        if (file == null) {
            throw new NotFoundException("File not found for " + path);
        }
        return Response.ok().entity(file.getInputStream()).type(TIKA.detect(file.getName())).build();
    }

    @PUT
    @Path("/file/{path:.*}")
    @Consumes({MediaType.MEDIA_TYPE_WILDCARD})
    @ApiOperation(value = "Update file",
                  notes = "Update an existing file with new content")
    @ApiResponses({@ApiResponse(code = 200, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response updateFile(@ApiParam(value = "Full path to a file", required = true)
                               @PathParam("path") String path,
                               InputStream content) throws NotFoundException, ForbiddenException, ServerException {
        final FileEntry file = projectManager.asFile(path);

        if (file == null) {
            throw new NotFoundException("File not found for " + path);
        }

        file.updateContent(content);

        eventService.publish(new ProjectItemModifiedEvent(ProjectItemModifiedEvent.EventType.UPDATED,
                                                          workspace,
                                                          file.getProject(),
                                                          file.getPath().toString(),
                                                          false));

        return Response.ok().build();
    }

    @POST
    @Path("/copy/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Copy resource",
                  notes = "Copy resource to a new location which is specified in a query parameter")
    @ApiResponses({@ApiResponse(code = 201, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "Resource already exists"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response copy(@ApiParam("Path to a resource") @PathParam("path") String path,
                         @ApiParam(value = "Path to a new location", required = true) @QueryParam("to") String newParent,
                         CopyOptions copyOptions) throws NotFoundException,
                                                         ForbiddenException,
                                                         ConflictException,
                                                         ServerException {
        final VirtualFileEntry entry = projectManager.asVirtualFileEntry(path);

        // used to indicate over write of destination
        boolean isOverWrite = false;
        // used to hold new name set in request body
        String newName = entry.getName();
        if (copyOptions != null) {
            if (copyOptions.getOverWrite() != null) {
                isOverWrite = copyOptions.getOverWrite();
            }
            if (copyOptions.getName() != null) {
                newName = copyOptions.getName();
            }
        }

        final VirtualFileEntry copy = projectManager.copyTo(path, newParent, newName, isOverWrite);

        final URI location = getServiceContext().getServiceUriBuilder()
                                                .path(getClass(), copy.isFile() ? "getFile" : "getChildren")
                                                .build(new String[]{copy.getPath().toString().substring(1)}, false);

        if (copy.isFolder()) {
            try {
                final RegisteredProject project = projectManager.getProject(copy.getPath().toString());
                final String name = project.getName();
                final String projectType = project.getProjectType().getId();
                logProjectCreatedEvent(name, projectType);
            } catch (NotFoundException ignore) {
            }
        }

        return Response.created(location).build();
    }

    @POST
    @Path("/move/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Move resource",
                  notes = "Move resource to a new location which is specified in a query parameter")
    @ApiResponses({@ApiResponse(code = 201, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "Resource already exists"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response move(@ApiParam("Path to a resource to be moved") @PathParam("path") String path,
                         @ApiParam("Path to a new location") @QueryParam("to") String newParent,
                         MoveOptions moveOptions) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        final VirtualFileEntry entry = projectManager.asVirtualFileEntry(path);

        // used to indicate over write of destination
        boolean isOverWrite = false;
        // used to hold new name set in request body
        String newName = entry.getName();
        if (moveOptions != null) {
            if (moveOptions.getOverWrite() != null) {
                isOverWrite = moveOptions.getOverWrite();
            }
            if (moveOptions.getName() != null) {
                newName = moveOptions.getName();
            }
        }

        final VirtualFileEntry move = projectManager.moveTo(path, newParent, newName, isOverWrite);

        final URI location = getServiceContext().getServiceUriBuilder()
                                                .path(getClass(), move.isFile() ? "getFile" : "getChildren")
                                                .build(new String[]{move.getPath().toString().substring(1)}, false);

        return Response.created(location).build();
    }

    @POST
    @Path("/upload/zipproject/{path:.*}")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Upload zip project",
                  notes = "Upload project from local zip",
                  response = ProjectConfigDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = ""),
                   @ApiResponse(code = 401, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 403, message = "Forbidden operation"),
                   @ApiResponse(code = 409, message = "Resource already exists"),
                   @ApiResponse(code = 500, message = "Unsupported source type")})
    public List<SourceEstimation> uploadProjectFromZip(@ApiParam(value = "Path in the project", required = true)
                                                       @PathParam("path") String path,
                                                       @ApiParam(value = "Force rewrite existing project", allowableValues = "true,false")
                                                       @QueryParam("force") boolean force,
                                                       Iterator<FileItem> formData) throws ServerException,
                                                                                           IOException,
                                                                                           ConflictException,
                                                                                           ForbiddenException,
                                                                                           NotFoundException,
                                                                                           BadRequestException {
        // Not all importers uses virtual file system API. In this case virtual file system API doesn't get events and isn't able to set
        final FolderEntry baseProjectFolder = (FolderEntry)getVirtualFile(path, force);

        int stripNumber = 0;
        String projectName = "";
        String projectDescription = "";
        FileItem contentItem = null;

        while (formData.hasNext()) {
            FileItem item = formData.next();
            if (!item.isFormField()) {
                if (contentItem == null) {
                    contentItem = item;
                } else {
                    throw new ServerException("More then one upload file is found but only one is expected. ");
                }
            } else {
                switch (item.getFieldName()) {
                    case ("name"):
                        projectName = item.getString().trim();
                        break;
                    case ("description"):
                        projectDescription = item.getString().trim();
                        break;
                    case ("skipFirstLevel"):
                        stripNumber = Boolean.parseBoolean(item.getString().trim()) ? 1 : 0;
                        break;
                }
            }
        }

        if (contentItem == null) {
            throw new ServerException("Cannot find zip file for upload.");
        }

        try (InputStream zip = contentItem.getInputStream()) {
            baseProjectFolder.getVirtualFile().unzip(zip, true, stripNumber);
        }

        return resolveSources(path);
    }

    @POST
    @Path("/import/{path:.*}")
    @Consumes(ExtMediaType.APPLICATION_ZIP)
    @ApiOperation(value = "Import zip",
                  notes = "Import resources as zip")
    @ApiResponses({@ApiResponse(code = 201, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "Resource already exists"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response importZip(@ApiParam(value = "Path to a location (where import to?)")
                              @PathParam("path") String path,
                              InputStream zip,
                              @DefaultValue("false") @QueryParam("skipFirstLevel") Boolean skipFirstLevel) throws NotFoundException,
                                                                                                                  ConflictException,
                                                                                                                  ForbiddenException,
                                                                                                                  ServerException {
        final FolderEntry parent = projectManager.asFolder(path);

        if (parent == null) {
            throw new NotFoundException("Parent not found for " + path);
        }

        importZip(parent.getVirtualFile(), zip, true, skipFirstLevel);

        try {
            final RegisteredProject project = projectManager.getProject(path);
            eventService.publish(new ProjectCreatedEvent(workspace, project.getPath()));
            final String projectType = project.getProjectType().getId();
            logProjectCreatedEvent(path, projectType);
        } catch (NotFoundException ignore) {
        }

        return Response.created(getServiceContext().getServiceUriBuilder()
                                                   .path(getClass(), "getChildren")
                                                   .build(new String[]{parent.getPath().toString().substring(1)}, false)).build();
    }

    @GET
    @Path("/export/{path:.*}")
    @Produces(ExtMediaType.APPLICATION_ZIP)
    @ApiOperation(value = "Download ZIP",
                  notes = "Export resource as zip. It can be an entire project or folder")
    @ApiResponses({@ApiResponse(code = 201, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public InputStream exportZip(@ApiParam(value = "Path to resource to be exported")
                                 @PathParam("path") String path) throws NotFoundException, ForbiddenException, ServerException {

        final FolderEntry folder = projectManager.asFolder(path);

        if (folder == null) {
            throw new NotFoundException("Folder not found " + path);
        }

        return folder.getVirtualFile().zip();
    }

    @GET
    @Path("/export/file/{path:.*}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportFile(@ApiParam(value = "Path to resource to be imported")
                               @PathParam("path") String path) throws NotFoundException, ForbiddenException, ServerException {

        final FileEntry file = projectManager.asFile(path);

        if (file == null) {
            throw new NotFoundException("File not found " + path);
        }

        final VirtualFile virtualFile = file.getVirtualFile();

        return Response.ok(virtualFile.getContent(), TIKA.detect(virtualFile.getName()))
                       .lastModified(new Date(virtualFile.getLastModificationDate()))
                       .header(HttpHeaders.CONTENT_LENGTH, Long.toString(virtualFile.getLength()))
                       .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + virtualFile.getName() + '"')
                       .build();
    }

    @GET
    @Path("/children/{parent:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get project children items",
                  notes = "Request all children items for a project, such as files and folders",
                  response = ItemReference.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public List<ItemReference> getChildren(@ApiParam(value = "Path to a project", required = true)
                                           @PathParam("parent") String path) throws NotFoundException,
                                                                                    ForbiddenException,
                                                                                    ServerException {
        final FolderEntry folder = projectManager.asFolder(path);

        if (folder == null) {
            throw new NotFoundException("Parent not found for " + path);
        }

        final List<VirtualFileEntry> children = folder.getChildren();
        final ArrayList<ItemReference> result = new ArrayList<>(children.size());
        for (VirtualFileEntry child : children) {
            if (child.isFile()) {
                result.add(injectFileLinks(asDto((FileEntry)child)));
            } else {
                result.add(injectFolderLinks(asDto((FolderEntry)child)));
            }
        }

        return result;
    }

    @GET
    @Path("/tree/{parent:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get project tree",
                  notes = "Get project tree. Depth is specified in a query parameter",
                  response = TreeElement.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public TreeElement getTree(@ApiParam(value = "Path to resource. Can be project or its folders", required = true)
                               @PathParam("parent") String path,
                               @ApiParam(value = "Tree depth. This parameter can be dropped. If not specified ?depth=1 is used by default")
                               @DefaultValue("1") @QueryParam("depth") int depth,
                               @ApiParam(value = "include children files (in addition to children folders). This parameter can be dropped" +
                                                 ". If not specified ?includeFiles=false is used by default")
                               @DefaultValue("false") @QueryParam("includeFiles") boolean includeFiles) throws NotFoundException,
                                                                                                               ForbiddenException,
                                                                                                               ServerException {
        final FolderEntry folder = projectManager.asFolder(path);

        return newDto(TreeElement.class).withNode(injectFolderLinks(asDto(folder)))
                                        .withChildren(getTree(folder, depth, includeFiles));
    }

    @GET
    @Path("/item/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get file or folder",
                  response = ItemReference.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public ItemReference getItem(@ApiParam(value = "Path to resource. Can be project or its folders", required = true)
                                 @PathParam("path") String path) throws NotFoundException,
                                                                        ForbiddenException,
                                                                        ServerException {
        final VirtualFileEntry entry = projectManager.getProjectsRoot().getChild(path);

        if (entry == null) {
            throw new NotFoundException("Project " + path + " was not found");
        }

        if (entry.isFile()) {
            return injectFileLinks(asDto((FileEntry)entry));
        } else {
            return injectFolderLinks(asDto((FolderEntry)entry));
        }
    }

    @GET
    @Path("/search/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search for resources",
                  notes = "Search for resources applying a number of search filters as query parameters",
                  response = ItemReference.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "Conflict error"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public List<ItemReference> search(@ApiParam(value = "Path to resource, i.e. where to search?", required = true)
                                      @PathParam("path") String path,
                                      @ApiParam(value = "Resource name")
                                      @QueryParam("name") String name,
                                      @ApiParam(value = "Search keywords")
                                      @QueryParam("text") String text,
                                      @ApiParam(value = "Maximum items to display. If this parameter is dropped, there are no limits")
                                      @QueryParam("maxItems") @DefaultValue("-1") int maxItems,
                                      @ApiParam(value = "Skip count")
                                      @QueryParam("skipCount") int skipCount) throws NotFoundException,
                                                                                     ForbiddenException,
                                                                                     ConflictException,
                                                                                     ServerException {
        final Searcher searcher;
        try {
            searcher = projectManager.getSearcher();
        } catch (NotFoundException e) {
            LOG.warn(e.getLocalizedMessage());
            return Collections.emptyList();
        }

        if (skipCount < 0) {
            throw new ConflictException(String.format("Invalid 'skipCount' parameter: %d.", skipCount));
        }

        final QueryExpression expr = new QueryExpression()
                .setPath(path.startsWith("/") ? path : ('/' + path))
                .setName(name)
                .setText(text)
                .setMaxItems(maxItems)
                .setSkipCount(skipCount);

        final SearchResult result = searcher.search(expr);
        final List<SearchResultEntry> searchResultEntries = result.getResults();
        final List<ItemReference> items = new ArrayList<>(searchResultEntries.size());
        final FolderEntry root = projectManager.getProjectsRoot();

        for (SearchResultEntry searchResultEntry : searchResultEntries) {
            final VirtualFileEntry child = root.getChild(searchResultEntry.getFilePath());

            if (child != null && child.isFile()) {
                items.add(injectFileLinks(asDto((FileEntry)child)));
            }
        }

        return items;
    }

    private void logProjectCreatedEvent(@NotNull String projectName, @NotNull String projectType) {
        LOG.info("EVENT#project-created# PROJECT#{}# TYPE#{}# WS#{}# USER#{}# PAAS#default#",
                 projectName,
                 projectType,
                 workspace,
                 EnvironmentContext.getCurrent().getSubject().getUserId());
    }

    private VirtualFileEntry getVirtualFile(String path, boolean force) throws ServerException,
                                                                               ForbiddenException,
                                                                               ConflictException,
                                                                               NotFoundException {
        final VirtualFileEntry virtualFile = projectManager.getProjectsRoot().getChild(path);
        if (virtualFile != null && virtualFile.isFile()) {
            // File with same name exist already exists.
            throw new ConflictException(String.format("File with the name '%s' already exists.", path));
        } else {
            if (virtualFile == null) {
                return projectManager.getProjectsRoot().createFolder(path);
            } else if (!force) {
                // Project already exists.
                throw new ConflictException(String.format("Project with the name '%s' already exists.", path));
            }
        }

        return virtualFile;
    }

    private List<TreeElement> getTree(FolderEntry folder,
                                      int depth,
                                      boolean includeFiles) throws ServerException, NotFoundException {
        if (depth == 0) {
            return null;
        }

        final List<? extends VirtualFileEntry> children;

        if (includeFiles) {
            children = folder.getChildFoldersFiles();
        } else {
            children = folder.getChildFolders();
        }

        final List<TreeElement> nodes = new ArrayList<>(children.size());
        for (VirtualFileEntry child : children) {
            if (child.isFolder()) {
                nodes.add(newDto(TreeElement.class)
                                  .withNode(injectFolderLinks(asDto((FolderEntry)child)))
                                  .withChildren(getTree((FolderEntry)child, depth - 1, includeFiles)));
            } else {
                nodes.add(newDto(TreeElement.class).withNode(injectFileLinks(asDto((FileEntry)child))));
            }
        }

        return nodes;
    }

    /* --------------------------------------------------------------------------- */
    /* TODO check "upload" methods below, they were copied from old VFS as is      */
    /* --------------------------------------------------------------------------- */

    private static Response uploadFile(VirtualFile parent, Iterator<FileItem> formData) throws ForbiddenException,
                                                                                               ConflictException,
                                                                                               ServerException {
        try {
            FileItem contentItem = null;
            String name = null;
            boolean overwrite = false;

            while (formData.hasNext()) {
                FileItem item = formData.next();
                if (!item.isFormField()) {
                    if (contentItem == null) {
                        contentItem = item;
                    } else {
                        throw new ServerException("More then one upload file is found but only one should be. ");
                    }
                } else if ("name".equals(item.getFieldName())) {
                    name = item.getString().trim();
                } else if ("overwrite".equals(item.getFieldName())) {
                    overwrite = Boolean.parseBoolean(item.getString().trim());
                }
            }

            if (contentItem == null) {
                throw new ServerException("Cannot find file for upload. ");
            }
            if (name == null || name.isEmpty()) {
                name = contentItem.getName();
            }

            try {
                try {
                    parent.createFile(name, contentItem.getInputStream());
                } catch (ConflictException e) {
                    if (!overwrite) {
                        throw new ConflictException("Unable upload file. Item with the same name exists. ");
                    }
                    parent.getChild(org.eclipse.che.api.vfs.Path.of(name)).updateContent(contentItem.getInputStream(), null);
                }
            } catch (IOException ioe) {
                throw new ServerException(ioe.getMessage(), ioe);
            }

            return Response.ok("", MediaType.TEXT_HTML).build();
        } catch (ForbiddenException | ConflictException | ServerException e) {
            HtmlErrorFormatter.sendErrorAsHTML(e);
            // never thrown
            throw e;
        }
    }

    private static Response uploadZip(VirtualFile parent, Iterator<FileItem> formData) throws ForbiddenException,
                                                                                              ConflictException,
                                                                                              ServerException {
        try {
            FileItem contentItem = null;
            boolean overwrite = false;
            boolean skipFirstLevel = false;
            while (formData.hasNext()) {
                FileItem item = formData.next();
                if (!item.isFormField()) {
                    if (contentItem == null) {
                        contentItem = item;
                    } else {
                        throw new ServerException("More then one upload file is found but only one should be. ");
                    }
                } else if ("overwrite".equals(item.getFieldName())) {
                    overwrite = Boolean.parseBoolean(item.getString().trim());
                } else if ("skipFirstLevel".equals(item.getFieldName())) {
                    skipFirstLevel = Boolean.parseBoolean(item.getString().trim());
                }
            }
            if (contentItem == null) {
                throw new ServerException("Cannot find file for upload. ");
            }
            try {
                importZip(parent, contentItem.getInputStream(), overwrite, skipFirstLevel);
            } catch (IOException ioe) {
                throw new ServerException(ioe.getMessage(), ioe);
            }
            return Response.ok("", MediaType.TEXT_HTML).build();
        } catch (ForbiddenException | ConflictException | ServerException e) {
            HtmlErrorFormatter.sendErrorAsHTML(e);
            // never thrown
            throw e;
        }
    }

    private static void importZip(VirtualFile parent, InputStream in, boolean overwrite, boolean skipFirstLevel) throws ForbiddenException,
                                                                                                                        ConflictException,
                                                                                                                        ServerException {
        int stripNum = skipFirstLevel ? 1 : 0;
        parent.unzip(in, overwrite, stripNum);
    }

    private ItemReference injectFileLinks(ItemReference itemReference) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final List<Link> links = new ArrayList<>();
        final String relPath = itemReference.getPath().substring(1);

        links.add(createLink(GET,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "getFile")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_GET_CONTENT));
        links.add(createLink(PUT,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "updateFile")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             MediaType.WILDCARD,
                             null,
                             LINK_REL_UPDATE_CONTENT));
        links.add(createLink(DELETE,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "delete")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             LINK_REL_DELETE));

        return itemReference.withLinks(links);
    }

    private ItemReference injectFolderLinks(ItemReference itemReference) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final List<Link> links = new ArrayList<>();
        final String relPath = itemReference.getPath().substring(1);

        links.add(createLink(GET,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "getChildren")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_CHILDREN));
        links.add(createLink(GET,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "getTree")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_TREE));
        links.add(createLink(DELETE,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "delete")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             LINK_REL_DELETE));

        return itemReference.withLinks(links);
    }

    private ProjectConfigDto injectProjectLinks(ProjectConfigDto projectConfig) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final List<Link> links = new ArrayList<>();
        final String relPath = projectConfig.getPath().substring(1);

        links.add(createLink(PUT,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "updateProject")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             APPLICATION_JSON,
                             APPLICATION_JSON,
                             LINK_REL_UPDATE_PROJECT));
        links.add(createLink(GET,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "getChildren")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_CHILDREN));
        links.add(createLink(GET,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "getTree")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_TREE));
        links.add(createLink(DELETE,
                             uriBuilder.clone()
                                       .path(ProjectService.class, "delete")
                                       .build(new String[]{relPath}, false)
                                       .toString(),
                             LINK_REL_DELETE));

        return projectConfig.withLinks(links);
    }
}
