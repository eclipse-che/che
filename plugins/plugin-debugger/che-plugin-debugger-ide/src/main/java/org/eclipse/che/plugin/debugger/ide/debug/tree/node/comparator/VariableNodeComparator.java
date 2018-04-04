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
package org.eclipse.che.plugin.debugger.ide.debug.tree.node.comparator;

import java.util.Comparator;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;

/**
 * Comparator to sort {@link VariableNode} by name in the debugger tree.
 *
 * @author Olexander Andriienko
 */
public class VariableNodeComparator implements Comparator<Node> {
  @Override
  public int compare(Node o1, Node o2) {
    if (o1 instanceof VariableNode && o2 instanceof VariableNode) {
      return ((VariableNode) o1)
          .getData()
          .getName()
          .compareTo(((VariableNode) o2).getData().getName());
    }
    return 0;
  }
}
