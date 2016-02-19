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
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImportOutputWSLineConsumer;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherEventType;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationListener;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Facade for all project related operations
 *
 * @author gazarenkov
 */
@Singleton
public final class ProjectManager {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectManager.class);

    private final VirtualFileSystem vfs;

    private final EventService           eventService;
    private final ProjectTypeRegistry    projectTypeRegistry;
    private final ProjectHandlerRegistry handlers;

    private final ProjectRegistry projectRegistry;

    private final ProjectImporterRegistry importers;

    private final FileTreeWatcher                fileWatcher;
    private final FileWatcherNotificationHandler fileWatchNotifier;

    private final ExecutorService executor = Executors.newFixedThreadPool(1 + Runtime.getRuntime().availableProcessors(),
                                                                          new ThreadFactoryBuilder()
                                                                                  .setNameFormat("ProjectService-IndexingThread-")
                                                                                  .setDaemon(true).build()
                                                                         );

    @Inject
    @SuppressWarnings("unchecked")
    public ProjectManager(VirtualFileSystemProvider vfsProvider,
                          EventService eventService,
                          ProjectTypeRegistry projectTypeRegistry,
                          ProjectHandlerRegistry handlers,
                          ProjectImporterRegistry importers,
                          ProjectRegistry projectRegistry,
                          FileWatcherNotificationHandler fileWatcherNotificationHandler,
                          FileTreeWatcher fileTreeWatcher
                          //WorkspaceHolder workspaceHolder
                         )
            throws ServerException, NotFoundException, ProjectTypeConstraintException,
                   ValueStorageException, IOException, InterruptedException {

        this.vfs = vfsProvider.getVirtualFileSystem();
        this.eventService = eventService;
        this.projectTypeRegistry = projectTypeRegistry;
        this.handlers = handlers;
        this.importers = importers;
        this.projectRegistry = projectRegistry;

        this.fileWatchNotifier = fileWatcherNotificationHandler;
        this.fileWatcher = fileTreeWatcher;

        initWatcher();
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }


    public FolderEntry getProjectsRoot() throws ServerException, NotFoundException {
        return new FolderEntry(vfs.getRoot());
    }

    public ProjectTypeRegistry getProjectTypeRegistry() {
        return this.projectTypeRegistry;
    }

