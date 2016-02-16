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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectCreatedHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.handlers.ProjectTypeChangedHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.Variable;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherEventType;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationListener;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystem;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Facade for all project related operations
 * @author gazarenkov
 */
@Singleton
public final class ProjectManager {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectManager.class);

    private final LocalVirtualFileSystem vfs;

    private final EventService           eventService;
    private final ProjectTypeRegistry    projectTypeRegistry;
    private final ProjectHandlerRegistry handlers;
    private final WorkspaceHolder        workspaceHolder;

    private final Map<String, ProjectImpl> projects;

    private final ProjectImporterRegistry importers;

    private final FileTreeWatcher fileWatcher;
    private final DefaultFileWatcherNotificationHandler fileWatchNotifier;



    @Inject
    @SuppressWarnings("unchecked")
    public ProjectManager(LocalVirtualFileSystem vfs,
                          EventService eventService,
                          ProjectTypeRegistry projectTypeRegistry,
                          ProjectHandlerRegistry handlers,
                          ProjectImporterRegistry importers,
                          WorkspaceHolder workspaceHolder
                         )
            throws ServerException, NotFoundException, ProjectTypeConstraintException, InvalidValueException,
                   ValueStorageException, IOException, InterruptedException {

        this.vfs = vfs;
        this.eventService = eventService;
        this.projectTypeRegistry = projectTypeRegistry;
        this.handlers = handlers;
        this.importers = importers;

        this.workspaceHolder = workspaceHolder;

        this.projects = new HashMap<>();


        this.fileWatchNotifier = new DefaultFileWatcherNotificationHandler(vfs);
        this.fileWatcher = new FileTreeWatcher(vfs.getRoot().toIoFile(), new HashSet<>(), fileWatchNotifier);


        initWatcher();

        initProjects();

    }


    public FolderEntry getProjectsRoot() throws ServerException, NotFoundException {
        return new FolderEntry(vfs.getRoot());
    }

    public ProjectTypeRegistry getProjectTypeRegistry() {
        return this.projectTypeRegistry;
    }

    public ProjectHandlerRegistry getHandlers() {
        return handlers;
    }

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
    public List<ProjectImpl> getProjects() {

        return new ArrayList(projects.values());
    }

    /**
     * @param path
     *         where to find
     * @return child projects
     */
    public List<String> getProjects(String path) {

        Path root = Path.of(absolutizePath(path));
        List<String> children = new ArrayList<>();

        for (String key : projects.keySet()) {
            if (Path.of(key).isChild(root)) {
                children.add(key);
            }
        }

        return children;
    }

    /**
     * @param projectPath
     * @return project or null if not found
     */
    public ProjectImpl getProject(String projectPath) {

        return projects.get(absolutizePath(projectPath));

    }

    /**
     * @param path
     * @return the project owned this path.
     * @throws NotFoundException
     *         if not such a project found
     */
    public ProjectImpl getOwnerProject(String path) throws NotFoundException {

        // it is a project
        if (projects.containsKey(path))
            return projects.get(path);

        // otherwise try to find matched parent
        Path test;
        while ((test = Path.of(path).getParent()) != null) {
            ProjectImpl project = projects.get(test.toString());
            if (project != null)
                return project;

            path = test.toString();
        }

        // path is out of projects
        throw new NotFoundException("Owner project not found " + path);


    }

    /**
     * create project with optional adding it as a module to other
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
    public ProjectImpl createProject(ProjectConfig projectConfig,
                                     Map<String, String> options)
            throws ConflictException,
                   ForbiddenException,
                   ServerException,
                   NotFoundException {
        return createProject(projectConfig, options, null);

    }


    /**
     * create project with optional adding it as a module to other
     *
     * @param projectConfig
     *         - project configuration
     * @param options
     *         - options for generator
     * @param addAsModuleTo
     *         - path to the project to add this as a module or null if no parent
     * @return new project
     * @throws ConflictException
     * @throws ForbiddenException
     * @throws ServerException
     * @throws NotFoundException
     */
    public ProjectImpl createProject(ProjectConfig projectConfig,
                                     Map<String, String> options,
                                     String addAsModuleTo) throws ConflictException,
                                                                  ForbiddenException,
                                                                  ServerException,
                                                                  NotFoundException {

        if (projectConfig.getPath() == null)
            throw new ConflictException("Path for new project should be defined ");


        String path = absolutizePath(projectConfig.getPath());

        if (projectConfig.getType() == null)
            throw new ConflictException("Project Type is not defined " + path);

        FolderEntry projectFolder = new FolderEntry(vfs.getRoot().createFolder(path));

        ProjectImpl project = new ProjectImpl(projectFolder, projectConfig, true, this);

        CreateProjectHandler generator = handlers.getCreateProjectHandler(projectConfig.getType());

        if (generator != null) {
            Map<String, AttributeValue> valueMap = new HashMap<>();

            Map<String, List<String>> attributes = projectConfig.getAttributes();

            if (attributes != null) {
                for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
                    valueMap.put(entry.getKey(), new AttributeValue(entry.getValue()));
                }
            }

            if(options == null)
                options = new HashMap<>();

            generator.onCreateProject(projectFolder, valueMap, options);
        }

        // cache newly created project
        projects.put(path, project);

        ProjectCreatedHandler projectCreatedHandler = handlers.getProjectCreatedHandler(projectConfig.getType());

        if (projectCreatedHandler != null) {
            projectCreatedHandler.onProjectCreated(projectFolder);
        }

        if (addAsModuleTo != null) {
            ProjectImpl parent = getProject(addAsModuleTo);
            if (parent == null)
                throw new NotFoundException("Parent project not found at path: " + addAsModuleTo);
            parent.addModule(path);
        }

       // workspaceHolder.updateProjects(projects.values());

        return project;
    }

    /**
     *
     * @param newConfig - new config
     * @return updated config
     * @throws ForbiddenException
     * @throws ServerException
     * @throws NotFoundException
     * @throws ConflictException
     * @throws IOException
     */
    public ProjectImpl updateProject(ProjectConfig newConfig) throws ForbiddenException,
                                                                     ServerException,
                                                                     NotFoundException,
                                                                     ConflictException,
                                                                     IOException {

        // find project to update
        String apath;
        if(newConfig.getPath() != null) {
            apath = absolutizePath(newConfig.getPath());
        } else {
            throw new ConflictException("Project path is not defined");
        }

        ProjectImpl oldProject = getProject(apath);

        // If a project does not exist in the target path, create a new one
        if (oldProject == null)
            throw new NotFoundException(String.format("Project '%s' doesn't exist.", apath));

        // merge


        // store old types to
        String oldProjectType = oldProject.getProjectType().getId();
        List<String> oldMixins = new ArrayList<>(oldProject.getMixinTypes().keySet());

        // the new project
        ProjectImpl project = new ProjectImpl(oldProject.getBaseFolder(), newConfig, true, this);

        projects.put(apath, project);



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


    private void deleteProject(String path) {
        projects.remove(path);
        // remove ref from modules if any
        for (ProjectImpl p : projects.values()) {
            p.getModulePaths().remove(path);
        }
        // TODO fire
    }

    public ProjectImpl importProject(String path, SourceStorage sourceStorage)
            throws ServerException, IOException, ForbiddenException, UnauthorizedException, ConflictException, NotFoundException {

        final ProjectImporter importer = importers.getImporter(sourceStorage.getType());
        if (importer == null) {
            throw new ServerException(String.format("Unable import sources project from '%s'. Sources type '%s' is not supported.",
                                                    sourceStorage.getLocation(), sourceStorage.getType()));
        }
        // Preparing websocket output publisher to broadcast output of import process to the ide clients while importing
        final LineConsumerFactory outputOutputConsumerFactory = () -> new ProjectImportOutputWSLineConsumer(path,
                                                                                                            workspaceHolder.getWorkspace()
                                                                                                                           .getId(),
                                                                                                            300
        );

        // Not all importers uses virtual file system API. In this case virtual file system API doesn't get events and isn't able to set
        // correct creation time. Need do it manually.
        VirtualFileEntry vf = getProjectsRoot().getChild(path);

        if (vf != null && vf.isFile())
            throw new NotFoundException("Item on base path found and is not a folder" + path);
        else if(vf == null)
            vf = getProjectsRoot().createFolder(path);


        importer.importSources((FolderEntry)vf, sourceStorage, outputOutputConsumerFactory);

        String name = vf.getPath().getName();

        ProjectImpl project = new ProjectImpl((FolderEntry)vf, new ImportedProjectConf(name, path, sourceStorage), true, this);

        projects.put(vf.getPath().toString(), project);

        return project;

    }


    public Map<String, AttributeValue> estimateProject(String path, String projectTypeId)
            throws ServerException, ForbiddenException, NotFoundException, ValueStorageException, ProjectTypeConstraintException {
        ProjectType projectType = projectTypeRegistry.getProjectType(projectTypeId);
        if (projectType == null) {
            throw new NotFoundException("Project Type " + projectTypeId + " not found.");
        }

        final VirtualFileEntry baseFolder = getProjectsRoot().getChild(path.startsWith("/") ? path.substring(1) : path);
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
    public List<SourceEstimation> resolveSources(String path, boolean transientOnly)
            throws ServerException, ForbiddenException, NotFoundException, ProjectTypeConstraintException {
        final List<SourceEstimation> estimations = new ArrayList<>();
        boolean isPresentPrimaryType = false;
        for (ProjectType type : projectTypeRegistry.getProjectTypes(ProjectTypeRegistry.CHILD_TO_PARENT_COMPARATOR)) {
            if (transientOnly && type.isPersisted()) {
                continue;
            }

            final HashMap<String, List<String>> attributes = new HashMap<>();

            try {
                for (Map.Entry<String, AttributeValue> attr : estimateProject(path, type.getId()).entrySet()) {
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

    /**
     * deletes item including project
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



        String apath = absolutizePath(path);

        // delete item
        VirtualFile item = vfs.getRoot().getChild(Path.of(apath));
        if(item == null)
            return;

        item.delete();
        if(projects.get(apath) == null) {
            // fire event for file/folder
            return;
        }


        // process project
        for(String childPath : getProjects(apath)) {
            deleteProject(childPath);
        }
        deleteProject(apath);

    }

    public ProjectImpl addModule(String pathToParent, String pathToModule) throws NotFoundException, InvalidValueException {

        ProjectImpl parent = getProject(pathToParent);
        if(parent == null)
            throw new NotFoundException("Item not found "+pathToParent);

        ProjectImpl module = getProject(pathToModule);
        if(module == null)
            throw new NotFoundException("Project not found "+pathToModule);

        if(module.getPath().equals(parent.getPath()))
            throw new InvalidValueException("Can not add same project as a module to itself " + pathToParent);

        // check if the module does not belong to some other project
        for(ProjectImpl project : projects.values()) {
            if(project.getModulePaths().contains(module.getPath()))
                throw new InvalidValueException("Project "+module.getPath()+" already exists as a module in the project "+project.getPath());
        }

        parent.addModule(pathToModule);

        return parent;

    }


    public void deleteModule(String ownerPath, String modulePath) {
        ProjectImpl owner = projects.get(absolutizePath(ownerPath));
        if(owner != null)
            owner.getModulePaths().remove(modulePath);
    }


    public VirtualFileEntry copyTo(String itemPath, String newParentPath, String newName, boolean overwrite)
            throws ServerException, NotFoundException, ConflictException, ForbiddenException {

        VirtualFile oldItem = vfs.getRoot().getChild(Path.of(itemPath));
        if(oldItem == null)
            throw new NotFoundException("Item not found "+itemPath);

        VirtualFile newParent = vfs.getRoot().getChild(Path.of(newParentPath));
        if(oldItem == null)
            throw new NotFoundException("New parent not found "+newParentPath);

        VirtualFile newItem = oldItem.copyTo(newParent, newName, overwrite);
        ProjectImpl owner = getOwnerProject(newItem.getPath().toString());

        VirtualFileEntry copy;
        if(newItem.isFile())
            copy = new FileEntry(newItem, owner.getPath());
        else
            copy = new FolderEntry(newItem, owner.getPath());

        if(copy.isProject()) {
            projects.get(copy.getProject()).getTypes();
            // fire event
        }

        return copy;


    }


    public VirtualFileEntry moveTo(String itemPath, String newParentPath, String newName, boolean overwrite)
            throws ServerException, NotFoundException, ConflictException, ForbiddenException {

        VirtualFile oldItem = vfs.getRoot().getChild(Path.of(itemPath));
        if(oldItem == null)
            throw new NotFoundException("Item not found "+itemPath);

        VirtualFile newParent;
        if(newParentPath == null)
            // rename only
            newParent = oldItem.getParent();
        else
            newParent = vfs.getRoot().getChild(Path.of(newParentPath));

        if(newParent == null)
            throw new NotFoundException("New parent not found "+newParentPath);

        // TODO lock token ?
        VirtualFile newItem = oldItem.moveTo(newParent, newName, overwrite, null);

        ProjectImpl owner = getOwnerProject(newItem.getPath().toString());

        VirtualFileEntry move;
        if(newItem.isFile())
            move = new FileEntry(newItem, owner.getPath());
        else
            move = new FolderEntry(newItem, owner.getPath());

        if(move.isProject()) {
            projects.get(move.getProject()).getTypes();
            // fire event
        }

//        if (move.isFolder()) {
//            final ProjectImpl project = projectManager.getProject(move.getPath().toString());
//            if (project != null) {
//                final String name = project.getName();
//                final String projectType = project.getProjectType().getId();
////                LOG.info("EVENT#project-destroyed# PROJECT#{}# TYPE#{}# WS#{}# USER#{}#", name, projectType,
////                         EnvironmentContext.getCurrent().getWorkspaceName(), EnvironmentContext.getCurrent().getUser().getName());
//
////                logProjectCreatedEvent(name, projectType);
//            }
//        }


        return move;
    }



    /* ===================================== */
    /*  Private methods                      */
    /* ===================================== */


    protected void initProjects()
            throws ServerException, NotFoundException, ProjectTypeConstraintException, InvalidValueException,
                   ValueStorageException {

        // take from vfs and config and merge
        // the difference between two lists (from config and from VFS)
        // is that config's one contains not only projects placed on root
        // but also  sub-projects
        UsersWorkspace workspace = workspaceHolder.getWorkspace();
        List<? extends ProjectConfig> projectConfigs = workspace.getProjects();
        if(projectConfigs == null)
            projectConfigs = new ArrayList<>();

        for (ProjectConfig projectConfig : projectConfigs) {

            initProject(projectConfig, false);
            initSubProjectsRecursively(projectConfig);

        }

        // only projects expected
        for (VirtualFile projectRoot : vfs.getRoot().getChildren()) {

            if (projectRoot.isFile()) {
                LOG.error("Plain file (not a folder) is not expected at the root of VFS: %s", projectRoot.getPath());
                continue; //  strange
            }

            if (!projects.containsKey(projectRoot.getPath().toString()))
                projects.put(projectRoot.getPath().toString(), new ProjectImpl(new FolderEntry(projectRoot), null, false, this));

        }

    }

    private void initSubProjectsRecursively(ProjectConfig parent)
            throws ServerException, ProjectTypeConstraintException, InvalidValueException, NotFoundException,
                   ValueStorageException {
        for (ProjectConfig pc : parent.getModules()) {

            projects.put(absolutizePath(pc.getPath()), new ProjectImpl(folder(absolutizePath(pc.getPath())), pc, false, this));

            //initProject(pc, false);

            initSubProjectsRecursively(pc);
        }

    }


    private void initProject(ProjectConfig config, boolean updated)
            throws ServerException, ProjectTypeConstraintException, InvalidValueException,
                   NotFoundException, ValueStorageException {

        String path = absolutizePath(config.getPath());
        projects.put(path, new ProjectImpl(folder(path), config, updated, this));
    }

    private void initWatcher() throws IOException {
        FileWatcherNotificationListener defaultListener = new FileWatcherNotificationListener(VirtualFileFilter.ACCEPT_ALL) {
            @Override
            public void onFileWatcherEvent(VirtualFile virtualFile, FileWatcherEventType eventType) {
                LOG.debug("FS event detected: " + eventType + " " + virtualFile.getPath().toString() + " " + virtualFile.isFile());
            }
        };
        fileWatchNotifier.addNotificationListener(defaultListener);
        fileWatcher.startup();
    }

    private FolderEntry folder(String path) throws ServerException {

        VirtualFile vf = vfs.getRoot().getChild(Path.of(path));
        return (vf == null) ? null : new FolderEntry(vf);
    }

    String absolutizePath(String path) {

        return (path.startsWith("/")) ? path : "/".concat(path);
    }


    // =================================
    // Inner classes
    // =================================

    private static class ImportedProjectConf implements ProjectConfig {

        private final String        name;
        private final String        path;
        private final SourceStorage source;

        private ImportedProjectConf(String name, String path, SourceStorage source) {
            this.name = name;
            this.path = path;
            this.source = source;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public String getType() {
            return BaseProjectType.ID;
        }

        @Override
        public List<String> getMixins() {
            return new ArrayList<>();
        }

        @Override
        public Map<String, List<String>> getAttributes() {
            return new HashMap<>();
        }

        @Override
        public List<? extends ProjectConfig> getModules() {
            return new ArrayList<>();
        }

        @Override
        public SourceStorage getSource() {
            return source;
        }


    }


}
