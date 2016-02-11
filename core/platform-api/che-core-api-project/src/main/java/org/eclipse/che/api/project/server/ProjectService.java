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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileSystemImpl;
import org.eclipse.che.api.vfs.server.search.QueryExpression;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.eclipse.che.api.project.server.DtoConverter.toProjectConfig;
import static org.eclipse.che.dto.server.DtoFactory.newDto;


/**
 * @author andrew00x
 * @author Eugene Voevodin
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
@Api(value = "/project",
     description = "Project manager")
@Path("/project/{ws-id}")
@Singleton // important to have singleton
public class ProjectService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    private final FilesBuffer     filesBuffer = FilesBuffer.get();
    private final ExecutorService executor    = Executors.newFixedThreadPool(1 + Runtime.getRuntime().availableProcessors(),
                                                                             new ThreadFactoryBuilder()
                                                                                     .setNameFormat("ProjectService-IndexingThread-")
                                                                                     .setDaemon(true).build());
    @Inject
    private ProjectManager          projectManager;
    @Inject
    private ProjectImporterRegistry importers;
    @Inject
    private SearcherProvider        searcherProvider;
    @Inject
    private EventService            eventService;
    @Inject
    private ProjectTypeRegistry     typeRegistry;

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }

    @ApiOperation(value = "Gets list of projects in root folder",
                  response = ProjectConfigDto.class,
                  responseContainer = "List",
                  position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Server error")})
    @GenerateLink(rel = Constants.LINK_REL_GET_PROJECTS)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProjectConfigDto> getProjects(@ApiParam("ID of workspace to get projects")
                                              @PathParam("ws-id") String workspace,
                                              @ApiParam("Include project attributes")
                                              @QueryParam("includeAttributes")
                                              @DefaultValue("false") boolean includeAttributes) throws IOException,
                                                                                                       ServerException,
                                                                                                       ConflictException,
                                                                                                       ForbiddenException,
                                                                                                       NotFoundException {
        List<Project> projects = projectManager.getProjects(workspace);

        List<ProjectConfigDto> projectConfigs = new ArrayList<>(projects.size());

        for (Project project : projects) {
            try {
                projectConfigs.add(toProjectConfig(project, getServiceContext().getServiceUriBuilder()));
            } catch (RuntimeException exception) {
                // Ignore known error for single project.
                // In result we won't have them in explorer tree but at least 'bad' projects won't prevent to show 'good' projects.
                LOG.warn(exception.getMessage(), exception);

                NotValidProject notValidProject = new NotValidProject(project.getBaseFolder(), projectManager);
                projectConfigs.add(toProjectConfig(notValidProject, getServiceContext().getServiceUriBuilder()));
            }
        }

        addNotSynchronizedProjectsFromVFS(projectConfigs, workspace);
        addNotSynchronizedProjectsFromWorkspace(projectConfigs, workspace);

        return projectConfigs;
    }

    private void addNotSynchronizedProjectsFromVFS(List<ProjectConfigDto> projectConfigs, String workspaceId) throws ServerException,
                                                                                                                     ValueStorageException,
                                                                                                                     NotFoundException,
                                                                                                                     ForbiddenException {
        FolderEntry projectsRoot = projectManager.getProjectsRoot(workspaceId);
        List<VirtualFileEntry> children = projectsRoot.getChildren();
        for (VirtualFileEntry child : children) {
            if (child.isFolder()) {
                FolderEntry folderEntry = (FolderEntry)child;
                if (!projectManager.isProjectFolder(folderEntry)) {
                    Project notValidProject = new Project(folderEntry, projectManager);

                    ProjectConfigDto projectConfig = toProjectConfig(notValidProject, getServiceContext().getServiceUriBuilder());

                    ProjectProblemDto projectProblem = newDto(ProjectProblemDto.class).withCode(9)
                                                                                      .withMessage("Project is not synchronized");
                    projectConfig.getProblems().add(projectProblem);

                    projectConfigs.add(projectConfig);
                }
            }
        }
    }

    private void addNotSynchronizedProjectsFromWorkspace(List<ProjectConfigDto> projectConfigs,
                                                         String workspaceId) throws ServerException,
                                                                                    ValueStorageException,
                                                                                    NotFoundException,
                                                                                    ForbiddenException {
        List<ProjectConfigDto> allProjectsFromWorkspace = projectManager.getAllProjectsFromWorkspace(workspaceId);

        List<String> configsNames = projectConfigs.stream().map(ProjectConfigDto::getName).collect(Collectors.toList());

        allProjectsFromWorkspace.stream()
                                .filter(projectFromWorkspace -> !configsNames.contains(projectFromWorkspace.getName()))
                                .forEach(projectFromWorkspace -> {
                                    ProjectProblemDto projectProblem = newDto(ProjectProblemDto.class).withCode(10)
                                                                                                      .withMessage(
                                                                                                              "Project is not synchronized");
                                    projectFromWorkspace.getProblems().add(projectProblem);

                                    projectConfigs.add(projectFromWorkspace);
                                });
    }

    @ApiOperation(value = "Gets project by ID of workspace and project's path",
                  response = ProjectConfigDto.class,
                  position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Project with specified path doesn't exist in workspace"),
            @ApiResponse(code = 403, message = "Access to requested project is forbidden"),
            @ApiResponse(code = 500, message = "Server error")})
    @GET
    @Path("/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProjectConfigDto getProject(@ApiParam(value = "ID of workspace to get projects", required = true)
                                       @PathParam("ws-id") String workspace,
                                       @ApiParam(value = "Path to requested project", required = true)
                                       @PathParam("path") String path) throws NotFoundException,
                                                                              ForbiddenException,
                                                                              ServerException,
                                                                              ConflictException {
        Project project = projectManager.getProject(workspace, path);

        if (project == null) {
            FolderEntry projectsRoot = projectManager.getProjectsRoot(workspace);

            VirtualFileEntry child = projectsRoot.getChild(path);

            if (child == null) {
                throw new NotFoundException(String.format("Project '%s' doesn't exist in workspace '%s'.", path, workspace));
            }

            boolean isFolder = child.isFolder();
            boolean isRoot = child.getParent().isRoot();

            if (isFolder && isRoot) {
                project = new NotValidProject((FolderEntry)child, projectManager);
            } else {
                throw new NotFoundException(String.format("Project '%s' doesn't exist in workspace '%s'.", path, workspace));
            }
        }

        try {
            return toProjectConfig(project, getServiceContext().getServiceUriBuilder());
        } catch (InvalidValueException e) {
            NotValidProject notValidProject = new NotValidProject(project.getBaseFolder(), projectManager);

            return toProjectConfig(notValidProject, getServiceContext().getServiceUriBuilder());
        }
    }

    @ApiOperation(value = "Creates new project",
                  response = ProjectConfigDto.class,
                  position = 3)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "Operation is forbidden"),
            @ApiResponse(code = 409, message = "Project with specified name already exist in workspace"),
            @ApiResponse(code = 500, message = "Server error")})

    @POST
    @GenerateLink(rel = Constants.LINK_REL_CREATE_PROJECT)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProjectConfigDto createProject(@ApiParam(value = "ID of workspace to create project", required = true)
                                          @PathParam("ws-id") String workspace,
                                          @ApiParam(value = "Name for new project", required = true)
                                          @Required
                                          @Description("project name")
                                          @QueryParam("name") String name,
                                          @Description("descriptor of project") ProjectConfigDto projectConfigDto)
            throws ConflictException, ForbiddenException, ServerException, NotFoundException {

        Map<String, String> options = Collections.emptyMap();

        projectConfigDto.setPath('/' + name);

        Project project = projectManager.createProject(workspace,
                                                       name,
                                                       projectConfigDto,
                                                       options);

        ProjectConfigDto configDto = toProjectConfig(project, getServiceContext().getServiceUriBuilder());

        eventService.publish(new ProjectCreatedEvent(workspace, project.getPath()));

        logProjectCreatedEvent(configDto.getName(), configDto.getType());

        return configDto;
    }

    @ApiOperation(value = "Get project modules",
                  notes = "Get project modules. Roles allowed: system/admin, system/manager.",
                  response = ProjectConfigDto.class,
                  responseContainer = "List",
                  position = 4)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/modules/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProjectConfig> getModules(@ApiParam(value = "Workspace ID", required = true)
                                          @PathParam("ws-id") String workspace,
                                          @ApiParam(value = "Path to a project", required = true)
                                          @PathParam("path") String path)
            throws NotFoundException, ForbiddenException, ServerException, ConflictException, IOException {

        Project parent = projectManager.getProject(workspace, path);
        if (parent == null) {
            throw new NotFoundException("Project " + path + " was not found");
        }
        return projectManager.getProjectModules(parent).stream().collect(Collectors.toCollection(LinkedList::new));
    }

    @ApiOperation(value = "Create a new module",
                  notes = "Create a new module in a specified project",
                  response = ProjectConfigDto.class,
                  position = 5)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 409, message = "Module already exists"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProjectConfigDto createModule(@ApiParam(value = "Workspace ID", required = true)
                                         @PathParam("ws-id") String workspace,
                                         @ApiParam(value = "Path to a target directory", required = true)
                                         @PathParam("path") String pathToParent,
                                         ProjectConfigDto moduleConfigDto) throws NotFoundException,
                                                                                  ConflictException,
                                                                                  ForbiddenException,
                                                                                  ServerException {
        filesBuffer.addToBuffer(moduleConfigDto.getName());

        ProjectConfigDto module = projectManager.addModule(workspace,
                                                           pathToParent,
                                                           moduleConfigDto,
                                                           null);

        eventService.publish(new ProjectCreatedEvent(workspace, module.getPath()));

        logProjectCreatedEvent(module.getName(), module.getType());

        return module;
    }

    @ApiOperation(value = "Updates existing project",
                  response = ProjectConfigDto.class,
                  position = 6)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Project with specified path doesn't exist in workspace"),
            @ApiResponse(code = 403, message = "Operation is forbidden"),
            @ApiResponse(code = 409, message = "Update operation causes conflicts"),
            @ApiResponse(code = 500, message = "Server error")})
    @PUT
    @Path("/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProjectConfigDto updateProject(@ApiParam(value = "ID of workspace", required = true)
                                          @PathParam("ws-id") String workspace,
                                          @ApiParam(value = "Path to updated project", required = true)
                                          @PathParam("path") String path,
                                          ProjectConfigDto projectConfigDto) throws NotFoundException,
                                                                                    ConflictException,
                                                                                    ForbiddenException,
                                                                                    ServerException,
                                                                                    IOException {
        projectConfigDto.getProblems().clear();

        String oldProjectName = path.startsWith("/") ? path.substring(1) : path;
        String newProjectName = projectConfigDto.getName();

        if (!oldProjectName.equals(newProjectName)) {
            projectManager.rename(workspace, path, newProjectName, null);
        }

        String newProjectPath = '/' + newProjectName;

        projectConfigDto.setName(newProjectName);
        projectConfigDto.setPath(newProjectPath);

        Project project = projectManager.getProject(workspace, newProjectPath);

        FolderEntry baseProjectFolder = (FolderEntry)projectManager.getProjectsRoot(workspace).getChild(newProjectPath);
        if (project != null) {
            project = projectManager.updateProject(workspace, newProjectPath, projectConfigDto);
            reindexProject(System.currentTimeMillis(), baseProjectFolder, project);
        } else {
            try {
                project = projectManager.convertFolderToProject(workspace, newProjectPath, projectConfigDto);
                reindexProject(System.currentTimeMillis(), baseProjectFolder, project);
                eventService.publish(new ProjectCreatedEvent(project.getWorkspace(), project.getPath()));
                logProjectCreatedEvent(projectConfigDto.getName(), projectConfigDto.getType());
            } catch (ConflictException | ForbiddenException | ServerException e) {
                project = new NotValidProject(baseProjectFolder, projectManager);

                return toProjectConfig(project, getServiceContext().getServiceUriBuilder());
            }
        }

        return toProjectConfig(project, getServiceContext().getServiceUriBuilder());
    }

    @ApiOperation(value = "Estimates if the folder supposed to be project of certain type",
                  response = Map.class,
                  position = 20)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Project with specified path doesn't exist in workspace"),
            @ApiResponse(code = 403, message = "Access to requested project is forbidden"),
            @ApiResponse(code = 500, message = "Server error")})
    @GET
    @Path("/estimate/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<String>> estimateProject(@ApiParam(value = "ID of workspace to estimate projects", required = true)
                                                     @PathParam("ws-id") String workspace,
                                                     @ApiParam(value = "Path to requested project", required = true)
                                                     @PathParam("path") String path,
                                                     @ApiParam(value = "Project Type ID to estimate against", required = true)
                                                     @QueryParam("type") String projectType)
            throws NotFoundException, ForbiddenException, ServerException, ConflictException {

        final HashMap<String, List<String>> attributes = new HashMap<>();

        for (Map.Entry<String, AttributeValue> attr : projectManager.estimateProject(workspace, path, projectType).entrySet()) {
            attributes.put(attr.getKey(), attr.getValue().getList());
        }

        return attributes;
    }

    @GET
    @Path("/resolve/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SourceEstimation> resolveSources(@ApiParam(value = "ID of workspace to estimate projects", required = true)
                                                 @PathParam("ws-id") String workspace,
                                                 @ApiParam(value = "Path to requested project", required = true)
                                                 @PathParam("path") String path)
            throws NotFoundException, ForbiddenException, ServerException, ConflictException {

        return projectManager.resolveSources(workspace, path, false);
    }

    @ApiOperation(value = "Create file",
                  notes = "Create a new file in a project. If file type isn't specified the server will resolve its type.",
                  position = 7)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 409, message = "File already exists"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Consumes({MediaType.MEDIA_TYPE_WILDCARD})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/file/{parent:.*}")
    public Response createFile(@ApiParam(value = "Workspace ID", required = true)
                               @PathParam("ws-id") String workspace,
                               @ApiParam(value = "Path to a target directory", required = true)
                               @PathParam("parent") String parentPath,
                               @ApiParam(value = "New file name", required = true)
                               @QueryParam("name") String fileName,
                               InputStream content) throws NotFoundException, ConflictException, ForbiddenException, ServerException {
        filesBuffer.addToBuffer(parentPath + '/' + fileName);

        final FolderEntry parent = asFolder(workspace, parentPath);
        final FileEntry newFile = parent.createFile(fileName, content);
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final ItemReference fileReference = DtoConverter.toItemReference(newFile, uriBuilder.clone());
        final URI location = uriBuilder.clone().path(getClass(), "getFile").build(workspace, newFile.getPath().substring(1));

        eventService.publish(new ProjectItemModifiedEvent(ProjectItemModifiedEvent.EventType.CREATED,
                                                          workspace, projectPath(newFile.getPath()), newFile.getPath(), false));

        return Response.created(location).entity(fileReference).build();
    }

    @ApiOperation(value = "Create a folder",
                  notes = "Create a folder is a specified project",
                  position = 8)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 409, message = "File already exists"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/folder/{path:.*}")
    public Response createFolder(@ApiParam(value = "Workspace ID", required = true)
                                 @PathParam("ws-id") String workspace,
                                 @ApiParam(value = "Path to a new folder destination", required = true)
                                 @PathParam("path") String path) throws ConflictException,
                                                                        ForbiddenException,
                                                                        ServerException,
                                                                        NotFoundException {
        filesBuffer.addToBuffer(path);

        final FolderEntry newFolder = projectManager.getProjectsRoot(workspace).createFolder(path);
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final ItemReference folderReference = DtoConverter.toItemReference(newFolder, uriBuilder.clone(), projectManager);
        final URI location = uriBuilder.clone().path(getClass(), "getChildren").build(workspace, newFolder.getPath().substring(1));

        eventService.publish(new ProjectItemModifiedEvent(ProjectItemModifiedEvent.EventType.CREATED,
                                                          workspace, projectPath(newFolder.getPath()), newFolder.getPath(), true));

        return Response.created(location).entity(folderReference).build();
    }

    @ApiOperation(value = "Upload a file",
                  notes = "Upload a new file",
                  position = 9)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 409, message = "File already exists"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.TEXT_HTML})
    @Path("/uploadfile/{parent:.*}")
    public Response uploadFile(@ApiParam(value = "Workspace ID", required = true)
                               @PathParam("ws-id") String workspace,
                               @ApiParam(value = "Destination path", required = true)
                               @PathParam("parent") String parentPath,
                               Iterator<FileItem> formData)
            throws NotFoundException, ConflictException, ForbiddenException, ServerException {
        final FolderEntry parent = asFolder(workspace, parentPath);

        return VirtualFileSystemImpl.uploadFile(parent.getVirtualFile(), formData);
    }

    @ApiOperation(value = "Upload zip folder",
                  notes = "Upload folder from local zip",
                  response = Response.class,
                  position = 10)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 401, message = "User not authorized to call this operation"),
            @ApiResponse(code = 403, message = "Forbidden operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 409, message = "Resource already exists"),
            @ApiResponse(code = 500, message = "Internal Server Error")})

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/upload/zipfolder/{path:.*}")
    public Response uploadFolderFromZip(@ApiParam(value = "Workspace ID", required = true)
                                        @PathParam("ws-id") String workspace,
                                        @ApiParam(value = "Path in the project", required = true)
                                        @PathParam("path") String path,
                                        Iterator<FileItem> formData)
            throws ServerException, ConflictException, ForbiddenException, NotFoundException {

        final FolderEntry parent = asFolder(workspace, path);
        return VirtualFileSystemImpl.uploadZip(parent.getVirtualFile(), formData);
    }

    @ApiOperation(value = "Get file content",
                  notes = "Get file content by its name",
                  position = 11)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/file/{path:.*}")
    public Response getFile(@ApiParam(value = "Workspace ID", required = true)
                            @PathParam("ws-id") String workspace,
                            @ApiParam(value = "Path to a file", required = true)
                            @PathParam("path") String path)
            throws IOException, NotFoundException, ForbiddenException, ServerException {
        final FileEntry file = asFile(workspace, path);
        return Response.ok().entity(file.getInputStream()).type(file.getMediaType()).build();
    }

    @ApiOperation(value = "Update file",
                  notes = "Update an existing file with new content",
                  position = 12)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @PUT
    @Consumes({MediaType.MEDIA_TYPE_WILDCARD})
    @Path("/file/{path:.*}")
    public Response updateFile(@ApiParam(value = "Workspace ID", required = true)
                               @PathParam("ws-id") String workspace,
                               @ApiParam(value = "Full path to a file", required = true)
                               @PathParam("path") String path,
                               InputStream content) throws NotFoundException, ForbiddenException, ServerException {
        final FileEntry file = asFile(workspace, path);
        file.updateContent(content);

        eventService.publish(new ProjectItemModifiedEvent(ProjectItemModifiedEvent.EventType.UPDATED,
                                                          workspace, projectPath(file.getPath()), file.getPath(), false));
        return Response.ok().build();
    }

    @ApiOperation(value = "Delete a resource",
                  notes = "Delete resources. If you want to delete a single project, specify project name. If a folder or file needs to " +
                          "be deleted a path to the requested resource needs to be specified",
                  position = 13)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ""),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("/{path:.*}")
    public void delete(@ApiParam("Workspace ID")
                       @PathParam("ws-id") String workspace,
                       @ApiParam("Path to a resource to be deleted")
                       @PathParam("path") String path) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        try {
            filesBuffer.addToBufferRecursive(getVirtualFileEntry(workspace, path).getVirtualFile());
        } catch (NotFoundException exception) {
            LOG.warn(exception.getMessage(), exception);

            projectManager.delete(workspace, path);

            return;
        }

        projectManager.delete(workspace, path);
    }

    @ApiOperation(value = "Delete module from project",
                  notes = "Delete module from project and update project in workspace",
                  position = 29)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ""),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("module/{path:.*}")
    public void deleteModule(@ApiParam("Workspace ID")
                             @PathParam("ws-id") String workspace,
                             @ApiParam("Path to a resource to be deleted")
                             @PathParam("path") String pathToParent,
                             @QueryParam("module") String pathToModule) throws NotFoundException,
                                                                               ForbiddenException,
                                                                               ConflictException,
                                                                               ServerException {
        filesBuffer.addToBufferRecursive(getVirtualFileEntry(workspace, pathToParent).getVirtualFile());

        projectManager.deleteModule(workspace, pathToParent, pathToModule);
    }

    @ApiOperation(value = "Copy resource",
                  notes = "Copy resource to a new location which is specified in a query parameter",
                  position = 15)
    @ApiResponses({@ApiResponse(code = 201, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "Resource already exists"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/copy/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response copy(@ApiParam("Workspace ID") @PathParam("ws-id") String workspace,
                         @ApiParam("Path to a resource") @PathParam("path") String path,
                         @ApiParam(value = "Path to a new location", required = true) @QueryParam("to") String newParent,
                         CopyOptions copyOptions) throws NotFoundException,
                                                         ForbiddenException,
                                                         ConflictException,
                                                         ServerException {
        final VirtualFileEntry entry = getVirtualFileEntry(workspace, path);

        filesBuffer.addToBuffer(newParent + '/' + entry.getName(), path);
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
        final VirtualFileEntry copy = entry.copyTo(newParent, newName, isOverWrite);
        final URI location = getServiceContext().getServiceUriBuilder()
                                                .path(getClass(), copy.isFile() ? "getFile" : "getChildren")
                                                .build(workspace, copy.getPath().substring(1));
        if (copy.isFolder()) {
            final Project project = projectManager.getProject(copy.getWorkspace(), copy.getPath());
            if (project != null) {
                final String name = project.getName();
                final String projectType = project.getConfig().getType();

                logProjectCreatedEvent(name, projectType);
            }
        }
        return Response.created(location).build();
    }

    @ApiOperation(value = "Move resource",
                  notes = "Move resource to a new location which is specified in a query parameter",
                  position = 15)
    @ApiResponses({@ApiResponse(code = 201, message = ""),
                   @ApiResponse(code = 403, message = "User not authorized to call this operation"),
                   @ApiResponse(code = 404, message = "Not found"),
                   @ApiResponse(code = 409, message = "Resource already exists"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/move/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response move(@ApiParam("Workspace ID") @PathParam("ws-id") String workspace,
                         @ApiParam("Path to a resource to be moved") @PathParam("path") String path,
                         @ApiParam("Path to a new location") @QueryParam("to") String newParent,
                         MoveOptions moveOptions)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        final VirtualFileEntry entry = getVirtualFileEntry(workspace, path);
        filesBuffer.addToBuffer(newParent + '/' + entry.getName(), path);

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

        entry.moveTo(newParent, newName, isOverWrite);
        final URI location = getServiceContext().getServiceUriBuilder()
                                                .path(getClass(), entry.isFile() ? "getFile" : "getChildren")
                                                .build(workspace, entry.getPath().substring(1));
        if (entry.isFolder()) {
            final Project project = projectManager.getProject(entry.getWorkspace(), entry.getPath());
            if (project != null) {
                final String name = project.getName();
                final String projectType = project.getConfig().getType();
                LOG.info("EVENT#project-destroyed# PROJECT#{}# TYPE#{}# WS#{}# USER#{}#", name, projectType,
                         EnvironmentContext.getCurrent().getWorkspaceName(), EnvironmentContext.getCurrent().getUser().getName());

                logProjectCreatedEvent(name, projectType);
            }
        }
        eventService.publish(new ProjectItemModifiedEvent(ProjectItemModifiedEvent.EventType.MOVED,
                                                          workspace, projectPath(entry.getPath()), entry.getPath(), entry.isFolder(),
                                                          path));
        return Response.created(location).build();
    }

    @ApiOperation(value = "Rename resource",
                  notes = "Rename resources. It can be project, module, folder or file",
                  position = 16)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 409, message = "Resource already exists"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/rename/{path:.*}")
    public Response rename(@ApiParam(value = "Workspace ID", required = true)
                           @PathParam("ws-id") String workspace,
                           @ApiParam(value = "Path to resource to be renamed", required = true)
                           @PathParam("path") String path,
                           @ApiParam(value = "New name", required = true)
                           @QueryParam("name") String newName,
                           @ApiParam(value = "New media type")
                           @QueryParam("mediaType") String newMediaType)
            throws NotFoundException, ConflictException, ForbiddenException, ServerException, IOException {
        final VirtualFileEntry entry = projectManager.rename(workspace, path, newName, newMediaType);
        if (entry == null) {
            throw new NotFoundException(String.format("Path '%s' doesn't exist.", path));
        }

        final int startOldName = path.lastIndexOf("/") + 1;
        final String pathToFile = path.substring(0, startOldName);
        filesBuffer.addToBuffer(pathToFile + newName, path);
        filesBuffer.addToBufferRecursive(entry.getVirtualFile());

        final URI location = getServiceContext().getServiceUriBuilder()
                                                .path(getClass(), entry.isFile() ? "getFile" : "getChildren")
                                                .build(workspace, entry.getPath().substring(1));
        eventService.publish(new ProjectItemModifiedEvent(ProjectItemModifiedEvent.EventType.RENAMED,
                                                          workspace,
                                                          projectPath(entry.getPath()),
                                                          entry.getPath(),
                                                          entry.isFolder(),
                                                          path));
        return Response.created(location).build();
    }

    @ApiOperation(value = "Import resource",
                  notes = "Import resource. JSON with a designated importer and project location is sent. It is possible to import from " +
                          "VCS or ZIP",
                  position = 17)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ""),
            @ApiResponse(code = 401, message = "User not authorized to call this operation"),
            @ApiResponse(code = 403, message = "Forbidden operation"),
            @ApiResponse(code = 409, message = "Resource already exists"),
            @ApiResponse(code = 500, message = "Unsupported source type")})
    @POST
    @Path("/import/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void importProject(@ApiParam(value = "Workspace ID", required = true)
                              @PathParam("ws-id") String workspace,
                              @ApiParam(value = "Path in the project", required = true)
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

        final ProjectImporter importer = importers.getImporter(sourceStorage.getType());
        if (importer == null) {
            throw new ServerException(String.format("Unable import sources project from '%s'. Sources type '%s' is not supported.",
                                                    sourceStorage.getLocation(), sourceStorage.getType()));
        }
        // Preparing websocket output publisher to broadcast output of import process to the ide clients while importing
        final LineConsumerFactory outputOutputConsumerFactory = () -> new ProjectImportOutputWSLineConsumer(path, workspace, 300);

        // Not all importers uses virtual file system API. In this case virtual file system API doesn't get events and isn't able to set
        // correct creation time. Need do it manually.
        VirtualFileEntry virtualFile = getVirtualFile(workspace, path, force);

        filesBuffer.addToBuffer(path);
        filesBuffer.addToBufferRecursive(virtualFile.getVirtualFile());

        final FolderEntry baseProjectFolder = (FolderEntry)virtualFile;
        importer.importSources(baseProjectFolder, sourceStorage, outputOutputConsumerFactory);
    }

    private VirtualFileEntry getVirtualFile(String workspace, String path, boolean force)
            throws ServerException, ForbiddenException, ConflictException, NotFoundException {
        VirtualFileEntry virtualFile = projectManager.getProjectsRoot(workspace).getChild(path);
        if (virtualFile != null && virtualFile.isFile()) {
            // File with same name exist already exists.
            throw new ConflictException(String.format("File with the name '%s' already exists.", path));
        } else {
            if (virtualFile == null) {
                return projectManager.getProjectsRoot(workspace).createFolder(path);
            } else if (!force) {
                // Project already exists.
                throw new ConflictException(String.format("Project with the name '%s' already exists.", path));
            }
        }
        return virtualFile;
    }


    /**
     * Some importers don't use virtual file system API and changes are not indexed.
     * Force searcher to reindex project to fix such issues.
     *
     * @param creationDate
     * @param baseProjectFolder
     * @param project
     * @throws ServerException
     */
    private void reindexProject(long creationDate, FolderEntry baseProjectFolder, final Project project) throws ServerException {
        final VirtualFile file = baseProjectFolder.getVirtualFile();
        executor.execute(() -> {
            try {
                searcherProvider.getSearcher(file.getMountPoint(), true).add(file);
            } catch (Exception e) {
                LOG.warn(String.format("Workspace: %s, project: %s", project.getWorkspace(), project.getPath()), e.getMessage());
            }
        });
        if (creationDate > 0) {
            final ProjectMisc misc = project.getMisc();
            misc.setCreationDate(creationDate);
        }
    }

    @ApiOperation(value = "Upload zip project",
                  notes = "Upload project from local zip",
                  response = ProjectConfigDto.class,
                  position = 18)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 401, message = "User not authorized to call this operation"),
            @ApiResponse(code = 403, message = "Forbidden operation"),
            @ApiResponse(code = 409, message = "Resource already exists"),
            @ApiResponse(code = 500, message = "Unsupported source type")})

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/upload/zipproject/{path:.*}")
    public ProjectConfigDto uploadProjectFromZip(@ApiParam(value = "Workspace ID", required = true)
                                                 @PathParam("ws-id") String workspace,
                                                 @ApiParam(value = "Path in the project", required = true)
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
        final FolderEntry baseProjectFolder = (FolderEntry)getVirtualFile(workspace, path, force);

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
                    throw new ServerException("More then one upload file is found but only one should be. ");
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

        ProjectConfigDto projectConfig = DtoFactory.getInstance()
                                                   .createDto(ProjectConfigDto.class)
                                                   .withName(projectName)
                                                   .withDescription(projectDescription);

        List<SourceEstimation> sourceEstimations = projectManager.resolveSources(workspace, path, false);

        for (SourceEstimation estimation : sourceEstimations) {
            ProjectType projectType = typeRegistry.getProjectType(estimation.getType());

            if (projectType.isPersisted()) {
                projectConfig.withType(projectType.getId());
                projectConfig.withAttributes(estimation.getAttributes());
            }
        }

        //project source already imported going to configure project
        return updateProject(workspace, path, projectConfig);
    }

    @ApiOperation(value = "Import zip",
                  notes = "Import resources as zip",
                  position = 19)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 409, message = "Resource already exists"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/import/{path:.*}")
    @Consumes(ExtMediaType.APPLICATION_ZIP)
    public Response importZip(@ApiParam(value = "Workspace ID", required = true)
                              @PathParam("ws-id") String workspace,
                              @ApiParam(value = "Path to a location (where import to?)")
                              @PathParam("path") String path,
                              InputStream zip,
                              @DefaultValue("false") @QueryParam("skipFirstLevel") Boolean skipFirstLevel)
            throws NotFoundException, ConflictException, ForbiddenException, ServerException {
        final FolderEntry parent = asFolder(workspace, path);
        VirtualFileSystemImpl.importZip(parent.getVirtualFile(), zip, true, skipFirstLevel);
        final Project project = projectManager.getProject(workspace, path);
        if (project != null) {
            eventService.publish(new ProjectCreatedEvent(project.getWorkspace(), project.getPath()));
            final String projectType = project.getConfig().getType();
            logProjectCreatedEvent(path, projectType);
        }
        return Response.created(getServiceContext().getServiceUriBuilder()
                                                   .path(getClass(), "getChildren")
                                                   .build(workspace, parent.getPath().substring(1))).build();
    }

    @ApiOperation(value = "Download ZIP",
                  notes = "Export resource as zip. It can be an entire project or folder",
                  position = 20)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/export/{path:.*}")
    @Produces(ExtMediaType.APPLICATION_ZIP)
    public ContentStream exportZip(@ApiParam(value = "Workspace ID", required = true)
                                   @PathParam("ws-id") String workspace,
                                   @ApiParam(value = "Path to resource to be imported")
                                   @PathParam("path") String path)
            throws NotFoundException, ForbiddenException, ServerException {
        final FolderEntry folder = asFolder(workspace, path);
        return VirtualFileSystemImpl.exportZip(folder.getVirtualFile());
    }

    @POST
    @Path("/export/{path:.*}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(ExtMediaType.APPLICATION_ZIP)
    public Response exportDiffZip(@PathParam("ws-id") String workspace, @PathParam("path") String path, InputStream in)
            throws NotFoundException, ForbiddenException, ServerException {
        final FolderEntry folder = asFolder(workspace, path);
        return VirtualFileSystemImpl.exportZip(folder.getVirtualFile(), in);
    }

    @POST
    @Path("/export/{path:.*}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public Response exportDiffZipMultipart(@PathParam("ws-id") String workspace, @PathParam("path") String path, InputStream in)
            throws NotFoundException, ForbiddenException, ServerException {
        final FolderEntry folder = asFolder(workspace, path);
        return VirtualFileSystemImpl.exportZipMultipart(folder.getVirtualFile(), in);
    }

    @GET
    @Path("/export/file/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportFile(@ApiParam(value = "Workspace ID", required = true)
                               @PathParam("ws-id") String workspace,
                               @ApiParam(value = "Path to resource to be imported")
                               @PathParam("path") String path)
            throws NotFoundException, ForbiddenException, ServerException {
        final FileEntry file = asFile(workspace, path);
        ContentStream content = file.getVirtualFile().getContent();
        return VirtualFileSystemImpl.downloadFile(content);
    }

    @ApiOperation(value = "Get project children items",
                  notes = "Request all children items for a project, such as files and folders",
                  response = ItemReference.class,
                  responseContainer = "List",
                  position = 21)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/children/{parent:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemReference> getChildren(@ApiParam(value = "Workspace ID", required = true)
                                           @PathParam("ws-id") String workspace,
                                           @ApiParam(value = "Path to a project", required = true)
                                           @PathParam("parent") String path)
            throws NotFoundException, ForbiddenException, ServerException {
        final FolderEntry folder = asFolder(workspace, path);
        final List<VirtualFileEntry> children = folder.getChildren();
        final ArrayList<ItemReference> result = new ArrayList<>(children.size());
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        for (VirtualFileEntry child : children) {
            if (child.isFile()) {
                result.add(DtoConverter.toItemReference((FileEntry)child, uriBuilder.clone()));
            } else {
                result.add(DtoConverter.toItemReference((FolderEntry)child, uriBuilder.clone(), projectManager));
            }
        }

        return result;
    }

    @ApiOperation(value = "Get project tree",
                  notes = "Get project tree. Depth is specified in a query parameter",
                  response = TreeElement.class,
                  position = 22)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/tree/{parent:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public TreeElement getTree(@ApiParam(value = "Workspace ID", required = true)
                               @PathParam("ws-id") String workspace,
                               @ApiParam(value = "Path to resource. Can be project or its folders", required = true)
                               @PathParam("parent") String path,
                               @ApiParam(value = "Tree depth. This parameter can be dropped. If not specified ?depth=1 is used by default")
                               @DefaultValue("1") @QueryParam("depth") int depth,
                               @ApiParam(value = "include children files (in addition to children folders). This parameter can be dropped" +
                                                 ". If not specified ?includeFiles=false is used by default")
                               @DefaultValue("false") @QueryParam("includeFiles") boolean includeFiles)
            throws NotFoundException, ForbiddenException, ServerException {
        final FolderEntry folder = asFolder(workspace, path);
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final DtoFactory dtoFactory = DtoFactory.getInstance();
        return dtoFactory.createDto(TreeElement.class)
                         .withNode(DtoConverter.toItemReference(folder, uriBuilder.clone(), projectManager))
                         .withChildren(getTree(folder, depth, includeFiles, uriBuilder, dtoFactory));
    }

    @ApiOperation(value = "Get file or folder",
                  response = ItemReference.class,
                  position = 28)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/item/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public ItemReference getItem(@ApiParam(value = "Workspace ID", required = true)
                                 @PathParam("ws-id") String workspace,
                                 @ApiParam(value = "Path to resource. Can be project or its folders", required = true)
                                 @PathParam("path") String path)
            throws NotFoundException, ForbiddenException, ServerException, ValueStorageException,
                   ProjectTypeConstraintException {

        Project project = projectManager.getProject(workspace, projectPath(path));
        final VirtualFileEntry entry;
        if (project != null) {
            // If there is a project, allow it to intercept getting file meta-data
            entry = project.getItem(path);
        } else {
            // If there is no project, try to retrieve the item directly
            FolderEntry wsRoot = projectManager.getProjectsRoot(workspace);
            if (wsRoot != null) {
                entry = wsRoot.getChild(path);
            } else {
                entry = null;
            }
        }
        if (entry == null) {
            throw new NotFoundException("Project " + path + " was not found");
        }

        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();

        ItemReference item;
        if (entry.isFile()) {
            item = DtoConverter.toItemReference((FileEntry)entry, uriBuilder.clone());
        } else {
            item = DtoConverter.toItemReference((FolderEntry)entry, uriBuilder.clone(), projectManager);
        }

        return item;
    }

    private List<TreeElement> getTree(FolderEntry folder, int depth, boolean includeFiles, UriBuilder uriBuilder, DtoFactory dtoFactory)
            throws ServerException {
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
                nodes.add(dtoFactory.createDto(TreeElement.class)
                                    .withNode(DtoConverter.toItemReference((FolderEntry)child, uriBuilder.clone(), projectManager))
                                    .withChildren(getTree((FolderEntry)child, depth - 1, includeFiles, uriBuilder, dtoFactory)));
            } else { // child.isFile()
                nodes.add(dtoFactory.createDto(TreeElement.class)
                                    .withNode(DtoConverter.toItemReference((FileEntry)child, uriBuilder.clone())));
            }
        }
        return nodes;
    }

    @ApiOperation(value = "Search for resources",
                  notes = "Search for resources applying a number of search filters as query parameters",
                  response = ItemReference.class,
                  responseContainer = "List",
                  position = 23)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 409, message = "Conflict error"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/search/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemReference> search(@ApiParam(value = "Workspace ID", required = true)
                                      @PathParam("ws-id") String workspace,
                                      @ApiParam(value = "Path to resource, i.e. where to search?", required = true)
                                      @PathParam("path") String path,
                                      @ApiParam(value = "Resource name")
                                      @QueryParam("name") String name,
                                      @ApiParam(value = "Media type")
                                      @QueryParam("mediatype") String mediatype,
                                      @ApiParam(value = "Search keywords")
                                      @QueryParam("text") String text,
                                      @ApiParam(value = "Maximum items to display. If this parameter is dropped, there are no limits")
                                      @QueryParam("maxItems") @DefaultValue("-1") int maxItems,
                                      @ApiParam(value = "Skip count")
                                      @QueryParam("skipCount") int skipCount)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        // to search from workspace root path should end with "/" i.e /{ws}/search/?<query>
        final FolderEntry folder = path.isEmpty() ? projectManager.getProjectsRoot(workspace) : asFolder(workspace, path);
        if (searcherProvider != null) {
            if (skipCount < 0) {
                throw new ConflictException(String.format("Invalid 'skipCount' parameter: %d.", skipCount));
            }
            final QueryExpression query = new QueryExpression().setPath(path.startsWith("/") ? path : ('/' + path))
                                                               .setName(name)
                                                               .setText(text)
                                                               .setSkipCount(skipCount)
                                                               .setMaxItems(maxItems);

            final String[] paths = searcherProvider.getSearcher(folder.getVirtualFile().getMountPoint(), true).search(query);
            final List<ItemReference> items = new ArrayList<>(paths.length);
            final FolderEntry root = projectManager.getProjectsRoot(workspace);
            final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
            for (String relativePath : paths) {
                VirtualFileEntry child = null;
                try {
                    child = root.getChild(relativePath);
                } catch (ForbiddenException ignored) {
                    // Ignore item that user can't access
                }
                if (child != null && child.isFile()) {
                    items.add(DtoConverter.toItemReference((FileEntry)child, uriBuilder.clone()));
                }
            }
            return items;
        }
        return Collections.emptyList();
    }

    @ApiOperation(value = "Get user permissions in a project",
                  notes = "Get permissions for a user in a specified project, such as read, write, build, " +
                          "run etc. ID of a user is set in a query parameter of a request URL.",
                  response = AccessControlEntry.class,
                  responseContainer = "List",
                  position = 24)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/permissions/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("workspace/admin")
    public List<AccessControlEntry> getPermissions(@ApiParam(value = "Workspace ID", required = true)
                                                   @PathParam("ws-id") String wsId,
                                                   @ApiParam(value = "Path to a project", required = true)
                                                   @PathParam("path") String path,
                                                   @ApiParam(value = "User ID", required = true)
                                                   @QueryParam("userid") Set<String> users)
            throws NotFoundException, ForbiddenException, ServerException {
        final Project project = projectManager.getProject(wsId, path);
        if (project == null) {
            throw new NotFoundException(String.format("Project '%s' doesn't exist in workspace '%s'.", path, wsId));
        }
        final List<AccessControlEntry> acl = project.getPermissions();
        if (!(users == null || users.isEmpty())) {
            for (Iterator<AccessControlEntry> itr = acl.iterator(); itr.hasNext(); ) {
                final AccessControlEntry ace = itr.next();
                final Principal principal = ace.getPrincipal();
                if (principal != null && (principal.getType() != Principal.Type.USER || !users.contains(principal.getName()))) {
                    itr.remove();
                }
            }
        }
        return acl;
    }

    @ApiOperation(value = "Set project visibility",
                  notes = "Set project visibility. Projects can be private or public",
                  position = 25)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/switch_visibility/{path:.*}")
    public void switchVisibility(@ApiParam(value = "Workspace ID", required = true)
                                 @PathParam("ws-id") String wsId,
                                 @ApiParam(value = "Path to a project", required = true)
                                 @PathParam("path") String path,
                                 @ApiParam(value = "Visibility type", required = true, allowableValues = "public,private")
                                 @QueryParam("visibility") String visibility)
            throws NotFoundException, ForbiddenException, ServerException {
        if (visibility == null || visibility.isEmpty()) {
            throw new ServerException(String.format("Invalid visibility '%s'", visibility));
        }
        final Project project = projectManager.getProject(wsId, path);
        if (project == null) {
            throw new NotFoundException(String.format("Project '%s' doesn't exist in workspace '%s'.", path, wsId));
        }
        project.setVisibility(visibility);
    }

    @ApiOperation(value = "Set permissions for a user in a project",
                  notes = "Set permissions for a user in a specified project, such as read, write, build, " +
                          "run etc. ID of a user is set in a query parameter of a request URL.",
                  response = AccessControlEntry.class,
                  responseContainer = "List",
                  position = 26)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this operation"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/permissions/{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("workspace/admin")
    public List<AccessControlEntry> setPermissions(@ApiParam(value = "Workspace ID", required = true)
                                                   @PathParam("ws-id") String wsId,
                                                   @ApiParam(value = "Path to a project", required = true)
                                                   @PathParam("path") String path,
                                                   @ApiParam(value = "Permissions", required = true)
                                                   List<AccessControlEntry> acl)
            throws ForbiddenException, ServerException, NotFoundException {
        final Project project = projectManager.getProject(wsId, path);
        if (project == null) {
            throw new ServerException(String.format("Project '%s' doesn't exist in workspace '%s'. ", path, wsId));
        }
        project.setPermissions(acl);
        return project.getPermissions();
    }

    private FileEntry asFile(String workspace, String path) throws ForbiddenException, NotFoundException, ServerException {
        final VirtualFileEntry entry = getVirtualFileEntry(workspace, path);
        if (!entry.isFile()) {
            throw new ForbiddenException(String.format("Item '%s' isn't a file. ", path));
        }
        return (FileEntry)entry;
    }

    private FolderEntry asFolder(String workspace, String path) throws ForbiddenException, NotFoundException, ServerException {
        final VirtualFileEntry entry = getVirtualFileEntry(workspace, path);
        if (!entry.isFolder()) {
            throw new ForbiddenException(String.format("Item '%s' isn't a folder. ", path));
        }
        return (FolderEntry)entry;
    }

    private VirtualFileEntry getVirtualFileEntry(String workspace, String path)
            throws NotFoundException, ForbiddenException, ServerException {
        final FolderEntry root = projectManager.getProjectsRoot(workspace);
        final VirtualFileEntry entry = root.getChild(path);
        if (entry == null) {
            throw new NotFoundException(String.format("Path '%s' doesn't exist.", path));
        }
        return entry;
    }

    private void logProjectCreatedEvent(@NotNull String projectName, @NotNull String projectType) {
        LOG.info("EVENT#project-created# PROJECT#{}# TYPE#{}# WS#{}# USER#{}# PAAS#default#",
                 projectName,
                 projectType,
                 EnvironmentContext.getCurrent().getWorkspaceId(),
                 EnvironmentContext.getCurrent().getUser().getId());
    }

    private String projectPath(String path) {
        int end = path.indexOf("/");
        if (end == -1) {
            return path;
        }
        return path.substring(0, end);
    }

    /**
     * Class for internal use. Need for marking not valid project.
     * This need for giving possibility to end user to fix problems in project settings.
     * Will be useful then we will migrate IDE2 project to the IDE3 file system.
     */
    private class NotValidProject extends Project {
        public NotValidProject(FolderEntry baseFolder, ProjectManager manager) {
            super(baseFolder, manager);
        }

        @Override
        public ProjectConfig getConfig() throws ServerException, ValueStorageException {
            throw new ServerException("Looks like this is not valid project. We will mark it as broken");
        }
    }
}
