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
package org.eclipse.che.ide.ui.smartTree.presentation;

import static com.google.common.base.Strings.nullToEmpty;

import com.google.common.base.Strings;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/** @author Vlad Zhukovskiy */
public class DefaultPresentationRenderer<N extends Node> extends AbstractPresentationRenderer<N> {

  public DefaultPresentationRenderer(TreeStyles treeStyles) {
    super(treeStyles);
  }

  @Override
  public Element render(N node, String domID, Tree.Joint joint, int depth) {

    if (node instanceof HasNewPresentation) {
      NewNodePresentation presentation = ((HasNewPresentation) node).getPresentation();

      return renderNewPresentation(presentation, domID, joint, depth);
    } else if (node instanceof HasPresentation) {
      NodePresentation presentation = ((HasPresentation) node).getPresentation(false);

      return renderGenericPresentation(presentation, domID, joint, depth);
    } else {
      NodePresentation presentation = new NodePresentation();
      presentation.setPresentableText(node.getName());

      return renderGenericPresentation(presentation, domID, joint, depth);
    }
  }

  Element renderNewPresentation(
      NewNodePresentation presentation, String domID, Tree.Joint joint, int depth) {
    Element rootContainer = getRootContainer(domID);
    Element nodeContainer = getNodeContainer();
    setIndentLevel(nodeContainer, depth);
    Element jointContainer = getJointContainer(joint);
    Element iconContainer = getIconContainer(presentation.getIcon());
    Element userElement = getUserElement(presentation.getUserElement());
    Element presentableTextContainer =
        getPresentableTextContainer(
            createStyledTextElement(presentation.getNodeText(), presentation.getNodeTextStyle()));
    Element infoTextContainer =
        getInfoTextContainer(
            createStyledTextElement(
                presentation.getNodeInfoText(), presentation.getNodeInfoTextStyle()));
    Element descendantsContainer = getDescendantsContainer();

    nodeContainer.appendChild(jointContainer);
    nodeContainer.appendChild(iconContainer);
    nodeContainer.appendChild(
        userElement == null ? Document.get().createSpanElement() : userElement);
    nodeContainer.appendChild(presentableTextContainer);
    nodeContainer.appendChild(infoTextContainer);

    rootContainer.appendChild(nodeContainer);
    rootContainer.appendChild(descendantsContainer);

    return rootContainer;
  }

  Element renderGenericPresentation(
      NodePresentation presentation, String domID, Tree.Joint joint, int depth) {
    Element rootContainer = getRootContainer(domID);

    Element nodeContainer = getNodeContainer();

    setIndentLevel(nodeContainer, depth);

    Element jointContainer = getJointContainer(joint);

    Element iconContainer = getIconContainer(presentation.getPresentableIcon());

    Element userElement = getUserElement(presentation.getUserElement());

    Element presentableTextContainer =
        getPresentableTextContainer(createPresentableTextElement(presentation));

    Element infoTextContainer = getInfoTextContainer(createInfoTextElement(presentation));

    Element descendantsContainer = getDescendantsContainer();

    nodeContainer.appendChild(jointContainer);
    nodeContainer.appendChild(iconContainer);
    nodeContainer.appendChild(
        userElement == null ? Document.get().createSpanElement() : userElement);
    nodeContainer.appendChild(presentableTextContainer);
    nodeContainer.appendChild(infoTextContainer);

    rootContainer.appendChild(nodeContainer);
    rootContainer.appendChild(descendantsContainer);

    return rootContainer;
  }

  private Element createStyledTextElement(String content, StyleConfigurator styleConfigurator) {
    DivElement textElement = Document.get().createDivElement();

    textElement.setInnerText(nullToEmpty(content));

    if (styleConfigurator != null) {
      styleConfigurator
          .getCssConfiguration()
          .forEach((p, v) -> textElement.getStyle().setProperty(p, v));
    }

    return textElement;
  }

  private Element createPresentableTextElement(NodePresentation presentation) {
    DivElement textElement = Document.get().createDivElement();

    textElement.setInnerText(nullToEmpty(presentation.getPresentableText()));
    textElement.setAttribute("style", presentation.getPresentableTextCss());

    // TODO support text colorization

    return textElement;
  }

  private Element createInfoTextElement(NodePresentation presentation) {
    DivElement textElement = Document.get().createDivElement();

    StringBuilder sb = new StringBuilder();

    if (presentation.getInfoTextWrapper() != null) {
      sb.append(presentation.getInfoTextWrapper().first);
    }

    if (!Strings.isNullOrEmpty(presentation.getInfoText())) {
      sb.append(presentation.getInfoText());
    }

    if (presentation.getInfoTextWrapper() != null) {
      sb.append(presentation.getInfoTextWrapper().second);
    }

    textElement.setInnerText(sb.toString());
    textElement.setAttribute("style", presentation.getInfoTextCss());

    // TODO support text colorization

    return textElement;
  }

  private void setIndentLevel(Element element, int depth) {
    element.getStyle().setPaddingLeft((double) depth * 16, Style.Unit.PX);
  }
}
