/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.explorer.project;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.statepersistance.StateComponent;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Persists and restore state of the project explorer presenter, like expanded nodes and showing
 * hidden files
 *
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ProjectExplorerStateComponent implements StateComponent {
  private static final String PATH_PARAM_ID = "revealPath";
  private static final String SHOW_HIDDEN_FILES = "showHiddenFiles";

  private final ProjectExplorerPresenter projectExplorer;
  private final TreeResourceRevealer revealer;
  private final LoaderFactory loaderFactory;
  private final PromiseProvider promises;

  @Inject
  public ProjectExplorerStateComponent(
      ProjectExplorerPresenter projectExplorer,
      TreeResourceRevealer revealer,
      LoaderFactory loaderFactory,
      PromiseProvider promises) {
    this.projectExplorer = projectExplorer;
    this.revealer = revealer;
    this.loaderFactory = loaderFactory;
    this.promises = promises;
  }

  @Override
  public JsonObject getState() {
    JsonObject state = Json.createObject();
    JsonArray array = Json.createArray();
    state.put(PATH_PARAM_ID, array);

    List<String> rawPaths =
        projectExplorer
            .getTree()
            .getNodeStorage()
            .getAll()
            .stream()
            .filter(node -> projectExplorer.getTree().isExpanded(node))
            .filter(node -> node instanceof ResourceNode)
            .map(node -> ((ResourceNode) node).getData().getLocation().toString())
            .collect(Collectors.toList());

    int i = 0;
    for (String path : rawPaths) {
      array.set(i++, path);
    }

    state.put(SHOW_HIDDEN_FILES, projectExplorer.isShowHiddenFiles());

    return state;
  }

  @Override
  public Promise<Void> loadState(@NotNull JsonObject state) {
    if (state.hasKey(SHOW_HIDDEN_FILES)) {
      projectExplorer.showHiddenFiles(state.getBoolean(SHOW_HIDDEN_FILES));
    }

    JsonArray paths =
        state.hasKey(PATH_PARAM_ID) ? state.getArray(PATH_PARAM_ID) : Json.createArray();

    if (paths.length() == 0) {
      return promises.resolve(null);
    }

    Promise<Node> revealPromise = null;

    MessageLoader loader = loaderFactory.newLoader("Restoring project structure...");
    loader.show();

    for (int i = 0; i < paths.length(); i++) {
      String path = paths.getString(i);
      if (revealPromise == null) {
        revealPromise = revealer.reveal(Path.valueOf(path), false).thenPromise(this::doExpand);
        continue;
      }

      revealPromise
          .thenPromise(node -> revealer.reveal(Path.valueOf(path), false))
          .thenPromise(this::doExpand)
          .catchError(this::logError);
    }

    if (revealPromise != null) {
      revealPromise
          .then(
              node -> {
                loader.hide();
              })
          .catchError(
              error -> {
                loader.hide();
              });
    }

    if (revealPromise == null) {
      return promises.resolve(null);
    }

    return revealPromise.thenPromise(ignored -> promises.resolve(null));
  }

  private Promise<Node> doExpand(Node node) {
    projectExplorer.getTree().setExpanded(node, true);

    return promises.resolve(null);
  }

  private Promise<Node> logError(PromiseError error) {
    Log.info(getClass(), error.getMessage());

    return promises.resolve(null);
  }

  @Override
  public int getPriority() {
    return MAX_PRIORITY;
  }

  @Override
  public String getId() {
    return "projectExplorer";
  }
}
