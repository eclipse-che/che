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

import com.google.common.base.Joiner;
import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.PromisableAction;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.statepersistance.PersistenceComponent;
import org.eclipse.che.ide.statepersistance.dto.ActionDescriptor;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.createFromCallback;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Persistence component that store state of expanded project nodes and after page reloading tries
 * to restore the stored state by revealing nodes in project explorer.
 *
 * @author Vlad Zhukovskyi
 * @since 4.5.0
 * @see PersistenceComponent
 */
@Singleton
public class RevealNodesPersistenceComponent implements PersistenceComponent {

    private static final String REVEAL_NODE_ACTION = "revealNode";
    private static final String PATH_PARAM_ID      = "revealPath";
    private static final String PATH_SPLITTER      = ":";

    private final ProjectExplorerPresenter projectExplorer;
    private final DtoFactory               dtoFactory;
    private final TreeResourceRevealer     revealer;
    private final LoaderFactory            loaderFactory;

    private Callback<Void, Throwable> actionCompletedCallback;

    @Inject
    public RevealNodesPersistenceComponent(ProjectExplorerPresenter projectExplorer,
                                           DtoFactory dtoFactory,
                                           TreeResourceRevealer revealer,
                                           ActionManager actionManager,
                                           LoaderFactory loaderFactory) {
        this.projectExplorer = projectExplorer;
        this.dtoFactory = dtoFactory;
        this.revealer = revealer;
        this.loaderFactory = loaderFactory;

        actionManager.registerAction(REVEAL_NODE_ACTION, new RevealNodeAction());
    }

    /** {@inheritDoc} */
    @Override
    public List<ActionDescriptor> getActions() {
        final List<Path> paths = new ArrayList<>();

        /*
           The main idea is to look up all expanded nodes in project tree and gather the last one's children.
           Then check if child is resource, then we store the path in user preference.
         */

        for (Node node : projectExplorer.getTree().getNodeStorage().getAll()) {
            if (projectExplorer.getTree().isExpanded(node) && node instanceof ResourceNode) {

                final List<Node> childrenToStore = projectExplorer.getTree().getNodeStorage().getChildren(node);

                for (Node children : childrenToStore) {
                    if (children instanceof ResourceNode) {
                        paths.add(((ResourceNode)children).getData().getLocation());
                    }
                }
            }
        }

        final ActionDescriptor descriptor = dtoFactory.createDto(ActionDescriptor.class)
                                                      .withId(REVEAL_NODE_ACTION)
                                                      .withParameters(singletonMap(PATH_PARAM_ID, Joiner.on(PATH_SPLITTER).join(paths)));

        return singletonList(descriptor);
    }

    @Singleton
    private class RevealNodeAction extends AbstractPerspectiveAction implements PromisableAction {

        RevealNodeAction() {
            super(singletonList(PROJECT_PERSPECTIVE_ID), null, null, null, null);
        }

        /** {@inheritDoc} */
        @Override
        public void updateInPerspective(@NotNull ActionEvent event) {
            event.getPresentation().setEnabledAndVisible(true);
        }

        /** {@inheritDoc} */
        @Override
        public void actionPerformed(ActionEvent e) {
            checkState(e.getParameters() != null);

            final String toReveal = e.getParameters().get(PATH_PARAM_ID);
            if (isNullOrEmpty(toReveal)) {
                return;
            }

            final String[] paths = toReveal.split(PATH_SPLITTER);

            Promise<Node> revealPromise = null;

            final MessageLoader loader = loaderFactory.newLoader("Restoring project structure...");
            loader.show();

            for (final String path : paths) {
                if (revealPromise == null) {
                    revealPromise = revealer.reveal(Path.valueOf(path)).thenPromise(new Function<Node, Promise<Node>>() {
                        @Override
                        public Promise<Node> apply(Node node) throws FunctionException {
                            if (node != null) {
                                projectExplorer.getTree().setExpanded(node, true, false);
                            }

                            return revealer.reveal(Path.valueOf(path));
                        }
                    });
                    continue;
                }

                revealPromise.thenPromise(new Function<Node, Promise<Node>>() {
                    @Override
                    public Promise<Node> apply(Node node) throws FunctionException {
                        if (node != null) {
                            projectExplorer.getTree().setExpanded(node, true, false);
                        }

                        return revealer.reveal(Path.valueOf(path));
                    }
                }).catchError(new Function<PromiseError, Node>() {
                    @Override
                    public Node apply(PromiseError error) throws FunctionException {
                        Log.info(getClass(), error.getMessage());

                        return null;
                    }
                });
            }

            if (revealPromise != null) {
                revealPromise.then(new Operation<Node>() {
                    @Override
                    public void apply(Node node) throws OperationException {
                        if (actionCompletedCallback != null) {
                            actionCompletedCallback.onSuccess(null);
                            loader.hide();
                        }
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError error) throws OperationException {
                        if (actionCompletedCallback != null) {
                            actionCompletedCallback.onFailure(error.getCause());
                            loader.hide();
                        }
                    }
                });
            }
        }

        /** {@inheritDoc} */
        @Override
        public Promise<Void> promise(final ActionEvent event) {
            final CallbackPromiseHelper.Call<Void, Throwable> call = new CallbackPromiseHelper.Call<Void, Throwable>() {

                @Override
                public void makeCall(final Callback<Void, Throwable> callback) {
                    actionCompletedCallback = callback;
                    actionPerformed(event);
                }
            };

            return createFromCallback(call);
        }
    }
}
