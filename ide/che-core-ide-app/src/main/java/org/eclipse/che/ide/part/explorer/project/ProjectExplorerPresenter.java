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
package org.eclipse.che.ide.part.explorer.project;

import com.google.common.base.MoreObjects;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.ProjectExplorerPart;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.marker.MarkerChangedEvent;
import org.eclipse.che.ide.api.resources.marker.MarkerChangedEvent.MarkerChangedHandler;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerView.ActionDelegate;
import org.eclipse.che.ide.project.node.SyntheticNodeUpdateEvent;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ContainerNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.NodeDescriptor;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent.SelectionChangedHandler;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.resources.Resource.PROJECT;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.DERIVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.SYNCHRONIZED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

/**
 * Project explorer presenter. Handle basic logic to control project tree display.
 *
 * @author Vlad Zhukovskiy
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectExplorerPresenter extends BasePresenter implements ActionDelegate,
                                                                       ProjectExplorerPart,
                                                                       ResourceChangedHandler,
                                                                       MarkerChangedHandler,
                                                                       SyntheticNodeUpdateEvent.SyntheticNodeUpdateHandler {
    private final ProjectExplorerView      view;
    private final ResourceNode.NodeFactory nodeFactory;
    private final SettingsProvider         settingsProvider;
    private final CoreLocalizationConstant locale;
    private final Resources                resources;

    private static final int PART_SIZE = 500;

    private boolean hiddenFilesAreShown;

    @Inject
    public ProjectExplorerPresenter(final ProjectExplorerView view,
                                    EventBus eventBus,
                                    CoreLocalizationConstant locale,
                                    Resources resources,
                                    final ResourceNode.NodeFactory nodeFactory,
                                    final SettingsProvider settingsProvider) {
        this.view = view;
        this.nodeFactory = nodeFactory;
        this.settingsProvider = settingsProvider;
        this.locale = locale;
        this.resources = resources;
        this.view.setDelegate(this);

        eventBus.addHandler(ResourceChangedEvent.getType(), this);
        eventBus.addHandler(MarkerChangedEvent.getType(), this);
        eventBus.addHandler(SyntheticNodeUpdateEvent.getType(), this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, new WorkspaceStoppedEvent.Handler() {
            @Override
            public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
                getTree().getNodeStorage().clear();
            }
        });

        view.getTree().getSelectionModel().addSelectionChangedHandler(new SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                setSelection(new Selection<>(event.getSelection()));
            }
        });
    }

    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();

        switch (delta.getKind()) {
            case SYNCHRONIZED:
                onResourceSynchronized(delta.getResource());
                break;
            case ADDED:
                onResourceAdded(delta);
                break;
            case REMOVED:
                onResourceRemoved(delta);
                break;
            case UPDATED:
                onResourceUpdated(delta);
        }
    }

    @SuppressWarnings("unchecked")
    private void onResourceSynchronized(Resource resource) {
        final Tree tree = view.getTree();

        Node node = getNode(resource.getLocation());

        if (node == null) {
            node = getParentNode(resource.getLocation());

            if (node != null) {
                tree.getNodeLoader().loadChildren(node, true);
            }

            return;
        }

        final String oldId = tree.getNodeStorage().getKeyProvider().getKey(node);
        ((ResourceNode)node).setData(resource);
        tree.getNodeStorage().reIndexNode(oldId, node);
        tree.refresh(node);

        if (tree.isExpanded(node)) {
            tree.getNodeLoader().loadChildren(node, true);
        }
    }

    private Node getNode(Path path) {
        final Tree tree = view.getTree();

        for (final Node node : tree.getNodeStorage().getAll()) {
            if (isNodeServesLocation(node, path)) {
                return node;
            }
        }

        return null;
    }

    private Node getParentNode(Path path) {
        Node node = null;

        while (node == null) {
            if (path.segmentCount() == 0) {
                return null;
            }

            path = path.parent();
            node = getNode(path);
        }

        return node;
    }

    private Node getProjectNode(Path path) {
        Node node;

        node = getNode(path);

        if (node == null) {
            return null;
        }

        while (!(node instanceof ResourceNode && ((ResourceNode)node).getData().isProject())) {
            node = node.getParent();

            if (node == null) {
                return null;
            }
        }

        return node;
    }

    @SuppressWarnings("unchecked")
    private void onResourceAdded(ResourceDelta delta) {
        if ((delta.getFlags() & DERIVED) == 0) {
            return;
        }

        final Tree tree = view.getTree();
        final NodeSettings nodeSettings = settingsProvider.getSettings();

        final Resource resource = delta.getResource();

        if ((delta.getFlags() & (MOVED_FROM | MOVED_TO)) != 0) {

            final Node node1 = getProjectNode(delta.getFromPath());
            final Node node2 = getProjectNode(delta.getToPath());

            //TODO: brutal fix for CHE-1535 just avoid NPE here
            if (node1 == null && node2 == null) {
                final Node node = tree.getSelectionModel().getSelectedNodes().get(0);
                if (node != null) {
                    tree.refresh(node);
                }
                return;
            }

            if (node1.equals(node2)) {
                if (tree.isExpanded(node1)) {
                    tree.getNodeLoader().loadChildren(node1, true);
                }
            } else {
                if (tree.isExpanded(node1)) {
                    tree.getNodeLoader().loadChildren(node1, true);
                }

                if (node2 != null && tree.isExpanded(node2)) {
                    tree.getNodeLoader().loadChildren(node2, true);
                }
            }

            return;
        } else {
            //process root project
            if (resource.getLocation().segmentCount() == 1 && resource.getResourceType() == PROJECT) {

                final Node rootProject = getNode(resource.getLocation());

                if (rootProject == null) {
                    final ContainerNode node = nodeFactory.newContainerNode((Container)resource, nodeSettings);
                    tree.getNodeStorage().add(node);
                } else {
                    final String oldId = tree.getNodeStorage().getKeyProvider().getKey(rootProject);
                    ((ResourceNode)rootProject).setData(resource);
                    tree.getNodeStorage().reIndexNode(oldId, rootProject);
                    tree.refresh(rootProject);

                    if (tree.isExpanded(rootProject)) {
                        tree.getNodeLoader().loadChildren(rootProject, true);
                    }
                }

                return;
            }
        }

        if (!nodeSettings.isShowHiddenFiles() && resource.getName().startsWith(".")) {
            return;
        }

        final Node node = MoreObjects.firstNonNull(getNode(resource.getLocation()), getParentNode(resource.getLocation()));

        if (node != null && tree.isExpanded(node)) {
            tree.getNodeLoader().loadChildren(node, true);
        }
    }

    @SuppressWarnings("unchecked")
    private void onResourceRemoved(final ResourceDelta delta) {
        final Tree tree = view.getTree();
        final Resource resource = delta.getResource();

        final Node node = getNode(resource.getLocation());

        if (node == null) {
            return;
        }

        if (resource.getLocation().segmentCount() == 1) {
                tree.getNodeStorage().remove(node);
            return;
        }

        final Node parentNode = getParentNode(resource.getLocation());

        if (parentNode == null) {
            return;
        }

        final Path parentLocation = ((ResourceNode)parentNode).getData().getLocation();

        if (resource.getLocation().parent().equals(parentLocation)) {
            tree.getNodeStorage().remove(node);
        } else {
            tree.getNodeStorage().remove(node);

            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    tree.getNodeLoader().loadChildren(parentNode, true);
                }
            });
        }
    }

    private boolean isNodeServesLocation(Node node, Path location) {
        return node instanceof ResourceNode && ((ResourceNode)node).getData().getLocation().equals(location);
    }

    @SuppressWarnings("unchecked")
    private void onResourceUpdated(ResourceDelta delta) {
        final Tree tree = view.getTree();

        for (Node node : tree.getNodeStorage().getAll()) {
            if (node instanceof ResourceNode && ((ResourceNode)node).getData().getLocation().equals(delta.getResource().getLocation())) {
                final String oldId = tree.getNodeStorage().getKeyProvider().getKey(node);
                ((ResourceNode)node).setData(delta.getResource());
                tree.getNodeStorage().reIndexNode(oldId, node);
                tree.refresh(node);
                return;
            }
        }
    }

    @Override
    public void onMarkerChanged(MarkerChangedEvent event) {

    }

    @Override
    public void onSyntheticNodeUpdate(SyntheticNodeUpdateEvent event) {
        final Tree tree = getTree();
        final NodeDescriptor descriptor = tree.getNodeDescriptor(event.getNode());

        if (descriptor == null) {
            return;
        }

        if (descriptor.isLoaded()) {
            tree.getNodeLoader().loadChildren(event.getNode(), false);
        }
    }

    public Tree getTree() {
        return view.getTree();
    }

    /** {@inheritDoc} */
    @Override
    public View getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getTitle() {
        return locale.projectExplorerButtonTitle();
    }

    /** {@inheritDoc} */
    @Override
    public SVGResource getTitleImage() {
        return resources.projectExplorerPartIcon();
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public String getTitleToolTip() {
        return locale.projectExplorerPartTooltip();
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return PART_SIZE;
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /**
     * Activate "Go Into" mode on specified node if.
     * Node should support this mode. See {@link Node#supportGoInto()}.
     *
     * @param node
     *         node which should be activated in "Go Into" mode
     */
    @Deprecated
    public void goInto(Node node) {
        view.setGoIntoModeOn(node);
    }

    /**
     * Get "Go Into" state on current tree.
     *
     * @return true - if "Go Into" mode has been activated.
     */
    @Deprecated
    public boolean isGoIntoActivated() {
        return view.isGoIntoActivated();
    }

    /**
     * Collapse all non-leaf nodes.
     */
    @Deprecated
    public void collapseAll() {
        view.collapseAll();
    }

    /**
     * Configure tree to show or hide files that starts with ".", e.g. hidden files.
     *
     * @param show
     *         true - if those files should be shown, otherwise - false
     */
    @Deprecated
    public void showHiddenFiles(boolean show) {
        hiddenFilesAreShown = show;
        view.showHiddenFilesForAllExpandedNodes(show);
    }

    /**
     * Retrieve status of showing hidden files.
     *
     * @return true - if hidden files are shown, otherwise - false
     */
    @Deprecated
    public boolean isShowHiddenFiles() {
        return hiddenFilesAreShown;
    }

}
