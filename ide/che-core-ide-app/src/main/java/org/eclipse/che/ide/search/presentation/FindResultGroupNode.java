/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.search.presentation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.ui.smartTree.compare.NameComparator;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * Tree node represent search result.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
public class FindResultGroupNode extends AbstractTreeNode implements HasPresentation {

  private final CoreLocalizationConstant locale;

  private NodePresentation nodePresentation;
  private FindResultNodeFactory nodeFactory;
  private PromiseProvider promiseProvider;
  private List<SearchResult> findResults;
  private String request;

  @Inject
  public FindResultGroupNode(
      CoreLocalizationConstant locale,
      FindResultNodeFactory nodeFactory,
      PromiseProvider promiseProvider,
      @Assisted List<SearchResult> findResult,
      @Assisted String request) {
    this.locale = locale;
    this.nodeFactory = nodeFactory;
    this.promiseProvider = promiseProvider;
    this.findResults = findResult;
    this.request = request;
  }

  /** {@inheritDoc} */
  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    List<Node> fileNodes = new ArrayList<>();
    for (SearchResult searchResult : findResults) {
      FoundItemNode foundItemNode = nodeFactory.newFoundItemNode(searchResult, request);
      fileNodes.add(foundItemNode);
    }
    //sort nodes by file name
    Collections.sort(fileNodes, new NameComparator());

    return promiseProvider.resolve(fileNodes);
  }

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return locale.actionFullTextSearch();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isLeaf() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    int total = 0;
    for (SearchResult searchResult : findResults) {
      total += searchResult.getOccurrences().size();
    }
    StringBuilder resultTitle =
        new StringBuilder("Found occurrences of '" + request + "\'  (" + total + " occurrence");
    if (total > 1) {
      resultTitle.append("s)");
    } else {
      resultTitle.append(")");
    }
    presentation.setPresentableText(resultTitle.toString());
  }
}
