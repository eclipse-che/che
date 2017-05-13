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
package org.eclipse.che.ide.context;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.app.StartUpAction;
import org.eclipse.che.ide.api.data.HasDataObject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.SelectionChangedEvent;
import org.eclipse.che.ide.api.event.SelectionChangedHandler;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.event.WindowActionHandler;
import org.eclipse.che.ide.api.machine.ActiveRuntime;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.ResourcePathComparator;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.client.StartUpActionsParser;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.ResourceManagerInitializer;
import org.eclipse.che.ide.resources.impl.ResourceDeltaImpl;
import org.eclipse.che.ide.resources.impl.ResourceManager;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.gwt.user.client.Random.nextInt;
import static java.util.Collections.addAll;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

/**
 * Implementation of {@link AppContext}.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class AppContextImpl implements AppContext,
                                       SelectionChangedHandler,
                                       ResourceChangedHandler,
                                       WindowActionHandler,
                                       WorkspaceStartedEvent.Handler,
                                       WorkspaceStoppedEvent.Handler,
                                       ResourceManagerInitializer {
    private static final String APP_ID = String.valueOf(nextInt(Integer.MAX_VALUE));

    private final QueryParameters                        queryParameters;
    private final List<String>                           projectsInImport;
    private final EventBus                               eventBus;
    private final ResourceManager.ResourceManagerFactory resourceManagerFactory;
    private final Provider<EditorAgent>                  editorAgentProvider;
    private final Provider<AppStateManager>              appStateManager;

    private final List<Project>  rootProjects      = newArrayList();
    private final List<Resource> selectedResources = newArrayList();

    private final CurrentUser currentUser;

    /**
     * List of actions with parameters which comes from startup URL.
     * Can be processed after IDE initialization as usual after starting ws-agent.
     */
    private final List<StartUpAction> startAppActions;

    private Workspace       userWorkspace;
    private FactoryDto      factory;
    private Path            projectsRoot;
    private ActiveRuntime   runtime;
    private ResourceManager resourceManager;

    @Inject
    public AppContextImpl(EventBus eventBus,
                          QueryParameters queryParameters,
                          ResourceManager.ResourceManagerFactory resourceManagerFactory,
                          Provider<EditorAgent> editorAgentProvider,
                          Provider<AppStateManager> appStateManager,
                          CurrentUser currentUser) {
        this.eventBus = eventBus;
        this.queryParameters = queryParameters;
        this.resourceManagerFactory = resourceManagerFactory;
        this.editorAgentProvider = editorAgentProvider;
        this.appStateManager = appStateManager;
        this.currentUser = currentUser;
        this.startAppActions = StartUpActionsParser.getStartUpActions();

        projectsInImport = new ArrayList<>();

        eventBus.addHandler(SelectionChangedEvent.TYPE, this);
        eventBus.addHandler(ResourceChangedEvent.getType(), this);
        eventBus.addHandler(WindowActionEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    }

    private static native String masterFromIDEConfig() /*-{
        if ($wnd.IDE && $wnd.IDE.config) {
            return $wnd.IDE.config.restContext;
        } else {
            return null;
        }
    }-*/;

    @Override
    public Workspace getWorkspace() {
        return userWorkspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        if (workspace != null) {
            userWorkspace = workspace;
            if (workspace.getRuntime() != null) {
                runtime = new ActiveRuntime(workspace);
            }
        } else {
            userWorkspace = null;
            runtime = null;
        }
    }

    @Override
    public String getWorkspaceId() {
        if (userWorkspace == null) {
            throw new IllegalArgumentException(getClass() + " Workspace can not be null.");
        }

        return userWorkspace.getId();
    }

    @Override
    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    @Override
    public List<String> getImportingProjects() {
        return projectsInImport;
    }

    @Override
    public void addProjectToImporting(String pathToProject) {
        projectsInImport.add(pathToProject);
    }

    @Override
    public void removeProjectFromImporting(String pathToProject) {
        projectsInImport.remove(pathToProject);
    }

    @Override
    public List<StartUpAction> getStartAppActions() {
        return startAppActions;
    }

    @Override
    public FactoryDto getFactory() {
        return factory;
    }

    @Override
    public void setFactory(FactoryDto factory) {
        this.factory = factory;
    }

    @Override
    public DevMachine getDevMachine() {
        return runtime.getDevMachine();
    }

    @Override
    public void initResourceManager() {
        if (runtime.getDevMachine() == null) {
            //should never happened, but anyway
            Log.error(AppContextImpl.class, "Dev machine is not initialized");
        }

        if (!rootProjects.isEmpty()) {
            for (Project project : rootProjects) {
                eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(project, REMOVED)));
            }
            rootProjects.clear();
        }

        resourceManager = resourceManagerFactory.newResourceManager(runtime.getDevMachine());
        resourceManager.getWorkspaceProjects().then(projects -> {
            rootProjects.clear();
            addAll(rootProjects, projects);
            rootProjects.sort(ResourcePathComparator.getInstance());
            eventBus.fireEvent(new WorkspaceReadyEvent(projects));
        }).catchError(error -> {
            Log.error(AppContextImpl.class, error.getCause());
        });
    }

    @Override
    public String getWorkspaceName() {
        return userWorkspace.getConfig().getName();
    }

    /** {@inheritDoc} */
    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();
        final Resource resource = delta.getResource();

        if (delta.getKind() == ADDED) {
            if ((delta.getFlags() & (MOVED_FROM | MOVED_TO)) != 0) {

                for (Project rootProject : rootProjects) {
                    if (rootProject.getLocation().equals(delta.getFromPath()) && resource.isProject()) {
                        rootProjects.set(rootProjects.indexOf(rootProject), resource.asProject());
                        break;
                    }
                }

                for (Resource selectedResource : selectedResources) {
                    if (selectedResource.getLocation().equals(delta.getFromPath())) {
                        selectedResources.set(selectedResources.indexOf(selectedResource), resource);
                        break;
                    }
                }
            } else if (resource.getLocation().segmentCount() == 1 && resource.isProject()) {
                boolean exists = rootProjects.stream().anyMatch(it -> it.getLocation().equals(resource.getLocation()));

                if (!exists) {
                    rootProjects.add(resource.asProject());
                    rootProjects.sort(ResourcePathComparator.getInstance());
                }
            }
        } else if (delta.getKind() == REMOVED) {

            for (Project rootProject : rootProjects) {
                if (rootProject.getLocation().equals(resource.getLocation()) && resource.isProject()) {
                    rootProjects.remove(rootProjects.indexOf(rootProject));
                    break;
                }
            }

            for (Resource selectedResource : selectedResources) {
                if (selectedResource.getLocation().equals(resource.getLocation())) {
                    selectedResources.remove(selectedResources.indexOf(selectedResource));
                    break;
                }
            }
        } else if (delta.getKind() == UPDATED) {

            for (Project rootProject : rootProjects) {
                if (rootProject.getLocation().equals(resource.getLocation()) && resource.isProject()) {
                    rootProjects.set(rootProjects.indexOf(rootProject), resource.asProject());
                    break;
                }
            }

            for (Resource selectedResource : selectedResources) {
                if (selectedResource.getLocation().equals(resource.getLocation())) {
                    selectedResources.set(selectedResources.indexOf(selectedResource), resource);
                    break;
                }
            }
        }
    }

    @Override
    public Path getProjectsRoot() {
        return projectsRoot;
    }

    public void setProjectsRoot(Path projectsRoot) {
        this.projectsRoot = projectsRoot;
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent event) {
        final Selection<?> selection = event.getSelection();
        if (selection instanceof Selection.NoSelectionProvided) {
            return;
        }

        selectedResources.clear();

        if (selection != null) {
            for (Object o : selection.getAllElements()) {
                if (o instanceof HasDataObject && ((HasDataObject)o).getData() instanceof Resource) {
                    selectedResources.add((Resource)((HasDataObject)o).getData());
                } else if (o instanceof Resource) {
                    selectedResources.add((Resource)o);
                }
            }
        }
    }

    @Override
    public Project[] getProjects() {
        return rootProjects.toArray(new Project[rootProjects.size()]);
    }

    @Override
    public Container getWorkspaceRoot() {
        checkState(resourceManager != null, "Workspace configuration has not been received yet");

        return resourceManager.getWorkspaceRoot();
    }

    @Override
    public Resource getResource() {
        return selectedResources.isEmpty() ? null : selectedResources.get(0);
    }

    @Override
    public Resource[] getResources() {
        return selectedResources.toArray(new Resource[selectedResources.size()]);
    }

    @Override
    public Project getRootProject() {
        if (rootProjects.isEmpty()) {
            return null;
        }

        if (selectedResources.isEmpty()) {
            EditorAgent editorAgent = editorAgentProvider.get();
            if (editorAgent == null) {
                return null;
            }

            final EditorPartPresenter editor = editorAgent.getActiveEditor();
            if (editor == null) {
                return null;
            }

            final VirtualFile file = editor.getEditorInput().getFile();

            if (file instanceof SyntheticNode) {
                final Path projectPath = ((SyntheticNode)file).getProject();
                for (Project project : rootProjects) {
                    if (project.getLocation().equals(projectPath)) {
                        return project;
                    }
                }
            }

            return null;
        } else {
            Project root = null;

            for (Project project : rootProjects) {
                if (project.getLocation().isPrefixOf(selectedResources.get(0).getLocation())) {
                    root = project;
                    break;
                }
            }

            if (root == null) {
                return null;
            }

            for (int i = 1; i < selectedResources.size(); i++) {
                if (!root.getLocation().isPrefixOf(selectedResources.get(i).getLocation())) {
                    return null;
                }
            }

            return root;
        }
    }

    @Override
    public void onWindowClosing(WindowActionEvent event) {
        appStateManager.get().persistWorkspaceState(getWorkspaceId());
    }

    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
        setWorkspace(event.getWorkspace());
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        appStateManager.get().persistWorkspaceState(getWorkspaceId()).then(ignored -> {
            for (Project project : rootProjects) {
                eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(project, REMOVED)));
            }

            rootProjects.clear();
            resourceManager = null;
        });

        clearRuntime();
    }

    private void clearRuntime() {
        runtime = null;
    }

    @Override
    public void onWindowClosed(WindowActionEvent event) {
    }

    @Override
    public String getMasterEndpoint() {
        String fromUrl = queryParameters.getByName("master");
        if (fromUrl == null || fromUrl.isEmpty())
            return masterFromIDEConfig();
        else
            return fromUrl;
    }

    @Override
    public String getDevAgentEndpoint() {
        String fromUrl = queryParameters.getByName("agent");
        if (fromUrl == null || fromUrl.isEmpty())
            return runtime.getDevMachine().getWsAgentBaseUrl();
        else
            return fromUrl;
    }

    @Override
    public String getAppId() {
        return APP_ID;
    }

    @Override
    public ActiveRuntime getActiveRuntime() {
        return runtime;
    }
}