//    public ProjectHandlerRegistry getHandlers() {
//        return handlers;
//    }

    public Searcher getSearcher() throws NotFoundException, ServerException {

        SearcherProvider provider = vfs.getSearcherProvider();
        if (provider == null)
            throw new NotFoundException("SearcherProvider is not defined in VFS");

        return provider.getSearcher(vfs);
    }

    public void addWatchListener(FileWatcherNotificationListener listener) {
        this.fileWatchNotifier.addNotificationListener(listener);
    }

    public void removeWatchListener(FileWatcherNotificationListener listener) {
        this.fileWatchNotifier.removeNotificationListener(listener);
    }

    public void addWatchExcludeMatcher(PathMatcher matcher) {
        this.fileWatcher.addExcludeMatcher(matcher);
    }

    public void removeWatchExcludeMatcher(PathMatcher matcher) {
        this.fileWatcher.removeExcludeMatcher(matcher);
    }


    /**
     * @return all the projects
     */
    public List<RegisteredProject> getProjects() {

        return projectRegistry.getProjects();
    }


    /**
     * @param projectPath
     * @return project or null if not found
     */
    public RegisteredProject getProject(String projectPath) {

        return projectRegistry.getProject(projectPath);

    }


    /**
     * create project:
     * - take project config
     *
     * @param projectConfig
     *         - project configuration
     * @param options
     *         - options for generator
     * @return new project
     * @throws ConflictException
     * @throws ForbiddenException
     * @throws ServerException
     * @throws NotFoundException
     */
    public RegisteredProject createProject(ProjectConfig projectConfig,
                                           Map<String, String> options) throws ConflictException,
                                                                               ForbiddenException,
                                                                               ServerException,
                                                                               NotFoundException,
                                                                               ProjectTypeConstraintException {

        // path and primary type is mandatory
        if (projectConfig.getPath() == null)
            throw new ConflictException("Path for new project should be defined ");

        String path = ProjectRegistry.absolutizePath(projectConfig.getPath());

        if (projectConfig.getType() == null)
            throw new ConflictException("Project Type is not defined " + path);

        if (getProject(path) != null)
            throw new ConflictException("Project config already exists " + path);


        FolderEntry projectFolder = new FolderEntry(vfs.getRoot().createFolder(path));
        CreateProjectHandler generator = handlers.getCreateProjectHandler(projectConfig.getType());

        if (generator != null) {
            Map<String, AttributeValue> valueMap = new HashMap<>();

            Map<String, List<String>> attributes = projectConfig.getAttributes();

            if (attributes != null) {
                for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
                    valueMap.put(entry.getKey(), new AttributeValue(entry.getValue()));
                }
            }

            if (options == null)
                options = new HashMap<>();

            generator.onCreateProject(projectFolder, valueMap, options);
        }


        try {
            return projectRegistry.putProject(projectConfig, projectFolder, true);
        } catch (Exception e) {
            // rollback project folder
            projectFolder.getVirtualFile().delete();
            throw e;
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
     *         - new config
     * @return updated config
     * @throws ForbiddenException
     * @throws ServerException
     * @throws NotFoundException
     * @throws ConflictException
     * @throws IOException
     */
    public RegisteredProject updateProject(ProjectConfig newConfig) throws ForbiddenException,
                                                                           ServerException,
                                                                           NotFoundException,
                                                                           ConflictException,
                                                                           IOException {

        String apath = newConfig.getPath();

        if (newConfig.getPath() == null)
            throw new ConflictException("Project path is not defined");

        RegisteredProject oldProject = projectRegistry.getProject(apath);

        // If a project does not exist in the target path, create a new one
        if (oldProject == null)
            throw new NotFoundException(String.format("Project '%s' doesn't exist.", apath));

        RegisteredProject project = projectRegistry.putProject(newConfig, oldProject.getBaseFolder(), true);

        // TODO move to register?
        reindexProject(project);

        return project;
    }

    public RegisteredProject importProject(String path, SourceStorage sourceStorage)
            throws ServerException, IOException, ForbiddenException, UnauthorizedException, ConflictException, NotFoundException {

        final ProjectImporter importer = importers.getImporter(sourceStorage.getType());
        if (importer == null) {
            throw new NotFoundException(String.format("Unable import sources project from '%s'. Sources type '%s' is not supported.",
                                                    sourceStorage.getLocation(), sourceStorage.getType()));
        }
        // Preparing websocket output publisher to broadcast output of import process to the ide clients while importing
        final LineConsumerFactory outputOutputConsumerFactory = () -> new ProjectImportOutputWSLineConsumer(path,
                                                                                                            projectRegistry
                                                                                                                    .getWorkspaceId(),
                                                                                                            300);


        // Not all importers uses virtual file system API. In this case virtual file system API doesn't get events and isn't able to set
        // correct creation time. Need do it manually.
        //VirtualFileEntry vf = getProjectsRoot().getChild(path);

        FolderEntry folder = asFolder(path);

        if (folder == null)
            folder = getProjectsRoot().createFolder(path);


        importer.importSources(folder, sourceStorage, outputOutputConsumerFactory);

        String name = folder.getPath().getName();

        return projectRegistry.putProject(new NewProjectConfig(path, name, BaseProjectType.ID, sourceStorage), folder, true);

    }


    public ProjectTypeResolution estimateProject(String path, String projectTypeId)
            throws ServerException, NotFoundException, ValueStorageException {



        ProjectTypeDef projectType = projectTypeRegistry.getProjectType(projectTypeId);
        if (projectType == null) {
            throw new NotFoundException("Project Type " + projectTypeId + " not found.");
        }

       FolderEntry baseFolder = asFolder(path);

        if(baseFolder == null)
            throw new NotFoundException("Folder not found: "+path);

        return projectType.resolveSources(baseFolder);

    }

    // ProjectSuggestion
    public List<ProjectTypeResolution> resolveSources(String path, boolean transientOnly)
            throws ServerException, NotFoundException {

        final List<ProjectTypeResolution> resolutions = new ArrayList<>();
//        boolean isPresentPrimaryType = false;

        for (ProjectType type : projectTypeRegistry.getProjectTypes(ProjectTypeRegistry.CHILD_TO_PARENT_COMPARATOR)) {
            if (transientOnly && type.isPersisted()) {
                continue;
            }


            try {
                ProjectTypeResolution resolution = estimateProject(path, type.getId());
                if(resolution.matched()) {
                    resolutions.add(resolution);
                }
            } catch (ValueStorageException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }


        }

//        if (!isPresentPrimaryType) {
//            estimations.add(newDto(SourceEstimation.class).withType(BaseProjectType.ID));
//        }

        return resolutions;
    }

    /**
     * deletes item including project
     *
     * @param path
     * @throws ServerException
     * @throws ForbiddenException
     * @throws NotFoundException
     * @throws ConflictException
     */
    public void delete(String path) throws ServerException,
                                           ForbiddenException,
                                           NotFoundException,
                                           ConflictException {


        String apath = ProjectRegistry.absolutizePath(path);

        // delete item
        VirtualFile item = vfs.getRoot().getChild(Path.of(apath));
        if (item == null)
            return;

        item.delete();

        // delete child projects
        projectRegistry.removeProjects(apath);

    }


    public VirtualFileEntry copyTo(String itemPath, String newParentPath, String newName, boolean overwrite)
            throws ServerException, NotFoundException, ConflictException, ForbiddenException {

        VirtualFile oldItem = vfs.getRoot().getChild(Path.of(itemPath));
        if (oldItem == null)
            throw new NotFoundException("Item not found " + itemPath);

        VirtualFile newParent = vfs.getRoot().getChild(Path.of(newParentPath));
        if (oldItem == null)
            throw new NotFoundException("New parent not found " + newParentPath);

        VirtualFile newItem = oldItem.copyTo(newParent, newName, overwrite);
        RegisteredProject owner = projectRegistry.getParentProject(newItem.getPath().toString());

        VirtualFileEntry copy;
        if (newItem.isFile())
            copy = new FileEntry(newItem, owner.getPath());
        else
            copy = new FolderEntry(newItem, owner.getPath());

        if (copy.isProject()) {
            projectRegistry.getProject(copy.getProject()).getTypes();
            // fire event
        }

        return copy;


    }


    public VirtualFileEntry moveTo(String itemPath, String newParentPath, String newName, boolean overwrite)
            throws ServerException, NotFoundException, ConflictException, ForbiddenException {

        VirtualFile oldItem = vfs.getRoot().getChild(Path.of(itemPath));
        if (oldItem == null)
            throw new NotFoundException("Item not found " + itemPath);

        VirtualFile newParent;
        if (newParentPath == null)
            // rename only
            newParent = oldItem.getParent();
        else
            newParent = vfs.getRoot().getChild(Path.of(newParentPath));

        if (newParent == null)
            throw new NotFoundException("New parent not found " + newParentPath);

        // TODO lock token ?
        VirtualFile newItem = oldItem.moveTo(newParent, newName, overwrite, null);

        RegisteredProject owner = projectRegistry.getParentProject(newItem.getPath().toString());

        VirtualFileEntry move;
        if (newItem.isFile())
            move = new FileEntry(newItem, owner.getPath());
        else
            move = new FolderEntry(newItem, owner.getPath());

        if (move.isProject()) {
            projectRegistry.getProject(move.getProject()).getTypes();
            // fire event
        }

        return move;
    }


    // TODO do we need ForbiddenException
    FolderEntry asFolder(String path) throws NotFoundException, ServerException {
        final VirtualFileEntry entry = asVirtualFileEntry(path);
        if (entry == null)
            return null;
        if (!entry.isFolder()) {
            throw new NotFoundException(String.format("Item '%s' isn't a folder. ", path));
        }
        return (FolderEntry)entry;
    }

    // TODO do we need ForbiddenException
    VirtualFileEntry asVirtualFileEntry(String path)
            throws NotFoundException, ServerException {
        String apath = ProjectRegistry.absolutizePath(path);
        final FolderEntry root = getProjectsRoot();
        final VirtualFileEntry entry = root.getChild(apath);
//        if (entry == null) {
//            throw new NotFoundException(String.format("Path '%s' doesn't exist.", apath));
//        }
        return entry;
    }

    FileEntry asFile(String path) throws NotFoundException, ServerException {
        final VirtualFileEntry entry = asVirtualFileEntry(path);
        if (entry == null)
            return null;
        if (!entry.isFile()) {
            throw new NotFoundException(String.format("Item '%s' isn't a file. ", path));
        }
        return (FileEntry)entry;
    }



    /* ===================================== */
    /*  Private methods                      */
    /* ===================================== */


    /**
     * Some importers don't use virtual file system API and changes are not indexed.
     * Force searcher to reindex project to fix such issues.
     *
     * @param project
     * @throws ServerException
     */
    private void reindexProject(final RegisteredProject project) throws ServerException {
        final VirtualFile file = project.getBaseFolder().getVirtualFile();
        executor.execute(() -> {
            try {

                Searcher searcher;
                try {
                    searcher = getSearcher();
                } catch (NotFoundException e) {
                    LOG.warn(e.getLocalizedMessage());
                    return;
                }
                searcher.add(file);
                //SearcherProvider sp = this.projectManager.getVfs().getSearcherProvider();
                //if(sp != null)
                //    sp.getSearcher(projectManager.getVfs(), true).add(file);
                //searcherProvider.getSearcher(projectManager.getVfs(), true).add(file);
            } catch (Exception e) {
                LOG.warn(String.format("Project: %s", project.getPath()), e.getMessage());
            }
        });
    }


    private void initWatcher() throws IOException {
        FileWatcherNotificationListener defaultListener = new FileWatcherNotificationListener(VirtualFileFilter.ACCEPT_ALL) {
            @Override
            public void onFileWatcherEvent(VirtualFile virtualFile, FileWatcherEventType eventType) {
                LOG.debug("FS event detected: " + eventType + " " + virtualFile.getPath().toString() + " " + virtualFile.isFile());
                eventService.publish(DtoFactory.newDto(VfsWatchEvent.class)
                                               .withPath(virtualFile.getPath().toString())
                                               .withFile(virtualFile.isFile())
                                               .withType(eventType));
            }
        };
        fileWatchNotifier.addNotificationListener(defaultListener);
        fileWatcher.startup();
    }


}
