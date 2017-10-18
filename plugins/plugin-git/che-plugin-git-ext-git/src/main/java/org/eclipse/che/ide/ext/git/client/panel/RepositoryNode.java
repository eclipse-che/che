package org.eclipse.che.ide.ext.git.client.panel;

import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import elemental.html.SpanElement;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Represents an item in the repositories list.
 *
 * @author Mykola Morhun
 */
public class RepositoryNode extends AbstractTreeNode implements HasPresentation {
  private static final String CHANGES_PREFIX = "\u2213"; // minus-plus sign '∓'

  private final GitResources gitResources;

  private NodePresentation nodePresentation;

  private final String repository;
  private int changes;

  /**
   * Creates new repository entry.
   *
   * @param repository repository name
   * @param changes number of changed file in the repository
   */
  @Inject
  public RepositoryNode(
      @Assisted String repository, @Assisted Integer changes, GitResources gitResources) {
    this.repository = repository;
    this.changes = changes;

    this.gitResources = gitResources;
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
    presentation.setPresentableTextCss("margin-left: 2px;");
    presentation.setPresentableIcon(gitResources.gitLogo());

    if (changes > 0) {
      SpanElement spanElement =
          Elements.createSpanElement(gitResources.gitPanelCss().repositoryChangesLabel());
      spanElement.setInnerText(CHANGES_PREFIX + changes);
      presentation.setUserElement((Element) spanElement);
    } else {
      presentation.setUserElement(null); // remove label from node
    }
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
