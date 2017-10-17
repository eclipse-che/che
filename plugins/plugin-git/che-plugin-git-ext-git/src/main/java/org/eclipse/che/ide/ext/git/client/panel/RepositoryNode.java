package org.eclipse.che.ide.ext.git.client.panel;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
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
  private int changes;

  /**
   * Creates new repository entry.
   *
   * @param repository repository name
   * @param changes number of changed file in the repository
   */
  public RepositoryNode(String repository, int changes) {
    this.repository = repository;
    this.changes = changes;
  }

  public String getRepository() {
    return repository;
  }

  public int getChanges() {
    return changes;
  }

  public void setChanges(int changes) {
    this.changes = changes;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    presentation.setPresentableText(repository);
    presentation.setInfoText(String.valueOf(changes));
    presentation.setInfoTextCss("color: blue; text-align: right; background-color: yellow;"); // TODO use resources
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
    return Promises.resolve(Collections.<Node>emptyList()); // TODO do not use deprecated
  }
}
