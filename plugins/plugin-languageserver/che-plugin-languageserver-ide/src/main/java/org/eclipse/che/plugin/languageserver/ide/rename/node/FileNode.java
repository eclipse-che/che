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
package org.eclipse.che.plugin.languageserver.ide.rename.node;

import static java.util.stream.Collectors.toList;

import com.google.inject.assistedinject.Assisted;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.AbstractPresentationNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameFile;

/** Tree node, represent {@link RenameFile} */
public class FileNode extends AbstractPresentationNode {

  private final NodesResources resources;
  private final PromiseProvider promiseProvider;
  private final RenameNodeFactory factory;
  private final RenameFile renameFile;

  private final List<Node> child;

  @Inject
  public FileNode(
      NodesResources resources,
      PromiseProvider promiseProvider,
      RenameNodeFactory factory,
      @Assisted RenameFile renameFile) {
    this.resources = resources;
    this.promiseProvider = promiseProvider;
    this.factory = factory;
    this.renameFile = renameFile;
    child = renameFile.getChanges().stream().map(factory::create).collect(toList());
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return promiseProvider.resolve(child);
  }

  @Override
  public String getName() {
    return renameFile.getFileName();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    presentation.setPresentableText(renameFile.getFileName());
    presentation.setPresentableIcon(resources.file());
  }
}
