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
package org.eclipse.che.ide.ext.git.client.compare.changespanel;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView.ActionDelegate;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * Node Element used for setting it to TreeNodeStorage and viewing changed files.
 *
 * @author Igor Vinokur
 */
public class ChangedFileNode extends AbstractTreeNode implements HasPresentation, HasAction {

  private NodePresentation nodePresentation;

  private final String pathName;
  private final Status status;
  private final GitResources gitResources;
  private final ActionDelegate actionDelegate;
  private final boolean viewPath;

  /**
   * Create instance of ChangedFileNode.
   *
   * @param pathName name of the file that represents this node with its full path
   * @param status git status of the file that represents this node
   * @param gitResources resources that contain icons
   * @param actionDelegate sends delegated events from the view
   * @param viewPath <code>true</code> if it is needed to view file name with its full path, and
   *     <code>false</code> if it is needed to view only name of the file
   */
  ChangedFileNode(
      String pathName,
      Status status,
      GitResources gitResources,
      ActionDelegate actionDelegate,
      boolean viewPath) {
    this.pathName = pathName;
    this.status = status;
    this.gitResources = gitResources;
    this.actionDelegate = actionDelegate;
    this.viewPath = viewPath;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return Promises.resolve(Collections.<Node>emptyList());
  }

  @Override
  public String getName() {
    return pathName;
  }

  /**
   * Git status of the file.
   *
   * @return Git status of the file
   */
  public Status getStatus() {
    return status;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    String name = Path.valueOf(pathName).lastSegment();
    presentation.setPresentableText(viewPath ? name : pathName);

    switch (status) {
      case MODIFIED:
        presentation.setPresentableIcon(gitResources.iconModified());
        return;
      case DELETED:
        presentation.setPresentableIcon(gitResources.iconDeleted());
        return;
      case ADDED:
        presentation.setPresentableIcon(gitResources.iconAdded());
        return;
      case RENAMED:
        presentation.setPresentableIcon(gitResources.iconRenamed());
        return;
      case COPIED:
        presentation.setPresentableIcon(gitResources.iconCopied());
        return;
      case UNTRACKED:
        presentation.setPresentableIcon(gitResources.iconUntracked());
        return;
      default:
    }
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

  @Override
  public void actionPerformed() {
    actionDelegate.onFileNodeDoubleClicked(pathName, status);
  }
}
