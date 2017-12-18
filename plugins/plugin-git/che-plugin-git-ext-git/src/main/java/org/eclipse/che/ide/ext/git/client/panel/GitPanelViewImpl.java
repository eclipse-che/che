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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.ext.git.client.panel.GitPanelView.ActionDelegate;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeStorage.StoreSortInfo;
import org.eclipse.che.ide.ui.smartTree.SelectionModel;
import org.eclipse.che.ide.ui.smartTree.SortDir;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/** @author Mykola Morhun */
public class GitPanelViewImpl extends BaseView<ActionDelegate> implements GitPanelView {
  interface GitPanelViewImplUiBinder extends UiBinder<Widget, GitPanelViewImpl> {}

  @UiField SimplePanel changesPanel;

  @UiField(provided = true)
  final GitResources gitResources;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  @UiField(provided = true)
  Tree repositoriesList;

  private final RepositoryNodeFactory repositoryNodeFactory;

  @Inject
  public GitPanelViewImpl(
      GitPanelViewImplUiBinder uiBinder,
      RepositoryNodeFactory repositoryNodeFactory,
      GitResources gitResources,
      GitLocalizationConstant locale) {
    this.repositoryNodeFactory = repositoryNodeFactory;
    this.gitResources = gitResources;
    this.locale = locale;

    createRepositoriesList();
    setContentWidget(uiBinder.createAndBindUi(this));
  }

  private void createRepositoriesList() {
    NodeStorage nodeStorage = new NodeStorage();
    NodeLoader nodeLoader = new NodeLoader();
    repositoriesList = new Tree(nodeStorage, nodeLoader);
    repositoriesList.getElement().getStyle().setProperty("maxHeight", "200px");
    repositoriesList
        .getNodeStorage()
        .addSortInfo(new StoreSortInfo(Comparator.comparing(Node::getName), SortDir.ASC));
    SelectionModel selectionModel = repositoriesList.getSelectionModel();
    selectionModel.setSelectionMode(SelectionModel.Mode.SINGLE);
    selectionModel.addSelectionChangedHandler(
        event -> {
          Node node = getFirst(event.getSelection(), null);
          if (node == null) {
            delegate.onRepositorySelectionChanged(null);
          } else {
            delegate.onRepositorySelectionChanged(node.getName());
          }
        });
  }

  @Override
  public void setChangesPanelView(ChangesPanelView changesPanelView) {
    this.changesPanel.add(changesPanelView);
  }

  @Override
  public void addRepository(String repository) {
    repositoriesList.getNodeStorage().add(repositoryNodeFactory.newRepositoryNode(repository, 0));
  }

  @Override
  public void removeRepository(String repository) {
    repositoriesList.getNodeStorage().remove(findNode(repository));
  }

  @Override
  public void renameRepository(String oldRepositoryName, String newRepositoryName) {
    RepositoryNode node = findNode(oldRepositoryName);
    if (node != null) {
      NodeStorage nodeStorage = repositoriesList.getNodeStorage();
      nodeStorage.remove(node);
      nodeStorage.add(
          repositoryNodeFactory.newRepositoryNode(newRepositoryName, node.getChanges()));
    }
  }

  @Override
  public void updateRepositoryChanges(String repository, int changes) {
    RepositoryNode node = findNode(repository);
    if (node != null) {
      node.setChanges(changes);
      repositoriesList.refresh(node);
    }
  }

  @Override
  @Nullable
  public String getSelectedRepository() {
    List<Node> selection = repositoriesList.getSelectionModel().getSelectedNodes();
    if (selection.isEmpty()) {
      return null;
    }
    return selection.get(0).getName();
  }

  private RepositoryNode findNode(String repositoryName) {
    for (Node node : repositoriesList.getNodeStorage().getAll()) {
      if (repositoryName.equals(node.getName())) {
        return (RepositoryNode) node;
      }
    }
    return null;
  }
}
