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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Comparator;
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
import org.eclipse.che.ide.ui.smartTree.NodeStorage.StoreSortInfo;
import org.eclipse.che.ide.ui.smartTree.SelectionModel;
import org.eclipse.che.ide.ui.smartTree.SortDir;
import org.eclipse.che.ide.ui.smartTree.Tree;

/** @author Mykola Morhun */
public class GitPanelViewImpl extends BaseView<ActionDelegate> implements GitPanelView {
  interface GitPanelViewImplUiBinder extends UiBinder<Widget, GitPanelViewImpl> {}

  @UiField FlowPanel changesPanel;
  @UiField SimplePanel repositoriesPanel;

  @UiField(provided = true)
  final GitResources gitResources;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private final RepositoryNodeFactory repositoryNodeFactory;

  private Tree repositoriesList;

  @Inject
  public GitPanelViewImpl(
      GitPanelViewImplUiBinder uiBinder,
      RepositoryNodeFactory repositoryNodeFactory,
      PartStackUIResources resources,
      GitResources gitResources,
      GitLocalizationConstant locale) {
    super(resources);

    this.repositoryNodeFactory = repositoryNodeFactory;
    this.gitResources = gitResources;
    this.locale = locale;

    setContentWidget(uiBinder.createAndBindUi(this));

    createRepositoriesList();
  }

  private void createRepositoriesList() {
    NodeStorage nodeStorage = new NodeStorage();
    NodeLoader nodeLoader = new NodeLoader();
    repositoriesList = new Tree(nodeStorage, nodeLoader);
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
    this.repositoriesPanel.add(repositoriesList);
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
  public void updateRepositoryChanges(String repository, int changes) {
    RepositoryNode node = findNode(repository);
    if (node != null) {
      node.setChanges(changes);
      repositoriesList.refresh(node);
    }
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
