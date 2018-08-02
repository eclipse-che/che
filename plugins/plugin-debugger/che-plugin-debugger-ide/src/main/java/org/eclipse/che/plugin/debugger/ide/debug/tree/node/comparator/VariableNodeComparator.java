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
