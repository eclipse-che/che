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

import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import elemental.html.SpanElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.UrlBuilder;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasNewPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NewNodePresentation;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;

public class UsagesNode extends AbstractTreeNode implements HasNewPresentation {

  private NodeFactory nodeFactory;
  private String name;
  private UsagesResponse response;
  private PromiseProvider promiseProvider;
  private JavaResources resources;
  private TreeStyles styles;

  @Inject
  public UsagesNode(
      @Assisted UsagesResponse response,
      NodeFactory nodeFactory,
      PromiseProvider promiseProvider,
      TreeStyles styles,
      JavaResources resources) {
    this.response = response;
    this.nodeFactory = nodeFactory;
    this.promiseProvider = promiseProvider;
    this.resources = resources;
    this.styles = styles;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isLeaf() {
    return response.getSearchResults().isEmpty();
  }

  @Override
  public NewNodePresentation getPresentation() {
    SpanElement spanElement =
        Elements.createSpanElement(styles.treeStylesCss().presentableTextContainer());
    spanElement.setInnerHTML(
        "Usages of <span class=\""
            + resources.css().searchMatch()
            + "\">"
            + response.getSearchedElement()
            + "</span> ["
            + calculateMatchCount(response)
            + " occurrences]");

    return new NewNodePresentation.Builder().withUserElement((Element) spanElement).build();
  }

  private int calculateMatchCount(UsagesResponse response) {
    return calculateMatchCount(response.getSearchResults());
  }

  private int calculateMatchCount(List<SearchResult> searchResults) {
    int count = 0;
    for (SearchResult result : searchResults) {
      count += calculateMatchCount(result);
    }
    return count;
  }

  private int calculateMatchCount(SearchResult result) {
    int count = result.getMatches().size();
    count += calculateMatchCount(result.getChildren());
    return count;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    try {
      Map<String, List<SearchResult>> children = new HashMap<>();
      for (SearchResult result : response.getSearchResults()) {
        Path p = toPath(result.getUri());
        if (p.isEmpty()) {
          throw new IllegalArgumentException("Path of length 0: " + result.getUri());
        }
        List<SearchResult> results = children.get(p.segment(0));
        if (results == null) {
          results = new ArrayList<>();
          children.put(p.segment(0), results);
        }
        results.add(result);
      }

      return promiseProvider.resolve(
          children
              .entrySet()
              .stream()
              .map(entry -> nodeFactory.createProject(entry.getKey(), entry.getValue()))
              .collect(Collectors.toList()));
    } catch (Exception e) {
      return promiseProvider.reject(e);
    }
  }

  static Path toPath(String uri) {
    return uri.startsWith("/") ? new Path(uri) : new Path(new UrlBuilder(uri).getPath());
  }
}
