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
package org.eclipse.che.ide.search.selectpath;

import static com.google.common.collect.Lists.newArrayList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.search.FullTextSearchView;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.SettingsProvider;

/**
 * Presenter for choosing directory for searching.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectPathPresenter implements SelectPathView.ActionDelegate {
  private final SelectPathView view;
  private final AppContext appContext;
  private final ResourceNode.NodeFactory nodeFactory;
  private final SettingsProvider settingsProvider;

  private FullTextSearchView.ActionDelegate searcher;

  @Inject
  public SelectPathPresenter(
      SelectPathView view,
      AppContext appContext,
      ResourceNode.NodeFactory nodeFactory,
      SettingsProvider settingsProvider) {
    this.view = view;
    this.appContext = appContext;
    this.nodeFactory = nodeFactory;
    this.settingsProvider = settingsProvider;
    this.view.setDelegate(this);
  }

  /**
   * Show tree view with all root nodes of the workspace.
   *
   * @param searcher delegate from the root widget of the full-text-search mechanism
   */
  public void show(FullTextSearchView.ActionDelegate searcher) {
    this.searcher = searcher;

    List<Node> rootNodes = newArrayList();

    for (Project project : appContext.getProjects()) {
      rootNodes.add(nodeFactory.newContainerNode(project, settingsProvider.getSettings()));
    }

    view.setStructure(rootNodes);
    view.showDialog();
  }

  /** {@inheritDoc} */
  @Override
  public void setSelectedPath(String path) {
    searcher.setPathDirectory(path);
    searcher.setFocus();
  }
}
