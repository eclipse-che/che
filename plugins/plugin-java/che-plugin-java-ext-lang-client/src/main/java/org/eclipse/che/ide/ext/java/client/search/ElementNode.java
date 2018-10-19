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
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasNewPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NewNodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.che.plugin.languageserver.ide.navigation.symbol.SymbolKindHelper;

public class ElementNode extends AbstractTreeNode implements HasNewPresentation {

  private SearchResult element;
  private PromiseProvider promiseProvider;
  private NodeFactory nodeFactory;
  private FindUsagesPresenter presenter;
  private SymbolKindHelper symbolHelper;

  @Inject
  public ElementNode(
      @Assisted SearchResult pkg,
      PromiseProvider promiseProvider,
      NodeFactory nodeFactory,
      FindUsagesPresenter presenter,
      SymbolKindHelper symbolHelper) {
    this.element = pkg;
    this.promiseProvider = promiseProvider;
    this.nodeFactory = nodeFactory;
    this.presenter = presenter;
    this.symbolHelper = symbolHelper;
  }

  @Override
  public String getName() {
    return element.getName();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public NewNodePresentation getPresentation() {
    return new NewNodePresentation.Builder()
        .withIcon(symbolHelper.getIcon(element.getKind()))
        .withNodeText(element.getName())
        .build();
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    if (!element.getMatches().isEmpty()) {
      return presenter
          .computeMatches(this)
          .then(
              (List<MatchNode> matches) -> {
                List<Node> result = new ArrayList<>(matches);
                result.addAll(computeChildren());
                return result;
              });
    } else {
      return promiseProvider.create(
          Executor.create(
              (resolve, reject) -> {
                resolve.apply(computeChildren());
              }));
    }
  }

  private List<Node> computeChildren() {
    List<Node> result = new ArrayList<>();

    for (SearchResult child : element.getChildren()) {
      result.add(nodeFactory.createElementNode(child));
    }
    return result;
  }

  public SearchResult getElement() {
    return element;
  }
}
