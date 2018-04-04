/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.compare.changespanel;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * Node Element used for setting it to TreeNodeStorage and viewing folders that contain changed
 * files.
 *
 * @author Igor Vinokur
 */
public class ChangedFolderNode extends AbstractTreeNode implements HasPresentation {

  private final Path path;
  private final String name;
  private final NodesResources nodesResources;

  private NodePresentation nodePresentation;

  /**
   * Create instance of ChangedFolderNode.
   *
   * @param name name of the folder that represents this node
   * @param nodesResources resources that contain icons
   */
  ChangedFolderNode(String name, Path path, NodesResources nodesResources) {
    this.name = name;
    this.path = path;
    this.nodesResources = nodesResources;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return Promises.resolve(children);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableText(name);
    presentation.setPresentableIcon(nodesResources.simpleFolder());
  }

  @Override
  public NodePresentation getPresentation(boolean update) {
    if (nodePresentation == null) {
      nodePresentation = new NodePresentation();
      updatePresentation(nodePresentation);
    }

    if (update) {
      updatePresentation(nodePresentation);
    }
    return nodePresentation;
  }

  /** Returns path of the node. */
  public Path getPath() {
    return path;
  }
}
