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

import com.google.gwt.dom.client.Element;
import com.google.inject.assistedinject.Assisted;
import elemental.html.SpanElement;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.project.shared.SearchOccurrence;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.resources.SearchItemReference;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Instance describe file node where found given text
 *
 * @author Vitalii Parfonov
 */
public class FoundItemNode extends AbstractTreeNode implements HasPresentation {

  private NodePresentation nodePresentation;
  private FindResultNodeFactory nodeFactory;
  private PromiseProvider promiseProvider;
  private Resources resources;
  private SearchItemReference searchItemReference;
  private String request;

  @Inject
  public FoundItemNode(
      FindResultNodeFactory nodeFactory,
      PromiseProvider promiseProvider,
      Resources resources,
      @Assisted SearchItemReference searchItemReference,
      @Assisted String request) {
    this.nodeFactory = nodeFactory;
    this.promiseProvider = promiseProvider;
    this.resources = resources;
    this.searchItemReference = searchItemReference;
    this.request = request;
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
    return searchItemReference.getName();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isLeaf() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    StringBuilder resultTitle = new StringBuilder();
    resultTitle.append(" (");
    resultTitle.append(searchItemReference.getOccurrences().size());
    resultTitle.append(" occurrence");
    if (searchItemReference.getOccurrences().size() > 1) {
      resultTitle.append('s');
    }
    resultTitle.append(" of '");
    resultTitle.append(request);
    resultTitle.append('\'');
    resultTitle.append(" found)");
    presentation.setPresentableText(resultTitle.toString());
    SpanElement spanElement = Elements.createSpanElement(resources.coreCss().foundItem());
    spanElement.setId(searchItemReference.getPath());
    spanElement.setInnerText(searchItemReference.getPath());
    presentation.setUserElement((Element) spanElement);
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    List<Node> fileNodes;
    List<SearchOccurrence> occurrences = searchItemReference.getOccurrences();
    occurrences.sort(
        Comparator.comparingInt(
            (SearchOccurrence searchOccurrence) -> searchOccurrence.getLineNumber()));
    fileNodes =
        occurrences
            .stream()
            .map(
                occurrence ->
                    nodeFactory.newFoundOccurrenceNode(occurrence, searchItemReference.getPath()))
            .collect(Collectors.toList());
    return promiseProvider.resolve(fileNodes);
  }
}
