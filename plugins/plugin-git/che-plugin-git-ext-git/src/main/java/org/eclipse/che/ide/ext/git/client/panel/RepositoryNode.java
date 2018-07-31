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
package org.eclipse.che.ide.ext.git.client.panel;

import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import elemental.html.SpanElement;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasNewPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NewNodePresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.StyleConfigurator;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Represents an item in the repositories list.
 *
 * @author Mykola Morhun
 */
public class RepositoryNode extends AbstractTreeNode implements HasNewPresentation {
  private static final String CHANGES_PREFIX = "\u2213"; // minus-plus sign '∓'

  private final GitResources gitResources;

  private NewNodePresentation.Builder nodePresentationBuilder;

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

    createBaseNodePresentation();
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

  private void createBaseNodePresentation() {
    StyleConfigurator.Builder styleBuilder = new StyleConfigurator.Builder();
    styleBuilder.withProperty("marginLeft", "2px");
    StyleConfigurator repositoryNodeStyle = styleBuilder.build();

    nodePresentationBuilder = new NewNodePresentation.Builder();
    nodePresentationBuilder
        .withNodeText(repository)
        .withNodeTextStyle(repositoryNodeStyle)
        .withIcon(gitResources.gitLogo());
  }

  @Override
  public NewNodePresentation getPresentation() {
    // Immutable part of node presentation is cached in nodePresentationBuilder field.
    // Every time here should be set variable parts of the node builder.
    if (changes > 0) {
      SpanElement spanElement =
          Elements.createSpanElement(gitResources.gitPanelCss().repositoryChangesLabel());
      spanElement.setInnerText(CHANGES_PREFIX + changes);
      nodePresentationBuilder.withUserElement((Element) spanElement);
    } else {
      nodePresentationBuilder.withUserElement(null); // remove label from node
    }

    return nodePresentationBuilder.build();
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
