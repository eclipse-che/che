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
package org.eclipse.che.ide.ext.git.client.compare.changedpanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.inject.Inject;

import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.SelectionModel;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.compare.NameComparator;
import org.eclipse.che.ide.ui.smartTree.presentation.PresentationRenderer;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link ChangedPanelView}.
 *
 * @author Igor Vinokur
 */
public class ChangedPanelViewImpl extends Composite implements ChangedPanelView {
    interface TreeViewImplUiBinder extends UiBinder<DockLayoutPanel, ChangedPanelViewImpl> {
    }

    private static TreeViewImplUiBinder uiBinder = GWT.create(TreeViewImplUiBinder.class);

    @UiField
    LayoutPanel changedPanel;
    @UiField
    Button      changeViewModeButton;
    @UiField
    Button      expandButton;
    @UiField
    Button      collapseButton;
    @UiField(provided = true)
    final GitLocalizationConstant locale;
    @UiField(provided = true)
    final GitResources            res;

    private ActionDelegate delegate;
    private Tree           tree;
    private Set<Path>      nodePaths;

    private final NodesResources nodesResources;

    @Inject
    public ChangedPanelViewImpl(GitResources resources,
                                GitLocalizationConstant locale,
                                NodesResources nodesResources) {
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
        selectionModel.addSelectionChangedHandler(event -> {
            List<Node> selection = event.getSelection();
            if (!selection.isEmpty()) {
                delegate.onNodeSelected(selection.get(0));
            }
        });
        changedPanel.add(tree);

        createButtons();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void viewChangedFilesAsList(@NotNull Map<String, Status> items) {
        NodeStorage nodeStorage = tree.getNodeStorage();
        nodeStorage.clear();
        items.keySet().forEach(file -> nodeStorage.add(new ChangedFileNode(file, items.get(file), nodesResources, delegate, false)));
    }

    @Override
    public void clearNodeStorage() {
        tree.getNodeStorage().clear();
    }

    @Override
    public void viewChangedFilesAsTree(@NotNull Map<String, Status> items) {
        NodeStorage nodeStorage = tree.getNodeStorage();
        nodeStorage.clear();
        fetchGroupedNodes(items).forEach(nodeStorage::add);
        tree.expandAll();
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
    public void setTextToChangeViewModeButton(String text) {
        changeViewModeButton.setText(text);
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

    private List<Node> fetchGroupedNodes(Map<String, Status> items) {
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

            //Collect child files of all folders of current nesting level
            Map<String, List<Node>> currentChildNodes = new HashMap<>();
            for (String file : allFiles) {
                Path pathName = Path.valueOf(file);
                if (pathName.segmentCount() != i) {
                    continue;
                }
                Node fileNode = new ChangedFileNode(file, items.get(file), nodesResources, delegate, true);
                String filePath = pathName.removeLastSegments(1).toString();
                if (currentChildNodes.keySet().contains(filePath)) {
                    currentChildNodes.get(filePath).add(fileNode);
                } else {
                    List<Node> listFiles = new ArrayList<>();
                    listFiles.add(fileNode);
                    currentChildNodes.put(filePath, listFiles);
                }
            }

            //Map child files to related folders of current nesting level or just create a common folder
            for (String path : allFolders) {
                nodePaths.add(Path.valueOf(path));
                if (!(Path.valueOf(path).segmentCount() == i - 1)) {
                    continue;
                }
                Node folder = new ChangedFolderNode(getTransitFolderName(allFolders, path), Path.valueOf(path), nodesResources);
                if (currentChildNodes.keySet().contains(path)) {
                    folder.setChildren(currentChildNodes.get(path));
                }
                preparedNodes.put(path, folder);
            }

            //Take all child folders and nest them to related parent folders of current nesting level
            List<String> currentPaths = new ArrayList<>(preparedNodes.keySet());
            for (String parentPath : currentPaths) {
                List<Node> nodesToNest = new ArrayList<>();
                for (String nestedItem : currentPaths) {
                    if (!parentPath.equals(nestedItem) && (nestedItem.startsWith(parentPath + "/") || parentPath.isEmpty())) {
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
        return null;
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
