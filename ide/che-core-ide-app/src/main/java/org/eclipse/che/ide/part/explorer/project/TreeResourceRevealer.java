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
package org.eclipse.che.ide.part.explorer.project;

import com.google.common.base.Optional;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent.RevealResourceHandler;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.event.PostLoadEvent;
import org.eclipse.che.ide.ui.smartTree.event.PostLoadEvent.PostLoadHandler;

import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.util.Arrays.copyOf;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

/**
 * Search node handler, perform searching specified node in the tree by storable value.
 * For example if user passes "/project/path/to/file" then this node handler will check
 * opened root nodes and if it contains project node with path "/project" then it will
 * search children by path "path/to/file".
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

    private DelayedTask selectTask = new DelayedTask() {
        @Override
        public void onExecute() {
            if (toSelect != null) {
                final Node[] copy = copyOf(toSelect, toSelect.length);

                if (copy.length == 1) {
                    tree.getSelectionModel().select(copy[0], false);
                    tree.scrollIntoView(copy[0]);
                }

                toSelect = null;
            }
        }
    };

    @Inject
    public TreeResourceRevealer(ProjectExplorerView projectExplorer, EventBus eventBus, PromiseProvider promises) {
        this.tree = projectExplorer.getTree();

        queue = promises.resolve(null);

        eventBus.addHandler(RevealResourceEvent.getType(), new RevealResourceHandler() {
            @Override
            public void onRevealResource(final RevealResourceEvent event) {
                queue.thenPromise(new Function<Void, Promise<Void>>() {
                    @Override
                    public Promise<Void> apply(Void ignored) throws FunctionException {
                        return reveal(event.getLocation()).catchError(new Function<PromiseError, Void>() {
                            @Override
                            public Void apply(PromiseError arg) throws FunctionException {
                                return null;
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Search node in the project explorer tree by storable path.
     *
     * @param path
     *         path to node
     * @return promise object with found node or promise error if node wasn't found
     */
    public Promise<Node> reveal(final Path path) {
        return reveal(path, true);
    }

    /**
     * Search node in the project explorer tree by storable path.
     *
     * @param path
     *         path to node
     * @param select
     *         select node after reveal
     * @return promise object with found node or promise error if node wasn't found
     */
    public Promise<Node> reveal(final Path path, final boolean select) {

        return queue.thenPromise(new Function<Void, Promise<Node>>() {
            @Override
            public Promise<Node> apply(Void ignored) throws FunctionException {
                return createFromAsyncRequest(new RequestCall<Node>() {
                    @Override
                    public void makeCall(AsyncCallback<Node> callback) {
                        reveal(path, select, callback);
                    }
                });
            }
        });
    }

    protected void reveal(final Path path, final boolean select, final AsyncCallback<Node> callback) {
        if (path == null) {
            callback.onFailure(new IllegalArgumentException("Invalid search path"));
        }

        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                if (tree.getNodeLoader().isBusy()) {
                    return true;
                }

                final Optional<ResourceNode> optRoot = getRootResourceNode(path);

                if (!optRoot.isPresent()) {
                    callback.onFailure(new IllegalStateException());
                    return false;
                }

                final ResourceNode root = optRoot.get();

                if (root.getData().getLocation().equals(path)) {
                    callback.onSuccess(root);
                    return false;
                }

                expandToPath(root, path, select).then(new Operation<ResourceNode>() {
                    @Override
                    public void apply(ResourceNode node) throws OperationException {
                        callback.onSuccess(node);
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        callback.onFailure(arg.getCause());
                    }
                });

                return false;
            }
        }, 500);

    }

    private Promise<ResourceNode> expandToPath(final ResourceNode root, final Path path, final boolean select) {
        return createFromAsyncRequest(new RequestCall<ResourceNode>() {
            @Override
            public void makeCall(final AsyncCallback<ResourceNode> callback) {
                expand(root, path, select, callback);
            }
        });
    }

    protected void expand(final ResourceNode parent, final Path segment, final boolean select, final AsyncCallback<ResourceNode> callback) {

        if (parent.getData().getLocation().equals(segment)) {
            if (select) {
                if (toSelect == null) {
                    toSelect = new Node[]{parent};
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

        handler[0] = tree.getNodeLoader().addPostLoadHandler(new PostLoadHandler() {
            @Override
            public void onPostLoad(PostLoadEvent event) {
                if (!event.getRequestedNode().equals(parent)) {
                    return;
                }

                if (handler[0] != null) {
                    handler[0].removeHandler();
                }

                final List<Node> children = tree.getNodeStorage().getChildren(event.getRequestedNode());

                for (Node child : children) {
                    if (child instanceof ResourceNode && ((ResourceNode)child).getData().getLocation().isPrefixOf(segment)) {
                        expand((ResourceNode)child, segment, select, callback);
                        return;
                    }
                }

                callback.onFailure(new IllegalStateException("Not found"));
            }
        });

        tree.getNodeLoader().loadChildren(parent);
    }

    private Optional<ResourceNode> getRootResourceNode(Path path) {
        for (Node root : tree.getRootNodes()) {
            if (!(root instanceof ResourceNode)) {
                continue;
            }

            final Path rootPath = ((ResourceNode)root).getData().getLocation();

            if (!rootPath.isPrefixOf(path)) {
                continue;
            }

            return of((ResourceNode)root);
        }

        return absent();
    }
}
