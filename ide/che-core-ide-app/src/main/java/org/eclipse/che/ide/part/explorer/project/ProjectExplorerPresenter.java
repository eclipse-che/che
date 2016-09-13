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

import com.google.common.base.Optional;
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
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.NodeDescriptor;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent.SelectionChangedHandler;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
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
        final Tree tree = view.getTree();
        final ResourceDelta delta = event.getDelta();
        final Resource resource = delta.getResource();

        if (delta.getKind() == UPDATED) {
            for (Node node : tree.getNodeStorage().getAll()) {
                if (node instanceof ResourceNode && ((ResourceNode)node).getData().getLocation().equals(delta.getResource().getLocation())) {
                    final String oldId = tree.getNodeStorage().getKeyProvider().getKey(node);
                    ((ResourceNode)node).setData(delta.getResource());
                    tree.getNodeStorage().reIndexNode(oldId, node);
                    tree.refresh(node);

                    if (tree.isExpanded(node)) {
                        reloadNode(node);
                    }

                    return;
                }
            }
        }

        final NodeSettings nodeSettings = settingsProvider.getSettings();

        // process root projects, they have only one segment in path
        if (resource.getLocation().segmentCount() == 1) {
            if (delta.getKind() == ADDED) {
                tree.getNodeStorage().add(nodeFactory.newContainerNode((Container)resource, nodeSettings));
            } else if (delta.getKind() == REMOVED) {
                Node node = getNode(resource.getLocation());

                if (node != null) {
                    tree.getNodeStorage().remove(node);
                }
            }
        } else {
            final Optional<Container> parent = resource.getParent();

            if (parent.isPresent()) {
                final Container container = parent.get();
                final Node parentNode = firstNonNull(getNode(container.getLocation()), getParentNode(container.getLocation()));

                if (parentNode != null && tree.isExpanded(parentNode)) {
                    reloadNode(parentNode);
                }
            }

            // process movement
            if ((delta.getFlags() & (MOVED_FROM | MOVED_TO)) != 0) {
                final Node parentNode = getParentNode(delta.getFromPath());

                if (parentNode != null && tree.isExpanded(parentNode)) {
                    reloadNode(parentNode);
                }
            }
        }
    }

    private void reloadNode(final Node node) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                view.getTree().getNodeLoader().loadChildren(node, true);
            }
        });
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

    private boolean isNodeServesLocation(Node node, Path location) {
        return node instanceof ResourceNode && ((ResourceNode)node).getData().getLocation().equals(location);
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
