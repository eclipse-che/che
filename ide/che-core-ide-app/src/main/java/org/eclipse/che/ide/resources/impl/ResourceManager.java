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
package org.eclipse.che.ide.resources.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.ng.DeletedFilesController;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.project.QueryExpression;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Project.ProblemProjectMarker;
import org.eclipse.che.ide.api.resources.Project.ProjectRequest;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.api.resources.marker.MarkerChangedEvent;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.COPIED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.DERIVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.SYNCHRONIZED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;
import static org.eclipse.che.ide.util.Arrays.add;
import static org.eclipse.che.ide.util.Arrays.removeAll;
import static org.eclipse.che.ide.util.NameUtils.checkFileName;
import static org.eclipse.che.ide.util.NameUtils.checkFolderName;
import static org.eclipse.che.ide.util.NameUtils.checkProjectName;

/**
 * Acts as the service lay between the user interactions with resources and data transfer layer.
 * Main necessity of this manager is to encapsulate business logic which serves resources from
 * the interfaces.
 * <p/>
 * This manager is not intended to be operated with third-party components. Only resources can
 * operate with it. To operate with resources use {@link AppContext#getWorkspaceRoot()} and
 * {@link AppContext#getProjects()}.
 *
 * @author Vlad Zhukovskiy
 * @see AppContext
 * @see AppContextImpl
 * @since 4.4.0
 */
@Beta
public final class ResourceManager {
    /**
     * Describes zero depth level for the descendants.
     */
    private static final int DEPTH_ZERO = 0;

    /**
     * Describes first depth level for the descendants.
     */
    private static final int DEPTH_ONE = 1;

    /**
     * Relative link for the content url.
     *
     * @see #newResourceFrom(ItemReference)
     */
    private static final String GET_CONTENT_REL = "get content";

    /**
     * Empty projects container.
     *
     * @see #getWorkspaceProjects()
     */
    private static final Project[] NO_PROJECTS = new Project[0];

    private static final Resource[] NO_RESOURCES = new Resource[0];

    private final ProjectServiceClient   ps;
    private final EventBus               eventBus;
    private final EditorAgent            editorAgent;
    private final DeletedFilesController deletedFilesController;
    private final ResourceFactory        resourceFactory;
    private final PromiseProvider        promises;
    private final DtoFactory             dtoFactory;
    private final ProjectTypeRegistry    typeRegistry;
    private       DevMachine             devMachine;

    /**
     * Link to the workspace content root. Immutable among the workspace life.
     */
    private final Container          workspaceRoot;
    private final WsAgentURLModifier urlModifier;

    /**
     * Internal store, which caches requested resources from the server.
     */
    private ResourceStore store;

    /**
     * Cached dto project configuration.
     */
    private ProjectConfigDto[] cachedConfigs;

    @Inject
    public ResourceManager(@Assisted DevMachine devMachine,
                           ProjectServiceClient ps,
                           EventBus eventBus,
                           EditorAgent editorAgent,
                           DeletedFilesController deletedFilesController,
                           ResourceFactory resourceFactory,
                           PromiseProvider promises,
                           DtoFactory dtoFactory,
                           ProjectTypeRegistry typeRegistry,
                           ResourceStore store,
                           WsAgentURLModifier urlModifier) {
        this.devMachine = devMachine;
        this.ps = ps;
        this.eventBus = eventBus;
        this.editorAgent = editorAgent;
        this.deletedFilesController = deletedFilesController;
        this.resourceFactory = resourceFactory;
        this.promises = promises;
        this.dtoFactory = dtoFactory;
        this.typeRegistry = typeRegistry;
        this.store = store;
        this.urlModifier = urlModifier;

        this.workspaceRoot = resourceFactory.newFolderImpl(Path.ROOT, this);
    }

