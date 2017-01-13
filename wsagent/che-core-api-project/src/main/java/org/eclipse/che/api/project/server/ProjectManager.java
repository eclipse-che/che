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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.project.NewProjectConfig;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.RegisteredProject.Problem;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationListener;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * Facade for all project related operations.
 *
 * @author gazarenkov
 */
@Singleton
public class ProjectManager {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectManager.class);

    private final VirtualFileSystem              vfs;
    private final ProjectTypeRegistry            projectTypeRegistry;
    private final ProjectRegistry                projectRegistry;
    private final ProjectHandlerRegistry         handlers;
    private final ProjectImporterRegistry        importers;
    private final FileTreeWatcher                fileWatcher;
    private final FileWatcherNotificationHandler fileWatchNotifier;
    private final ExecutorService                executor;
    private final WorkspaceProjectsSyncer        workspaceProjectsHolder;
    private final FileWatcherManager             fileWatcherManager;

    @Inject
    public ProjectManager(VirtualFileSystemProvider vfsProvider,
                          EventService eventService,
                          ProjectTypeRegistry projectTypeRegistry,
                          ProjectRegistry projectRegistry,
                          ProjectHandlerRegistry handlers,
                          ProjectImporterRegistry importers,
                          FileWatcherNotificationHandler fileWatcherNotificationHandler,
                          FileTreeWatcher fileTreeWatcher,
                          WorkspaceProjectsSyncer workspaceProjectsHolder,
                          FileWatcherManager fileWatcherManager) throws ServerException {
        this.vfs = vfsProvider.getVirtualFileSystem();
        this.projectTypeRegistry = projectTypeRegistry;
        this.projectRegistry = projectRegistry;
        this.handlers = handlers;
        this.importers = importers;
        this.fileWatchNotifier = fileWatcherNotificationHandler;
        this.fileWatcher = fileTreeWatcher;
        this.workspaceProjectsHolder = workspaceProjectsHolder;
        this.fileWatcherManager = fileWatcherManager;

        executor = Executors.newFixedThreadPool(1 + Runtime.getRuntime().availableProcessors(),
                                                new ThreadFactoryBuilder().setNameFormat("ProjectService-IndexingThread-")
                                                                          .setUncaughtExceptionHandler(
                                                                                  LoggingUncaughtExceptionHandler.getInstance())
                                                                          .setDaemon(true).build());
    }

    @PostConstruct
    void initWatcher() throws IOException {
        FileWatcherNotificationListener defaultListener =
                new FileWatcherNotificationListener(file -> !(file.getPath().toString().contains(".che")
                                                              || file.getPath().toString().contains(".#"))) {
                    @Override
                    public void onFileWatcherEvent(VirtualFile virtualFile, FileWatcherEventType eventType) {
                        LOG.debug("FS event detected: " + eventType + " " + virtualFile.getPath().toString() + " " + virtualFile.isFile());
                    }
                };
        fileWatchNotifier.addNotificationListener(defaultListener);
        try {
            fileWatcher.startup();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            fileWatchNotifier.removeNotificationListener(defaultListener);
        }
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }

    public FolderEntry getProjectsRoot() throws ServerException {
        return new FolderEntry(vfs.getRoot(), projectRegistry);
    }

    public Searcher getSearcher() throws NotFoundException, ServerException {
        final SearcherProvider provider = vfs.getSearcherProvider();
        if (provider == null) {
            throw new NotFoundException("SearcherProvider is not defined in VFS");
        }

        return provider.getSearcher(vfs);
    }

    public void addWatchListener(FileWatcherNotificationListener listener) {
        fileWatchNotifier.addNotificationListener(listener);
    }

    public void removeWatchListener(FileWatcherNotificationListener listener) {
        fileWatchNotifier.removeNotificationListener(listener);
    }

    public void addWatchExcludeMatcher(PathMatcher matcher) {
        fileWatcher.addExcludeMatcher(matcher);
    }

    public void removeWatchExcludeMatcher(PathMatcher matcher) {
        fileWatcher.removeExcludeMatcher(matcher);
    }

    /**
     * @return all the projects
     *
     * @throws ServerException
     *         if projects are not initialized yet
     */
    public List<RegisteredProject> getProjects() throws ServerException {
        return projectRegistry.getProjects();
    }

    /**
     * @param projectPath
     *
     * @return project
     *
     * @throws ServerException
     *         if projects are not initialized yet
     * @throws ServerException
     *         if project not found
     */
    public RegisteredProject getProject(String projectPath) throws ServerException, NotFoundException {
        final RegisteredProject project = projectRegistry.getProject(projectPath);
        if (project == null) {
            throw new NotFoundException(format("Project '%s' doesn't exist.", projectPath));
        }

        return project;
    }

    /**
     * Create project:
     * - take project config
     *
     * @param projectConfig
     *         project configuration
     * @param options
     *         options for generator
     *
     * @return new project
     *
     * @throws ConflictException
     * @throws ForbiddenException
     * @throws ServerException
     * @throws NotFoundException
     */
    public RegisteredProject createProject(ProjectConfig projectConfig, Map<String, String> options) throws ConflictException,
                                                                                                            ForbiddenException,
                                                                                                            ServerException,
                                                                                                            NotFoundException {
        fileWatcherManager.suspend();
        try {
            // path and primary type is mandatory
            if (projectConfig.getPath() == null) {
                throw new ConflictException("Path for new project should be defined ");
            }

            if (projectConfig.getType() == null) {
                throw new ConflictException("Project Type is not defined " + projectConfig.getPath());
            }

            final String path = ProjectRegistry.absolutizePath(projectConfig.getPath());
            if (projectRegistry.getProject(path) != null) {
                throw new ConflictException("Project config already exists for " + path);
            }

            return doCreateProject(projectConfig, options);
        } finally {
            fileWatcherManager.resume();
        }
    }

    /** Note: Use {@link FileWatcherManager#suspend()} and {@link FileWatcherManager#resume()} while creating a project */
    private RegisteredProject doCreateProject(ProjectConfig projectConfig, Map<String, String> options) throws ConflictException,
                                                                                                               ForbiddenException,
                                                                                                               ServerException,
                                                                                                               NotFoundException {
        final String path = ProjectRegistry.absolutizePath(projectConfig.getPath());
        final CreateProjectHandler generator = handlers.getCreateProjectHandler(projectConfig.getType());
        FolderEntry projectFolder;
        if (generator != null) {
            Map<String, AttributeValue> valueMap = new HashMap<>();
            Map<String, List<String>> attributes = projectConfig.getAttributes();
            if (attributes != null) {
                for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
                    valueMap.put(entry.getKey(), new AttributeValue(entry.getValue()));
                }
            }
            if (options == null) {
                options = new HashMap<>();
            }
            Path projectPath = Path.of(path);
            generator.onCreateProject(projectPath, valueMap, options);
            projectFolder = new FolderEntry(vfs.getRoot().getChild(projectPath), projectRegistry);
        } else {
            projectFolder = new FolderEntry(vfs.getRoot().createFolder(path), projectRegistry);
        }

        final RegisteredProject project = projectRegistry.putProject(projectConfig, projectFolder, true, false);
        workspaceProjectsHolder.sync(projectRegistry);
        projectRegistry.fireInitHandlers(project);

        return project;
    }

    /**
     * Create batch of projects according to their configurations.
     * <p/>
     * Notes: - a project will be created by importing when project configuration contains {@link SourceStorage} object,
     * otherwise this one will be created corresponding its {@link NewProjectConfig}:
     * <li> - {@link NewProjectConfig} object contains only one mandatory {@link NewProjectConfig#setPath(String)} field.
     * In this case Project will be created as project of {@link BaseProjectType} type </li>
     * <li> - a project will be created as project of {@link BaseProjectType} type with {@link Problem#code} = 12
     * when declared primary project type is not registered, </li>
     * <li> - a project will be created with {@link Problem#code} = 12 and without mixin project type
     * when declared mixin project type is not registered</li>
     * <li> - for creating a project by generator {@link NewProjectConfig#getOptions()} should be specified.</li>
     *
     * @param projectConfigList
     *         the list of configurations to create projects
     * @param rewrite
     *         whether rewrite or not (throw exception otherwise) if such a project exists
     * @return the list of new projects
     * @throws BadRequestException
     *         when {@link NewProjectConfig} object not contains mandatory {@link NewProjectConfig#setPath(String)} field.
     * @throws ConflictException
     *         when the same path project exists and {@code rewrite} is {@code false}
     * @throws ForbiddenException
     *         when trying to overwrite the project and this one contains at least one locked file
     * @throws NotFoundException
     *         when parent folder does not exist
     * @throws UnauthorizedException
     *         if user isn't authorized to access to location at importing source code
     * @throws ServerException
     *         if other error occurs
     */
    public List<RegisteredProject> createBatchProjects(List<? extends NewProjectConfig> projectConfigList, boolean rewrite, ProjectOutputLineConsumerFactory lineConsumerFactory)
            throws BadRequestException, ConflictException, ForbiddenException, NotFoundException, ServerException, UnauthorizedException,
                   IOException {
        fileWatcherManager.suspend();
        try {
            final List<RegisteredProject> projects = new ArrayList<>(projectConfigList.size());
            validateProjectConfigurations(projectConfigList, rewrite);

            final List<NewProjectConfig> sortedConfigList = projectConfigList
                    .stream()
                    .sorted((config1, config2) -> config1.getPath().compareTo(config2.getPath()))
                    .collect(Collectors.toList());

            for (NewProjectConfig projectConfig : sortedConfigList) {
                RegisteredProject registeredProject;
                final String pathToProject = projectConfig.getPath();

                //creating project(by config or by importing source code)
                try {
                    final SourceStorage sourceStorage = projectConfig.getSource();
                    if (sourceStorage != null && !isNullOrEmpty(sourceStorage.getLocation())) {
                        doImportProject(pathToProject, sourceStorage, rewrite, lineConsumerFactory.setProjectName(projectConfig.getPath()));
                    } else if (!isVirtualFileExist(pathToProject)) {
                        registeredProject = doCreateProject(projectConfig, projectConfig.getOptions());
                        projects.add(registeredProject);
                        continue;
                    }
                } catch (Exception e) {
                    if (!isVirtualFileExist(pathToProject)) {//project folder is absent
                        rollbackCreatingBatchProjects(projects);
                        throw e;
                    }
                }

                //update project
                if (isVirtualFileExist(pathToProject)) {
                    try {
                        registeredProject = updateProject(projectConfig);
                    } catch (Exception e) {
                        registeredProject = projectRegistry.putProject(projectConfig, asFolder(pathToProject), true, false);
                        registeredProject.getProblems().add(new Problem(14, "The project is not updated, caused by " + e.getLocalizedMessage()));
                    }
                } else {
                    registeredProject = projectRegistry.putProject(projectConfig, null, true, false);
                }

                projects.add(registeredProject);
            }

            return projects;

        } finally {
            fileWatcherManager.resume();
        }
    }

    private void rollbackCreatingBatchProjects(List<RegisteredProject> projects) {
        for (RegisteredProject project : projects) {
            try {
                final FolderEntry projectFolder = project.getBaseFolder();
                if (projectFolder != null) {
                    projectFolder.getVirtualFile().delete();
                }
                projectRegistry.removeProjects(project.getPath());
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage());
            }
        }
    }

    private void validateProjectConfigurations(List<? extends NewProjectConfig> projectConfigList, boolean rewrite)
            throws NotFoundException, ServerException, ConflictException, ForbiddenException, BadRequestException {

        for (NewProjectConfig projectConfig : projectConfigList) {
            final String pathToProject = projectConfig.getPath();
            if (isNullOrEmpty(pathToProject)) {
                throw new BadRequestException("Path for new project should be defined");
            }

            final String path = ProjectRegistry.absolutizePath(pathToProject);
            final RegisteredProject registeredProject = projectRegistry.getProject(path);
            if (registeredProject != null && rewrite) {
                delete(path);
            } else if (registeredProject != null) {
                throw new ConflictException(format("Project config already exists for %s", path));
            }

            final String projectTypeId = projectConfig.getType();
            if (isNullOrEmpty(projectTypeId)) {
                projectConfig.setType(BaseProjectType.ID);
            }
        }
    }

    /**
     * Updating project means:
     * - getting the project (should exist)
     * - updating name and description
     * - changing project types and provided attributes
     * - refreshing provided (transient) project types and attributes
     *
     * @param newConfig
     *         new config
     *
     * @return updated config
     *
     * @throws ForbiddenException
     * @throws ServerException
     * @throws NotFoundException
     * @throws ConflictException
     */
    public RegisteredProject updateProject(ProjectConfig newConfig) throws ForbiddenException,
                                                                           ServerException,
                                                                           NotFoundException,
                                                                           ConflictException {
        final String path = newConfig.getPath();
        if (path == null) {
            throw new ConflictException("Project path is not defined");
        }

        final FolderEntry baseFolder = asFolder(path);
        if (baseFolder == null) {
            throw new NotFoundException(format("Folder '%s' doesn't exist.", path));
        }

        final RegisteredProject project = projectRegistry.putProject(newConfig, baseFolder, true, false);
        workspaceProjectsHolder.sync(projectRegistry);

        projectRegistry.fireInitHandlers(project);

        // TODO move to register?
        reindexProject(project);

        return project;
    }

    /**
     *
     * Import source code as a Basic type of Project
     *
     * @param path where to import
     * @param sourceStorage where sources live
     * @param rewrite whether rewrite or not (throw exception othervise) if such a project exists
     *
     * @return Project
     *
     * @throws ServerException
     * @throws IOException
     * @throws ForbiddenException
     * @throws UnauthorizedException
     * @throws ConflictException
     * @throws NotFoundException
     */
    public RegisteredProject importProject(String path, SourceStorage sourceStorage, boolean rewrite, LineConsumerFactory lineConsumerFactory) throws ServerException,
                                                                                                             IOException,
                                                                                                             ForbiddenException,
                                                                                                             UnauthorizedException,
                                                                                                             ConflictException,
                                                                                                             NotFoundException {
        fileWatcherManager.suspend();
        try {
            return doImportProject(path, sourceStorage, rewrite, lineConsumerFactory);
        } finally {
            fileWatcherManager.resume();
        }
    }

    /** Note: Use {@link FileWatcherManager#suspend()} and {@link FileWatcherManager#resume()} while importing source code */
    private RegisteredProject doImportProject(String path, SourceStorage sourceStorage, boolean rewrite, LineConsumerFactory lineConsumerFactory) throws ServerException,
                                                                                                                IOException,
                                                                                                                ForbiddenException,
                                                                                                                UnauthorizedException,
                                                                                                                ConflictException,
                                                                                                                NotFoundException {
        final ProjectImporter importer = importers.getImporter(sourceStorage.getType());
        if (importer == null) {
            throw new NotFoundException(format("Unable import sources project from '%s'. Sources type '%s' is not supported.",
                                               sourceStorage.getLocation(), sourceStorage.getType()));
        }

        String normalizePath = (path.startsWith("/")) ? path : "/".concat(path);
        FolderEntry folder = asFolder(normalizePath);
        if (folder != null && !rewrite) {
            throw new ConflictException(format("Project %s already exists ", path));
        }

        if (folder == null) {
            folder = getProjectsRoot().createFolder(normalizePath);
        }

        try {
            importer.importSources(folder, sourceStorage, lineConsumerFactory);
        } catch (final Exception e) {
            folder.remove();
            throw e;
        }

        final String name = folder.getPath().getName();
        for (ProjectConfig project : workspaceProjectsHolder.getProjects()) {
            if (normalizePath.equals(project.getPath())) {
                // TODO Needed for factory project importing with keepDir. It needs to find more appropriate solution
                List<String> innerProjects = projectRegistry.getProjects(normalizePath);
                for (String innerProject : innerProjects) {
                    RegisteredProject registeredProject = projectRegistry.getProject(innerProject);
                    projectRegistry.putProject(registeredProject, asFolder(registeredProject.getPath()), true, false);
                }
                RegisteredProject rp = projectRegistry.putProject(project, folder, true, false);
                workspaceProjectsHolder.sync(projectRegistry);
                return rp;
            }
        }

        RegisteredProject rp = projectRegistry
                .putProject(new NewProjectConfigImpl(normalizePath, name, BaseProjectType.ID, sourceStorage), folder, true, false);
        workspaceProjectsHolder.sync(projectRegistry);
        return rp;
    }

    /**
     * Estimates if the folder can be treated as a project of particular type
     *
     * @param path to the folder
     * @param projectTypeId project type to estimate
     *
     * @return resolution object
     * @throws ServerException
     * @throws NotFoundException
     */
    public ProjectTypeResolution estimateProject(String path, String projectTypeId) throws ServerException,
                                                                                           NotFoundException {
        final ProjectTypeDef projectType = projectTypeRegistry.getProjectType(projectTypeId);
        if (projectType == null) {
            throw new NotFoundException("Project Type to estimate needed.");
        }

        final FolderEntry baseFolder = asFolder(path);

        if (baseFolder == null) {
            throw new NotFoundException("Folder not found: " + path);
        }

        return projectType.resolveSources(baseFolder);
    }

    /**
     * Estimates to which project types the folder can be converted to
     *
     * @param path to the folder
     * @param transientOnly whether it can be estimated to the transient types of Project only
     *
     * @return list of resolutions
     * @throws ServerException
     * @throws NotFoundException
     */
    public List<ProjectTypeResolution> resolveSources(String path, boolean transientOnly) throws ServerException, NotFoundException {
        final List<ProjectTypeResolution> resolutions = new ArrayList<>();

        for (ProjectType type : projectTypeRegistry.getProjectTypes(ProjectTypeRegistry.CHILD_TO_PARENT_COMPARATOR)) {
            if (transientOnly && type.isPersisted()) {
                continue;
            }

            final ProjectTypeResolution resolution = estimateProject(path, type.getId());
            if (resolution.matched()) {
                resolutions.add(resolution);
            }
        }

        return resolutions;
    }

    /**
     * deletes item including project
     *
     * @param path
     *
     * @throws ServerException
     * @throws ForbiddenException
     * @throws NotFoundException
     * @throws ConflictException
     */
    public void delete(String path) throws ServerException, ForbiddenException, NotFoundException, ConflictException {
        final String apath = ProjectRegistry.absolutizePath(path);

        // delete item
        final VirtualFile item = vfs.getRoot().getChild(Path.of(apath));
        if (item != null) {
            item.delete();
        }

        // delete child projects
        projectRegistry.removeProjects(apath);

        workspaceProjectsHolder.sync(projectRegistry);
    }

    /**
     * Copies item to new path with
     *
     * @param itemPath path to item to copy
     * @param newParentPath path where the item should be copied to
     * @param newName new item name
     * @param overwrite whether existed (if any) item should be overwritten
     *
     * @return new item
     * @throws ServerException
     * @throws NotFoundException
     * @throws ConflictException
     * @throws ForbiddenException
     */
    public VirtualFileEntry copyTo(String itemPath, String newParentPath, String newName, boolean overwrite) throws ServerException,
                                                                                                                    NotFoundException,
                                                                                                                    ConflictException,
                                                                                                                    ForbiddenException {
        VirtualFile oldItem = vfs.getRoot().getChild(Path.of(itemPath));
        if (oldItem == null) {
            throw new NotFoundException("Item not found " + itemPath);
        }

        VirtualFile newParent = vfs.getRoot().getChild(Path.of(newParentPath));
        if (newParent == null) {
            throw new NotFoundException("New parent not found " + newParentPath);
        }

        final VirtualFile newItem = oldItem.copyTo(newParent, newName, overwrite);
        final RegisteredProject owner = projectRegistry.getParentProject(newItem.getPath().toString());
        if (owner == null) {
            throw new NotFoundException("Parent project not found " + newItem.getPath().toString());
        }

        final VirtualFileEntry copy;
        if (newItem.isFile()) {
            copy = new FileEntry(newItem, projectRegistry);
        } else {
            copy = new FolderEntry(newItem, projectRegistry);
        }

        if (copy.isProject()) {
            projectRegistry.getProject(copy.getProject()).getTypes();
            // fire event
        }

        return copy;
    }

    /**
     * Moves item to the new path
     *
     * @param itemPath path to the item
     * @param newParentPath path of new parent
     * @param newName new item's name
     * @param overwrite whether existed (if any) item should be overwritten
     *
     * @return new item
     * @throws ServerException
     * @throws NotFoundException
     * @throws ConflictException
     * @throws ForbiddenException
     */
    public VirtualFileEntry moveTo(String itemPath, String newParentPath, String newName, boolean overwrite) throws ServerException,
                                                                                                                    NotFoundException,
                                                                                                                    ConflictException,
                                                                                                                    ForbiddenException {
        final VirtualFile oldItem = vfs.getRoot().getChild(Path.of(itemPath));
        if (oldItem == null) {
            throw new NotFoundException("Item not found " + itemPath);
        }

        final VirtualFile newParent;
        if (newParentPath == null) {
            // rename only
            newParent = oldItem.getParent();
        } else {
            newParent = vfs.getRoot().getChild(Path.of(newParentPath));
        }

        if (newParent == null) {
            throw new NotFoundException("New parent not found " + newParentPath);
        }

        // TODO lock token ?
        final VirtualFile newItem = oldItem.moveTo(newParent, newName, overwrite, null);
        final RegisteredProject owner = projectRegistry.getParentProject(newItem.getPath().toString());
        if (owner == null) {
            throw new NotFoundException("Parent project not found " + newItem.getPath().toString());
        }

        final VirtualFileEntry move;
        if (newItem.isFile()) {
            move = new FileEntry(newItem, projectRegistry);
        } else {
            move = new FolderEntry(newItem, projectRegistry);
        }

        if (move.isProject()) {
            final RegisteredProject project = projectRegistry.getProject(itemPath);
            NewProjectConfig projectConfig = new NewProjectConfigImpl(newItem.getPath().toString(),
                                                                      project.getType(),
                                                                      project.getMixins(),
                                                                      newName,
                                                                      project.getDescription(),
                                                                      project.getAttributes(),
                                                                      null,
                                                                      project.getSource());

            if (move instanceof FolderEntry) {
                projectRegistry.removeProjects(project.getPath());
                updateProject(projectConfig);
            }
        }

        return move;
    }

    boolean isVirtualFileExist(String path) throws ServerException {
        return asVirtualFileEntry(path) != null;
    }

    FolderEntry asFolder(String path) throws NotFoundException, ServerException {
        final VirtualFileEntry entry = asVirtualFileEntry(path);
        if (entry == null) {
            return null;
        }

        if (!entry.isFolder()) {
            throw new NotFoundException(format("Item '%s' isn't a folder. ", path));
        }

        return (FolderEntry)entry;
    }

    VirtualFileEntry asVirtualFileEntry(String path) throws ServerException {
        final String apath = ProjectRegistry.absolutizePath(path);
        final FolderEntry root = getProjectsRoot();
        return root.getChild(apath);
    }

    FileEntry asFile(String path) throws NotFoundException, ServerException {
        final VirtualFileEntry entry = asVirtualFileEntry(path);
        if (entry == null) {
            return null;
        }

        if (!entry.isFile()) {
            throw new NotFoundException(format("Item '%s' isn't a file. ", path));
        }

        return (FileEntry)entry;
    }

    /**
     * Some importers don't use virtual file system API and changes are not indexed.
     * Force searcher to reindex project to fix such issues.
     *
     * @param project
     *
     * @throws ServerException
     */
    private void reindexProject(final RegisteredProject project) throws ServerException {
        final VirtualFile file = project.getBaseFolder().getVirtualFile();
        executor.execute(() -> {
            try {
                final Searcher searcher;
                try {
                    searcher = getSearcher();
                } catch (NotFoundException e) {
                    LOG.warn(e.getLocalizedMessage());
                    return;
                }
                searcher.add(file);
            } catch (Exception e) {
                LOG.warn(format("Project: %s", project.getPath()), e.getMessage());
            }
        });
    }
}
