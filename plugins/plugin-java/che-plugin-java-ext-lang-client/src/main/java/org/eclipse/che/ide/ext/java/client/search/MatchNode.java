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

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import elemental.html.SpanElement;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.languageserver.shared.model.SnippetResult;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasNewPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NewNodePresentation;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;

public class MatchNode extends AbstractTreeNode implements HasNewPresentation, HasAction {
  private String uri;
  private SnippetResult snippet;
  private JavaResources resources;
  private OpenFileInEditorHelper openHelper;
  private PromiseProvider promiseProvider;
  private TreeStyles styles;

  @Inject
  public MatchNode(
      @Assisted("uri") String uri,
      @Assisted("snippet") SnippetResult snippet,
      JavaResources resources,
      NodeFactory nodeFactory,
      OpenFileInEditorHelper openHelper,
      PromiseProvider promiseProvider,
      TreeStyles styles) {
    this.uri = uri;
    this.snippet = snippet;
    this.resources = resources;
    this.openHelper = openHelper;
    this.promiseProvider = promiseProvider;
    this.styles = styles;
  }

  @Override
  public String getName() {
    return snippet.getSnippet();
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public NewNodePresentation getPresentation() {
    SpanElement spanElement =
        Elements.createSpanElement(styles.treeStylesCss().presentableTextContainer());

    SpanElement lineNumberElement = Elements.createSpanElement();
    lineNumberElement.setInnerHTML(
        String.valueOf(snippet.getLineIndex() + 1) + ":&nbsp;&nbsp;&nbsp;");
    spanElement.appendChild(lineNumberElement);

    SpanElement textElement = Elements.createSpanElement();
    LinearRange matchInLine = snippet.getRangeInSnippet();
    String matchedLine = snippet.getSnippet();
    if (matchedLine != null && matchInLine != null) {
      String startLine = matchedLine.substring(0, matchInLine.getOffset());
      textElement.appendChild(Elements.createTextNode(startLine));
      SpanElement highlightElement = Elements.createSpanElement(resources.css().searchMatch());
      highlightElement.setInnerText(
          matchedLine.substring(
              matchInLine.getOffset(), matchInLine.getOffset() + matchInLine.getLength()));
      textElement.appendChild(highlightElement);

      textElement.appendChild(
          Elements.createTextNode(
              matchedLine.substring(matchInLine.getOffset() + matchInLine.getLength())));
    } else {
      textElement.appendChild(Elements.createTextNode("Can't find sources"));
    }
    spanElement.appendChild(textElement);

    return new NewNodePresentation.Builder()
        .withIcon(resources.searchMatch())
        .withUserElement((Element) spanElement)
        .build();
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return promiseProvider.resolve(Collections.emptyList());
  }

  @Override
  public void actionPerformed() {
    openHelper.openLocation(uri, snippet.getLinearRange());
  }

  @VisibleForTesting
  SnippetResult getSnippet() {
    return snippet;
  }
}
