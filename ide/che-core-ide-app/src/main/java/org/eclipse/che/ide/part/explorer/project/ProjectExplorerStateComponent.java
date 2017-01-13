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

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.eclipse.che.api.promises.client.*;
import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists and restore state of the project explorer presenter, like expanded nodes and showing hidden files
 */
@Singleton
public class ProjectExplorerStateComponent implements StateComponent {
    private static final String PATH_PARAM_ID = "revealPath";
    private static final String SHOW_HIDDEN_FILES = "showHiddenFiles";

    private final ProjectExplorerPresenter projectExplorer;
    private final TreeResourceRevealer revealer;
    private final LoaderFactory loaderFactory;

    @Inject
    public ProjectExplorerStateComponent(ProjectExplorerPresenter projectExplorer, TreeResourceRevealer revealer, LoaderFactory loaderFactory) {
        this.projectExplorer = projectExplorer;
        this.revealer = revealer;
        this.loaderFactory = loaderFactory;
    }

    @Override
    public JsonObject getState() {
        final List<Path> paths = new ArrayList<>();

        /*
           The main idea is to look up all expanded nodes in project tree and gather the last one's children.
           Then check if child is resource, then we store the path in user preference.
         */

        outer:
        for (Node node : projectExplorer.getTree().getNodeStorage().getAll()) {
            if (projectExplorer.getTree().isExpanded(node) && node instanceof ResourceNode) {

                final List<Node> childrenToStore = projectExplorer.getTree().getNodeStorage().getChildren(node);

                for (Node children : childrenToStore) {
                    if (children instanceof ResourceNode) {
                        paths.add(((ResourceNode) children).getData().getLocation());
                        continue outer;
                    }
                }
            }
        }

        JsonObject state = Json.createObject();
        JsonArray array = Json.createArray();
        state.put(PATH_PARAM_ID, array);
        int i = 0;
        for (Path path : paths) {
            array.set(i++, path.toString());
        }

        state.put(SHOW_HIDDEN_FILES, projectExplorer.isShowHiddenFiles());

        return state;
    }

    @Override
    public void loadState(@NotNull JsonObject state) {

        if (state.hasKey(SHOW_HIDDEN_FILES)) {
            projectExplorer.showHiddenFiles(state.getBoolean(SHOW_HIDDEN_FILES));
        }

        JsonArray paths = state.hasKey(PATH_PARAM_ID) ? state.getArray(PATH_PARAM_ID) : Json.createArray();

        if (paths.length() == 0) {
            return;
        }

        Promise<Node> revealPromise = null;

        final MessageLoader loader = loaderFactory.newLoader("Restoring project structure...");
        loader.show();

        for (int i = 0; i < paths.length(); i++) {
            final String path = paths.getString(i);
            if (revealPromise == null) {
                revealPromise = revealer.reveal(Path.valueOf(path), false).thenPromise(new Function<Node, Promise<Node>>() {
                    @Override
                    public Promise<Node> apply(Node node) throws FunctionException {
                        if (node != null) {
                            projectExplorer.getTree().setExpanded(node, true, false);
                        }

                        return revealer.reveal(Path.valueOf(path), false);
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

                    return revealer.reveal(Path.valueOf(path), false);
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
                    loader.hide();
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError error) throws OperationException {
                    loader.hide();
                }
            });
        }
    }
}
