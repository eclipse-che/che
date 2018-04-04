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
 * Base node renderer which do nothing.
 *
 * @author Vlad Zhukovskyi
 * @since 5.11.0
 */
public abstract class BaseNodeRenderer<D> implements NodeRenderer<D> {
  @Override
  public Element getNodeKeyTextContainer(SpanElement treeNodeLabel) {
    return null;
  }

  @Override
  public SpanElement renderNodeContents(D data) {
    return null;
  }

  @Override
  public void updateNodeContents(TreeNodeElement<D> treeNode) {}
}
