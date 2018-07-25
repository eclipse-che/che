/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CheckBox;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangedFileNode;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangedFolderNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;

class CheckBoxRender extends DefaultPresentationRenderer<Node> {

  private final SelectableChangesPanelView.ActionDelegate delegate;
  private Set<Path> allNodePaths;

  private final Set<Path> unselected;
  private final Set<Path> indeterminate;

  CheckBoxRender(TreeStyles treeStyles, SelectableChangesPanelView.ActionDelegate delegate) {
    super(treeStyles);

    this.delegate = delegate;
    this.allNodePaths = new HashSet<>();
    this.unselected = new HashSet<>();
    this.indeterminate = new HashSet<>();
  }

  @Override
  public Element render(
      final Node node, final String domID, final Tree.Joint joint, final int depth) {
    // Initialize HTML elements.
    final Element rootContainer = super.render(node, domID, joint, depth);
    final Element nodeContainer = rootContainer.getFirstChildElement();
    final Element checkBoxElement = new CheckBox().getElement();
    final InputElement checkBoxInputElement =
        (InputElement) checkBoxElement.getElementsByTagName("input").getItem(0);

    final Path nodePath =
        node instanceof ChangedFileNode
            ? Path.valueOf(node.getName())
            : ((ChangedFolderNode) node).getPath();
    setCheckBoxState(nodePath, checkBoxInputElement);
    setCheckBoxClickHandler(nodePath, checkBoxElement, checkBoxInputElement.isChecked());

    // Paste check-box element to node container.
    nodeContainer.insertAfter(checkBoxElement, nodeContainer.getFirstChild());

    return rootContainer;
  }

  private void setCheckBoxState(Path nodePath, InputElement checkBoxInputElement) {
    if (indeterminate.contains(nodePath)) {
      checkBoxInputElement.setId(checkBoxInputElement.getId() + "-indeterminate");
      setIndeterminate(checkBoxInputElement);
    } else if (!unselected.contains(nodePath)) {
      checkBoxInputElement.setChecked(true);
      checkBoxInputElement.setId(checkBoxInputElement.getId() + "-checked");
    } else {
      checkBoxInputElement.setId(checkBoxInputElement.getId() + "-unchecked");
    }
  }

  private void setCheckBoxClickHandler(Path nodePath, Element checkBoxElement, boolean isChecked) {
    Event.sinkEvents(checkBoxElement, Event.ONCLICK);
    Event.setEventListener(
        checkBoxElement,
        event -> {
          if (Event.ONCLICK == event.getTypeInt()
              && event.getTarget().getTagName().equalsIgnoreCase("label")) {
            handleCheckBoxSelection(nodePath, isChecked);
            delegate.refreshNodes();
          }
        });
  }

  private native void setIndeterminate(Element checkbox) /*-{
        checkbox.indeterminate = true;
    }-*/;

  void setNodePaths(Set<Path> paths) {
    allNodePaths = paths;
    unselected.clear();
    unselected.addAll(paths);
  }

  /**
   * Mark all related to node check-boxes checked or unchecked according to node path and value.
   * E.g. if parent check-box is marked as checked, all child check-boxes will be checked too, and
   * vise-versa.
   */
  void handleCheckBoxSelection(Path nodePath, boolean value) {
    allNodePaths
        .stream()
        .sorted(Comparator.comparing(Path::toString))
        .filter(
            path ->
                !(path.equals(nodePath) || path.isEmpty())
                    && path.isPrefixOf(nodePath)
                    && !hasSelectedChildren(path))
        .forEach(path -> handleCheckBoxState(path, value));

    allNodePaths
        .stream()
        .sorted((path1, path2) -> path2.toString().compareTo(path1.toString()))
        .filter(path -> !path.isEmpty() && (nodePath.isPrefixOf(path) || path.isPrefixOf(nodePath)))
        .forEach(path -> handleCheckBoxState(path, value));
  }

  private void handleCheckBoxState(Path path, boolean isChecked) {
    if (isChecked) {
      unselected.add(path);
    } else {
      unselected.remove(path);
    }

    if (delegate.getAllFiles().contains(path.toString())) {
      delegate.onFileNodeCheckBoxValueChanged(path, !isChecked);
    }

    if (hasSelectedChildren(path) && !hasAllSelectedChildren(path)) {
      indeterminate.add(path);
    } else {
      indeterminate.remove(path);
    }
  }

  private boolean hasSelectedChildren(Path givenPath) {
    return allNodePaths
        .stream()
        .anyMatch(
            path ->
                givenPath.isPrefixOf(path)
                    && !path.equals(givenPath)
                    && !unselected.contains(path));
  }

  private boolean hasAllSelectedChildren(Path givenPath) {
    return allNodePaths
        .stream()
        .filter(path -> !(path.equals(givenPath)) && givenPath.isPrefixOf(path))
        .noneMatch(unselected::contains);
  }
}
