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
package org.eclipse.che.ide.ext.git.client.compare.changespanel;

import static java.util.Comparator.naturalOrder;
import static org.eclipse.che.ide.ext.git.client.compare.changespanel.ViewMode.TREE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.SelectionModel;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.compare.NameComparator;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent.SelectionChangedHandler;
import org.eclipse.che.ide.ui.smartTree.presentation.PresentationRenderer;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;

/**
 * Implementation of {@link ChangesPanelView}.
 *
 * @author Igor Vinokur
 */
public class ChangesPanelViewImpl extends Composite implements ChangesPanelView {
  interface TreeViewImplUiBinder extends UiBinder<DockLayoutPanel, ChangesPanelViewImpl> {}

  private static TreeViewImplUiBinder uiBinder = GWT.create(TreeViewImplUiBinder.class);

  @UiField LayoutPanel changesPanel;
  @UiField Button changeViewModeButton;
  @UiField Button expandButton;
  @UiField Button collapseButton;
  @UiField TextBox commitHash;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  @UiField(provided = true)
  final GitResources res;

  private ActionDelegate delegate;
  private Tree tree;
  private Set<Path> nodePaths;

  private final NodesResources nodesResources;

  @Inject
  public ChangesPanelViewImpl(
      GitResources resources,
      GitLocalizationConstant locale,
      NodesResources nodesResources,
      ClipboardButtonBuilder clipboardButtonBuilder) {
    this.res = resources;
    this.locale = locale;
    this.nodesResources = nodesResources;
    this.nodePaths = new HashSet<>();

    initWidget(uiBinder.createAndBindUi(this));

    NodeStorage nodeStorage = new NodeStorage();
    NodeLoader nodeLoader = new NodeLoader();
    tree = new Tree(nodeStorage, nodeLoader);
    SelectionModel selectionModel = tree.getSelectionModel();
    selectionModel.setSelectionMode(SelectionModel.Mode.SINGLE);
    changesPanel.add(tree);

    createButtons();

    Element copyToClipboardElement = clipboardButtonBuilder.withResourceWidget(commitHash).build();
    copyToClipboardElement.getStyle().setFloat(Style.Float.RIGHT);
    copyToClipboardElement.getStyle().setPosition(Style.Position.ABSOLUTE);
    copyToClipboardElement.getStyle().setTop(0., Style.Unit.PX);
    copyToClipboardElement.getStyle().setRight(5., Style.Unit.PX);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void addSelectionHandler(SelectionChangedHandler handler) {
    tree.getSelectionModel().addSelectionChangedHandler(handler);
  }

  @Override
  public void viewChangedFiles(AlteredFiles files, ViewMode viewMode) {
    NodeStorage nodeStorage = tree.getNodeStorage();
    nodeStorage.clear();
    if (viewMode == TREE) {
      getGroupedNodes(files.getChangedFilesMap()).forEach(nodeStorage::add);
      tree.expandAll();
    } else {
      files
          .getAlteredFilesList()
          .forEach(
              file ->
                  nodeStorage.add(
                      new ChangedFileNode(
                          file, files.getStatusByFilePath(file), res, delegate, false)));
    }

    commitHash.setText(files.getCommitB());
  }

  @Override
  public void resetPanelState() {
    tree.getNodeStorage().clear();
  }

  @Override
  public void collapseAllDirectories() {
    tree.collapseAll();
  }

  @Override
  public void expandAllDirectories() {
    tree.expandAll();
  }

  @Override
  public void setEnableExpandCollapseButtons(boolean enabled) {
    expandButton.setEnabled(enabled);
    collapseButton.setEnabled(enabled);
  }

  @Override
  public void setEnabledChangeViewModeButton(boolean enabled) {
    changeViewModeButton.setEnabled(enabled);
  }

  @Override
  public void updateChangeViewModeButton(ViewMode viewMode) {
    switch (viewMode) {
      case TREE:
        changeViewModeButton.getElement().setInnerHTML(FontAwesome.LIST_UL);
        changeViewModeButton.setTitle(locale.changeListRowListViewButtonText());
        break;
      case LIST:
        changeViewModeButton.getElement().setInnerHTML(FontAwesome.FOLDER);
        changeViewModeButton.setTitle(locale.changeListGroupByDirectoryButtonText());
        break;
    }
  }

  @Override
  public void setTreeRender(PresentationRenderer render) {
    tree.setPresentationRenderer(render);
  }

  @Override
  public TreeStyles getTreeStyles() {
    return tree.getTreeStyles();
  }

  @Override
  public void refreshNodes() {
    tree.getAllChildNodes(tree.getRootNodes(), false).forEach(node -> tree.refresh(node));
  }

  @Override
  public Set<Path> getNodePaths() {
    return nodePaths;
  }

  private void createButtons() {
    changeViewModeButton.addClickHandler(clickEvent -> delegate.onChangeViewModeButtonClicked());

    expandButton.setTitle(locale.changeListExpandCollapseAllButtonTitle());
    expandButton.getElement().setInnerHTML(FontAwesome.EXPAND);
    expandButton.addClickHandler(clickEvent -> delegate.onExpandButtonClicked());

    collapseButton.setTitle(locale.changeListCollapseAllButtonTitle());
    collapseButton.getElement().setInnerHTML(FontAwesome.COMPRESS);
    collapseButton.addClickHandler(clickEvent -> delegate.onCollapseButtonClicked());
  }

  private List<Node> getGroupedNodes(Map<String, Status> items) {
    List<String> allFiles = new ArrayList<>(items.keySet());
    List<String> allFolders = new ArrayList<>();
    for (String file : allFiles) {
      nodePaths.add(Path.valueOf(file));
      String path = file.substring(0, file.lastIndexOf("/"));
      if (!allFolders.contains(path)) {
        allFolders.add(path);
      }
    }
    List<String> commonPaths = getCommonPaths(allFolders);
    for (String commonPath : commonPaths) {
      if (!allFolders.contains(commonPath)) {
        allFolders.add(commonPath);
      }
    }

    Map<String, Node> preparedNodes = new HashMap<>();
    for (int i = getMaxNestedLevel(allFiles); i > 0; i--) {

      // Collect child files of all folders of current nesting level
      Map<String, List<Node>> currentChildNodes = new HashMap<>();
      for (String file : allFiles) {
        Path pathName = Path.valueOf(file);
        if (pathName.segmentCount() != i) {
          continue;
        }
        Node fileNode = new ChangedFileNode(file, items.get(file), res, delegate, true);
        String filePath = pathName.removeLastSegments(1).toString();
        if (currentChildNodes.keySet().contains(filePath)) {
          currentChildNodes.get(filePath).add(fileNode);
        } else {
          List<Node> listFiles = new ArrayList<>();
          listFiles.add(fileNode);
          currentChildNodes.put(filePath, listFiles);
        }
      }

      // Map child files to related folders of current nesting level or just create a common folder
      for (String path : allFolders) {
        nodePaths.add(Path.valueOf(path));
        if (!(Path.valueOf(path).segmentCount() == i - 1)) {
          continue;
        }
        Node folder =
            new ChangedFolderNode(
                getTransitFolderName(allFolders, path), Path.valueOf(path), nodesResources);
        if (currentChildNodes.keySet().contains(path)) {
          folder.setChildren(currentChildNodes.get(path));
        }
        preparedNodes.put(path, folder);
      }

      // Take all child folders and nest them to related parent folders of current nesting level
      List<String> currentPaths = new ArrayList<>(preparedNodes.keySet());
      for (String parentPath : currentPaths) {
        List<Node> nodesToNest = new ArrayList<>();
        for (String nestedItem : currentPaths) {
          if (!parentPath.equals(nestedItem)
              && (nestedItem.startsWith(parentPath + "/") || parentPath.isEmpty())) {
            nodesToNest.add(preparedNodes.remove(nestedItem));
          }
        }
        if (nodesToNest.isEmpty() && !parentPath.isEmpty()) {
          continue;
        }
        nodesToNest.sort(new NameComparator());
        if (currentChildNodes.keySet().contains(parentPath)) {
          nodesToNest.addAll(currentChildNodes.get(parentPath));
        }
        if (parentPath.isEmpty()) {
          return nodesToNest;
        } else {
          preparedNodes.get(parentPath).setChildren(nodesToNest);
        }
      }
    }
    ArrayList<Node> nodes = new ArrayList<>(preparedNodes.values());
    nodes.sort(new NameComparator());
    return new ArrayList<>(nodes);
  }

  private String getTransitFolderName(List<String> allPaths, String comparedPath) {
    Path path = Path.valueOf(comparedPath);
    int segmentCount = path.segmentCount();
    for (int i = segmentCount; i > 0; i--) {
      if (allPaths.contains(path.removeLastSegments(segmentCount - i + 1).toString())) {
        return path.removeFirstSegments(i - 1).toString();
      }
    }
    return comparedPath;
  }

  private int getMaxNestedLevel(List<String> items) {
    int level = 0;
    for (String item : items) {
      int currentLevel = Path.valueOf(item).segmentCount();
      level = currentLevel > level ? currentLevel : level;
    }
    return level;
  }

  private List<String> getCommonPaths(List<String> allPaths) {
    List<String> commonPaths = new ArrayList<>();
    allPaths.sort(naturalOrder());
    for (String path : allPaths) {
      int pathIndex = allPaths.indexOf(path);
      if (pathIndex + 1 == allPaths.size()) {
        continue;
      }
      String commonPath = getCommonPath(allPaths.get(pathIndex), allPaths.get(pathIndex + 1));
      if (!commonPath.isEmpty() && !commonPaths.contains(commonPath)) {
        commonPaths.add(commonPath);
      }
    }
    return commonPaths;
  }

  private String getCommonPath(String firstPath, String secondPath) {
    Path commonPath = Path.valueOf(firstPath);
    int segmentCount = commonPath.segmentCount();
    for (int i = 1; i < segmentCount; i++) {
      String path = commonPath.removeLastSegments(segmentCount - i).toString();
      if (!secondPath.startsWith(path)) {
        return Path.valueOf(path).removeLastSegments(1).toString();
      }
    }
    return commonPath.toString();
  }
}
