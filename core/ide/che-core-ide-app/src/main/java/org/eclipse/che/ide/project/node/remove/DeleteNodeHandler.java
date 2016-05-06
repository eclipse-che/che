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
package org.eclipse.che.ide.project.node.remove;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

/**
 * Helper class which allow to delete multiple nodes with user prompt.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class DeleteNodeHandler {

    private final CoreLocalizationConstant localization;
    private final DialogFactory            dialogFactory;

    @Inject
    public DeleteNodeHandler(CoreLocalizationConstant localization, DialogFactory dialogFactory) {
        this.localization = localization;
        this.dialogFactory = dialogFactory;
    }

    @NotNull
    public Promise<Void> delete(@NotNull ResourceBasedNode<?> node) {
        return delete(node, false);
    }

    @NotNull
    public Promise<Void> delete(@NotNull ResourceBasedNode<?> node, boolean needConfirmation) {
        return deleteAll(Collections.<ResourceBasedNode<?>>singletonList(node), needConfirmation);
    }

    @NotNull
    public Promise<Void> deleteAll(@NotNull List<ResourceBasedNode<?>> nodes) {
        return deleteAll(nodes, false);
    }

    @NotNull
    public Promise<Void> deleteAll(@NotNull List<ResourceBasedNode<?>> nodes, boolean needConfirmation) {

        if (nodes == null || nodes.isEmpty()) {
            return Promises.reject(JsPromiseError.create("Nodes shouldn't be empty"));
        }

        final List<ResourceBasedNode<?>> filteredNodes = filterDescendants(nodes);

        if (!needConfirmation) {
            Promise<Void> promise = Promises.resolve(null);
            return chainNodes(promise, filteredNodes.iterator());
        }

        List<ResourceBasedNode<?>> projects = Lists.newArrayList(Iterables.filter(filteredNodes, isProjectNode()));

        if (projects.isEmpty()) {
            //if no project were found in nodes list
            return promptUserToDelete(filteredNodes);
        } else if (projects.size() < filteredNodes.size()) {
            //inform user that we can't delete mixed list of the nodes
            return Promises.reject(JsPromiseError.create(localization.mixedProjectDeleteMessage()));
        } else {
            //delete only project nodes
            return promptUserToDelete(projects);
        }
    }

    @NotNull
    private Predicate<ResourceBasedNode<?>> isProjectNode() {
        return new Predicate<ResourceBasedNode<?>>() {
            @Override
            public boolean apply(ResourceBasedNode<?> node) {
                return node instanceof ProjectNode;
            }
        };
    }

    @NotNull
    private Promise<Void> promptUserToDelete(@NotNull final List<ResourceBasedNode<?>> nodes) {
        return createFromAsyncRequest(new RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                String warningMessage = generateWarningMessage(nodes);

                boolean anyDirectories = false;

                String directoryName = null;
                for (ResourceBasedNode<?> node : nodes) {
                    if (node instanceof FolderReferenceNode) {
                        anyDirectories = true;
                        directoryName = node.getName();
                        break;
                    }
                }

                if (anyDirectories) {
                    warningMessage += nodes.size() == 1 ? localization.deleteAllFilesAndSubdirectories(directoryName)
                                                        : localization.deleteFilesAndSubdirectoriesInTheSelectedDirectory();
                }

                dialogFactory.createConfirmDialog(localization.deleteDialogTitle(),
                                                  warningMessage,
                                                  onConfirm(nodes, callback),
                                                  onCancel(callback)).show();
            }
        });
    }

    @NotNull
    private String generateWarningMessage(@NotNull List<ResourceBasedNode<?>> nodes) {
        if (nodes.size() == 1) {
            String name = nodes.get(0).getName();
            String type = getDisplayType(nodes.get(0));

            return "Delete " + type + " \"" + name + "\"?";
        }

        Map<String, Integer> pluralToSingular = new HashMap<>();
        for (ResourceBasedNode<?> node : nodes) {
            final String type = getDisplayType(node);

            if (!pluralToSingular.containsKey(type)) {
                pluralToSingular.put(type, 1);
            } else {
                Integer count = pluralToSingular.get(type);
                count++;
                pluralToSingular.put(type, count);
            }
        }

        StringBuilder buffer = new StringBuilder("Delete ");


        Iterator<Map.Entry<String, Integer>> iterator = pluralToSingular.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            buffer.append(entry.getValue())
                  .append(" ")
                  .append(entry.getKey());

            if (entry.getValue() > 1) {
                buffer.append("s");
            }

            while (iterator.hasNext()) {
                Map.Entry<String, Integer> e = iterator.next();

                buffer.append(" and ")
                      .append(e.getValue())
                      .append(" ")
                      .append(e.getKey());

                if (e.getValue() > 1) {
                    buffer.append("s");
                }
            }
        }

        buffer.append("?");

        return buffer.toString();
    }

    @NotNull
    private String getDisplayType(@NotNull ResourceBasedNode<?> node) {
        if (node instanceof ProjectNode) {
            return "project";
        } else if (node instanceof FolderReferenceNode) {
            return "folder";
        } else if (node instanceof FileReferenceNode) {
            return "file";
        } else {
            return "resource";
        }
    }

    @NotNull
    private List<ResourceBasedNode<?>> filterDescendants(@NotNull List<ResourceBasedNode<?>> nodes) {
        List<ResourceBasedNode<?>> filteredElements = Lists.newArrayList(nodes);

        int previousSize;

        do {
            previousSize = filteredElements.size();
            outer:
            for (ResourceBasedNode<?> element : filteredElements) {
                for (ResourceBasedNode<?> element2 : filteredElements) {
                    if (element == element2) {
                        continue;
                    }
                    if (isAncestor(element, element2)) {
                        filteredElements.remove(element2);
                        break outer;
                    }
                }
            }
        }
        while (filteredElements.size() != previousSize);

        return filteredElements;
    }

    private boolean isAncestor(@NotNull Node ancestor, @NotNull Node element) {
        if (ancestor == null) {
            return false;
        }

        Node parent = element;

        while (true) {
            if (parent == null) {
                return false;
            }

            if (parent.equals(ancestor)) {
                return true;
            }

            parent = parent.getParent();
        }
    }

    @NotNull
    private Promise<Void> chainNodes(@NotNull Promise<Void> promise,
                                     @NotNull Iterator<ResourceBasedNode<?>> nodes) {
        if (!nodes.hasNext()) {
            return promise;
        }

        final ResourceBasedNode<?> node = nodes.next();

        final Promise<Void> derivedPromise = promise.thenPromise(new Function<Void, Promise<Void>>() {
            @Override
            public Promise<Void> apply(Void empty) throws FunctionException {
                return node.delete();
            }
        });

        final Promise<Void> derivedErrorSafePromise = derivedPromise.catchErrorPromise(new Function<PromiseError, Promise<Void>>() {
            @Override
            public Promise<Void> apply(PromiseError arg) throws FunctionException {
                // 'hide' the error to avoid rejecting chain of promises
                return Promises.resolve(null);
            }
        });

        return chainNodes(derivedErrorSafePromise, nodes);
    }

    @NotNull
    private ConfirmCallback onConfirm(@NotNull final List<ResourceBasedNode<?>> node,
                                      @NotNull final AsyncCallback<Void> callback) {
        return new ConfirmCallback() {
            @Override
            public void accepted() {
                Promise<Void> promise = Promises.resolve(null);

                chainNodes(promise, node.iterator()).then(new Operation<Void>() {
                    @Override
                    public void apply(Void empty) throws OperationException {
                        callback.onSuccess(empty);
                    }
                });
            }
        };
    }

    private CancelCallback onCancel(final AsyncCallback<Void> callback) {
        return new CancelCallback() {
            @Override
            public void cancelled() {
                callback.onFailure(new Exception("Cancelled"));
            }
        };
    }
}
