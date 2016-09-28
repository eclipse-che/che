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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImportOutputWSLineConsumer;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationListener;
import org.eclipse.che.api.vfs.impl.file.event.LoEvent;
import org.eclipse.che.api.vfs.impl.file.event.detectors.ProjectTreeChangesDetector;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
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

/**
 * Facade for all project related operations.
 *
 * @author gazarenkov
 */
@Singleton
public final class ProjectManager {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectManager.class);

    private final VirtualFileSystem              vfs;
    private final EventService                   eventService;
    private final ProjectTypeRegistry            projectTypeRegistry;
    private final ProjectRegistry                projectRegistry;
    private final ProjectHandlerRegistry         handlers;
    private final ProjectImporterRegistry        importers;
    private final FileTreeWatcher                fileWatcher;
    private final FileWatcherNotificationHandler fileWatchNotifier;
    private final ExecutorService                executor;
    private final WorkspaceProjectsSyncer        workspaceProjectsHolder;
    private final ProjectTreeChangesDetector     projectTreeChangesDetector;

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
                          ProjectTreeChangesDetector projectTreeChangesDetector) throws ServerException {
        this.vfs = vfsProvider.getVirtualFileSystem();
        this.eventService = eventService;
        this.projectTypeRegistry = projectTypeRegistry;
        this.projectRegistry = projectRegistry;
        this.handlers = handlers;
        this.importers = importers;
        this.fileWatchNotifier = fileWatcherNotificationHandler;
        this.fileWatcher = fileTreeWatcher;
        this.workspaceProjectsHolder = workspaceProjectsHolder;
        this.projectTreeChangesDetector = projectTreeChangesDetector;

        executor = Executors.newFixedThreadPool(1 + Runtime.getRuntime().availableProcessors(),
                                                new ThreadFactoryBuilder().setNameFormat("ProjectService-IndexingThread-")
                                                                          .setDaemon(true).build());
    }

    @PostConstruct
    void initWatcher() throws IOException {
        FileWatcherNotificationListener defaultListener =
                new FileWatcherNotificationListener(file -> !(file.getPath().toString().contains(".codenvy")
                                                              || file.getPath().toString().contains(".#"))) {
                    @Override
                    public void onFileWatcherEvent(VirtualFile virtualFile, FileWatcherEventType eventType) {
                        LOG.debug("FS event detected: " + eventType + " " + virtualFile.getPath().toString() + " " + virtualFile.isFile());
                        eventService.publish(LoEvent.newInstance()
                                                    .withPath(virtualFile.getPath().toString())
                                                    .withName(virtualFile.getName())
                                                    .withItemType(virtualFile.isFile()
                                                                  ? LoEvent.ItemType.FILE
                                                                  : LoEvent.ItemType.DIR)
                                                    .withTime(System.currentTimeMillis())
                                                    .withEventType(eventType));
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
            throw new NotFoundException(String.format("Project '%s' doesn't exist.", projectPath));
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
        projectTreeChangesDetector.suspend();
        try {
            // path and primary type is mandatory
            if (projectConfig.getPath() == null) {
                throw new ConflictException("Path for new project should be defined ");
            }

            final String path = ProjectRegistry.absolutizePath(projectConfig.getPath());

            if (projectConfig.getType() == null) {
                throw new ConflictException("Project Type is not defined " + path);
            }

            if (projectRegistry.getProject(path) != null) {
                throw new ConflictException("Project config already exists " + path);
            }

            final FolderEntry projectFolder = new FolderEntry(vfs.getRoot().createFolder(path), projectRegistry);
            final CreateProjectHandler generator = handlers.getCreateProjectHandler(projectConfig.getType());

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
                generator.onCreateProject(projectFolder, valueMap, options);
            }

            final RegisteredProject project;
            try {
                project = projectRegistry.putProject(projectConfig, projectFolder, true, false);
            } catch (Exception e) {
                // rollback project folder
                projectFolder.getVirtualFile().delete();
                throw e;
            }

            workspaceProjectsHolder.sync(projectRegistry);

            projectRegistry.fireInitHandlers(project);

            return project;
        } finally {
            projectTreeChangesDetector.resume();
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
        String path = newConfig.getPath();

        if (path == null) {
            throw new ConflictException("Project path is not defined");
        }

        final FolderEntry baseFolder = asFolder(path);

        // If a project does not exist in the target path, create a new one
        if (baseFolder == null) {
            throw new NotFoundException(String.format("Folder '%s' doesn't exist.", path));
        }

        final RegisteredProject project = projectRegistry.putProject(newConfig, baseFolder, true, false);

        workspaceProjectsHolder.sync(projectRegistry);

        projectRegistry.fireInitHandlers(project);

        // TODO move to register?
        reindexProject(project);

        return project;
    }

    public RegisteredProject importProject(String path, SourceStorage sourceStorage, boolean rewrite) throws ServerException,
                                                                                                             IOException,
                                                                                                             ForbiddenException,
                                                                                                             UnauthorizedException,
                                                                                                             ConflictException,
                                                                                                             NotFoundException {
        projectTreeChangesDetector.suspend();
        try {
            final ProjectImporter importer = importers.getImporter(sourceStorage.getType());
            if (importer == null) {
                throw new NotFoundException(String.format("Unable import sources project from '%s'. Sources type '%s' is not supported.",
                                                          sourceStorage.getLocation(), sourceStorage.getType()));
            }

            // Preparing websocket output publisher to broadcast output of import process to the ide clients while importing
            final LineConsumerFactory outputOutputConsumerFactory =
                    () -> new ProjectImportOutputWSLineConsumer(path, workspaceProjectsHolder.getWorkspaceId(), 300);

            String normalizePath = (path.startsWith("/")) ? path : "/".concat(path);
            FolderEntry folder = asFolder(normalizePath);
            if (folder != null && !rewrite) {
                throw new ConflictException(String.format("Project %s already exists ", path));
            }

            if (folder == null) {
                folder = getProjectsRoot().createFolder(normalizePath);
            }

            try {
                importer.importSources(folder, sourceStorage, outputOutputConsumerFactory);
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
                    .putProject(new NewProjectConfig(normalizePath, name, BaseProjectType.ID, sourceStorage), folder, true, false);
            workspaceProjectsHolder.sync(projectRegistry);
            return rp;
        } finally {
            projectTreeChangesDetector.resume();
        }
    }

    public ProjectTypeResolution estimateProject(String path, String projectTypeId) throws ServerException,
                                                                                           NotFoundException,
                                                                                           ValueStorageException {
        final ProjectTypeDef projectType = projectTypeRegistry.getProjectType(projectTypeId);
        if (projectType == null) {
            throw new NotFoundException("Project Type " + projectTypeId + " not found.");
        }

        final FolderEntry baseFolder = asFolder(path);

        if (baseFolder == null) {
            throw new NotFoundException("Folder not found: " + path);
        }

        return projectType.resolveSources(baseFolder);
    }

    // ProjectSuggestion
    public List<ProjectTypeResolution> resolveSources(String path, boolean transientOnly) throws ServerException, NotFoundException {
        final List<ProjectTypeResolution> resolutions = new ArrayList<>();

        for (ProjectType type : projectTypeRegistry.getProjectTypes(ProjectTypeRegistry.CHILD_TO_PARENT_COMPARATOR)) {
            if (transientOnly && type.isPersisted()) {
                continue;
            }

            try {
                final ProjectTypeResolution resolution = estimateProject(path, type.getId());
                if (resolution.matched()) {
                    resolutions.add(resolution);
                }
            } catch (ValueStorageException e) {
                LOG.warn(e.getLocalizedMessage(), e);
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
            NewProjectConfig projectConfig = new NewProjectConfig(newItem.getPath().toString(),
                                                                  project.getType(),
                                                                  project.getMixins(),
                                                                  newName,
                                                                  project.getDescription(),
                                                                  project.getAttributes(),
                                                                  project.getSource());

            if (move instanceof FolderEntry) {
                projectRegistry.removeProjects(project.getPath());
                updateProject(projectConfig);
            }
        }

        return move;
    }

    FolderEntry asFolder(String path) throws NotFoundException, ServerException {
        final VirtualFileEntry entry = asVirtualFileEntry(path);
        if (entry == null) {
            return null;
        }

        if (!entry.isFolder()) {
            throw new NotFoundException(String.format("Item '%s' isn't a folder. ", path));
        }

        return (FolderEntry)entry;
    }

    VirtualFileEntry asVirtualFileEntry(String path) throws NotFoundException, ServerException {
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
            throw new NotFoundException(String.format("Item '%s' isn't a file. ", path));
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
                LOG.warn(String.format("Project: %s", project.getPath()), e.getMessage());
            }
        });
    }
}
