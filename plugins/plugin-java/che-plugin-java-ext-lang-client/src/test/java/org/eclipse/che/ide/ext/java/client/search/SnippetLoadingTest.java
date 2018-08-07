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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.che.api.languageserver.shared.model.SnippetParameters;
import org.eclipse.che.api.languageserver.shared.model.SnippetResult;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.client.promises.PromiseMocker;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.junit.Before;
import org.junit.Test;

public class SnippetLoadingTest {
  private TextDocumentServiceClient service;

  @Before
  public void setUp() {
    service = mock(TextDocumentServiceClient.class);
    List<SnippetResult> snippets =
        Arrays.asList(
            new SnippetResult(new LinearRange(2, 7), "foofoo", 37, new LinearRange(5, 18)),
            new SnippetResult(new LinearRange(200, 37), "foofoo", 37, new LinearRange(70, 18)),
            new SnippetResult(new LinearRange(27, 207), "foofoo", 37, new LinearRange(12, 18)),
            new SnippetResult(new LinearRange(13, 8), "foofoo", 37, new LinearRange(13, 8)));

    Promise<List<SnippetResult>> promise =
        new PromiseMocker<List<SnippetResult>>().applyOnThenFunction(snippets).getPromise();
    when(service.getSnippets(any(SnippetParameters.class))).thenReturn(promise);
  }

  @Test
  public void testSnippetMatching() {

    ElementNode[] node = new ElementNode[1];
    result("bar")
        .children(
            result("foo")
                .children(
                    result("foo")
                        .process(
                            n -> {
                              node[0] = n;
                            })
                        .match(200, 37)))
        .build(null);

    FindUsagesPresenter presenter =
        new FindUsagesPresenter(
            null, null, mock(FindUsagesView.class), null, service, null, null, null, null, null);
    presenter
        .computeMatches(node[0])
        .then(
            nodes -> {
              assertEquals(1, nodes.size());
              MatchNode match = nodes.get(0);
              assertEquals(
                  new SnippetResult(
                      new LinearRange(200, 37), "foofoo", 37, new LinearRange(70, 18)),
                  match.getSnippet());
            });
  }

  private static class SearchResultBuilder {
    private String uri;
    private List<SearchResultBuilder> children = new ArrayList<>();
    private List<LinearRange> matches = new ArrayList<>();
    private Consumer<ElementNode> processor;

    public SearchResultBuilder(String uri) {
      this.uri = uri;
    }

    public SearchResultBuilder children(SearchResultBuilder... builders) {
      children.addAll(Arrays.asList(builders));
      return this;
    }

    public SearchResultBuilder match(int offset, int length) {
      matches.add(new LinearRange(offset, length));
      return this;
    }

    public SearchResultBuilder process(Consumer<ElementNode> processor) {
      this.processor = processor;
      return this;
    }

    private ElementNode build(ElementNode parent) {
      SearchResult result = new SearchResult();
      final ElementNode node = mock(ElementNode.class);
      when(node.getParent()).thenReturn(parent);
      when(node.getElement()).thenReturn(result);
      result.setUri(uri);
      result.setMatches(matches);
      result.setChildren(
          children
              .stream()
              .map(
                  builder -> {
                    ElementNode childNode = builder.build(node);
                    return childNode.getElement();
                  })
              .collect(Collectors.toList()));
      if (processor != null) {
        processor.accept(node);
      }
      return node;
    }
  }

  private static SearchResultBuilder result(String uri) {
    return new SearchResultBuilder(uri);
  }
}
