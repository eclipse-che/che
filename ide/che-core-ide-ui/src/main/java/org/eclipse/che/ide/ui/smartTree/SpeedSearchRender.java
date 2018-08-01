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
package org.eclipse.che.ide.ui.smartTree;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.ui.smartTree.Tree.Joint;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Render for {@link SpeedSearch} matchings.
 *
 * @author Igor Vinokur
 */
public class SpeedSearchRender extends DefaultPresentationRenderer<Node> {

  private String searchRequest;
  private String searchPattern;
  private final String matchingStyle;

  SpeedSearchRender(TreeStyles treeStyles, String matchingStyle) {
    super(treeStyles);
    this.matchingStyle = matchingStyle;
  }

  void setSearchRequest(String searchRequest) {
    this.searchRequest = searchRequest;
  }

  void setRequestPattern(String requestPattern) {
    this.searchPattern = requestPattern;
  }

  @Override
  public Element render(final Node node, final String domID, final Joint joint, final int depth) {

    final Element rootContainer = super.render(node, domID, joint, depth);
    final Element nodeContainer = rootContainer.getFirstChildElement();

    if (searchRequest == null || searchRequest.isEmpty()) {
      return rootContainer;
    }

    Element item = nodeContainer.getElementsByTagName("span").getItem(0);
    String name = node.getName();
    String innerText = item.getInnerText();

    if (innerText.isEmpty()) {
      item = nodeContainer.getElementsByTagName("div").getItem(0).getFirstChildElement();
      innerText = item.getInnerText();
    }

    List<String> matchings = getMatchings(name);
    if (matchings.isEmpty()) {
      return rootContainer;
    }

    if (!name.toLowerCase().matches(searchPattern)) {
      return rootContainer;
    }

    item.setInnerText("");

    for (int i = 0; i < matchings.size(); i++) {
      String matching = matchings.get(i);
      SpanElement beforeMatchingElement = (SpanElement) Elements.createSpanElement();
      SpanElement matchingElement = (SpanElement) Elements.createSpanElement(matchingStyle);
      int matchingIndex = name.toLowerCase().indexOf(matching);
      int matchingLength = matching.length();
      beforeMatchingElement.setInnerText(name.substring(0, matchingIndex));
      matchingElement.setInnerText(name.substring(matchingIndex, matchingIndex + matchingLength));
      item.appendChild(beforeMatchingElement);
      item.appendChild(matchingElement);

      innerText = innerText.substring(innerText.toLowerCase().indexOf(matching) + matchingLength);
      if (i == matchings.size() - 1) {
        SpanElement afterMatchingElement = (SpanElement) Elements.createSpanElement();
        afterMatchingElement.setInnerText(innerText);
        item.appendChild(afterMatchingElement);
      } else {
        name = name.substring(matchingIndex + matchingLength);
      }
    }

    return rootContainer;
  }

  private List<String> getMatchings(String input) {
    String matching = "";
    int length = searchRequest.length();
    List<String> matchings = new ArrayList<>();
    for (int i = 0; i < length; i++) {

      String value = String.valueOf(searchRequest.charAt(i)).toLowerCase();
      String updatedInput = input.substring(input.indexOf(matching) + matching.length());

      if (input.toLowerCase().contains(matching + value)) {
        matching += value;
        if (i == length - 1) {
          matchings.add(matching);
          input = updatedInput;
        }
      } else if (!matching.isEmpty()) {
        matchings.add(matching);
        input = updatedInput;
        if (i == length - 1) {
          matchings.add(value);
          input = updatedInput;
        } else if (input.toLowerCase().contains(value)) {
          matching = value;
        } else {
          matching = "";
        }
      }
    }
    return matchings;
  }
}
