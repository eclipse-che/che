/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.smartTree.presentation;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import org.eclipse.che.ide.ui.smartTree.Tree.Joint;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.TreeView;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.ui.SVGResource;
import org.vectomatic.dom.svg.utils.OMSVGParser;

/**
 * Base class for providing own rendering mechanism.
 *
 * @author Vlad Zhukovskiy
 */
public abstract class AbstractPresentationRenderer<N extends Node>
    implements PresentationRenderer<N> {

  protected TreeStyles treeStyles;

  public AbstractPresentationRenderer(TreeStyles treeStyles) {
    this.treeStyles = treeStyles;
  }

  /** {@inheritDoc} */
  @Override
  public Element getRootContainer(String domID) {
    DivElement divElement = Document.get().createDivElement();
    divElement.setId(domID);
    divElement.setClassName(treeStyles.treeStylesCss().rootContainer());
    return divElement;
  }

  /** {@inheritDoc} */
  @Override
  public Element getNodeContainer() {
    DivElement divElement = Document.get().createDivElement();
    divElement.setClassName(treeStyles.treeStylesCss().nodeContainer());
    return divElement;
  }

  /** {@inheritDoc} */
  @Override
  public Element getJointContainer(Joint joint) {
    Element jointElement;

    switch (joint) {
      case COLLAPSED:
        OMSVGSVGElement svg = treeStyles.iconCollapsed().getSvg();
        svg.addClassNameBaseVal(treeStyles.treeStylesCss().jointContainer());
        svg.setWidth(Style.Unit.PX, 16.f);
        svg.setHeight(Style.Unit.PX, 16.f);
        jointElement = svg.getElement();
        break;
      case EXPANDED:
        OMSVGSVGElement svg1 = treeStyles.iconExpanded().getSvg();
        svg1.addClassNameBaseVal(treeStyles.treeStylesCss().jointContainer());
        svg1.setWidth(Style.Unit.PX, 16.f);
        svg1.setHeight(Style.Unit.PX, 16.f);
        jointElement = svg1.getElement();
        break;
      default:
        OMSVGSVGElement svgsvgElement = OMSVGParser.currentDocument().createSVGSVGElement();
        svgsvgElement.addClassNameBaseVal(treeStyles.treeStylesCss().jointContainer());
        svgsvgElement.setWidth(Style.Unit.PX, 16.f);
        svgsvgElement.setHeight(Style.Unit.PX, 16.f);
        jointElement = svgsvgElement.getElement();
    }

    DivElement wrapper = Document.get().createDivElement();
    wrapper.appendChild(jointElement);
    return jointElement;
  }

  /** {@inheritDoc} */
  @Override
  public Element getUserElement(Element userElement) {
    return userElement;
  }

  /** {@inheritDoc} */
  @Override
  public Element getIconContainer(SVGResource icon) {
    if (icon != null) {
      OMSVGSVGElement svg = icon.getSvg();
      svg.addClassNameBaseVal(treeStyles.treeStylesCss().iconContainer());
      svg.setWidth(Style.Unit.PX, 16);
      svg.setHeight(Style.Unit.PX, 16);
      return svg.getElement();
    }

    ImageElement emptyIcon = Document.get().createImageElement();
    emptyIcon.setSrc(TreeView.blankImageUrl);
    emptyIcon.setClassName(treeStyles.treeStylesCss().iconContainer());
    return emptyIcon;
  }

  /** {@inheritDoc} */
  @Override
  public Element getPresentableTextContainer(Element content) {
    DivElement divElement = Document.get().createDivElement();
    divElement.setClassName(treeStyles.treeStylesCss().presentableTextContainer());
    divElement.appendChild(content);
    return divElement;
  }

  /** {@inheritDoc} */
  @Override
  public Element getInfoTextContainer(Element content) {
    DivElement divElement = Document.get().createDivElement();
    divElement.setClassName(treeStyles.treeStylesCss().infoTextContainer());
    divElement.appendChild(content);
    return divElement;
  }

  /** {@inheritDoc} */
  @Override
  public Element getDescendantsContainer() {
    DivElement divElement = Document.get().createDivElement();
    divElement.setClassName(treeStyles.treeStylesCss().descendantsContainer());
    return divElement;
  }
}
