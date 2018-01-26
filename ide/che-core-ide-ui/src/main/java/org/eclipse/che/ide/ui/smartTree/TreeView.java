/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.smartTree;

import com.google.common.base.Strings;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Event;
import elemental.svg.SVGSVGElement;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/** @author Vlad Zhukovskiy */
public class TreeView {

  protected NodeDescriptor over;
  protected Tree tree;

  private int cacheSize = 20;
  private int cleanDelay = 500;
  private int scrollDelay = 1;

  public static String blankImageUrl =
      "data:image/gif;base64,R0lGODlhAQABAID/AMDAwAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==";

  public void bind(Tree tree) {
    this.tree = tree;
  }

  public void collapse(NodeDescriptor node) {
    getDescendantsContainer(node).getStyle().setDisplay(Style.Display.NONE);
    onJointChange(node, tree.getJoint(node.getNode()));
  }

  public void expand(NodeDescriptor node) {
    getDescendantsContainer(node).getStyle().setDisplay(Style.Display.BLOCK);
    onJointChange(node, tree.getJoint(node.getNode()));
  }

  /**
   * Returns the cache size.
   *
   * @return the cache size
   */
  public int getCacheSize() {
    return cacheSize;
  }

  public int getCleanDelay() {
    return cleanDelay;
  }

  public Element getDescendantsContainer(NodeDescriptor node) {
    if (node.getDescendantsContainerElement() == null) {
      Element element = getRootContainer(node).getChildNodes().getItem(1).cast();
      node.setDescendantsContainerElement(element);
    }
    return node.getDescendantsContainerElement();
  }

  /**
   * Gets the rendered element, if any, for the given tree node object. This method will look up the
   * dom element if it has not yet been seen. The getRootContainer() method for the node will return
   * the same value as this method does after it has been cached.
   *
   * @param node the tree node to find an element for
   * @return the element that the node represents, or null if not yet rendered
   */
  public Element getRootContainer(NodeDescriptor node) {
    if (node.getRootContainer() == null) {
      Element element = Document.get().getElementById(node.getDomId()).cast();
      node.setRootContainerElement(element);
    }
    return node.getRootContainer();
  }

  /**
   * Return div that contains joint element, check element, main icon element, text element
   *
   * @param node
   * @return
   */
  public Element getNodeContainer(NodeDescriptor node) {
    if (node.getNodeContainerElement() == null) {
      node.setNodeContainerElement(
          getRootContainer(node) != null ? getRootContainer(node).getFirstChildElement() : null);
    }
    return node.getNodeContainerElement().cast();
  }

  public Element getJointContainer(NodeDescriptor node) {
    if (node.getJointContainerElement() == null) {
      Element element = getNodeContainer(node).getChildNodes().getItem(0).cast();
      node.setJointContainerElement(element);
    }
    return node.getJointContainerElement();
  }

  public Element getIconContainer(NodeDescriptor node) {
    if (node.getIconContainerElement() == null) {
      Element element = getNodeContainer(node).getChildNodes().getItem(1).cast();
      node.setIconContainerElement(element);
    }
    return node.getIconContainerElement();
  }

  public Element getUserElementContainer(NodeDescriptor node) {
    if (node.getUserElement() == null) {
      Element element = getNodeContainer(node).getChildNodes().getItem(2).cast();
      node.setUserElement(element);
    }
    return node.getUserElement();
  }

  public Element getPresentableTextContainer(NodeDescriptor node) {
    if (node.getPresentableTextContainer() == null) {
      Element element = getNodeContainer(node).getChildNodes().getItem(3).cast();
      node.setPresentableTextContainer(element);
    }
    return node.getPresentableTextContainer();
  }

  public Element getInfoTextContainer(NodeDescriptor node) {
    if (node.getPresentableTextContainer() == null) {
      Element element = getNodeContainer(node).getChildNodes().getItem(4).cast();
      node.setInfoTextContainer(element);
    }
    return node.getInfoTextContainer();
  }

  public int getScrollDelay() {
    return scrollDelay;
  }

  public boolean isSelectableTarget(Node node, Element target) {
    NodeDescriptor nodeDescriptor = tree.getNodeDescriptor(node);
    return nodeDescriptor != null && !isJointElement(target);
  }

  public void onDropChange(NodeDescriptor nodeDescriptor, boolean drop) {
    Element e = tree.getView().getNodeContainer(nodeDescriptor);
    setClassName(e, tree.getTreeStyles().treeStylesCss().dragOver(), drop);
  }

  public void onEvent(Event ce) {

    int type = ce.getTypeInt();
    switch (type) {
      case Event.ONMOUSEOVER:
        if (tree.isTrackMouseOver()) {
          onMouseOver(ce);
        }
        break;
      case Event.ONMOUSEOUT:
        if (tree.isTrackMouseOver()) {
          onMouseOut(ce);
        }
        break;
    }
  }

