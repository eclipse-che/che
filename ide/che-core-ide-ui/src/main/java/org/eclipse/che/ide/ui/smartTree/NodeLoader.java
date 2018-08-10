/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.smartTree;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparingInt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ui.smartTree.Tree.Joint;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;
import org.eclipse.che.ide.ui.smartTree.event.BeforeLoadEvent;
import org.eclipse.che.ide.ui.smartTree.event.CancellableEvent;
import org.eclipse.che.ide.ui.smartTree.event.LoadEvent;
import org.eclipse.che.ide.ui.smartTree.event.LoadExceptionEvent;
import org.eclipse.che.ide.ui.smartTree.event.LoaderHandler;
import org.eclipse.che.ide.ui.smartTree.event.PostLoadEvent;
import org.eclipse.che.ide.ui.smartTree.handler.GroupingHandlerRegistration;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Class that perform loading node children. May transform nodes if ones passed set of node
 * interceptors.
 *
 * @author Vlad Zhukovskiy
 * @see NodeInterceptor
 */
public class NodeLoader implements LoaderHandler.HasLoaderHandlers {
  /**
   * Temporary storage for current requested nodes. When children have been loaded requested node
   * removes from temporary set.
   */
  Map<Node, Boolean> childRequested = new HashMap<>();

  /** Last processed node. Maybe used in general purpose. */
  private Node lastRequest;

  /**
   * Set of node interceptors. They need to modify children nodes before they will be set into
   * parent node.
   *
   * @see NodeInterceptor
   */
  private Set<NodeInterceptor> nodeInterceptors;

  private final Comparator<NodeInterceptor> priorityComparator =
      comparingInt(NodeInterceptor::getPriority);

  /**
   * When caching is on nodes will be loaded from cache if they exist otherwise nodes will be loaded
   * every time forcibly.
   */
  private boolean useCaching = false;

  private Tree tree;

  private GroupingHandlerRegistration handlerRegistration;

  private CTreeNodeLoaderHandler cTreeNodeLoaderHandler = new CTreeNodeLoaderHandler();

  /** Event handler for the loading events. */
  private class CTreeNodeLoaderHandler
      implements LoadEvent.LoadHandler,
          LoadExceptionEvent.LoadExceptionHandler,
          BeforeLoadEvent.BeforeLoadHandler {
    @Override
    public void onLoad(final LoadEvent event) {
      Node parent = event.getRequestedNode();
      tree.getView().onLoadChange(tree.getNodeDescriptor(parent), false);

      // remove joint element if non-leaf node doesn't have any children
      if (!parent.isLeaf() && event.getReceivedNodes().isEmpty()) {
        tree.getView().onJointChange(tree.getNodeDescriptor(parent), Joint.EXPANDED);
      }

      NodeDescriptor requested = tree.getNodeDescriptor(parent);

      if (requested == null) {
        // smth happened, that requested node isn't registered in storage
        Log.error(this.getClass(), "Requested node not found.");
        return;
      }

      requested.setLoading(false);

      // search node which has been removed from server to remove them from the tree
      List<NodeDescriptor> removedNodes = findRemovedNodes(requested, event.getReceivedNodes());

      // now search new nodes to add then into the tree
      List<Node> newNodes = findNewNodes(requested, event.getReceivedNodes());

      if (removedNodes.isEmpty() && newNodes.equals(event.getReceivedNodes())) {
        tree.getNodeStorage().replaceChildren(parent, newNodes);
      } else {
        for (NodeDescriptor removed : removedNodes) {
          if (!tree.getNodeStorage().remove(removed.getNode())) {
            Log.info(this.getClass(), "Failed to remove node: " + removed.getNode().getName());
          }
        }

        for (Node newNode : newNodes) {
          tree.getNodeStorage().add(parent, newNode);
        }
      }

      // Iterate on nested descendants to make additional load request
      if (Boolean.TRUE.equals(childRequested.remove(parent))) {
        for (Node node : tree.getNodeStorage().getChildren(parent)) {
          if (tree.isExpanded(node) && !tree.getNodeDescriptor(node).getChildren().isEmpty()) {
            loadChildren(node, true);
          }
        }
      }

      fireEvent(new PostLoadEvent(event.getRequestedNode(), event.getReceivedNodes()));
    }

    @Override
    public void onLoadException(LoadExceptionEvent event) {
      final Node node = event.getRequestedNode();

      checkNotNull(node, "Null node occurred");

      final NodeDescriptor requested = tree.getNodeDescriptor(node);

      if (requested == null) {
        return;
      }

      tree.getView().onLoadChange(requested, false);
      requested.setLoading(false);
    }

    @Override
    public void onBeforeLoad(BeforeLoadEvent event) {
      NodeDescriptor requested = tree.getNodeDescriptor(event.getRequestedNode());

      if (requested == null) {
        return;
      }

      requested.setLoading(true);
    }
  }

