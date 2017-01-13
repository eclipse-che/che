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
package org.eclipse.che.ide.ext.git.client.compare.changedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.SelectionModel;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.compare.NameComparator;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;
import org.eclipse.che.ide.ui.window.Window;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ChangedListView}.
 *
 * @author Igor Vinokur
 */
@Singleton
public class ChangedListViewImpl extends Window implements ChangedListView {
    interface ChangedListViewImplUiBinder extends UiBinder<DockLayoutPanel, ChangedListViewImpl> {
    }

    private static ChangedListViewImplUiBinder uiBinder = GWT.create(ChangedListViewImplUiBinder.class);

    @UiField
    LayoutPanel changedFilesPanel;
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
    private Button         btnCompare;

    private final NodesResources nodesResources;

    @Inject
    protected ChangedListViewImpl(GitResources resources,
                                  GitLocalizationConstant locale,
                                  NodesResources nodesResources) {
        this.res = resources;
        this.locale = locale;
        this.nodesResources = nodesResources;

        DockLayoutPanel widget = uiBinder.createAndBindUi(this);

        this.setTitle(locale.changeListTitle());
        this.setWidget(widget);

        NodeStorage nodeStorage = new NodeStorage();
        NodeLoader nodeLoader = new NodeLoader();
        tree = new Tree(nodeStorage, nodeLoader);
        tree.getSelectionModel().setSelectionMode(SelectionModel.Mode.SINGLE);
        tree.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                List<Node> selection = event.getSelection();
                if (!selection.isEmpty()) {
                    delegate.onNodeSelected(selection.get(0));
                }
            }
        });
        changedFilesPanel.add(tree);

        createButtons();

        SafeHtmlBuilder shb = new SafeHtmlBuilder();

        shb.appendHtmlConstant("<table height =\"20\">");
        shb.appendHtmlConstant("<tr height =\"3\"></tr><tr>");
        shb.appendHtmlConstant("<td width =\"20\" bgcolor =\"dodgerBlue\"></td>");
        shb.appendHtmlConstant("<td>modified</td>");
        shb.appendHtmlConstant("<td width =\"20\" bgcolor =\"red\"></td>");
        shb.appendHtmlConstant("<td>deleted</td>");
        shb.appendHtmlConstant("<td width =\"20\" bgcolor =\"green\"></td>");
        shb.appendHtmlConstant("<td>added</td>");
        shb.appendHtmlConstant("<td width =\"20\" bgcolor =\"purple\"></td>");
        shb.appendHtmlConstant("<td>copied</td>");
        shb.appendHtmlConstant("</tr></table>");

        getFooter().add(new HTML(shb.toSafeHtml()));
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void viewChangedFilesAsList(@NotNull Map<String, Status> items) {
        tree.getNodeStorage().clear();
        for (String file : items.keySet()) {
            tree.getNodeStorage().add(new ChangedFileNode(file, items.get(file), nodesResources, delegate, false));
        }
    }

    @Override
    public void viewChangedFilesAsTree(@NotNull Map<String, Status> items) {
        tree.getNodeStorage().clear();
        List<Node> nodes = getGroupedNodes(items);
        if (nodes.size() == 1) {
            tree.getNodeStorage().add(nodes);
            tree.setExpanded(nodes.get(0), true);
        } else {
            for (Node node : nodes) {
                tree.getNodeStorage().add(node);
            }
        }
    }

    @Override
    public void collapseAllDirectories() {
        tree.collapseAll();
    }

    @Override
    public void expandAllDirectories() {
        tree.expandAll();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.show();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableCompareButton(boolean enabled) {
        btnCompare.setEnabled(enabled);
    }

    @Override
    public void setEnableExpandCollapseButtons(boolean enabled) {
        expandButton.setEnabled(enabled);
        collapseButton.setEnabled(enabled);
    }

    @Override
    public void setTextToChangeViewModeButton(String text) {
        changeViewModeButton.setText(text);
    }

    private void createButtons() {
        changeViewModeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                delegate.onChangeViewModeButtonClicked();
            }
        });

        expandButton.setTitle(locale.changeListExpandCollapseAllButtonTitle());
        expandButton.getElement().setInnerHTML(FontAwesome.EXPAND);
        expandButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                delegate.onExpandButtonClicked();
            }
        });

        collapseButton.setTitle(locale.changeListCollapseAllButtonTitle());
        collapseButton.getElement().setInnerHTML(FontAwesome.COMPRESS);
        collapseButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                delegate.onCollapseButtonClicked();
            }
        });

        Button btnClose = createButton(locale.buttonClose(), "git-compare-btn-close", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCloseClicked();
            }
        });
        addButtonToFooter(btnClose);

        btnCompare = createButton(locale.buttonCompare(), "git-compare-btn-compare", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCompareClicked();
            }
        });
        addButtonToFooter(btnCompare);
    }

    private List<Node> getGroupedNodes(Map<String, Status> items) {
        List<String> allFiles = new ArrayList<>(items.keySet());
        List<String> allPaths = new ArrayList<>();
        for (String file : allFiles) {
            String path = file.substring(0, file.lastIndexOf("/"));
            if (!allPaths.contains(path)) {
                allPaths.add(path);
            }
        }
        List<String> commonPaths = getCommonPaths(allPaths);
        for (String commonPath : commonPaths) {
            if (!allPaths.contains(commonPath)) {
                allPaths.add(commonPath);
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
            for (String path : allPaths) {
                if (!(Path.valueOf(path).segmentCount() == i - 1)) {
                    continue;
                }
                Node folder = new ChangedFolderNode(getTransitFolderName(allPaths, path), nodesResources);
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
                if (nodesToNest.isEmpty()) {
                    continue;
                }
                Collections.sort(nodesToNest, new NameComparator());
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
        Collections.sort(nodes, new NameComparator());
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