  public void onDepthUpdated(NodeDescriptor node, int newDepth) {
    Element nodeElement = getNodeContainer(node);

    nodeElement.getStyle().setPaddingLeft(newDepth * getIndenting(node), Style.Unit.PX);
  }

  public void onElementChanged(NodeDescriptor node, Element element) {
    Element el = getRootContainer(node).getFirstChildElement();

    if (el == null) {
      return;
    }
    el.removeFromParent();
    getRootContainer(node).insertFirst(element.getFirstChild());
    node.setNodeContainerElement(null);
    node.setJointContainerElement(null);

    onSelectChange(node.getNode(), tree.getSelectionModel().isSelected(node.getNode()));
  }

  public void onJointChange(NodeDescriptor node, Tree.Joint joint) {
    Element currJointEl = getJointContainer(node);

    if (currJointEl == null) {
      return;
    }

    Element jointContainer = tree.getPresentationRenderer().getJointContainer(joint);

    getNodeContainer(node).insertFirst(jointContainer);

    currJointEl.removeFromParent();

    node.setJointContainerElement(jointContainer);
  }

  public void onLoadChange(NodeDescriptor node, boolean loading) {
    if (node == null) {
      return;
    }

    Element rootContainer = getNodeContainer(node);

    if (rootContainer == null) {
      return;
    }

    if (loading) {
      rootContainer.addClassName(tree.getTreeStyles().treeStylesCss().loading());
    } else {
      rootContainer.removeClassName(tree.getTreeStyles().treeStylesCss().loading());
    }
  }

  public void onOverChange(NodeDescriptor node, boolean over) {
    if (tree.focused) {
      setClassName(getNodeContainer(node), tree.getTreeStyles().treeStylesCss().hover(), over);
    }
  }

  public void onSelectChange(Node node, boolean select) {
    if (select) {
      Node p = tree.getNodeStorage().getParent(node);
      if (p != null) {
        tree.setExpanded(tree.getNodeStorage().getParent(node), true);
      }
    }
    NodeDescriptor nodeDescriptor = tree.getNodeDescriptor(node);
    if (nodeDescriptor != null) {
      Element e = getNodeContainer(nodeDescriptor);
      if (e != null) {
        setClassName(e, tree.getTreeStyles().treeStylesCss().hover(), false);
        setClassName(e, tree.getTreeStyles().treeStylesCss().selected(), false);

        if (select) {
          setClassName(
              e,
              tree.focused
                  ? tree.getTreeStyles().treeStylesCss().selected()
                  : tree.getTreeStyles().treeStylesCss().hover(),
              true);
        }
      }
    }
  }

  public void onFocusUpdated() {
    for (Node node : tree.getSelectionModel().getSelectedNodes()) {
      onSelectChange(node, tree.getSelectionModel().isSelected(node));
    }
  }

  public void onTextChange(NodeDescriptor node, SafeHtml text) {
    Element textEl = getPresentableTextContainer(node);
    if (textEl != null) {
      textEl.setInnerHTML(Strings.isNullOrEmpty(text.asString()) ? "&#160;" : text.asString());
    }
  }

  public void onInfoTextChange(NodeDescriptor node, String text) {
    Element textEl = getInfoTextContainer(node);
    if (textEl != null) {
      textEl.setInnerHTML(Strings.isNullOrEmpty(text) ? "&#160;" : text);
    }
  }

  public void setCacheSize(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  public void setCleanDelay(int cleanDelay) {
    this.cleanDelay = cleanDelay;
  }

  public void setScrollDelay(int scrollDelay) {
    this.scrollDelay = scrollDelay;
  }

  protected int getCalculatedRowHeight() {
    return 20;
  }

  protected int getIndenting(NodeDescriptor node) {
    return 18;
  }

  protected void onMouseOut(NativeEvent ce) {
    if (over != null) {
      onOverChange(over, false);
      over = null;
    }
  }

  protected void onMouseOver(NativeEvent ne) {
    NodeDescriptor nodeDescriptor = tree.getNodeDescriptor((Element) ne.getEventTarget().cast());
    if (nodeDescriptor != null) {
      if (over != nodeDescriptor) {
        onMouseOut(ne);
        over = nodeDescriptor;
        onOverChange(over, true);
      }
    }
  }

  private void setClassName(Element element, String cls, boolean add) {
    if (element == null) {
      return;
    }

    if (add) {
      element.addClassName(cls);
    } else {
      element.removeClassName(cls);
    }
  }

  private boolean isJointElement(Element element) {
    if (element instanceof SVGSVGElement) {
      SVGSVGElement joint = (SVGSVGElement) element;
      return joint.getClassList().contains(tree.getTreeStyles().treeStylesCss().joint());
    }

    return false;
  }
}
