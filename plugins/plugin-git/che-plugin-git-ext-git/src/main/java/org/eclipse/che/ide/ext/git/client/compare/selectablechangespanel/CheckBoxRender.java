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

    // Set check-box state.
    final Path nodePath =
        node instanceof ChangedFileNode
            ? Path.valueOf(node.getName())
            : ((ChangedFolderNode) node).getPath();

    if (indeterminate.contains(nodePath)) {
      checkBoxInputElement.setId(checkBoxInputElement.getId() + "-indeterminate");
      setIndeterminate(checkBoxInputElement);
    } else if (!unselected.contains(nodePath)) {
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
            delegate.refreshNodes();
          }
        });

    // Paste check-box element to node container.
    nodeContainer.insertAfter(checkBoxElement, nodeContainer.getFirstChild());

    return rootContainer;
  }

  void setNodePaths(Set<Path> paths) {
    allNodePaths = paths;
    unselected.clear();
    unselected.addAll(paths);
  }

  private native void setIndeterminate(Element checkbox) /*-{
        checkbox.indeterminate = true;
    }-*/;

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
                    && !hasSelectedChildes(path))
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

    if (hasSelectedChildes(path) && !hasAllSelectedChildes(path)) {
      indeterminate.add(path);
    } else {
      indeterminate.remove(path);
    }
  }

  private boolean hasSelectedChildes(Path givenPath) {
    return allNodePaths
        .stream()
        .anyMatch(
            path ->
                givenPath.isPrefixOf(path)
                    && !path.equals(givenPath)
                    && !unselected.contains(path));
  }

  private boolean hasAllSelectedChildes(Path givenPath) {
    return allNodePaths
        .stream()
        .filter(path -> !(path.equals(givenPath)) && givenPath.isPrefixOf(path))
        .noneMatch(unselected::contains);
  }
}
