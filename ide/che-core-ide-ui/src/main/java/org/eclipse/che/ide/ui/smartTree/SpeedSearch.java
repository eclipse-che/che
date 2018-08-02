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
package org.eclipse.che.ide.ui.smartTree;

import static com.google.gwt.dom.client.Style.BorderStyle.SOLID;
import static com.google.gwt.dom.client.Style.Position.FIXED;
import static com.google.gwt.dom.client.Style.Unit.PX;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_BACKSPACE;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ESCAPE;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.ide.api.theme.Style.theme;

import com.google.common.base.Predicate;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.ui.smartTree.converter.NodeConverter;
import org.eclipse.che.ide.ui.smartTree.converter.impl.NodeNameConverter;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * @author Vlad Zhukovskiy
 * @author Igor Vinokur
 */
class SpeedSearch {

  private static final String ID = "speedSearch";
  private static final int SEARCH_DELAY_MS = 100;

  private final Tree tree;
  private final NodeStorage nodeStorage;
  private final NodeConverter<Node, String> nodeConverter;
  private final SpeedSearchRender searchRender;
  private final boolean filterNodes;
  private DelayedTask searchTask;
  private StringBuilder searchRequest;
  private SearchPopUp searchPopUp;
  private List<Node> savedNodes;
  private List<Node> filteredNodes;

  private boolean update;

  /**
   * Searches and highlights matchings in the given tree by pattern generated from entered search
   * request.
   *
   * @param tree the tree to search in
   * @param matchingStyle style of the matching
   * @param nodeConverter instance of the {@link NodeConverter}
   * @param filterNodes a flag that indicates if needed to remove nodes from the tree that doesn't
   *     match the search pattern
   */
  SpeedSearch(
      Tree tree,
      String matchingStyle,
      NodeConverter<Node, String> nodeConverter,
      boolean filterNodes) {
    this.tree = tree;
    this.filterNodes = filterNodes;
    this.searchRequest = new StringBuilder();
    this.searchRender = new SpeedSearchRender(tree.getTreeStyles(), matchingStyle);
    this.nodeConverter = nodeConverter != null ? nodeConverter : new NodeNameConverter();
    this.nodeStorage = tree.getNodeStorage();
    this.savedNodes = new ArrayList<>();
    this.filteredNodes = new ArrayList<>();
    this.tree.setPresentationRenderer(searchRender);
    this.tree.addKeyPressHandler(
        event -> {
          event.stopPropagation();
          searchRequest.append(String.valueOf(event.getCharCode()));
          update();
        });
    this.tree.addKeyDownHandler(
        event -> {
          switch (event.getNativeKeyCode()) {
            case KEY_ENTER:
              removeSearchPopUpFromTreeIfIsShown();
              break;
            case KEY_BACKSPACE:
              if (!searchRequest.toString().isEmpty()) {
                event.preventDefault();
                searchRequest.setLength(searchRequest.length() - 1);
                doSearch();
              }
              break;
            case KEY_ESCAPE:
              if (searchRequest.length() != 0) {
                event.stopPropagation();
                searchRequest.setLength(0);
                update();
              }
              break;
          }
        });
    this.tree.addExpandHandler(
        event -> {
          if (update) {
            savedNodes = new ArrayList<>(getVisibleNodes());
            if (getVisibleNodes()
                .stream()
                .filter(node -> !node.isLeaf())
                .allMatch(tree::isExpanded)) {
              update = false;
              doSearch();
            }
          } else {
            Node expandedNode = event.getNode();
            List<Node> visibleChildren = nodeStorage.getChildren(expandedNode);
            updateSavedNodes(expandedNode, visibleChildren);
            if (visibleChildren.size() != getFilteredChildren(expandedNode).size()) {
              visibleChildren
                  .stream()
                  .filter(node -> !matchesToSearchRequest().apply(node))
                  .forEach(nodeStorage::remove);
            }
          }
        });

    this.nodeStorage.addStoreClearHandler(event -> update = true);

    initSearchPopUp();
  }

  // Update saved nodes with nodes that were redrawn
  private void updateSavedNodes(Node parent, List<Node> children) {
    List<Node> savedChildren =
        savedNodes
            .stream()
            .filter(node -> node.getParent() != null && node.getParent().equals(parent))
            .collect(toList());
    if (savedChildren.size() == children.size()) {
      savedChildren.forEach(
          node -> {
            int i = savedNodes.indexOf(node);
            savedNodes.remove(node);
            savedNodes.add(i, children.get(savedChildren.indexOf(node)));
          });
    }
  }

  private void initSearchPopUp() {
    searchPopUp = new SearchPopUp();
    Style style = searchPopUp.getElement().getStyle();

    style.setBackgroundColor(theme.backgroundColor());
    style.setBorderStyle(SOLID);
    style.setBorderColor(theme.getPopupBorderColor());
    style.setBorderWidth(1, PX);
    style.setPadding(2, PX);
    style.setPosition(FIXED);
    style.setBottom(33, PX);
    style.setLeft(20, PX);
  }

  private void addSearchPopUpToTreeIfNotDisplayed() {
    if (Document.get().getElementById(ID) == null) {
      searchPopUp.setVisible(true);
      tree.getParent().getElement().appendChild(searchPopUp.getElement());
    }
  }

