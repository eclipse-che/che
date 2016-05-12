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
package org.eclipse.che.ide.project.node;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.event.project.CreateProjectHandler;
import org.eclipse.che.ide.api.event.project.DeleteProjectEvent;
import org.eclipse.che.ide.api.event.project.DeleteProjectHandler;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.api.project.node.settings.SettingsProvider;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.project.node.factory.NodeFactory;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.removeIf;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;

/**
 * Helper class to define various functionality related with nodes.
 * Such as get node children. Wrapping nodes.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class NodeManager {
    protected final NodeFactory            nodeFactory;
    protected final ProjectServiceClient   projectService;
    protected final DtoUnmarshallerFactory dtoUnmarshaller;
    protected final NodesResources         nodesResources;
    protected final SettingsProvider       nodeSettingsProvider;
    protected final DtoFactory             dtoFactory;
    protected final Set<NodeIconProvider>  nodeIconProvider;
    protected final AppContext             appContext;

    @Inject
    public NodeManager(NodeFactory nodeFactory,
                       ProjectServiceClient projectService,
                       DtoUnmarshallerFactory dtoUnmarshaller,
                       NodesResources nodesResources,
                       SettingsProvider nodeSettingsProvider,
                       DtoFactory dtoFactory,
                       Set<NodeIconProvider> nodeIconProvider,
                       final AppContext appContext,
                       EventBus eventBus) {
        this.nodeFactory = nodeFactory;
        this.projectService = projectService;
        this.dtoUnmarshaller = dtoUnmarshaller;
        this.nodesResources = nodesResources;
        this.nodeSettingsProvider = nodeSettingsProvider;
        this.dtoFactory = dtoFactory;
        this.nodeIconProvider = nodeIconProvider;
        this.appContext = appContext;


        eventBus.addHandler(DeleteProjectEvent.TYPE, new DeleteProjectHandler() {
            @Override
            public void onProjectDeleted(final DeleteProjectEvent event) {
                removeIf(appContext.getWorkspace().getConfig().getProjects(), new Predicate<ProjectConfig>() {
                    @Override
                    public boolean apply(@Nullable ProjectConfig input) {
                        return input.getPath().equals(event.getProjectConfig().getPath());
                    }
                });
            }
        });

        eventBus.addHandler(CreateProjectEvent.TYPE, new CreateProjectHandler() {
            @Override
            public void onProjectCreated(CreateProjectEvent event) {
                appContext.getWorkspace().getConfig().getProjects().add(event.getProjectConfig());
            }
        });

        eventBus.addHandler(ProjectUpdatedEvent.getType(), new ProjectUpdatedEvent.ProjectUpdatedHandler() {
            @Override
            public void onProjectUpdated(final ProjectUpdatedEvent event) {
                final Optional<ProjectConfigDto> configOptional = tryFind(appContext.getWorkspace().getConfig().getProjects(), new Predicate<ProjectConfigDto>() {
                    @Override
                    public boolean apply(@Nullable ProjectConfigDto input) {
                        return input.getPath().equals(event.getPath());
                    }
                });

                if (!configOptional.isPresent()) {
                    return;
                }

                if (appContext.getWorkspace().getConfig().getProjects().remove(configOptional.get())) {
                    appContext.getWorkspace().getConfig().getProjects().add(event.getUpdatedProjectDescriptor());
                }
            }
        });
    }

    /** Children operations ********************* */
    @NotNull
    public Promise<List<Node>> getChildren(ItemReference itemReference,
                                           ProjectConfigDto projectConfigDto,
                                           NodeSettings nodeSettings) {
        return getChildren(itemReference.getPath(), projectConfigDto, nodeSettings);
    }

    @NotNull
    public Promise<List<Node>> getChildren(ProjectConfigDto projectConfigDto,
                                           NodeSettings nodeSettings) {
        return getChildren(projectConfigDto.getPath(), projectConfigDto, nodeSettings);
    }

    @NotNull
    public Promise<List<Node>> getChildren(final String path,
                                           ProjectConfigDto projectConfigDto,
                                           NodeSettings nodeSettings) {
        return newPromise(new RequestCall<List<ItemReference>>() {
            @Override
            public void makeCall(AsyncCallback<List<ItemReference>> callback) {
                projectService
                        .getChildren(appContext.getDevMachine(), path, newCallback(callback, dtoUnmarshaller.newListUnmarshaller(ItemReference.class)));
            }
        }).thenPromise(filterItemReference())
          .thenPromise(createItemReferenceNodes(projectConfigDto, nodeSettings))
          .catchError(handleError());
    }

    @NotNull
    public RequestCall<List<ItemReference>> getItemReferenceRC(@NotNull final String path) {
        return new RequestCall<List<ItemReference>>() {
            @Override
            public void makeCall(AsyncCallback<List<ItemReference>> callback) {
                projectService
                        .getChildren(appContext.getDevMachine(), path, _callback(callback, dtoUnmarshaller.newListUnmarshaller(ItemReference.class)));
            }
        };
    }

    public Function<List<ItemReference>, Promise<List<ItemReference>>> filterItemReference() {
        //filter item references before they will be transformed int nodes
        return self();
    }

    private Function<List<ItemReference>, Promise<List<Node>>> createItemReferenceNodes(final ProjectConfigDto projectConfigDto,
                                                                                        final NodeSettings nodeSettings) {
        return new Function<List<ItemReference>, Promise<List<Node>>>() {
            @Override
            public Promise<List<Node>> apply(List<ItemReference> itemRefList) throws FunctionException {
                if (itemRefList == null || itemRefList.isEmpty()) {
                    return Promises.resolve(Collections.<Node>emptyList());
                }

                final List<Node> nodes = new ArrayList<>(itemRefList.size());

                for (ItemReference itemReference : itemRefList) {
                    //Skip files which starts with "." if enabled
                    if (!nodeSettings.isShowHiddenFiles() && itemReference.getName().startsWith(".")) {
                        continue;
                    }

                    Node node = createNodeByType(itemReference, projectConfigDto, nodeSettings);
                    if (node != null) {
                        nodes.add(node);
                    }

                }

                return Promises.resolve(nodes);
            }
        };
    }

    public Node createNodeByType(final ItemReference itemReference, ProjectConfigDto configDto, NodeSettings settings) {
        String itemType = itemReference.getType();

        if ("file".equals(itemType)) {
            return nodeFactory.newFileReferenceNode(itemReference, configDto, settings);
        }

        if ("folder".equals(itemType) || "project".equals(itemType)) {
            return nodeFactory.newFolderReferenceNode(itemReference, configDto, settings);
        }

        return null;
    }

    @NotNull
    private Function<PromiseError, List<Node>> handleError() {
        return new Function<PromiseError, List<Node>>() {
            @Override
            public List<Node> apply(PromiseError arg) throws FunctionException {
                return Collections.emptyList();
            }
        };
    }

    /**
     * Get project list and construct project nodes for the tree.
     *
     * @return list of the {@link ProjectNode} nodes.
     */
    @NotNull
    public Promise<List<Node>> getProjectNodes() {
        return projectService.getProjects(appContext.getDevMachine()).then(new Function<List<ProjectConfigDto>, List<Node>>() {
            @Override
            public List<Node> apply(List<ProjectConfigDto> projects) throws FunctionException {
                if (projects == null) {
                    return Collections.emptyList();
                }

                //fill workspace projects with loaded actual configs, temporary solution that will be replaced after GA release
                appContext.getWorkspace().getConfig().withProjects(new ArrayList<>(projects));

                final Iterable<ProjectConfigDto> rootProjects = filter(projects, new Predicate<ProjectConfigDto>() {
                    @Override
                    public boolean apply(@Nullable ProjectConfigDto input) {
                        final Path path = Path.valueOf(input.getPath());

                        // For paths like: '/project' or '/project/' segment count always will be equals to 1
                        return path.segmentCount() == 1;
                    }
                });

                final NodeSettings settings = nodeSettingsProvider.getSettings();

                final Iterable<Node> nodes = transform(rootProjects, new com.google.common.base.Function<ProjectConfigDto, Node>() {
                    @javax.annotation.Nullable
                    @Override
                    public Node apply(@Nullable ProjectConfigDto project) {
                        return nodeFactory.newProjectNode(project, settings == null ? NodeSettings.DEFAULT_SETTINGS : settings);
                    }
                });

                return newArrayList(nodes);
            }
        });
    }

    /** Common methods ********************* */

    @NotNull
    protected <T> AsyncRequestCallback<T> _callback(@NotNull final AsyncCallback<T> callback, @NotNull Unmarshallable<T> u) {
        return new AsyncRequestCallback<T>(u) {
            @Override
            protected void onSuccess(T result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable e) {
                callback.onFailure(e);
            }
        };
    }

    @NotNull
    public NodesResources getNodesResources() {
        return nodesResources;
    }

    @NotNull
    public ProjectNode wrap(ProjectConfigDto projectConfig) {
        NodeSettings nodeSettings = nodeSettingsProvider.getSettings();
        return nodeFactory.newProjectNode(projectConfig, nodeSettings == null ? NodeSettings.DEFAULT_SETTINGS : nodeSettings);
    }

    @Nullable
    public ItemReferenceBasedNode wrap(ItemReference itemReference, ProjectConfigDto projectConfig) {
        NodeSettings nodeSettings = nodeSettingsProvider.getSettings();
        if (nodeSettings == null) {
            nodeSettings = NodeSettings.DEFAULT_SETTINGS;
        }

        ItemReferenceBasedNode node = null;

        if ("file".equals(itemReference.getType())) {
            node = nodeFactory.newFileReferenceNode(itemReference, projectConfig, nodeSettings);
        } else if ("folder".equals(itemReference.getType())) {
            node = nodeFactory.newFolderReferenceNode(itemReference, projectConfig, nodeSettings);
        }

        return node;
    }

    @Deprecated
    public static boolean isProjectOrModuleNode(Node node) {
        return node instanceof ProjectNode || node instanceof ModuleNode;
    }

    protected <T> Function<T, Promise<T>> self() {
        return new Function<T, Promise<T>>() {
            @Override
            public Promise<T> apply(T self) throws FunctionException {
                return Promises.resolve(self);
            }
        };
    }

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public Set<NodeIconProvider> getNodeIconProvider() {
        return nodeIconProvider;
    }
}