  private List<Node> findNewNodes(NodeDescriptor parent, final List<Node> loadedChildren) {
    final List<NodeDescriptor> existed = parent.getChildren();

    if (existed == null || existed.isEmpty()) {
      return loadedChildren;
    }

    Iterable<Node> newItems =
        Iterables.filter(
            loadedChildren,
            loadedChild -> {
              for (NodeDescriptor nodeDescriptor : existed) {
                if (nodeDescriptor.getNode().equals(loadedChild)) {
                  return false;
                }
              }
              return true;
            });

    return Lists.newArrayList(newItems);
  }

  private List<NodeDescriptor> findRemovedNodes(
      NodeDescriptor parent, final List<Node> loadedChildren) {
    List<NodeDescriptor> existed = parent.getChildren();

    if (existed == null || existed.isEmpty()) {
      return Collections.emptyList();
    }

    Iterable<NodeDescriptor> removedItems =
        Iterables.filter(
            existed,
            existedChild -> {
              boolean found = false;
              for (Node loadedChild : loadedChildren) {
                if (existedChild.getNode().equals(loadedChild)) {
                  found = true;
                }
              }
              return !found;
            });

    return Lists.newArrayList(removedItems);
  }

  private SimpleEventBus eventBus;

  /** Creates a new tree node value provider instance. */
  public NodeLoader() {
    this(null);
  }

  /**
   * Creates a new tree node value provider instance.
   *
   * @param nodeInterceptors set of {@link NodeInterceptor}
   */
  public NodeLoader(@Nullable Set<NodeInterceptor> nodeInterceptors) {
    this.nodeInterceptors = new HashSet<>();
    if (nodeInterceptors != null) {
      this.nodeInterceptors.addAll(nodeInterceptors);
    }
  }

  /**
   * Checks whether node has children or not. This method may allow tree to determine whether to
   * show expand control near non-leaf node.
   *
   * @param parent node
   * @return true if node has children, otherwise false
   */
  public boolean mayHaveChildren(@NotNull Node parent) {
    return !parent.isLeaf();
  }

  /**
   * Check if node has children. This method make a request to server to read children count.
   *
   * @param node node
   * @return true if node has children, otherwise false
   */
  public Promise<Boolean> hasChildren(@NotNull Node node) {
    return node.getChildren(false).thenPromise(children -> Promises.resolve(!children.isEmpty()));
  }

  /**
   * Initiates a load request for the parent's children.
   *
   * @param parent parent node
   * @return true if the load was requested, otherwise false
   */
  public boolean loadChildren(@NotNull Node parent) {
    return loadChildren(parent, false);
  }

  public boolean loadChildren(Node parent, boolean reloadExpandedChild) {
    // we don't need to load children for leaf nodes
    if (parent.isLeaf()) {
      return false;
    }

    if (childRequested.containsKey(parent)) {
      return false;
    }

    childRequested.put(parent, reloadExpandedChild);
    return doLoad(parent);
  }

  /**
   * Called when children haven't been successfully loaded. Also fire {@link
   * org.eclipse.che.ide.ui.smartTree.event.LoadExceptionEvent} event.
   *
   * @param parent parent node, children which haven't been loaded
   * @return instance of {@link org.eclipse.che.api.promises.client.Operation} which contains
   *     promise with error
   */
  @NotNull
  private Operation<PromiseError> onLoadFailure(@NotNull final Node parent) {
    return error -> {
      childRequested.remove(parent);
      fireEvent(new LoadExceptionEvent(parent, error.getCause()));
    };
  }

  /**
   * Called when children have been successfully received. Also fire {@link
   * org.eclipse.che.ide.ui.smartTree.event.LoadEvent} event.
   *
   * @param parent parent node, children which have been loaded
   */
  private void onLoadSuccess(@NotNull final Node parent, List<Node> children) {
    fireEvent(new LoadEvent(parent, children));
  }

  /**
   * Initiates a load request for the parent's children. Also fire {@link
   * org.eclipse.che.ide.ui.smartTree.event.BeforeLoadEvent} event.
   *
   * @param parent parent node
   * @return true if load was requested, otherwise false
   */
  private boolean doLoad(@NotNull final Node parent) {
    if (fireEvent(new BeforeLoadEvent(parent))) {
      lastRequest = parent;

      parent
          .getChildren(!useCaching)
          .then(interceptChildren(parent))
          .catchError(onLoadFailure(parent));
      return true;
    }

    return false;
  }