  private void removeSearchPopUpFromTreeIfIsShown() {
    searchRequest.setLength(0);
    Element popUp = Document.get().getElementById(ID);
    if (popUp != null) {
      popUp.removeFromParent();
    }
  }

  private void update() {
    if (searchTask == null) {
      searchTask =
          new DelayedTask() {
            @Override
            public void onExecute() {
              doSearch();
            }
          };
    }
    searchTask.delay(SEARCH_DELAY_MS);
  }

  void closePopUp() {
    removeSearchPopUpFromTreeIfIsShown();
    searchRequest.setLength(0);
    searchRender.setRequestPattern("");
    searchRender.setSearchRequest("");
  }

  private void doSearch() {
    if (searchRequest.toString().isEmpty()) {
      removeSearchPopUpFromTreeIfIsShown();
    } else {
      addSearchPopUpToTreeIfNotDisplayed();
      searchPopUp.setSearchRequest(searchRequest.toString());
    }
    searchRender.setSearchRequest(searchRequest.toString());
    searchRender.setRequestPattern(getSearchPattern());
    tree.getSelectionModel().deselectAll();
    List<Node> filteredChildNodes =
        savedNodes.stream().filter(matchesToSearchRequest()::apply).collect(toList());
    filteredNodes =
        savedNodes
            .stream()
            .filter(
                savedNode ->
                    matchesToSearchRequest().apply(savedNode)
                        || filteredChildNodes
                            .stream()
                            .anyMatch(filteredNode -> nestedNodeOf(filteredNode, savedNode)))
            .collect(toList());

    if (filterNodes) {
      for (Node savedNode : savedNodes) {
        if (!filteredNodes.contains(savedNode)) {
          if (getVisibleNodes().contains(savedNode)) {
            nodeStorage.remove(savedNode);
          }
        } else if (!getVisibleNodes().contains(savedNode)) {
          Node parent = savedNode.getParent();
          if (parent == null) {
            nodeStorage.add(savedNode);
          } else if (getVisibleNodes().contains(parent)) {
            List<Node> filteredChildren = getFilteredChildren(parent);
            if (filteredChildren.contains(savedNode)) {
              nodeStorage.insert(parent, filteredChildren.indexOf(savedNode), savedNode);
            }
          }
        }
      }
      tree.expandAll();
    }
    getVisibleNodes().forEach(tree::refresh);

    setSelection();
  }

  private void setSelection() {
    Optional<Node> startsOptional =
        filteredNodes
            .stream()
            .filter(
                node ->
                    node.getName().toLowerCase().startsWith(searchRequest.toString().toLowerCase()))
            .findFirst();
    Optional<Node> containsOptional =
        filteredNodes
            .stream()
            .filter(
                node ->
                    node.getName().toLowerCase().contains(searchRequest.toString().toLowerCase()))
            .findFirst();
    Optional<Node> matchesOptional =
        filteredNodes
            .stream()
            .filter(node -> node.getName().toLowerCase().matches(getSearchPattern()))
            .findFirst();

    if (startsOptional.isPresent()) {
      tree.getSelectionModel().select(startsOptional.get(), true);
    } else if (containsOptional.isPresent()) {
      tree.getSelectionModel().select(containsOptional.get(), true);
    } else if (matchesOptional.isPresent()) {
      tree.getSelectionModel().select(matchesOptional.get(), true);
    } else {
      filteredNodes
          .stream()
          .findFirst()
          .ifPresent(node -> tree.getSelectionModel().select(node, true));
    }
  }

  private List<Node> getFilteredChildren(Node parent) {
    return filteredNodes
        .stream()
        .filter(node -> node.getParent() != null && node.getParent().equals(parent))
        .collect(toList());
  }

  /** Returns {@code true} if given node is nested in given parent. */
  private boolean nestedNodeOf(Node node, Node parent) {
    while (node != null) {
      node = node.getParent();
      if (parent.equals(node)) {
        return true;
      }
    }
    return false;
  }

  private List<Node> getVisibleNodes() {
    List<Node> rootNodes = tree.getRootNodes();
    return tree.getAllChildNodes(rootNodes, true);
  }

  private Predicate<Node> matchesToSearchRequest() {
    return inputNode -> {
      String nodeString = nodeConverter.convert(inputNode);
      return nodeString.toLowerCase().matches(getSearchPattern().toLowerCase());
    };
  }

  private String getSearchPattern() {
    StringBuilder pattern = new StringBuilder(".*");
    for (int i = 0; i < searchRequest.length(); i++) {
      pattern.append(searchRequest.charAt(i)).append(".*");
    }
    return pattern.toString().toLowerCase();
  }

  private class SearchPopUp extends HorizontalPanel {
    private Label searchText;

    private SearchPopUp() {
      getElement().setId(ID);
      setVisible(false); // by default

      Label icon = new Label();
      icon.getElement().setInnerHTML(FontAwesome.SEARCH);
      Style iconStyle = icon.getElement().getStyle();
      iconStyle.setFontSize(16, PX);
      iconStyle.setMarginLeft(5, PX);
      iconStyle.setMarginRight(5, PX);

      searchText = new Label();
      Style searchTextStyle = searchText.getElement().getStyle();
      searchTextStyle.setFontSize(12, PX);
      searchTextStyle.setMarginRight(5, PX);
      searchTextStyle.setMarginTop(4, PX);

      add(icon);
      add(searchText);
    }

    private void setSearchRequest(String request) {
      searchText.setText(request);
    }
  }
}
