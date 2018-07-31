/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.explorer.project;

import static java.util.Arrays.copyOf;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Search node handler, perform searching specified node in the tree by storable value. For example
 * if user passes "/project/path/to/file" then this node handler will check opened root nodes and if
 * it contains project node with path "/project" then it will search children by path
 * "path/to/file".
 *
 * @author Vlad Zhukovskiy
 * @see RevealResourceEvent
 * @since 4.4.0
 */
@Singleton
public class TreeResourceRevealer {

  private Promise<Void> queue;

  private Tree tree;

  private Node[] toSelect = null;
  private boolean isFocusRequired = true;

  private DelayedTask selectTask =
      new DelayedTask() {
        @Override
        public void onExecute() {
          if (toSelect != null) {
            final Node[] copy = copyOf(toSelect, toSelect.length);

            if (copy.length == 1) {
              tree.getSelectionModel().select(copy[0], false);
              tree.scrollIntoView(copy[0], isFocusRequired);
            }

            toSelect = null;
            isFocusRequired = true;
          }
        }
      };

  @Inject
  public TreeResourceRevealer(
      ProjectExplorerView projectExplorer, EventBus eventBus, PromiseProvider promises) {
    this.tree = projectExplorer.getTree();

    queue = promises.resolve(null);

    eventBus.addHandler(
        RevealResourceEvent.getType(),
        event ->
            queue.thenPromise(
                ignored ->
                    reveal(
                            event.getLocation(),
                            event.isSelectionRequired(),
                            event.isFocusRequired())
                        .catchError((Function<PromiseError, Void>) arg -> null)));
  }

  /**
   * Search node in the project explorer tree by storable path.
   *
   * @param path path to node
   * @return promise object with found node or promise error if node wasn't found
   */
  public Promise<Node> reveal(final Path path) {
    return reveal(path, true);
  }

  /**
   * Search node in the project explorer tree by storable path.
   *
   * @param path path to node
   * @param select select node after reveal
   * @return promise object with found node or promise error if node wasn't found
   */
  public Promise<Node> reveal(final Path path, final boolean select) {
    return reveal(path, select, select);
  }

  /**
   * Search node in the project explorer tree by storable path.
   *
   * @param path path to node
   * @param select select node after reveal
   * @param isFocusRequired whether tree should take focus after reveal
   * @return promise object with found node or promise error if node wasn't found
   */
  public Promise<Node> reveal(
      final Path path, final boolean select, final boolean isFocusRequired) {
    return queue.thenPromise(
        ignored ->
            createFromAsyncRequest(callback -> reveal(path, select, isFocusRequired, callback)));
  }

  protected void reveal(
      final Path path,
      final boolean select,
      final boolean isFocusRequired,
      final AsyncCallback<Node> callback) {
    if (path == null) {
      callback.onFailure(new IllegalArgumentException("Invalid search path"));
    }

    Scheduler.get()
        .scheduleFixedDelay(
            () -> {
              if (tree.getNodeLoader().isBusy()) {
                return true;
              }

              ResourceNode nodeByPath = getNodeByPath(path);
              if (nodeByPath != null) {

                if (select) {
                  TreeResourceRevealer.this.isFocusRequired = isFocusRequired;

                  if (toSelect == null) {
                    toSelect = new Node[] {nodeByPath};
                  } else {
                    final int index = toSelect.length;
                    toSelect = copyOf(toSelect, index + 1);
                    toSelect[index] = nodeByPath;
                  }

                  selectTask.delay(200);
                }

                callback.onSuccess(nodeByPath);
                return false;
              }

              ResourceNode root = getRootResourceNode(path);

              if (root == null) {
                callback.onFailure(new IllegalStateException());
                return false;
              }

              if (root.getData().getLocation().equals(path)) {
                callback.onSuccess(root);
                return false;
              }

              expandToPath(root, path, select, isFocusRequired)
                  .then(callback::onSuccess)
                  .catchError(
                      arg -> {
                        callback.onFailure(arg.getCause());
                      });

              return false;
            },
            500);
  }

  private Promise<ResourceNode> expandToPath(
      final ResourceNode root,
      final Path path,
      final boolean select,
      final boolean isFocusRequired) {
    return createFromAsyncRequest(
        callback -> expand(root, path, select, isFocusRequired, callback));
  }

  protected void expand(
      final ResourceNode parent,
      final Path segment,
      final boolean select,
      final boolean isFocusRequired,
      final AsyncCallback<ResourceNode> callback) {

    if (parent.getData().getLocation().equals(segment)) {
      if (select) {
        this.isFocusRequired = isFocusRequired;

        if (toSelect == null) {
          toSelect = new Node[] {parent};
        } else {
          final int index = toSelect.length;
          toSelect = copyOf(toSelect, index + 1);
          toSelect[index] = parent;
        }

        selectTask.delay(200);
      }

      callback.onSuccess(parent);
      return;
    }

    final HandlerRegistration[] handler = new HandlerRegistration[1];

    handler[0] =
        tree.getNodeLoader()
            .addPostLoadHandler(
                event -> {
                  if (!event.getRequestedNode().equals(parent)) {
                    return;
                  }

                  if (handler[0] != null) {
                    // Do not remove the handler immediately to not to lose 'loadChildren' events
                    // that were fired after the children request.
                    new Timer() {
                      @Override
                      public void run() {
                        handler[0].removeHandler();
                      }
                    }.schedule(2000);
                  }
                  final List<Node> children =
                      tree.getNodeStorage().getChildren(event.getRequestedNode());

                  for (Node child : children) {
                    if (child instanceof ResourceNode
                        && ((ResourceNode) child).getData().getLocation().isPrefixOf(segment)) {
                      expand((ResourceNode) child, segment, select, isFocusRequired, callback);
                      return;
                    }
                  }

                  callback.onFailure(new IllegalStateException("Not found"));
                });

    tree.getNodeLoader().loadChildren(parent);
  }

  private ResourceNode getNodeByPath(Path path) {
    return (ResourceNode)
        tree.getNodeStorage()
            .getAll()
            .stream()
            .filter(node -> node instanceof ResourceNode)
            .filter(node -> ((ResourceNode) node).getData().getLocation().equals(path))
            .findFirst()
            .orElse(null);
  }

  private ResourceNode getRootResourceNode(Path path) {
    return (ResourceNode)
        tree.getRootNodes()
            .stream()
            .filter(node -> node instanceof ResourceNode)
            .filter(node -> ((ResourceNode) node).getData().getLocation().isPrefixOf(path))
            .findFirst()
            .orElse(null);
  }
}
