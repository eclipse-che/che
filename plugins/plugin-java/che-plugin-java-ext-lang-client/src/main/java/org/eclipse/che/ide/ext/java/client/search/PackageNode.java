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
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasNewPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NewNodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.lsp4j.SymbolKind;

public class PackageNode extends AbstractTreeNode implements HasNewPresentation {

  private SearchResult pkg;
  private PromiseProvider promiseProvider;
  private JavaResources resources;
  private NodeFactory nodeFactory;

  @Inject
  public PackageNode(
      @Assisted SearchResult pkg,
      PromiseProvider promiseProvider,
      JavaResources resources,
      NodeFactory nodeFactory) {
    this.pkg = pkg;
    this.promiseProvider = promiseProvider;
    this.resources = resources;
    this.nodeFactory = nodeFactory;
  }

  @Override
  public String getName() {
    return pkg.getName();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public NewNodePresentation getPresentation() {
    String container =
        UsagesNode.toPath(pkg.getUri()).removeFirstSegments(1).makeRelative().toString();
    return new NewNodePresentation.Builder()
        .withIcon(resources.packageItem())
        .withNodeText(pkg.getName())
        .withNodeInfoText("- " + container)
        .build();
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return promiseProvider.create(
        Executor.create(
            (resolve, reject) -> {
              try {
                resolve.apply(computeChildren());
              } catch (Exception e) {
                reject.apply(JsPromiseError.create(e));
              }
            }));
  }

  private List<Node> computeChildren() {
    List<Node> result = new ArrayList<>();
    for (SearchResult child : pkg.getChildren()) {
      if (child.getKind() == SymbolKind.File && child.getMatches().isEmpty()) {
        // skip file level if no matches
        child
            .getChildren()
            .forEach(grandchild -> result.add(nodeFactory.createElementNode(grandchild)));
      } else {
        result.add(nodeFactory.createElementNode(child));
      }
    }
    return result;
  }
}
