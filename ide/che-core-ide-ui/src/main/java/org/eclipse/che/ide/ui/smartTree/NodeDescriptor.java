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
package org.eclipse.che.ide.ui.smartTree;

import com.google.gwt.dom.client.Element;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.ui.smartTree.data.MutableNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Node descriptor. Uses internally in the tree.
 *
 * @author Vlad Zhukovskiy
 */
public class NodeDescriptor {
  private NodeStorage nodeStorage;
  private Node node;
  private NodeDescriptor parent;
  private List<NodeDescriptor> children = new ArrayList<>();
  private boolean root;

  private String domId;

  private boolean childrenRendered;
  private boolean expand;
  private boolean expandDeep;
  private boolean expanded;
  private boolean loaded;
  private boolean loading;

  private Element rootContainerElement;
  private Element nodeContainerElement;
  private Element jointContainerElement;
  private Element iconContainerElement;
  private Element userElement;
  private Element presentableTextContainer;
  private Element infoTextContainer;
  private Element loadElement;
  private Element descendantsContainerElement;

  public NodeDescriptor(NodeStorage nodeStorage, Node node) {
    this.nodeStorage = nodeStorage;
    if (node == null) {
      root = true;
    }
    this.node = node;
  }

  protected void addChild(int index, NodeDescriptor child) {
    final int actualIndex;

    if (nodeStorage.isSorted()) {
      int insertPos = Collections.binarySearch(children, child, nodeStorage.buildFullComparator());
      actualIndex = insertPos < 0 ? (-insertPos - 1) : insertPos;
    } else {
      actualIndex = index;
    }

    children.add(actualIndex, child);
    child.parent = this;
  }

  public void addChildren(int index, List<NodeDescriptor> children) {
    if (nodeStorage.isSorted()) {
      getChildren().addAll(children);
      Collections.sort(getChildren(), nodeStorage.buildFullComparator());
    } else {
      int actualIndex = index == 0 ? 0 : (getChildren().indexOf(getChildren().get(index - 1)) + 1);
      getChildren().addAll(actualIndex, children);
    }

    for (NodeDescriptor child : children) {
      child.parent = this;
    }
  }

  public void clear() {
    children.clear();
  }

  public List<NodeDescriptor> getChildren() {
    return children;
  }

  public Node getNode() {
    return node;
  }

  public NodeDescriptor getParent() {
    return parent;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public void setParent(NodeDescriptor parent) {
    this.parent = parent;
  }

  public boolean isRoot() {
    return root;
  }

  public void remove(NodeDescriptor descriptor) {
    children.remove(descriptor);
  }

  public void reset() {
    expand = false;
    expanded = false;
    childrenRendered = false;
  }

  public void clearElements() {
    rootContainerElement = null;
    nodeContainerElement = null;
    jointContainerElement = null;
    iconContainerElement = null;
    userElement = null;
    presentableTextContainer = null;
    infoTextContainer = null;
    loadElement = null;
    descendantsContainerElement = null;
    //        domId = null;
  }

  public Element getDescendantsContainerElement() {
    return descendantsContainerElement;
  }

  public String getDomId() {
    return domId;
  }

  public Element getRootContainer() {
    return rootContainerElement;
  }

  public Element getNodeContainerElement() {
    return nodeContainerElement;
  }

  public Element getIconContainerElement() {
    return iconContainerElement;
  }

  public Element getJointContainerElement() {
    return jointContainerElement;
  }

  public Element getPresentableTextContainer() {
    return presentableTextContainer;
  }

  public Element getInfoTextContainer() {
    return infoTextContainer;
  }

  public boolean isChildrenRendered() {
    return childrenRendered;
  }

  public boolean isExpand() {
    return expand;
  }

  public boolean isExpandDeep() {
    return expandDeep;
  }

  public boolean isExpanded() {
    return expanded;
  }

  public boolean isLeaf() {
    return node.isLeaf();
  }

  public boolean isLoaded() {
    return loaded;
  }

  public boolean isLoading() {
    return loading;
  }

  public void setChildrenRendered(boolean childrenRendered) {
    this.childrenRendered = childrenRendered;
  }

  public void setDescendantsContainerElement(Element descendantsContainerElement) {
    this.descendantsContainerElement = descendantsContainerElement;
  }

  public void setNodeContainerElement(Element nodeContainerElement) {
    this.nodeContainerElement = nodeContainerElement;
  }

  public void setRootContainerElement(Element rootContainerElement) {
    this.rootContainerElement = rootContainerElement;
  }

  public void setExpand(boolean expand) {
    this.expand = expand;
  }

  public void setExpandDeep(boolean expandDeep) {
    this.expandDeep = expandDeep;
  }

  public void setExpanded(boolean expanded) {
    this.expanded = expanded;
  }

  public void setIconContainerElement(Element iconContainerElement) {
    this.iconContainerElement = iconContainerElement;
  }

  public void setJointContainerElement(Element jointContainerElement) {
    this.jointContainerElement = jointContainerElement;
  }

  public void setLeaf(boolean leaf) {
    if (node instanceof MutableNode) {
      ((MutableNode) node).setLeaf(leaf);
    }
  }

  public void setLoaded(boolean loaded) {
    this.loaded = loaded;
  }

  public void setLoading(boolean loading) {
    this.loading = loading;
  }

  public void setPresentableTextContainer(Element presentableTextContainer) {
    this.presentableTextContainer = presentableTextContainer;
  }

  public void setDomId(String domId) {
    this.domId = domId;
  }

  public void setInfoTextContainer(Element infoTextContainer) {
    this.infoTextContainer = infoTextContainer;
  }

  public Element getLoadElement() {
    return loadElement;
  }

  public void setLoadElement(Element loadElement) {
    this.loadElement = loadElement;
  }

  public Element getUserElement() {
    return userElement;
  }

  public void setUserElement(Element userElement) {
    this.userElement = userElement;
  }

  @Override
  public String toString() {
    return "NodeDescriptor{"
        + "expand="
        + expand
        + ", expandDeep="
        + expandDeep
        + ", expanded="
        + expanded
        + ", loaded="
        + loaded
        + ", loading="
        + loading
        + ", childrenRendered="
        + childrenRendered
        + ", domId='"
        + domId
        + '\''
        + '}';
  }
}
