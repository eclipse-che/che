package org.eclipse.che.ide.ext.git.client.panel;

import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * Represents an item in the repositories list.
 *
 * @author Mykola Morhun
 */
public class RepositoryNode extends AbstractTreeNode implements HasPresentation {

  private NodePresentation nodePresentation;

  private final String repository;
  private int changedFiles;

  public RepositoryNode(String repository) {
    this.repository = repository;
    this.changedFiles = 0;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    presentation.setPresentableText(repository + " : " + changedFiles);
  }

  @Override
  public NodePresentation getPresentation(boolean update) {
    if (nodePresentation == null) {
      nodePresentation = new NodePresentation();
      updatePresentation(nodePresentation);
      return nodePresentation;
    }

    if (update) {
      updatePresentation(nodePresentation);
    }
    return nodePresentation;
  }

  @Override
  public String getName() {
    return repository;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    // no children are supported
    return null;
  }
}
