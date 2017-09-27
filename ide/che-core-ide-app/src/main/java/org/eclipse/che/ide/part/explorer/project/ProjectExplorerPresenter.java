/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.explorer.project;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto.Type.START;
import static org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto.Type.STOP;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.TreeExpander;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.extension.ExtensionsInitializedEvent;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
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
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerView.ActionDelegate;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.project.node.SyntheticNodeUpdateEvent;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.NodeDescriptor;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.providers.DynaObject;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Project explorer presenter. Handle basic logic to control project tree display.
 *
 * @author Vlad Zhukovskiy
 * @author Dmitry Shnurenko
 */
@Singleton
@DynaObject
public class ProjectExplorerPresenter extends BasePresenter
    implements ActionDelegate,
        ResourceChangedHandler,
        MarkerChangedHandler,
        SyntheticNodeUpdateEvent.SyntheticNodeUpdateHandler {
  private static final int PART_SIZE = 500;
  private final ProjectExplorerView view;
  private final EventBus eventBus;
  private final ResourceNode.NodeFactory nodeFactory;
  private final SettingsProvider settingsProvider;
  private final CoreLocalizationConstant locale;
  private final Resources resources;
  private final TreeExpander treeExpander;
  private final AppContext appContext;
  private final RequestTransmitter requestTransmitter;
  private NotificationManager notificationManager;
  private final DtoFactory dtoFactory;
  private UpdateTask updateTask = new UpdateTask();
  private Set<Path> expandQueue = new HashSet<>();
  private boolean hiddenFilesAreShown;

  @Inject
  public ProjectExplorerPresenter(
      ProjectExplorerView view,
      EventBus eventBus,
      CoreLocalizationConstant locale,
      Resources resources,
      ResourceNode.NodeFactory nodeFactory,
      SettingsProvider settingsProvider,
      AppContext appContext,
      Provider<WorkspaceAgent> workspaceAgentProvider,
      RequestTransmitter requestTransmitter,
      NotificationManager notificationManager,
      DtoFactory dtoFactory) {
    this.view = view;
    this.eventBus = eventBus;
    this.nodeFactory = nodeFactory;
    this.settingsProvider = settingsProvider;
    this.locale = locale;
    this.resources = resources;
    this.appContext = appContext;
    this.requestTransmitter = requestTransmitter;
    this.notificationManager = notificationManager;
    this.dtoFactory = dtoFactory;
    this.view.setDelegate(this);

    eventBus.addHandler(ResourceChangedEvent.getType(), this);
    eventBus.addHandler(MarkerChangedEvent.getType(), this);
    eventBus.addHandler(SyntheticNodeUpdateEvent.getType(), this);
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, event -> getTree().getNodeStorage().clear());

    view.getTree()
        .getSelectionModel()
        .addSelectionChangedHandler(event -> setSelection(new Selection<>(event.getSelection())));

    view.getTree()
        .addBeforeExpandHandler(
            event -> {
              NodeDescriptor nodeDescriptor = view.getTree().getNodeDescriptor(event.getNode());

              if (event.getNode() instanceof SyntheticNode
                  && nodeDescriptor != null
                  && nodeDescriptor.isExpandDeep()) {
                event.setCancelled(true);
              }
            });

    view.getTree()
        .getNodeLoader()
        .addPostLoadHandler(
            event -> {
              for (Node node : event.getReceivedNodes()) {
                if (node instanceof ResourceNode
                    && expandQueue.remove(((ResourceNode) node).getData().getLocation())) {
                  view.getTree().setExpanded(node, true);
                }
              }
            });

    treeExpander = new ProjectExplorerTreeExpander(view.getTree(), appContext);

    registerNative();

    // when ide has already initialized, then we force set focus to the current part
    eventBus.addHandler(
        ExtensionsInitializedEvent.getType(),
        event -> partStack.setActivePart(ProjectExplorerPresenter.this));

    Scheduler.get()
        .scheduleDeferred(
            () -> {
              PartStack partStack =
                  checkNotNull(
                      workspaceAgentProvider.get().getPartStack(PartStackType.NAVIGATION),
                      "Navigation part stack should not be a null");
              partStack.addPart(ProjectExplorerPresenter.this);
              partStack.setActivePart(ProjectExplorerPresenter.this);
            });
  }

  public void addSelectionHandler(SelectionHandler<Node> handler) {
    getTree().getSelectionModel().addSelectionHandler(handler);
  }

  @Inject
  public void initFileWatchers() {
    final String endpointId = "ws-agent";
    final String method = "track/project-tree";

    getTree()
        .addExpandHandler(
            event -> {
              Node node = event.getNode();

              if (node instanceof ResourceNode) {
                Resource data = ((ResourceNode) node).getData();
                requestTransmitter
                    .newRequest()
                    .endpointId(endpointId)
                    .methodName(method)
                    .paramsAsDto(
                        dtoFactory
                            .createDto(ProjectTreeTrackingOperationDto.class)
                            .withPath(data.getLocation().toString())
                            .withType(START))
                    .sendAndSkipResult();
              }
            });

    getTree()
        .addCollapseHandler(
            event -> {
              Node node = event.getNode();

              if (node instanceof ResourceNode) {
                Resource data = ((ResourceNode) node).getData();
                requestTransmitter
                    .newRequest()
                    .endpointId(endpointId)
                    .methodName(method)
                    .paramsAsDto(
                        dtoFactory
                            .createDto(ProjectTreeTrackingOperationDto.class)
                            .withPath(data.getLocation().toString())
                            .withType(STOP))
                    .sendAndSkipResult();
              }
            });
  }

  /* Expose Project Explorer's internal API to the world, to allow automated Selenium scripts expand all projects tree. */
  private native void registerNative() /*-{
        var that = this;

        var ProjectExplorer = {};

        ProjectExplorer.expandAll = $entry(function () {
            that.@org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter::doExpand()();
        });

        ProjectExplorer.collapseAll = $entry(function () {
            that.@org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter::doCollapse()();
        });

        ProjectExplorer.reveal = $entry(function (path) {
            that.@org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter::doReveal(*)(path);
        });

        ProjectExplorer.refresh = $entry(function () {
            that.@org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter::doRefresh()();
        });

        $wnd.IDE.ProjectExplorer = ProjectExplorer;
    }-*/;

  private void doExpand() {
    if (treeExpander.isExpandEnabled()) {
      treeExpander.expandTree();
    }
  }

  private void doCollapse() {
    if (treeExpander.isCollapseEnabled()) {
      treeExpander.collapseTree();
    }
  }

  private void doReveal(String path) {
    eventBus.fireEvent(new RevealResourceEvent(Path.valueOf(path)));
  }

  private void doRefresh() {
    appContext.getWorkspaceRoot().synchronize();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onResourceChanged(ResourceChangedEvent event) {
    final Tree tree = view.getTree();
    final ResourceDelta delta = event.getDelta();
    final Resource resource = delta.getResource();
    final NodeSettings nodeSettings = settingsProvider.getSettings();

    // process root projects, they have only one segment in path
    if (resource.getLocation().segmentCount() == 1) {
      if (delta.getKind() == ADDED) {
        if ((delta.getFlags() & (MOVED_FROM | MOVED_TO)) != 0) {
          Node node = getNode(delta.getFromPath());
          if (node != null) {
            boolean expanded = tree.isExpanded(node);

            tree.getNodeStorage().remove(node);

            node = nodeFactory.newContainerNode((Container) resource, nodeSettings);
            tree.getNodeStorage().add(node);
            if (expanded) {
              tree.setExpanded(node, true);
            }
          }
        } else if (getNode(resource.getLocation()) == null) {
          tree.getNodeStorage()
              .add(nodeFactory.newContainerNode((Container) resource, nodeSettings));
        }
      } else if (delta.getKind() == REMOVED) {
        Node node = getNode(resource.getLocation());

        if (node != null) {
          tree.getNodeStorage().remove(node);
          if (resource.isProject()) {
            notificationManager.notify(
                locale.projectRemoved(node.getName()), SUCCESS, NOT_EMERGE_MODE);
          }
        }
      } else if (delta.getKind() == UPDATED) {
        for (Node node : tree.getNodeStorage().getAll()) {
          if (node instanceof ResourceNode
              && ((ResourceNode) node)
                  .getData()
                  .getLocation()
                  .equals(delta.getResource().getLocation())) {
            final String oldId = tree.getNodeStorage().getKeyProvider().getKey(node);
            ((ResourceNode) node).setData(delta.getResource());
            tree.getNodeStorage().reIndexNode(oldId, node);
            tree.refresh(node);
            updateTask.submit(delta.getResource().getLocation());
          }
        }
      }
    } else {

      if ((delta.getFlags() & (MOVED_FROM | MOVED_TO)) != 0) {
        final Node node = getNode(delta.getFromPath());

        if (node != null && tree.isExpanded(node)) {
          expandQueue.add(delta.getToPath());
        }
      }

      final Node node = getNode(delta.getResource().getLocation());
      if (node != null) {

        if (node instanceof ResourceNode && !delta.getResource().isProject()) {
          ((ResourceNode) node).setData(delta.getResource());
        }

        if (node instanceof HasPresentation) {
          tree.refresh(node);
        }
      }

      updateTask.submit(resource.getLocation());

      if (delta.getFromPath() != null) {
        updateTask.submit(delta.getFromPath());
      }
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

  private boolean isNodeServesLocation(Node node, Path location) {
    return node instanceof ResourceNode
        && ((ResourceNode) node).getData().getLocation().equals(location);
  }

  @Override
  public void onMarkerChanged(MarkerChangedEvent event) {
    final Tree tree = view.getTree();
    for (Node node : tree.getNodeStorage().getAll()) {
      if (node instanceof ResourceNode
          && ((ResourceNode) node)
              .getData()
              .getLocation()
              .equals(event.getResource().getLocation())) {
        tree.refresh(node);
      }
    }
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
   * Activate "Go Into" mode on specified node if. Node should support this mode. See {@link
   * Node#supportGoInto()}.
   *
   * @param node node which should be activated in "Go Into" mode
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

  /** Collapse all non-leaf nodes. */
  @Deprecated
  public void collapseAll() {
    view.collapseAll();
  }

  /**
   * Configure tree to show or hide files that starts with ".", e.g. hidden files.
   *
   * @param show true - if those files should be shown, otherwise - false
   */
  @Deprecated
  public void showHiddenFiles(boolean show) {
    hiddenFilesAreShown = show;
    settingsProvider.getSettings().setShowHiddenFiles(show);
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

  private class UpdateTask extends DelayedTask {

    private Set<Path> toRefresh = new HashSet<>();

    public void submit(Path path) {
      toRefresh.add(path.uptoSegment(1));

      delay(500);
    }

    @Override
    public void onExecute() {
      Scheduler.get()
          .scheduleDeferred(
              () -> {
                if (view.getTree().getNodeLoader().isBusy()) {
                  delay(500);

                  return;
                }

                final Set<Path> updateQueue = Sets.newHashSet(toRefresh);
                toRefresh.clear();

                for (Path path : updateQueue) {
                  final Node node = getNode(path);

                  if (node == null) {
                    continue;
                  }

                  if (getTree().isExpanded(node)) {
                    view.getTree().getNodeLoader().loadChildren(node, true);
                  }
                }
              });
    }
  }
}
