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
package org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangedFileNode;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangedFolderNode;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelViewImpl;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;

/**
 * Implementation of {@link SelectableChangesPanelView}.
 *
 * @author Igor Vinokur
 */
@Singleton
public class SelectableChangesPanelViewImpl extends ChangesPanelViewImpl
    implements SelectableChangesPanelView {

  private SelectableChangesPanelView.ActionDelegate delegate;
  private CheckBoxRender render;

  @Inject
  public SelectableChangesPanelViewImpl(
      GitResources resources, GitLocalizationConstant locale, NodesResources nodesResources) {
    super(resources, locale, nodesResources);
    this.render = new CheckBoxRender();
    super.setTreeRender(render);
  }

  @Override
  public void setDelegate(SelectableChangesPanelView.ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setMarkedCheckBoxes(Set<Path> paths) {
    render.setNodePaths(getNodePaths());
    paths.forEach(path -> render.handleCheckBoxSelection(path, false));
  }

  private class CheckBoxRender extends DefaultPresentationRenderer<Node> {

    private Set<Path> allNodePaths;

    private final Set<Path> unselectedNodePaths;
    private final Set<Path> indeterminateNodePaths;

    private CheckBoxRender() {
      super(SelectableChangesPanelViewImpl.super.getTreeStyles());

      this.allNodePaths = new HashSet<>();
      this.unselectedNodePaths = new HashSet<>();
      this.indeterminateNodePaths = new HashSet<>();
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

      // Set check-box state.
      final Path nodePath =
          node instanceof ChangedFileNode
              ? Path.valueOf(node.getName())
              : ((ChangedFolderNode) node).getPath();

      if (indeterminateNodePaths.contains(nodePath)) {
        checkBoxInputElement.setId(checkBoxInputElement.getId() + "-indeterminate");
        setIndeterminate(checkBoxInputElement);
      } else if (!unselectedNodePaths.contains(nodePath)) {
        checkBoxInputElement.setChecked(true);
        checkBoxInputElement.setId(checkBoxInputElement.getId() + "-checked");
      } else {
        checkBoxInputElement.setId(checkBoxInputElement.getId() + "-unchecked");
      }

      // Add check-box click handler.
      Event.sinkEvents(checkBoxElement, Event.ONCLICK);
      Event.setEventListener(
          checkBoxElement,
          event -> {
            if (Event.ONCLICK == event.getTypeInt()
                && event.getTarget().getTagName().equalsIgnoreCase("label")) {
              handleCheckBoxSelection(nodePath, checkBoxInputElement.isChecked());
              refreshNodes();
            }
          });

      // Paste check-box element to node container.
      nodeContainer.insertAfter(checkBoxElement, nodeContainer.getFirstChild());

      return rootContainer;
    }

    private void setNodePaths(Set<Path> paths) {
      allNodePaths = paths;
      unselectedNodePaths.clear();
      unselectedNodePaths.addAll(paths);
    }

    private native void setIndeterminate(Element checkbox) /*-{
        checkbox.indeterminate = true;
    }-*/;

    /**
     * Mark all related to node check-boxes checked or unchecked according to node path and value.
     * E.g. if parent check-box is marked as checked, all child check-boxes will be checked too, and
     * vise-versa.
     */
    private void handleCheckBoxSelection(Path nodePath, boolean value) {
      allNodePaths
          .stream()
          .sorted(Comparator.comparing(Path::toString))
          .filter(
              path ->
                  !(path.equals(nodePath) || path.isEmpty())
                      && path.isPrefixOf(nodePath)
                      && !hasSelectedChildes(path))
          .forEach(path -> handleCheckBoxState(path, value));

      allNodePaths
          .stream()
          .sorted((path1, path2) -> path2.toString().compareTo(path1.toString()))
          .filter(
              path -> !path.isEmpty() && (nodePath.isPrefixOf(path) || path.isPrefixOf(nodePath)))
          .forEach(path -> handleCheckBoxState(path, value));
    }

    private void handleCheckBoxState(Path path, boolean isChecked) {
      if (isChecked) {
        unselectedNodePaths.add(path);
      } else {
        unselectedNodePaths.remove(path);
      }

      if (delegate.getAllFiles().contains(path.toString())) {
        delegate.onFileNodeCheckBoxValueChanged(path, !isChecked);
      }

      if (hasSelectedChildes(path) && !hasAllSelectedChildes(path)) {
        indeterminateNodePaths.add(path);
      } else {
        indeterminateNodePaths.remove(path);
      }
    }

    private boolean hasSelectedChildes(Path givenPath) {
      return allNodePaths
          .stream()
          .anyMatch(
              path ->
                  givenPath.isPrefixOf(path)
                      && !path.equals(givenPath)
                      && !unselectedNodePaths.contains(path));
    }

    private boolean hasAllSelectedChildes(Path givenPath) {
      return allNodePaths
          .stream()
          .filter(path -> !(path.equals(givenPath)) && givenPath.isPrefixOf(path))
          .noneMatch(unselectedNodePaths::contains);
    }
  }
}
