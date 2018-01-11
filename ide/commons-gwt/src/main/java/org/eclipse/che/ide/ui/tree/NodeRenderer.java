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
package org.eclipse.che.ide.ui.tree;

import elemental.dom.Element;
import elemental.html.SpanElement;

/**
 * Flyweight renderer whose job it is to take a NodeData and construct the appropriate DOM structure
 * for the tree node contents.
 *
 * @param <D> The type of data we want to render.
 */
public interface NodeRenderer<D> {

  /**
   * Takes in a {@link SpanElement} constructed via a call to {@link #renderNodeContents} and
   * returns an element whose contract is that it contains only text corresponding to the key for
   * the node's underlying data.
   *
   * <p>This ofcourse depends on the structure that was generated via the call to {@link
   * #renderNodeContents}.
   */
  Element getNodeKeyTextContainer(SpanElement treeNodeLabel);

  /**
   * Constructs the label portion of a {@link TreeNodeElement}. Labels can have arbitrary DOM
   * structure, with one constraint. At least one element MUST contain only text that corresponds to
   * the String key for the underlying node's data.
   */
  SpanElement renderNodeContents(D data);

  /**
   * Updates the node's contents to reflect the current state of the node.
   *
   * @param treeNode the tree node that contains the rendered node contents
   */
  void updateNodeContents(TreeNodeElement<D> treeNode);
}
