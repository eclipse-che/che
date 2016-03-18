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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Provider;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.server.handlers.CreateModuleHandler;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.PostImportProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectCreatedHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.handlers.ProjectTypeChangedHandler;
import org.eclipse.che.api.project.server.handlers.RemoveModuleHandler;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.Variable;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.observation.VirtualFileEvent;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.api.project.server.Constants.CODENVY_DIR;
import static org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent.EventType.DELETED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author andrew00x
 * @author Artem Zatsarynnyi
 */
@Singleton
public final class DefaultProjectManager implements ProjectManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultProjectManager.class);

    private static final int CACHE_NUM  = 1 << 2;
    private static final int CACHE_MASK = CACHE_NUM - 1;
    private static final int SEG_SIZE   = 32;

    private final Lock[]                                     miscLocks;
    private final Cache<Pair<String, String>, ProjectMisc>[] miscCaches;

    private final VirtualFileSystemRegistry         fileSystemRegistry;
    private final EventService                      eventService;
    private final EventSubscriber<VirtualFileEvent> vfsSubscriber;
    private final ProjectTypeRegistry               projectTypeRegistry;
    private final ProjectHandlerRegistry            handlers;
    private final String                            apiEndpoint;
    private final Provider<AttributeFilter>         filterProvider;
    private final HttpJsonRequestFactory            httpJsonRequestFactory;

    @Inject
    @SuppressWarnings("unchecked")
    public DefaultProjectManager(VirtualFileSystemRegistry fileSystemRegistry,
                                 EventService eventService,
                                 ProjectTypeRegistry projectTypeRegistry,
                                 ProjectHandlerRegistry handlers,
                                 Provider<AttributeFilter> filterProvider,
                                 @Named("api.endpoint") String apiEndpoint,
                                 HttpJsonRequestFactory httpJsonRequestFactory) {

        this.fileSystemRegistry = fileSystemRegistry;
        this.eventService = eventService;
        this.projectTypeRegistry = projectTypeRegistry;
        this.handlers = handlers;
        this.apiEndpoint = apiEndpoint;
        this.filterProvider = filterProvider;
        this.httpJsonRequestFactory = httpJsonRequestFactory;

        this.miscCaches = new Cache[CACHE_NUM];
        this.miscLocks = new Lock[CACHE_NUM];
        for (int i = 0; i < CACHE_NUM; i++) {
            miscLocks[i] = new ReentrantLock();
            miscCaches[i] = CacheBuilder.newBuilder()
                                        .concurrencyLevel(SEG_SIZE)
                                        .removalListener(new RemovalListener<Pair<String, String>, ProjectMisc>() {
                                            @Override
                                            public void onRemoval(RemovalNotification<Pair<String, String>, ProjectMisc> n) {
                                                if (n.getValue().isUpdated()) {
                                                    final int index = n.getKey().hashCode() & CACHE_MASK;
                                                    miscLocks[index].lock();
                                                    try {
                                                        writeProjectMisc(n.getValue().getProject(), n.getValue());
                                                    } catch (Exception e) {
                                                        LOG.error(e.getMessage(), e);
                                                    } finally {
                                                        miscLocks[index].unlock();
                                                    }
                                                }
                                            }
                                        }).build();
        }

        vfsSubscriber = new EventSubscriber<VirtualFileEvent>() {
            @Override
            public void onEvent(VirtualFileEvent event) {
                final String workspace = event.getWorkspaceId();
                final String path = event.getPath();
                if (path.endsWith(Constants.CODENVY_MISC_FILE_RELATIVE_PATH)) {
                    return;
                }
                switch (event.getType()) {
                    case CONTENT_UPDATED:
                    case CREATED:
                    case DELETED:
                    case MOVED:
                    case RENAMED: {
                        final int length = path.length();
                        for (int i = 1; i < length && (i = path.indexOf('/', i)) > 0; i++) {
                            final String projectPath = path.substring(0, i);
                            try {
                                final Project project = getProject(workspace, projectPath);
                                if (project != null) {
                                    getProjectMisc(project).setModificationDate(System.currentTimeMillis());
                                }
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                        break;
                    }
                }
            }
        };
    }

    private static String projectPath(String path) {
        int end = path.indexOf("/");
        if (end == -1) {
            return path;
        }
        return path.substring(0, end);
    }

    @PostConstruct
    void start() {
        eventService.subscribe(vfsSubscriber);
    }

    @PreDestroy
    void stop() {
        eventService.unsubscribe(vfsSubscriber);
        for (int i = 0, length = miscLocks.length; i < length; i++) {
            miscLocks[i].lock();
            try {
                miscCaches[i].cleanUp();
            } finally {
                miscLocks[i].unlock();
            }
        }
    }

    @Override
    public List<Project> getProjects(String workspace) throws ServerException, NotFoundException, ForbiddenException {
        final FolderEntry myRoot = getProjectsRoot(workspace);
        final List<Project> projects = new ArrayList<>();
        for (FolderEntry folder : myRoot.getChildFolders()) {
            final Project project = getProject(workspace, folder.getPath());
            if (project != null) {
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    public Project getProject(String workspace, String projectPath) throws ForbiddenException, ServerException, NotFoundException {
        final FolderEntry myRoot = getProjectsRoot(workspace);
        final VirtualFileEntry child = myRoot.getChild(projectPath.startsWith("/") ? projectPath.substring(1) : projectPath);
        if (child != null && child.isFolder() && isProjectFolder((FolderEntry)child)) {
            return new Project((FolderEntry)child, this);
        }
        return null;
    }

    @Override
    public Project createProject(String workspace,
                                 String name,
                                 ProjectConfig projectConfig,
                                 Map<String, String> options) throws ConflictException,
                                                                     ForbiddenException,
                                                                     ServerException,
                                                                     NotFoundException {
        FolderEntry myRoot = getProjectsRoot(workspace);
        FolderEntry projectFolder = myRoot.createFolder(name);
        Project project = new Project(projectFolder, this);

        CreateProjectHandler generator = handlers.getCreateProjectHandler(projectConfig.getType());

        if (generator != null) {
            Map<String, AttributeValue> valueMap = new HashMap<>();

            Map<String, List<String>> attributes = projectConfig.getAttributes();

            if (attributes != null) {
                for (String key : attributes.keySet()) {
                    valueMap.put(key, new AttributeValue(attributes.get(key)));
                }
            }

            generator.onCreateProject(project.getBaseFolder(), valueMap, options);
        }

        project.updateConfig(projectConfig);

        ProjectMisc misc = project.getMisc();
        misc.setCreationDate(System.currentTimeMillis());
        misc.save(); // Important to save misc!!

        ProjectCreatedHandler projectCreatedHandler = handlers.getProjectCreatedHandler(projectConfig.getType());

        if (projectCreatedHandler != null) {
            projectCreatedHandler.onProjectCreated(project.getBaseFolder());
        }

        return project;
    }

    @Override
    public Project updateProject(String workspace, String path, ProjectConfig newConfig) throws ForbiddenException,
                                                                                                ServerException,
                                                                                                NotFoundException,
                                                                                                ConflictException,
                                                                                                IOException {
        Project project = getProject(workspace, path);
        String oldProjectType = null;
        List<String> oldMixins = new ArrayList<>();
        // If a project does not exist in the target path, create a new one
        if (project == null) {
            FolderEntry projectsRoot = getProjectsRoot(workspace);
            VirtualFileEntry child = projectsRoot.getChild(path);
            if (child != null && child.isFolder() && child.getParent().isRoot()) {
                project = new Project((FolderEntry)child, this);
            } else {
                throw new NotFoundException(String.format("Project '%s' doesn't exist in workspace '%s'.", path, workspace));
            }
        } else {
            try {
                ProjectConfig config = project.getConfig();
                oldProjectType = config.getType();
                oldMixins = config.getMixins();
            } catch (ProjectTypeConstraintException | ValueStorageException e) {
                // here we allow changing bad project type on registered
                LOG.warn(e.getMessage());
            }
        }
        project.updateConfig(newConfig);
        // handle project type changes
        // post actions on changing project type
        // base or mixin
        if (!newConfig.getType().equals(oldProjectType)) {
            ProjectTypeChangedHandler projectTypeChangedHandler = handlers.getProjectTypeChangedHandler(newConfig.getType());
            if (projectTypeChangedHandler != null) {
                projectTypeChangedHandler.onProjectTypeChanged(project.getBaseFolder());
            }
        }
        List<String> mixins = firstNonNull(newConfig.getMixins(), Collections.<String>emptyList());
        for (String mixin : mixins) {
            if (!oldMixins.contains(mixin)) {
                ProjectTypeChangedHandler projectTypeChangedHandler = handlers.getProjectTypeChangedHandler(mixin);
                if (projectTypeChangedHandler != null) {
                    projectTypeChangedHandler.onProjectTypeChanged(project.getBaseFolder());
                }
            }
        }
        return project;
    }

    @Override
    public ProjectConfigDto addModule(String workspaceId,
                                      String pathToParent,
                                      ProjectConfigDto createdModuleDto,
                                      Map<String, String> options) throws ConflictException,
                                                                          ForbiddenException,
                                                                          ServerException,
                                                                          NotFoundException {
        if (createdModuleDto == null) {
            throw new ConflictException("Module not found and module configuration is not defined");
        }

        String[] pathToParentParts = pathToParent.split(String.format("(?=[%s])", File.separator));

        String pathToProject = pathToParentParts[0];

        ProjectConfigDto projectFromWorkspaceDto = getProjectFromWorkspace(workspaceId, pathToProject);

        if (projectFromWorkspaceDto == null) {
            throw new NotFoundException("Parent Project not found " + pathToProject);
        }

        String absolutePathToParent = pathToParent.startsWith("/") ? pathToParent : '/' + pathToParent;

        ProjectConfigDto parentModule = projectFromWorkspaceDto.findModule(absolutePathToParent);

        if (parentModule == null) {
            parentModule = projectFromWorkspaceDto;
        }

        parentModule.getModules().add(createdModuleDto);

        VirtualFileEntry parentFolder = getProjectsRoot(workspaceId).getChild(absolutePathToParent);

        if (parentFolder == null) {
            throw new NotFoundException("Parent folder not found for this node " + pathToParent);
        }

        String createdModuleName = createdModuleDto.getName();

        VirtualFileEntry moduleFolder = ((FolderEntry)parentFolder).getChild(createdModuleName);

        if (moduleFolder == null) {
            moduleFolder = ((FolderEntry)parentFolder).createFolder(createdModuleName);
        }

        Project createdModule = new Project((FolderEntry)moduleFolder, this);

        Map<String, AttributeValue> projectAttributes = new HashMap<>();

        Map<String, List<String>> attributes = createdModuleDto.getAttributes();

        if (attributes != null) {
            for (String key : attributes.keySet()) {
                projectAttributes.put(key, new AttributeValue(attributes.get(key)));
            }
        }

        CreateProjectHandler generator = this.getHandlers().getCreateProjectHandler(createdModuleDto.getType());

        if (generator != null) {
            generator.onCreateProject(createdModule.getBaseFolder(), projectAttributes, options);
        }

        ProjectMisc misc = createdModule.getMisc();
        misc.setCreationDate(System.currentTimeMillis());
        misc.save(); // Important to save misc!!

        CreateModuleHandler moduleHandler = this.getHandlers().getCreateModuleHandler(createdModuleDto.getType());

        if (moduleHandler != null) {
            moduleHandler.onCreateModule((FolderEntry)parentFolder,
                                         createdModule.getPath(),
                                         createdModuleDto.getType(),
                                         options);
        }

        createdModuleDto.setPath(createdModule.getPath());

        AttributeFilter attributeFilter = filterProvider.get();

        attributeFilter.addPersistedAttributesToProject(createdModuleDto, (FolderEntry)moduleFolder);

        updateProjectInWorkspace(workspaceId, projectFromWorkspaceDto);

        attributeFilter.addRuntimeAttributesToProject(createdModuleDto, (FolderEntry)moduleFolder);

        return createdModuleDto;
    }

    @Override
    public FolderEntry getProjectsRoot(String workspace) throws ServerException, NotFoundException {
        return new FolderEntry(workspace, fileSystemRegistry.getProvider(workspace).getMountPoint(true).getRoot());
    }

    @Override
    public ProjectConfig getProjectConfig(Project project) throws ServerException,
                                                                  ProjectTypeConstraintException,
                                                                  ValueStorageException,
                                                                  ForbiddenException,
                                                                  NotFoundException {
        ProjectConfigDto projectConfigDto = getProjectFromWorkspace(project.getWorkspace(), project.getPath());
        if (projectConfigDto == null) {
            projectConfigDto = findModule(project);
        }

        FolderEntry projectFolder = project.getBaseFolder();

        AttributeFilter attributeFilter = filterProvider.get();

        attributeFilter.addPersistedAttributesToProject(projectConfigDto, projectFolder);
        attributeFilter.addRuntimeAttributesToProject(projectConfigDto, projectFolder);

        return projectConfigDto;
    }

    private ProjectConfigDto findModule(Project project) throws ServerException {
        String path = project.getPath();
        if (!path.contains("/")){
            return newDto(ProjectConfigDto.class);
        }

        String[] parts = path.split("/");

        int projectNameIndex = 1;

        ProjectConfigDto projectConfig = getProjectFromWorkspace(project.getWorkspace(), "/" + parts[projectNameIndex]);

        if (projectConfig == null) {
            return newDto(ProjectConfigDto.class);
        } else {
            return projectConfig.findModule(project.getPath());
        }
    }

    @Override
    public void updateProjectConfig(Project project, ProjectConfig projectConfig) throws ServerException,
                                                                                         ValueStorageException,
                                                                                         ProjectTypeConstraintException,
                                                                                         ForbiddenException, NotFoundException {
        List<ProjectConfigDto> modules = new ArrayList<>();

        for (ProjectConfig config : projectConfig.getModules()) {
            getModulesRecursive(config, modules);
        }

        final ProjectConfigDto projectConfigDto = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                            .withPath(projectConfig.getPath())
                                                            .withName(projectConfig.getName())
                                                            .withType(projectConfig.getType())
                                                            .withMixins(projectConfig.getMixins())
                                                            .withAttributes(projectConfig.getAttributes())
                                                            .withDescription(projectConfig.getDescription())
                                                            .withModules(modules)
                                                            .withSource(getSourceStorageDto(projectConfig));

        createCodenvyFolder(project);

        filterProvider.get().addPersistedAttributesToProject(projectConfigDto, project.getBaseFolder());

        storeCalculatedAttributes(project, projectConfig);

        updateProjectInWorkspace(project.getWorkspace(), projectConfigDto);
    }

    private void storeCalculatedAttributes(Project project, ProjectConfig projectConfig) throws ProjectTypeConstraintException,
                                                                                                ValueStorageException,
                                                                                                InvalidValueException, NotFoundException {
        final ProjectTypes types = new ProjectTypes(project, projectConfig, this);
        types.removeTransient();

        for (Map.Entry<String, List<String>> entry : projectConfig.getAttributes().entrySet()) {
            String attributeName = entry.getKey();
            List<String> attributeValue = entry.getValue();

            // Try to find definition in all the types
            Attribute attributeDefinition = null;
            for (ProjectTypeDef projectType : types.getAll().values()) {
                attributeDefinition = projectType.getAttribute(attributeName);
                if (attributeDefinition != null) {
                    break;
                }
            }

            // initialize provided attributes
            if (attributeDefinition != null && attributeDefinition.isVariable()) {
                final Variable variable = (Variable)attributeDefinition;
                final ValueProviderFactory valueProviderFactory = variable.getValueProviderFactory();

                // store calculated attribute
                if (valueProviderFactory != null) {
                    valueProviderFactory.newInstance(project.getBaseFolder()).setValues(variable.getName(), attributeValue);
                }

                if (attributeValue == null && variable.isRequired()) {
                    throw new ProjectTypeConstraintException("Required attribute value is initialized with null value " + variable.getId());
                }
            }
        }
    }

    private void getModulesRecursive(ProjectConfig module, List<ProjectConfigDto> projectModules) {
        List<ProjectConfigDto> modules = new ArrayList<>();

        for (ProjectConfig config : module.getModules()) {
            getModulesRecursive(config, modules);
        }

        ProjectConfigDto moduleDto = newDto(ProjectConfigDto.class).withName(module.getName())
                                                                   .withPath(module.getPath())
                                                                   .withType(module.getType())
                                                                   .withDescription(module.getDescription())
                                                                   .withAttributes(module.getAttributes())
                                                                   .withModules(modules)
                                                                   .withContentRoot(module.getContentRoot())
                                                                   .withMixins(module.getMixins());
        projectModules.add(moduleDto);
    }


    private SourceStorageDto getSourceStorageDto(ProjectConfig moduleConfig) {
        SourceStorageDto storageDto = newDto(SourceStorageDto.class);
        if (moduleConfig.getSource() != null) {
            storageDto.withType(moduleConfig.getSource().getType())
                      .withLocation(moduleConfig.getSource().getLocation())
                      .withParameters(moduleConfig.getSource().getParameters());
        }

        return storageDto;
    }

    private void createCodenvyFolder(Project project) throws ServerException {
        // TODO: .codenvy folder creation should be removed when all project's meta-info will be stored on Workspace API
        try {
            final VirtualFileEntry codenvyDir = project.getBaseFolder().getChild(CODENVY_DIR);
            if (codenvyDir == null || !codenvyDir.isFolder()) {
                project.getBaseFolder().createFolder(CODENVY_DIR);
            }
        } catch (ForbiddenException | ConflictException e) {
            throw new ServerException(e.getServiceError());
        }
    }

    private UsersWorkspaceDto getWorkspace(String wsId) throws ServerException {
        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class).path(WorkspaceService.class, "getById")
                                      .build(wsId).toString();
        final Link link = newDto(Link.class).withMethod("GET").withHref(href);

        try {
            return httpJsonRequestFactory.fromLink(link)
                                         .request()
                                         .asDto(UsersWorkspaceDto.class);
        } catch (IOException | ApiException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public ProjectConfigDto getProjectFromWorkspace(@NotNull String wsId, @NotNull String projectPath) throws ServerException {
        final UsersWorkspaceDto usersWorkspaceDto = getWorkspace(wsId);
        final String path = projectPath.startsWith("/") ? projectPath : "/" + projectPath;
        for (ProjectConfigDto projectConfig : usersWorkspaceDto.getConfig().getProjects()) {
            if (path.equals(projectConfig.getPath())) {
                return projectConfig;
            }
        }
        return null;
    }

    public List<ProjectConfigDto> getAllProjectsFromWorkspace(@NotNull String workspaceId) throws ServerException {
        UsersWorkspaceDto usersWorkspaceDto = getWorkspace(workspaceId);

        return usersWorkspaceDto.getConfig().getProjects();
    }

    private void updateWorkspace(String wsId, WorkspaceConfigDto workspaceConfig) throws ServerException {
        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class).path(WorkspaceService.class, "update")
                                      .build(wsId).toString();
        final Link link = newDto(Link.class).withMethod("PUT").withHref(href);

        try {
            httpJsonRequestFactory.fromLink(link)
                                  .setBody(workspaceConfig)
                                  .request();
        } catch (IOException | ApiException e) {
            throw new ServerException(e.getMessage());
        }
    }

    private void updateProjectInWorkspace(String wsId, ProjectConfigDto projectConfig) throws ServerException {
        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class).path(WorkspaceService.class, "updateProject")
                                      .build(wsId).toString();
        final Link link = newDto(Link.class).withMethod("PUT").withHref(href);

        try {
            httpJsonRequestFactory.fromLink(link)
                                  .setBody(projectConfig)
                                  .request();
        } catch (NotFoundException e) {
            final String addProjectHref = UriBuilder.fromUri(apiEndpoint)
                                                    .path(WorkspaceService.class).path(WorkspaceService.class, "addProject")
                                                    .build(wsId).toString();
            final Link addProjectLink = newDto(Link.class).withMethod("POST").withHref(addProjectHref);
            try {
                httpJsonRequestFactory.fromLink(addProjectLink)
                                      .setBody(projectConfig)
                                      .request();
            } catch (IOException | ApiException e1) {
                throw new ServerException(e1.getMessage());
            }
        } catch (IOException | ApiException e) {
            throw new ServerException(e.getMessage());
        }
    }

    @Override
    public ProjectMisc getProjectMisc(Project project) throws ServerException {
        final String workspace = project.getWorkspace();
        final String path = project.getPath();
        final Pair<String, String> key = Pair.of(workspace, path);
        final int index = key.hashCode() & CACHE_MASK;
        miscLocks[index].lock();
        try {
            ProjectMisc misc = miscCaches[index].getIfPresent(key);
            if (misc == null) {
                miscCaches[index].put(key, misc = readProjectMisc(project));
            }
            return misc;
        } finally {
            miscLocks[index].unlock();
        }
    }

    private ProjectMisc readProjectMisc(Project project) throws ServerException {
        try {
            ProjectMisc misc;
            final FileEntry miscFile = (FileEntry)project.getBaseFolder().getChild(Constants.CODENVY_MISC_FILE_RELATIVE_PATH);
            if (miscFile != null) {
                try (InputStream in = miscFile.getInputStream()) {
                    final Properties properties = new Properties();
                    properties.loadFromXML(in);
                    misc = new ProjectMisc(properties, project);
                } catch (IOException e) {
                    throw new ServerException(e.getMessage(), e);
                }
            } else {
                misc = new ProjectMisc(project);
            }
            return misc;
        } catch (ForbiddenException e) {
            // If have access to the project then must have access to its meta-information.
            // If don't have access then treat that as server error.
            throw new ServerException(e.getServiceError());
        }
    }

    @Override
    public void saveProjectMisc(Project project, ProjectMisc misc) throws ServerException {
        if (misc.isUpdated()) {
            final String workspace = project.getWorkspace();
            final String path = project.getPath();
            final Pair<String, String> key = Pair.of(workspace, path);
            final int index = key.hashCode() & CACHE_MASK;
            miscLocks[index].lock();
            try {
                miscCaches[index].invalidate(key);
                writeProjectMisc(project, misc);
                miscCaches[index].put(key, misc);
            } finally {
                miscLocks[index].unlock();
            }
        }
    }

    private void writeProjectMisc(Project project, ProjectMisc misc) throws ServerException {
        try {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try {
                misc.asProperties().storeToXML(bout, null);
            } catch (IOException e) {
                throw new ServerException(e.getMessage(), e);
            }
            FileEntry miscFile = (FileEntry)project.getBaseFolder().getChild(Constants.CODENVY_MISC_FILE_RELATIVE_PATH);
            if (miscFile != null) {
                miscFile.updateContent(bout.toByteArray());
            } else {
                FolderEntry codenvy = (FolderEntry)project.getBaseFolder().getChild(CODENVY_DIR);
                if (codenvy == null) {
                    try {
                        codenvy = project.getBaseFolder().createFolder(CODENVY_DIR);
                    } catch (ConflictException e) {
                        // Already checked existence of folder ".codenvy".
                        throw new ServerException(e.getServiceError());
                    }
                }
                try {
                    codenvy.createFile(Constants.CODENVY_MISC_FILE, bout.toByteArray());
                } catch (ConflictException e) {
                    // Not expected, existence of file already checked
                    throw new ServerException(e.getServiceError());
                }
            }
            LOG.debug("Save misc file of project {} in {}", project.getPath(), project.getWorkspace());
        } catch (ForbiddenException e) {
            // If have access to the project then must have access to its meta-information. If don't have access then treat that as
            // server error.
            throw new ServerException(e.getServiceError());
        }
    }

    @Override
    public List<? extends ProjectConfig> getProjectModules(Project parent) throws ServerException,
                                                                                  ForbiddenException,
                                                                                  ConflictException,
                                                                                  IOException,
                                                                                  NotFoundException {

        ProjectConfigDto project = getProjectFromWorkspace(parent.getWorkspace(), parent.getPath());

        return project.getModules();
    }

    @Override
    public VirtualFileSystemRegistry getVirtualFileSystemRegistry() {
        return fileSystemRegistry;
    }

    @Override
    public ProjectTypeRegistry getProjectTypeRegistry() {
        return this.projectTypeRegistry;
    }

    @Override
    public ProjectHandlerRegistry getHandlers() {
        return handlers;
    }

    @Override
    public Map<String, AttributeValue> estimateProject(String workspace, String path, String projectTypeId)
            throws ServerException, ForbiddenException, NotFoundException, ValueStorageException, ProjectTypeConstraintException {
        ProjectType projectType = projectTypeRegistry.getProjectType(projectTypeId);
        if (projectType == null) {
            throw new NotFoundException("Project Type " + projectTypeId + " not found.");
        }

        final VirtualFileEntry baseFolder = getProjectsRoot(workspace).getChild(path.startsWith("/") ? path.substring(1) : path);
        if (!baseFolder.isFolder()) {
            throw new NotFoundException("Not a folder: " + path);
        }

        Map<String, AttributeValue> attributes = new HashMap<>();

        for (Attribute attr : projectType.getAttributes()) {
            if (attr.isVariable() && ((Variable)attr).getValueProviderFactory() != null) {

                Variable var = (Variable)attr;
                // getValue throws ValueStorageException if not valid
                attributes.put(attr.getName(), var.getValue((FolderEntry)baseFolder));
            }
        }

        return attributes;
    }

    // ProjectSuggestion
    public List<SourceEstimation> resolveSources(String workspace, String path, boolean transientOnly)
            throws ServerException, ForbiddenException, NotFoundException, ProjectTypeConstraintException {
        final List<SourceEstimation> estimations = new ArrayList<>();
        boolean isPresentPrimaryType = false;
        for (ProjectType type : projectTypeRegistry.getProjectTypes(ProjectTypeRegistry.CHILD_TO_PARENT_COMPARATOR)) {
            if (transientOnly && type.isPersisted()) {
                continue;
            }

            final HashMap<String, List<String>> attributes = new HashMap<>();

            try {
                for (Map.Entry<String, AttributeValue> attr : estimateProject(workspace, path, type.getId()).entrySet()) {
                    attributes.put(attr.getKey(), attr.getValue().getList());
                }

                if (!attributes.isEmpty()) {
                    estimations.add(newDto(SourceEstimation.class).withType(type.getId()).withAttributes(attributes));
                    ProjectType projectType = projectTypeRegistry.getProjectType(type.getId());
                    if (projectType.isPrimaryable()) {
                        isPresentPrimaryType = true;
                    }
                }
            } catch (ValueStorageException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        if (!isPresentPrimaryType) {
            estimations.add(newDto(SourceEstimation.class).withType(BaseProjectType.ID));
        }

        return estimations;
    }

    @Override
    public Project convertFolderToProject(String workspace, String path, ProjectConfig projectConfig) throws ConflictException,
                                                                                                             ForbiddenException,
                                                                                                             ServerException,
                                                                                                             NotFoundException,
                                                                                                             IOException {
        final VirtualFileEntry projectEntry = getProjectsRoot(workspace).getChild(path);
        if (projectEntry == null || !projectEntry.isFolder())
            throw new NotFoundException("Not found or not a folder " + path);

        FolderEntry projectFolder = (FolderEntry)projectEntry;

        final Project project = new Project(projectFolder, this);

        if (projectConfig.getType() != null) {
            //TODO: need add checking for concurrency attributes name in giving config and in estimation
            for (Map.Entry<String, AttributeValue> entry : estimateProject(workspace, path, projectConfig.getType()).entrySet()) {
                projectConfig.getAttributes().put(entry.getKey(), entry.getValue().getList());
            }
            project.updateConfig(projectConfig);
        }

        if (projectConfig.getType() != null) {
            PostImportProjectHandler postImportProjectHandler = handlers.getPostImportProjectHandler(projectConfig.getType());
            if (postImportProjectHandler != null) {
                postImportProjectHandler.onProjectImported(project.getBaseFolder());
            }
        }

        final ProjectMisc misc = project.getMisc();
        misc.setCreationDate(System.currentTimeMillis());
        misc.save(); // Important to save misc!!

        return project;
    }

    @Override
    public VirtualFileEntry rename(String workspace, String path, String newName, String newMediaType) throws ForbiddenException,
                                                                                                              ServerException,
                                                                                                              ConflictException,
                                                                                                              NotFoundException {
        final FolderEntry root = getProjectsRoot(workspace);
        final VirtualFileEntry entry = root.getChild(path);

        if (entry == null) {
            return null;
        }

        if (entry.isFile()) {
            // Use the same rules as in method createFile to make client side simpler.
            ((FileEntry)entry).rename(newName, newMediaType);
        } else if (entry.isFolder() && !isProjectFolder((FolderEntry)entry)) {
            entry.rename(newName);
        } else if (isProjectFolder((FolderEntry)entry)) {
            UsersWorkspaceDto usersWorkspace = getWorkspace(workspace);

            String oldProjectPath = path.startsWith("/") ? path : "/" + path;
            String newProjectPath = '/' + newName;

            List<ProjectConfigDto> projects = usersWorkspace.getConfig().getProjects();

            if (projects.isEmpty()) {
                renameProjectOnFileSystem(workspace, oldProjectPath, newName);
            }

            for (ProjectConfigDto projectConfig : projects) {
                String projectPath = projectConfig.getPath();

                if (projectPath == null) {
                    projectPath = '/' + projectConfig.getName();
                }

                if (projectPath.equals(oldProjectPath)) {
                    entry.rename(newName);

                    deleteProjectFromWorkspace(projectConfig, workspace);

                    projectConfig.setPath(newProjectPath);
                    projectConfig.setName(newName);

                    replaceModulesPaths(projectConfig, newProjectPath);

                    updateProjectInWorkspace(workspace, projectConfig);
                }
            }
        }

        return entry;
    }

    private void renameProjectOnFileSystem(String workspaceId, String oldProjectPath, String newName) throws ServerException,
                                                                                                             NotFoundException,
                                                                                                             ConflictException,
                                                                                                             ForbiddenException {
        FolderEntry projectsRoot = getProjectsRoot(workspaceId);
        List<VirtualFileEntry> children = projectsRoot.getChildren();

        for (VirtualFileEntry virtualFileEntry : children) {
            if (virtualFileEntry.getPath().equals(oldProjectPath)) {
                virtualFileEntry.rename(newName);
            }
        }
    }

    private void replaceModulesPaths(ProjectConfigDto projectConfig, String newProjectPath) {
        for (ProjectConfigDto module : projectConfig.getModules()) {
            String modulePath = module.getPath();

            String oldPath = modulePath.startsWith("/") ? modulePath.substring(1) : modulePath;

            int endProjectNameIndex = oldPath.indexOf('/');

            String newModulePath = newProjectPath + oldPath.substring(endProjectNameIndex);

            module.setPath(newModulePath);

            replaceModulesPaths(module, newProjectPath);
        }
    }

    @Override
    public void delete(String workspaceId, String deleteNodePath) throws ServerException,
                                                                         ForbiddenException,
                                                                         NotFoundException,
                                                                         ConflictException {

        String pathToProject = deleteNodePath.contains("/") ? deleteNodePath.substring(0, deleteNodePath.indexOf("/")) : deleteNodePath;

        ProjectConfigDto project = getProjectFromWorkspace(workspaceId, pathToProject);

        if (project != null && deleteNodePath.equals(pathToProject)) {
            deleteProjectFromWorkspace(project, workspaceId);
        }

        VirtualFileEntry entryToDelete = getEntryToDelete(workspaceId, deleteNodePath);

        if (entryToDelete == null) {
            return;
        }

        deleteEntryAndFireEvent(entryToDelete, workspaceId);
    }

    @Nullable
    private VirtualFileEntry getEntryToDelete(String workspaceId, String deleteNodePath) throws ServerException,
                                                                                                NotFoundException,
                                                                                                ForbiddenException {
        FolderEntry root = getProjectsRoot(workspaceId);
        VirtualFileEntry entryToDelete = root.getChild(deleteNodePath);

        if (entryToDelete == null) {
            return null;
        }

        return entryToDelete;
    }

    private void deleteProjectFromWorkspace(ProjectConfig project, String workspaceId) throws ServerException,
                                                                                              ForbiddenException {
        String projectName = project.getName();

        doDeleteProject(workspaceId, projectName);

        String projectType = project.getType();

        LOG.info("EVENT#project-destroyed# PROJECT#{}# TYPE#{}# WS#{}# USER#{}#",
                 project.getName(),
                 projectType != null ? projectType : "unknown",
                 EnvironmentContext.getCurrent().getWorkspaceName(),
                 EnvironmentContext.getCurrent().getUser().getName());
    }

    private void deleteEntryAndFireEvent(VirtualFileEntry entryToDelete, String workspaceId) throws ServerException, ForbiddenException {
        eventService.publish(new ProjectItemModifiedEvent(DELETED,
                                                          workspaceId,
                                                          projectPath(entryToDelete.getPath()),
                                                          entryToDelete.getPath(),
                                                          entryToDelete.isFolder()));
        entryToDelete.remove();
    }

    @Override
    public void deleteModule(String workspaceId, String pathToParent, String pathToModule) throws ServerException,
                                                                                                  NotFoundException,
                                                                                                  ForbiddenException,
                                                                                                  ConflictException {
        VirtualFileEntry entryToDelete = getEntryToDelete(workspaceId, pathToModule);

        pathToModule = pathToModule.startsWith("/") ? pathToModule.substring(1) : pathToModule;

        String pathToProject = pathToModule.contains("/") ? pathToModule.substring(0, pathToModule.indexOf("/")) : pathToModule;

        ProjectConfigDto project = getProjectFromWorkspace(workspaceId, pathToProject);

        deleteModuleFromProject(project, (FolderEntry)entryToDelete, workspaceId);
    }

    private void deleteModuleFromProject(ProjectConfigDto project, FolderEntry entryToDelete, String workspaceId) throws ServerException,
                                                                                                                         NotFoundException,
                                                                                                                         ConflictException,
                                                                                                                         ForbiddenException {
        String pathToModule = entryToDelete.getPath();
        String pathToParentModule = pathToModule.substring(0, pathToModule.lastIndexOf("/"));

        ProjectConfigDto parentModule = project.findModule(pathToParentModule);
        ProjectConfigDto moduleToDelete = project.findModule(pathToModule);

        if (parentModule == null) {
            parentModule = project;
        }

        if (moduleToDelete == null) {
            throw new NotFoundException("Module " + pathToModule + " not found");
        }

        parentModule.getModules().remove(moduleToDelete);

        updateProjectInWorkspace(workspaceId, project);

        RemoveModuleHandler moduleHandler = this.getHandlers().getRemoveModuleHandler(moduleToDelete.getType());

        if (moduleHandler != null) {
            moduleHandler.onRemoveModule(entryToDelete.getParent(), moduleToDelete);
        }

        deleteEntryAndFireEvent(entryToDelete, workspaceId);
    }

    private void doDeleteProject(String wsId, String projectName) throws ServerException {
        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class).path(WorkspaceService.class, "deleteProject")
                                      .build(wsId, projectName).toString();
        final Link link = newDto(Link.class).withMethod("DELETE").withHref(href);

        try {
            httpJsonRequestFactory.fromLink(link)
                                  .request();
        } catch (IOException | ApiException exception) {
            throw new ServerException(exception.getLocalizedMessage(), exception);
        }
    }

    @Override
    public boolean isProjectFolder(FolderEntry folder) throws ServerException {
        try {
            return getProjectFromWorkspace(folder.getWorkspace(), folder.getPath()) != null;
        } catch (ApiException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public boolean isModuleFolder(FolderEntry folder) throws ServerException {
        String pathToModuleFolder = folder.getPath();

        String[] pathToModuleParts = pathToModuleFolder.split(String.format("(?=[%s])", File.separator));

        ProjectConfigDto projectFromWorkspace = getProjectFromWorkspace(folder.getWorkspace(), pathToModuleParts[0]);

        return projectFromWorkspace != null && projectFromWorkspace.findModule(pathToModuleFolder) != null;
    }
}
