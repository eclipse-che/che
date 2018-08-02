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
package org.eclipse.che.plugin.debugger.ide.debug.tree.node.comparator;

import java.util.Comparator;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.WatchExpressionNode;

/**
 * Comparator to sort node by type in the debugger tree.
 *
 * @author Alexander Andrienko
 */
public class DebugNodeTypeComparator implements Comparator<Node> {

  /**
   * Returns node index to presentation sort value of this node. Higher index value mean: node
   * should be lower in the tree. Note: watch expressions should be above variables.
   *
   * @param node to get index
   */
  private int getNodeIndex(Node node) {
    if (node instanceof WatchExpressionNode) {
      return 1;
    }

    if (node instanceof VariableNode) {
      return 2;
    }

    return 0;
  }

  @Override
  public int compare(Node o1, Node o2) {
    int nodeIndex1 = getNodeIndex(o1);
    int nodeIndex2 = getNodeIndex(o2);

    if (nodeIndex1 != nodeIndex2) return nodeIndex1 - nodeIndex2;

    return 0;
  }
}