  /**
   * Fires the given event.
   *
   * @param event event to fire
   * @return true if the specified event wasn't cancelled, otherwise false
   */
  private boolean fireEvent(@NotNull GwtEvent<?> event) {
    if (eventBus != null) {
      eventBus.fireEvent(event);
    }
    if (event instanceof CancellableEvent) {
      return !((CancellableEvent) event).isCancelled();
    }
    return true;
  }

  /**
   * Returns the last processed node.
   *
   * @return last processed node
   */
  @Nullable
  public Node getLastRequest() {
    return lastRequest;
  }

  /**
   * Perform iteration on every node interceptor, passing to ones the list of children to filter
   * them before inserting into parent node.
   *
   * @param parent parent node
   * @return instance of {@link org.eclipse.che.api.promises.client.Function} with promise that
   *     contains list of intercepted children
   */
  @NotNull
  private Operation<List<Node>> interceptChildren(@NotNull final Node parent) {
    return children -> {
      // In case of nodeInterceptors is empty we still need to call iterate(...)
      // in  order to call set parent on children and call onLoadSuccess(...)

      LinkedList<NodeInterceptor> sortedByPriorityQueue = new LinkedList<>(nodeInterceptors);
      sortedByPriorityQueue.sort(priorityComparator);

      iterate(sortedByPriorityQueue, parent, children);
    };
  }

  private void iterate(
      final LinkedList<NodeInterceptor> deque, final Node parent, final List<Node> children) {
    if (deque.isEmpty()) {
      for (Node child : children) {
        child.setParent(parent);
      }
      onLoadSuccess(parent, children);
      return;
    }

    NodeInterceptor interceptor = deque.poll();

    interceptor
        .intercept(parent, children)
        .then(
            childrenList -> {
              iterate(deque, parent, childrenList);
            });
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public HandlerRegistration addBeforeLoadHandler(
      @NotNull BeforeLoadEvent.BeforeLoadHandler handler) {
    return addHandler(BeforeLoadEvent.getType(), handler);
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public HandlerRegistration addLoadExceptionHandler(
      @NotNull LoadExceptionEvent.LoadExceptionHandler handler) {
    return addHandler(LoadExceptionEvent.getType(), handler);
  }

  @Override
  public HandlerRegistration addPostLoadHandler(PostLoadEvent.PostLoadHandler handler) {
    return addHandler(PostLoadEvent.getType(), handler);
  }

  /** {@inheritDoc} */
  @Override
  public HandlerRegistration addLoaderHandler(LoaderHandler handler) {
    GroupingHandlerRegistration group = new GroupingHandlerRegistration();
    group.add(addHandler(BeforeLoadEvent.getType(), handler));
    group.add(addHandler(LoadEvent.getType(), handler));
    group.add(addHandler(LoadExceptionEvent.getType(), handler));
    group.add(addHandler(PostLoadEvent.getType(), handler));
    return group;
  }

  /** {@inheritDoc} */
  @Override
  public HandlerRegistration addLoadHandler(LoadEvent.LoadHandler handler) {
    return addHandler(LoadEvent.getType(), handler);
  }

  @NotNull
  protected <H extends EventHandler> HandlerRegistration addHandler(
      @NotNull GwtEvent.Type<H> type, @NotNull H handler) {
    if (eventBus == null) {
      eventBus = new SimpleEventBus();
    }
    return eventBus.addHandler(type, handler);
  }

  /**
   * Indicates that node value provider uses caching. It means that if node already has children
   * they will be returned to the tree, otherwise children nodes will be forcibly loaded from the
   * server.
   *
   * @return true if value provider uses caching, otherwise false
   */
  public boolean isUseCaching() {
    return useCaching;
  }

  /**
   * Set cache using.
   *
   * @param useCaching true if value provider should use caching, otherwise false
   */
  public void setUseCaching(boolean useCaching) {
    this.useCaching = useCaching;
  }

  /**
   * Return set of node interceptors.
   *
   * @return node interceptors list
   */
  public Set<NodeInterceptor> getNodeInterceptors() {
    return nodeInterceptors;
  }

  /**
   * Binds tree to current node loader.
   *
   * @param tree tree instance
   */
  public void bindTree(Tree tree) {
    if (this.tree != null) {
      handlerRegistration.removeHandler();
    }

    this.tree = tree;

    if (tree != null) {
      if (handlerRegistration == null) {
        handlerRegistration = new GroupingHandlerRegistration();
      }

      handlerRegistration.add(addBeforeLoadHandler(cTreeNodeLoaderHandler));
      handlerRegistration.add(addLoadHandler(cTreeNodeLoaderHandler));
      handlerRegistration.add(addLoadExceptionHandler(cTreeNodeLoaderHandler));
    }
  }

  public boolean isBusy() {
    return !childRequested.isEmpty();
  }
}
