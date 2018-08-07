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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasNewPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NewNodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;

public class ProjectNode extends AbstractTreeNode implements HasNewPresentation {

  private String name;
  private List<Node> packages;
  private PromiseProvider promiseProvider;
  private NodesResources nodeResources;

  @Inject
  public ProjectNode(
      @Assisted String name,
      @Assisted List<SearchResult> children,
      NodeFactory nodeFactory,
      PromiseProvider promiseProvider,
      NodesResources nodeResources) {
    this.name = name;
    this.packages =
        children
            .stream()
            .map(child -> nodeFactory.createPackage(child))
            .collect(Collectors.toList());
    this.promiseProvider = promiseProvider;
    this.nodeResources = nodeResources;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isLeaf() {
    return packages.isEmpty();
  }

  @Override
  public NewNodePresentation getPresentation() {
    return new NewNodePresentation.Builder()
        .withIcon(nodeResources.projectFolder())
        .withNodeText(name)
        .build();
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return promiseProvider.resolve(packages);
  }
}
