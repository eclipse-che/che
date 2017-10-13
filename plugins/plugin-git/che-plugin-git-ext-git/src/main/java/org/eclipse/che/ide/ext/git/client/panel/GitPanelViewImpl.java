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
package org.eclipse.che.ide.ext.git.client.panel;

import static com.google.common.collect.Iterables.getFirst;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import javax.inject.Inject;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.ext.git.client.panel.GitPanelView.ActionDelegate;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.SelectionModel;
import org.eclipse.che.ide.ui.smartTree.Tree;

/** @author Mykola Morhun */
public class GitPanelViewImpl extends BaseView<ActionDelegate> implements GitPanelView {
  interface GitPanelViewImplUiBinder extends UiBinder<Widget, GitPanelViewImpl> {}

  @UiField FlowPanel changesPanel;
  @UiField LayoutPanel repositoriesPanel;

  @UiField(provided = true)
  final GitResources gitResources;
  @UiField(provided = true)
  final GitLocalizationConstant locale;

  Tree repositoriesList;

  private TreeSet<String> repositories;
  private Map<String, Integer> numberOfChanges;

  @Inject
  public GitPanelViewImpl(
      GitPanelViewImplUiBinder uiBinder,
      PartStackUIResources resources,
      GitResources gitResources,
      GitLocalizationConstant locale) {
    super(resources);

    this.gitResources = gitResources;
    this.locale = locale;

    this.repositories = new TreeSet<>();
    this.numberOfChanges = new HashMap<>();

    setContentWidget(uiBinder.createAndBindUi(this));

    createRepositoriesList();
  }

  private void createRepositoriesList() {
    NodeStorage nodeStorage = new NodeStorage();
    NodeLoader nodeLoader = new NodeLoader();
    repositoriesList = new Tree(nodeStorage, nodeLoader);
    SelectionModel selectionModel = repositoriesList.getSelectionModel();
    selectionModel.setSelectionMode(SelectionModel.Mode.SINGLE);
    selectionModel.addSelectionChangedHandler(event -> {
      Node node = getFirst(event.getSelection(), null);
      if (node == null) {
        delegate.onRepositorySelectionChanged(null);
      } else {
        delegate.onRepositorySelectionChanged(node.getName());
      }
    });
    this.changesPanel.add(repositoriesList);
  }

  @Override
  public void setChangesPanelView(ChangesPanelView changesPanelView) {
    this.changesPanel.add(changesPanelView);
  }

  @Override
  public void addRepository(String repository) {
    // TODO save selection
    repositories.add(repository);
    numberOfChanges.put(repository, 0);
    updateRepositoriesList();
  }

  @Override
  public void removeRepository(String repository) {
    // TODO save selection
    repositories.remove(repository);
    numberOfChanges.remove(repository);
    updateRepositoriesList();
  }

  @Override
  public void updateRepositoryChanges(String repository, int changes) {
    numberOfChanges.put(repository, changes);
  }

  private void updateRepositoriesList() {
    repositoriesList.clear();
    NodeStorage nodeStorage = repositoriesList.getNodeStorage();
    repositories.forEach(repository -> nodeStorage.add(new RepositoryNode(repository)));
  }
}