    /**
     * Returns the workspace registered projects.
     *
     * @return the {@link Promise} with registered projects
     * @see Project
     * @since 4.4.0
     */
    public Promise<Project[]> getWorkspaceProjects() {
        return ps.getProjects().then(new Function<List<ProjectConfigDto>, Project[]>() {
            @Override
            public Project[] apply(List<ProjectConfigDto> dtoConfigs) throws FunctionException {
                store.clear();

                if (dtoConfigs.isEmpty()) {
                    cachedConfigs = new ProjectConfigDto[0];
                    return NO_PROJECTS;
                }

                cachedConfigs = dtoConfigs.toArray(new ProjectConfigDto[dtoConfigs.size()]);

                Project[] projects = NO_PROJECTS;

                for (ProjectConfigDto config : dtoConfigs) {
                    if (Path.valueOf(config.getPath()).segmentCount() == 1) {
                        final Project project = resourceFactory.newProjectImpl(config, ResourceManager.this);
                        store.register(project);

                        final Optional<ProblemProjectMarker> optionalMarker = getProblemMarker(config);

                        if (optionalMarker.isPresent()) {
                            project.addMarker(optionalMarker.get());
                        }

                        Project[] tmpProjects = copyOf(projects, projects.length + 1);
                        tmpProjects[projects.length] = project;
                        projects = tmpProjects;
                    }
                }

                /* We need to guarantee that list of projects would be sorted by the logic provided in compareTo method implementation. */
                java.util.Arrays.sort(projects);

                for (Project project : projects) {
                    eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(project, ADDED | DERIVED)));
                }

                return projects;
            }
        });
    }

    /**
     * Returns the workspace root container. This container is a holder which may contains only {@link Project}s.
     *
     * @return the workspace container
     * @see Container
     * @since 4.4.0
     */
    public Container getWorkspaceRoot() {
        return workspaceRoot;
    }

    /**
     * Update state of specific properties in project and save this state on the server.
     * As the result method should return the {@link Promise} with new {@link Project} object.
     * <p/>
     * During the update method have to iterate on children of updated resource and if any of
     * them has changed own type, e.g. folder -> project, project -> folder, specific event
     * has to be fired.
     * <p/>
     * Method is not intended to be called in third party components. It is the service method
     * for {@link Project}.
     *
     * @param path
     *         the path to project which should be updated
     * @param request
     *         the update request
     * @return the {@link Promise} with new {@link Project} object.
     * @see ResourceChangedEvent
     * @see ProjectRequest
     * @see Project#update()
     * @since 4.4.0
     */
    protected Promise<Project> update(final Path path, final ProjectRequest request) {

        final SourceStorageDto sourceDto = dtoFactory.createDto(SourceStorageDto.class);

        if (request.getBody().getSource() != null) {
            sourceDto.setLocation(request.getBody().getSource().getLocation());
            sourceDto.setType(request.getBody().getSource().getType());
            sourceDto.setParameters(request.getBody().getSource().getParameters());
        }

        final ProjectConfigDto dto = dtoFactory.createDto(ProjectConfigDto.class)
                                               .withName(request.getBody().getName())
                                               .withPath(path.toString())
                                               .withDescription(request.getBody().getDescription())
                                               .withType(request.getBody().getType())
                                               .withMixins(request.getBody().getMixins())
                                               .withAttributes(request.getBody().getAttributes())
                                               .withSource(sourceDto);

        return ps.updateProject(dto).thenPromise(new Function<ProjectConfigDto, Promise<Project>>() {
            @Override
            public Promise<Project> apply(ProjectConfigDto reference) throws FunctionException {

                /* Note: After update, project may become to be other type,
                   e.g. blank -> java or maven, or ant, or etc. And this may
                   cause sub-project creations. Simultaneously on the client
                   side there is outdated information about sub-projects, so
                   we need to get updated project list. */

                //dispose outdated resource
                final Optional<Resource> outdatedResource = store.getResource(path);

                checkState(outdatedResource.isPresent(), "Outdated resource wasn't found");

                final Resource resource = outdatedResource.get();

                checkState(resource instanceof Container, "Outdated resource is not a container");

                Container container = (Container)resource;

                if (resource instanceof Folder) {
                    Optional<Container> parent = resource.getParent();
                    checkState(parent.isPresent(), "Parent of the resource wasn't found");
                    container = parent.get();
                }

                return synchronize(container).then(new Function<Resource[], Project>() {
                    @Override
                    public Project apply(Resource[] synced) throws FunctionException {
                        final Optional<Resource> updatedProject = store.getResource(path);

                        checkState(updatedProject.isPresent(), "Updated resource is not present");
                        checkState(updatedProject.get().isProject(), "Updated resource is not a project");

                        eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(updatedProject.get(), UPDATED)));

                        return (Project)updatedProject.get();
                    }
                });
            }
        });
    }

    Promise<Folder> createFolder(final Container parent, final String name) {
        final Path path = Path.valueOf(name);

        return findResource(parent.getLocation().append(path), true).thenPromise(new Function<Optional<Resource>, Promise<Folder>>() {
            @Override
            public Promise<Folder> apply(Optional<Resource> resource) throws FunctionException {
                checkState(!resource.isPresent(), "Resource already exists");
                checkArgument(!parent.getLocation().isRoot(), "Failed to create folder in workspace root");

                if (path.segmentCount() == 1) {
                    checkArgument(checkFolderName(name), "Invalid folder name");
                }

                return ps.createFolder(parent.getLocation().append(name)).thenPromise(new Function<ItemReference, Promise<Folder>>() {
                    @Override
                    public Promise<Folder> apply(final ItemReference reference) throws FunctionException {

                        return getRemoteResources(parent, path.segmentCount(), true)
                                .then(new Function<Resource[], Folder>() {
                                    @Override
                                    public Folder apply(Resource[] resources) throws FunctionException {

                                        final Path referencePath = Path.valueOf(reference.getPath());

                                        for (Resource descendant : resources) {
                                            if (descendant.getLocation().equals(referencePath)) {
                                                eventBus.fireEvent(
                                                        new ResourceChangedEvent(new ResourceDeltaImpl(descendant, ADDED | DERIVED)));

                                                return (Folder)descendant;
                                            }
                                        }

                                        throw new IllegalArgumentException("Failed to locate created folder");
                                    }
                                });
                    }
                });
            }
        });
    }

    Promise<File> createFile(final Container parent, final String name, final String content) {
        checkArgument(checkFileName(name), "Invalid file name");

        return findResource(parent.getLocation().append(name), true).thenPromise(new Function<Optional<Resource>, Promise<File>>() {
            @Override
            public Promise<File> apply(Optional<Resource> resource) throws FunctionException {
                checkState(!resource.isPresent(), "Resource already exists");
                checkArgument(!parent.getLocation().isRoot(), "Failed to create file in workspace root");

                return ps.createFile(parent.getLocation().append(name), content).thenPromise(new Function<ItemReference, Promise<File>>() {
                    @Override
                    public Promise<File> apply(final ItemReference reference) throws FunctionException {

                        return getRemoteResources(parent, DEPTH_ONE, true)
                                .then(new Function<Resource[], File>() {
                                    @Override
                                    public File apply(Resource[] resources) throws FunctionException {

                                        final Path referencePath = Path.valueOf(reference.getPath());

                                        for (Resource descendant : resources) {
                                            if (descendant.getLocation().equals(referencePath)) {
                                                eventBus.fireEvent(
                                                        new ResourceChangedEvent(new ResourceDeltaImpl(descendant, ADDED | DERIVED)));

                                                return (File)descendant;
                                            }
                                        }

                                        throw new IllegalArgumentException("Failed to locate created file");
                                    }
                                });
                    }
                });
            }
        });
    }

    Promise<Project> createProject(final Project.ProjectRequest createRequest) {
        checkArgument(checkProjectName(createRequest.getBody().getName()), "Invalid project name");
        checkArgument(typeRegistry.getProjectType(createRequest.getBody().getType()) != null, "Invalid project type");

        final Path path = Path.valueOf(createRequest.getBody().getPath());

        return findResource(path, true).thenPromise(new Function<Optional<Resource>, Promise<Project>>() {
            @Override
            public Promise<Project> apply(Optional<Resource> resource) throws FunctionException {

                final MutableProjectConfig projectConfig = (MutableProjectConfig)createRequest.getBody();
                final ProjectConfigDto dto = dtoFactory.createDto(ProjectConfigDto.class)
                                                       .withName(projectConfig.getName())
                                                       .withPath(path.toString())
                                                       .withDescription(projectConfig.getDescription())
                                                       .withType(projectConfig.getType())
                                                       .withMixins(projectConfig.getMixins())
                                                       .withAttributes(projectConfig.getAttributes());


                if (resource.isPresent()) {
                    if (resource.get().isProject()) {
                        throw new IllegalStateException("Project already exists");
                    } else if (resource.get().isFile()) {
                        throw new IllegalStateException("File can not be converted to project");
                    }

                    return update(path, createRequest);
                }

                return ps.createProject(dto, projectConfig.getOptions()).thenPromise(new Function<ProjectConfigDto, Promise<Project>>() {
                    @Override
                    public Promise<Project> apply(ProjectConfigDto config) throws FunctionException {
                        final Project newResource = resourceFactory.newProjectImpl(config, ResourceManager.this);
                        store.register(newResource);

                        return ps.getProjects().then(new Function<List<ProjectConfigDto>, Project>() {
                            @Override
                            public Project apply(List<ProjectConfigDto> updatedConfiguration) throws FunctionException {

                                //cache new configs
                                cachedConfigs = updatedConfiguration.toArray(new ProjectConfigDto[updatedConfiguration.size()]);

                                eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(newResource, ADDED | DERIVED)));

                                return newResource;
                            }
                        });
                    }
                });
            }
        });
    }

    protected Promise<Project> importProject(final Project.ProjectRequest importRequest) {
        checkArgument(checkProjectName(importRequest.getBody().getName()), "Invalid project name");
        checkNotNull(importRequest.getBody().getSource(), "Null source configuration occurred");

        final Path path = Path.valueOf(importRequest.getBody().getPath());

        return findResource(path, true).thenPromise(new Function<Optional<Resource>, Promise<Project>>() {
            @Override
            public Promise<Project> apply(final Optional<Resource> resource) throws FunctionException {

                final SourceStorage sourceStorage = importRequest.getBody().getSource();
                final SourceStorageDto sourceStorageDto = dtoFactory.createDto(SourceStorageDto.class)
                                                                    .withType(sourceStorage.getType())
                                                                    .withLocation(sourceStorage.getLocation())
                                                                    .withParameters(sourceStorage.getParameters());

                return ps.importProject(path, sourceStorageDto).thenPromise(new Function<Void, Promise<Project>>() {
                    @Override
                    public Promise<Project> apply(Void ignored) throws FunctionException {

                        return ps.getProject(path).then(new Function<ProjectConfigDto, Project>() {
                            @Override
                            public Project apply(ProjectConfigDto config) throws FunctionException {
                                cachedConfigs = add(cachedConfigs, config);

                                Resource project = resourceFactory.newProjectImpl(config, ResourceManager.this);

                                checkState(project != null, "Failed to locate imported project's configuration");

                                store.register(project);

                                eventBus.fireEvent(new ResourceChangedEvent(
                                        new ResourceDeltaImpl(project, (resource.isPresent() ? UPDATED : ADDED) | DERIVED)));

                                return (Project)project;
                            }
                        });
                    }
                });
            }
        });
    }

    protected Promise<Resource> move(final Resource source, final Path destination, final boolean force) {
        checkArgument(!source.getLocation().isRoot(), "Workspace root is not allowed to be moved");

        return findResource(destination, true).thenPromise(new Function<Optional<Resource>, Promise<Resource>>() {
            @Override
            public Promise<Resource> apply(Optional<Resource> resource) throws FunctionException {
                checkState(!resource.isPresent() || force, "Cannot create '" + destination.toString() + "'. Resource already exists.");

                if (isResourceOpened(source)) {
                    deletedFilesController.add(source.getLocation().toString());
                }

                return ps.move(source.getLocation(), destination.parent(), destination.lastSegment(), force)
                         .thenPromise(new Function<Void, Promise<Resource>>() {
                             @Override
                             public Promise<Resource> apply(Void ignored) throws FunctionException {

                                 if (source.isProject()) {
                                     return Promises.resolve(source);
                                 }

                                 return findResource(destination, false).then(new Function<Optional<Resource>, Resource>() {
                                     @Override
                                     public Resource apply(Optional<Resource> movedResource) throws FunctionException {
                                         if (movedResource.isPresent()) {
                                             eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(movedResource.get(), source,
                                                                                                               ADDED | MOVED_FROM |
                                                                                                               MOVED_TO | DERIVED)));
                                             return movedResource.get();
                                         }

                                         throw new IllegalStateException("Resource not found");
                                     }
                                 });
                             }
                         });
            }
        });
    }

    protected Promise<Resource> copy(final Resource source, final Path destination, final boolean force) {
        checkArgument(!source.getLocation().isRoot(), "Workspace root is not allowed to be copied");

        return findResource(destination, true).thenPromise(new Function<Optional<Resource>, Promise<Resource>>() {
            @Override
            public Promise<Resource> apply(Optional<Resource> resource) throws FunctionException {
                checkState(!resource.isPresent() || force, "Cannot create '" + destination.toString() + "'. Resource already exists.");

                return ps.copy(source.getLocation(), destination.parent(), destination.lastSegment(), force)
                         .thenPromise(new Function<Void, Promise<Resource>>() {
                             @Override
                             public Promise<Resource> apply(Void ignored) throws FunctionException {

                                 return findResource(destination, false).then(new Function<Optional<Resource>, Resource>() {
                                     @Override
                                     public Resource apply(Optional<Resource> copiedResource) throws FunctionException {
                                         if (copiedResource.isPresent()) {
                                             eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(copiedResource.get(), source,
                                                                                                               ADDED | COPIED_FROM |
                                                                                                               DERIVED)));
                                             return copiedResource.get();
                                         }

                                         throw new IllegalStateException("Resource not found");
                                     }
                                 });
                             }
                         });
            }
        });
    }

    protected Promise<Void> delete(final Resource resource) {
        checkArgument(!resource.getLocation().isRoot(), "Workspace root is not allowed to be moved");

        return ps.deleteItem(resource.getLocation()).then(new Function<Void, Void>() {
            @Override
            public Void apply(Void ignored) throws FunctionException {

                Resource[] descToRemove = null;

                if (resource instanceof Container) {
                    final Optional<Resource[]> optDescendants = store.getAll(resource.getLocation());

                    if (optDescendants.isPresent()) {
                        descToRemove = optDescendants.get();
                    }
                }

                store.dispose(resource.getLocation(), !resource.isFile());

                if (isResourceOpened(resource)) {
                    deletedFilesController.add(resource.getLocation().toString());
                }

                eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(resource, REMOVED | DERIVED)));

                if (descToRemove != null) {
                    for (Resource toRemove : descToRemove) {
                        if (isResourceOpened(resource)) {
                            deletedFilesController.add(toRemove.getLocation().toString());
                        }

                        eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(toRemove, REMOVED | DERIVED)));
                    }
                }

                return null;
            }
        });
    }

    protected Promise<Void> write(final File file, String content) {
        checkArgument(content != null, "Null content occurred");

        return ps.setFileContent(file.getLocation(), content);
    }

    protected Promise<String> read(File file) {
        return ps.getFileContent(file.getLocation());
    }

    Promise<Resource[]> getRemoteResources(final Container container, final int depth, boolean includeFiles) {
        checkArgument(depth > -2, "Invalid depth");

        if (depth == DEPTH_ZERO) {
            return promises.resolve(NO_RESOURCES);
        }

        int depthToReload = depth;
        final Optional<Resource[]> descendants = store.getAll(container.getLocation());

        if (depthToReload != -1 && descendants.isPresent()) {
            for (Resource resource : descendants.get()) {
                if (resource.getLocation().segmentCount() - container.getLocation().segmentCount() > depth) {
                    depthToReload = resource.getLocation().segmentCount() - container.getLocation().segmentCount();
                }
            }
        }

        return ps.getTree(container.getLocation(), depthToReload, includeFiles).then(new Function<TreeElement, Resource[]>() {
            @Override
            public Resource[] apply(TreeElement tree) throws FunctionException {

                class Visitor implements ResourceVisitor {
                    Resource[] resources;

                    private int size    = 0; //size of total items
                    private int incStep = 50; //step to increase resource array

                    private Visitor() {
                        this.resources = NO_RESOURCES;
                    }

                    @Override
                    public void visit(Resource resource) {
                        if (resource.getResourceType() == PROJECT) {
                            final Optional<ProjectConfigDto> optionalConfig = findProjectConfigDto(resource.getLocation());

                            if (optionalConfig.isPresent()) {
                                final Optional<ProblemProjectMarker> optionalMarker = getProblemMarker(optionalConfig.get());

                                if (optionalMarker.isPresent()) {
                                    resource.addMarker(optionalMarker.get());
                                }
                            }
                        }

                        if (size > resources.length - 1) { //check load factor and increase resource array
                            resources = copyOf(resources, resources.length + incStep);
                        }

                        resources[size++] = resource;
                    }

                }

                final Visitor visitor = new Visitor();
                traverse(tree, visitor);

                return copyOf(visitor.resources, visitor.size);
            }
        }).then(new Function<Resource[], Resource[]>() {
            @Override
            public Resource[] apply(Resource[] reloaded) throws FunctionException {

                Resource[] result = new Resource[0];

                if (descendants.isPresent()) {
                    Resource[] outdated = descendants.get();

                    final Resource[] removed = removeAll(outdated, reloaded, false);
                    for (Resource resource : removed) {
                        store.dispose(resource.getLocation(), false);
                        eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(resource, REMOVED)));
                    }

                    final Resource[] updated = removeAll(outdated, reloaded, true);
                    for (Resource resource : updated) {
                        store.register(resource);

                        eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(resource, UPDATED)));

                        final Optional<Resource> registered = store.getResource(resource.getLocation());
                        if (registered.isPresent()) {
                            result = Arrays.add(result, registered.get());
                        }
                    }

                    final Resource[] added = removeAll(reloaded, outdated, false);
                    for (Resource resource : added) {
                        store.register(resource);

                        eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(resource, ADDED)));

                        final Optional<Resource> registered = store.getResource(resource.getLocation());
                        if (registered.isPresent()) {
                            result = Arrays.add(result, registered.get());
                        }
                    }


                } else {
                    for (Resource resource : reloaded) {
                        store.register(resource);

                        eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(resource, ADDED)));

                        final Optional<Resource> registered = store.getResource(resource.getLocation());
                        if (registered.isPresent()) {
                            result = Arrays.add(result, registered.get());
                        }
                    }
                }

                return result;
            }
        });
    }

    Promise<Optional<Container>> getContainer(final Path absolutePath) {
        return findResource(absolutePath, false).then(new Function<Optional<Resource>, Optional<Container>>() {
            @Override
            public Optional<Container> apply(Optional<Resource> optional) throws FunctionException {
                if (optional.isPresent()) {
                    final Resource resource = optional.get();
                    checkState(resource instanceof Container, "Not a container");

                    return of((Container)resource);
                }

                return absent();
            }
        });
    }

    protected Promise<Optional<File>> getFile(final Path absolutePath) {
        return findResource(absolutePath, false).then(new Function<Optional<Resource>, Optional<File>>() {
            @Override
            public Optional<File> apply(Optional<Resource> optional) throws FunctionException {
                if (optional.isPresent()) {
                    final Resource resource = optional.get();
                    checkState(resource.getResourceType() == FILE, "Not a file");

                    return of((File)resource);
                }

                return absent();
            }
        });
    }

    Optional<Container> parentOf(Resource resource) {
        final Path parentLocation = resource.getLocation().segmentCount() == 1 ? Path.ROOT : resource.getLocation().parent();
        final Optional<Resource> optionalParent = store.getResource(parentLocation);

        if (!optionalParent.isPresent()) {
            return absent();
        }

        final Resource parentResource = optionalParent.get();

        checkState(parentResource instanceof Container, "Parent resource is not a container");

        return of((Container)parentResource);
    }

    Promise<Resource[]> childrenOf(final Container container, boolean forceUpdate) {
        if (forceUpdate) {
            return getRemoteResources(container, DEPTH_ONE, true);
        }

        final Optional<Resource[]> optChildren = store.get(container.getLocation());

        if (optChildren.isPresent()) {
            return promises.resolve(optChildren.get());
        } else {
            return promises.resolve(NO_RESOURCES);
        }
    }

    private Promise<Optional<Resource>> findResource(final Path absolutePath, boolean quiet) {

        //search resource in local cache
        final Optional<Resource> optionalCachedResource = store.getResource(absolutePath);
        if (optionalCachedResource.isPresent()) {
            return promises.resolve(optionalCachedResource);
        }

        //request from server
        final Path projectPath = Path.valueOf(absolutePath.segment(0)).makeAbsolute();
        final Optional<Resource> optProject = store.getResource(projectPath);
        final boolean isPresent = optProject.isPresent();

        checkState(isPresent || quiet, "Resource with path '" + projectPath + "' doesn't exists");

        if (!isPresent) {
            return promises.resolve(Optional.<Resource>absent());
        }

        final Resource project = optProject.get();
        checkState(project.getResourceType() == PROJECT, "Resource with path '" + projectPath + "' isn't a project");

        final int seekDepth = absolutePath.segmentCount() - 1;

        return getRemoteResources((Container)project, seekDepth, true).then(new Function<Resource[], Optional<Resource>>() {
            @Override
            public Optional<Resource> apply(Resource[] resources) throws FunctionException {
                for (Resource resource : resources) {
                    if (absolutePath.equals(resource.getLocation())) {
                        return of(resource);
                    }
                }

                return absent();
            }
        });
    }

    private boolean isResourceOpened(final Resource resource) {
        if (!resource.isFile()) {
            return false;
        }

        File file = (File)resource;

        for (EditorPartPresenter editor : editorAgent.getOpenedEditors()) {
            String editorPath = editor.getEditorInput().getFile().getLocation().toString();
            if (editorPath.equals(file.getPath())) {
                return true;
            }
        }

        return false;
    }

    private void traverse(TreeElement tree, ResourceVisitor visitor) {
        for (final TreeElement element : tree.getChildren()) {

            final Resource resource = newResourceFrom(element.getNode());
            visitor.visit(resource);

            if (resource instanceof Container) {
                traverse(element, visitor);
            }
        }
    }

    private Resource newResourceFrom(final ItemReference reference) {
        final Path path = Path.valueOf(reference.getPath());

        switch (reference.getType()) {
            case "file":
                final Link link = reference.getLink(GET_CONTENT_REL);

                return resourceFactory.newFileImpl(path, link.getHref(), this);
            case "folder":
                return resourceFactory.newFolderImpl(path, this);
            case "project":
                final Optional<ProjectConfigDto> config = findProjectConfigDto(path);

                if (config.isPresent()) {
                    return resourceFactory.newProjectImpl(config.get(), this);
                } else {
                    return resourceFactory.newFolderImpl(path, this);
                }
            default:
                throw new IllegalArgumentException("Failed to recognize resource type to create.");
        }
    }

    private Optional<ProjectConfigDto> findProjectConfigDto(final Path path) {
        for (ProjectConfigDto config : cachedConfigs) {
            if (Path.valueOf(config.getPath()).equals(path)) {
                return of(config);
            }
        }

        return absent();
    }

    private Optional<ProblemProjectMarker> getProblemMarker(ProjectConfigDto projectConfigDto) {
        List<ProjectProblemDto> problems = projectConfigDto.getProblems();
        if (problems == null || problems.isEmpty()) {
            return absent();
        }

        Map<Integer, String> code2Message = new HashMap<>(problems.size());
        for (ProjectProblemDto problem : problems) {
            code2Message.put(problem.getCode(), problem.getMessage());
        }

        return of(new ProblemProjectMarker(code2Message));
    }

    protected Promise<Resource[]> synchronize(final Container container) {
        return ps.getProjects().thenPromise(new Function<List<ProjectConfigDto>, Promise<Resource[]>>() {
            @Override
            public Promise<Resource[]> apply(List<ProjectConfigDto> updatedConfiguration) throws FunctionException {
                cachedConfigs = updatedConfiguration.toArray(new ProjectConfigDto[updatedConfiguration.size()]);

                int maxDepth = 1;

                final Optional<Resource[]> descendants = store.getAll(container.getLocation());

                if (descendants.isPresent()) {
                    final Resource[] resources = descendants.get();

                    for (Resource resource : resources) {
                        final int segCount = resource.getLocation().segmentCount() - container.getLocation().segmentCount();

                        if (segCount > maxDepth) {
                            maxDepth = segCount;
                        }
                    }
                }

                final Container[] holder = new Container[]{container};

                if (holder[0].isProject()) {
                    final Optional<ProjectConfigDto> config = findProjectConfigDto(holder[0].getLocation());

                    if (config.isPresent()) {

                        final ProjectImpl project = resourceFactory.newProjectImpl(config.get(), ResourceManager.this);

                        store.register(project);
                        holder[0] = project;
                    }
                }

                return getRemoteResources(holder[0], maxDepth, true).then(
                        new Function<Resource[], Resource[]>() {
                            @Override
                            public Resource[] apply(Resource[] resources) throws FunctionException {

                                eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(holder[0], SYNCHRONIZED | DERIVED)));

                                return resources;
                            }
                        });
            }
        });
    }

    protected Promise<ResourceDelta[]> synchronize(final ResourceDelta[] deltas) {

        Promise<Void> promise = promises.resolve(null);

        for (final ResourceDelta delta : deltas) {
            if (delta.getKind() == ADDED) {
                if (delta.getFlags() == (MOVED_FROM | MOVED_TO)) {

                    promise.thenPromise(new Function<Void, Promise<Void>>() {
                        @Override
                        public Promise<Void> apply(Void ignored) throws FunctionException {
                            return onExternalDeltaMoved(delta);
                        }
                    });

                } else {

                    promise.thenPromise(new Function<Void, Promise<Void>>() {
                        @Override
                        public Promise<Void> apply(Void ignored) throws FunctionException {
                            return onExternalDeltaAdded(delta);
                        }
                    });

                }
            } else if (delta.getKind() == REMOVED) {

                promise.thenPromise(new Function<Void, Promise<Void>>() {
                    @Override
                    public Promise<Void> apply(Void ignored) throws FunctionException {
                        return onExternalDeltaRemoved(delta);
                    }
                });

            } else if (delta.getKind() == UPDATED) {

                promise.thenPromise(new Function<Void, Promise<Void>>() {
                    @Override
                    public Promise<Void> apply(Void ignored) throws FunctionException {
                        return onExternalDeltaUpdated(delta);
                    }
                });

            }
        }

        return promise.then(new Function<Void, ResourceDelta[]>() {
            @Override
            public ResourceDelta[] apply(Void ignored) throws FunctionException {
                return deltas;
            }
        });
    }

    private Promise<Void> onExternalDeltaMoved(final ResourceDelta delta) {
        //search resource to remove at first
        return findResource(delta.getFromPath(), true).thenPromise(new Function<Optional<Resource>, Promise<Void>>() {
            @Override
            public Promise<Void> apply(final Optional<Resource> toRemove) throws FunctionException {
                if (!toRemove.isPresent()) {
                    return promises.resolve(null);
                }

                store.dispose(delta.getFromPath(), true);

                return findResource(delta.getToPath(), true).then(new Function<Optional<Resource>, Void>() {
                    @Override
                    public Void apply(final Optional<Resource> resource) throws FunctionException {

                        if (resource.isPresent() && toRemove.isPresent()) {
                            Resource intercepted = resource.get();

                            if (!store.getResource(intercepted.getLocation()).isPresent()) {
                                store.register(intercepted);
                            }

                            eventBus.fireEvent(new ResourceChangedEvent(
                                    new ResourceDeltaImpl(intercepted, toRemove.get(), ADDED | MOVED_FROM | MOVED_TO | DERIVED)));
                        }

                        return null;
                    }
                });
            }
        });
    }

    private Promise<Void> onExternalDeltaAdded(final ResourceDelta delta) {
        return findResource(delta.getToPath(), true).then(new Function<Optional<Resource>, Void>() {
            @Override
            public Void apply(final Optional<Resource> resource) throws FunctionException {
                if (resource.isPresent()) {
                    Resource intercepted = resource.get();

                    if (!store.getResource(intercepted.getLocation()).isPresent()) {
                        store.register(intercepted);
                    }

                    eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(intercepted, ADDED | DERIVED)));
                } else if (delta.getToPath().segmentCount() == 1) {
                    ps.getProjects().then(new Function<List<ProjectConfigDto>, Void>() {
                        @Override
                        public Void apply(List<ProjectConfigDto> updatedConfiguration) throws FunctionException {
                            cachedConfigs = updatedConfiguration.toArray(new ProjectConfigDto[updatedConfiguration.size()]);

                            for (ProjectConfigDto config : cachedConfigs) {
                                if (Path.valueOf(config.getPath()).equals(delta.getToPath())) {
                                    final Project project = resourceFactory.newProjectImpl(config, ResourceManager.this);

                                    store.register(project);

                                    eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(project, ADDED)));
                                }
                            }

                            return null;
                        }
                    });
                }

                return null;
            }
        });
    }

    private Promise<Void> onExternalDeltaUpdated(final ResourceDelta delta) {
        if (delta.getToPath().segmentCount() == 0) {
            workspaceRoot.synchronize();

            return null;
        }

        return findResource(delta.getToPath(), true).then(new Function<Optional<Resource>, Void>() {
            @Override
            public Void apply(Optional<Resource> resource) throws FunctionException {

                if (resource.isPresent()) {
                    if (resource.get() instanceof Container) {
                        ((Container)resource.get()).synchronize();
                    } else {
                        eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(resource.get(), UPDATED | DERIVED)));
                    }
                }

                return null;
            }
        });
    }

    private Promise<Void> onExternalDeltaRemoved(final ResourceDelta delta) {
        return findResource(delta.getFromPath(), true).then(new Function<Optional<Resource>, Void>() {
            @Override
            public Void apply(Optional<Resource> resource) throws FunctionException {

                if (resource.isPresent()) {
                    store.dispose(resource.get().getLocation(), true);
                    eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(resource.get(), REMOVED | DERIVED)));
                }

                return null;
            }
        });
    }

    protected Promise<Resource[]> search(final Container container, String fileMask, String contentMask) {
        QueryExpression queryExpression = new QueryExpression();
        if (!isNullOrEmpty(contentMask)) {
            queryExpression.setText(contentMask);
        }
        if (!isNullOrEmpty(fileMask)) {
            queryExpression.setName(fileMask);
        }
        if (!container.getLocation().isRoot()) {
            queryExpression.setPath(container.getLocation().toString());
        }

        return ps.search(queryExpression).thenPromise(new Function<List<ItemReference>, Promise<Resource[]>>() {
            @Override
            public Promise<Resource[]> apply(final List<ItemReference> references) throws FunctionException {
                if (references.isEmpty()) {
                    return promises.resolve(NO_RESOURCES);
                }

                int maxDepth = 0;

                final Path[] paths = new Path[references.size()];

                for (int i = 0; i < paths.length; i++) {
                    final Path path = Path.valueOf(references.get(i).getPath());
                    paths[i] = path;

                    if (path.segmentCount() > maxDepth) {
                        maxDepth = path.segmentCount();
                    }
                }

                return getRemoteResources(container, maxDepth, true).then(new Function<Resource[], Resource[]>() {
                    @Override
                    public Resource[] apply(Resource[] resources) throws FunctionException {

                        Resource[] filtered = NO_RESOURCES;
                        Path[] mutablePaths = paths;

                        outer:
                        for (Resource resource : resources) {
                            if (resource.getResourceType() != FILE) {
                                continue;
                            }

                            for (int i = 0; i < mutablePaths.length; i++) {
                                Path path = mutablePaths[i];

                                if (path.segmentCount() == resource.getLocation().segmentCount() && path.equals(resource.getLocation())) {
                                    Resource[] tmpFiltered = copyOf(filtered, filtered.length + 1);
                                    tmpFiltered[filtered.length] = resource;
                                    filtered = tmpFiltered;

                                    //reduce the size of mutablePaths by removing already checked item
                                    int size = mutablePaths.length;
                                    int numMoved = mutablePaths.length - i - 1;
                                    if (numMoved > 0) {
                                        arraycopy(mutablePaths, i + 1, mutablePaths, i, numMoved);
                                    }
                                    mutablePaths = copyOf(mutablePaths, --size);

                                    continue outer;
                                }
                            }
                        }

                        return filtered;
                    }
                });
            }
        });
    }

    Promise<SourceEstimation> estimate(Container container, String projectType) {
        checkArgument(projectType != null, "Null project type");
        checkArgument(!projectType.isEmpty(), "Empty project type");

        return ps.estimate(container.getLocation(), projectType);
    }

    void notifyMarkerChanged(Resource resource, Marker marker, int status) {
        eventBus.fireEvent(new MarkerChangedEvent(resource, marker, status));
        eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(resource, UPDATED)));
    }

    protected String getUrl(Resource resource) {
        checkArgument(!resource.getLocation().isRoot(), "Workspace root doesn't have export URL");

        final String baseUrl = devMachine.getWsAgentBaseUrl() + "/project/export";

        if (resource.getResourceType() == FILE) {
            return baseUrl + "/file" + resource.getLocation();
        }

        return urlModifier.modify(baseUrl + resource.getLocation());
    }

    public Promise<List<SourceEstimation>> resolve(Project project) {
        return ps.resolveSources(project.getLocation());
    }

    interface ResourceVisitor {
        void visit(Resource resource);
    }

    public interface ResourceFactory {
        ProjectImpl newProjectImpl(ProjectConfig reference, ResourceManager resourceManager);

        FolderImpl newFolderImpl(Path path, ResourceManager resourceManager);

        FileImpl newFileImpl(Path path, String contentUrl, ResourceManager resourceManager);
    }

    public interface ResourceManagerFactory {
        ResourceManager newResourceManager(DevMachine devMachine);
    }
}
