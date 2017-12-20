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
package org.eclipse.che.ide.ext.java.client.search.node;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import elemental.html.SpanElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.AbstractPresentationNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Tree node represent search result.
 *
 * @author Evgen Vidolob
 */
public class ResultNode extends AbstractPresentationNode {

  private NodeFactory nodeFactory;
  private FindUsagesResponse response;
  private TreeStyles styles;
  private JavaResources resources;

  @Inject
  public ResultNode(
      TreeStyles styles,
      JavaResources resources,
      NodeFactory nodeFactory,
      @Assisted @NotNull FindUsagesResponse response) {
    this.resources = resources;
    this.nodeFactory = nodeFactory;
    this.response = response;
    this.styles = styles;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return createFromAsyncRequest(
        callback -> {
          final List<JavaProject> projects = response.getProjects();
          final List<Node> projectNodes = new ArrayList<>(projects.size());
          final List<Node> nodes =
              projects
                  .stream()
                  .map(javaProject -> nodeFactory.create(javaProject, response.getMatches()))
                  .collect(Collectors.toList());
          projectNodes.addAll(nodes);
          callback.onSuccess(projectNodes);
        });
  }

  @Override
  public String getName() {
    return "Usages of " + response.getSearchElementLabel();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    SpanElement spanElement =
        Elements.createSpanElement(styles.styles().presentableTextContainer());
    spanElement.setInnerHTML(
        "Usages of <span class=\""
            + resources.css().searchMatch()
            + "\">"
            + response.getSearchElementLabel()
            + "</span> ["
            + calculateMatchesSize(response.getMatches())
            + " occurrences]");
    presentation.setUserElement((Element) spanElement);
  }

  private String calculateMatchesSize(Map<String, List<Match>> matches) {
    int i = 0;
    for (Map.Entry<String, List<Match>> stringListEntry : matches.entrySet()) {
      i += stringListEntry.getValue().size();
    }

    return String.valueOf(i);
  }
}
