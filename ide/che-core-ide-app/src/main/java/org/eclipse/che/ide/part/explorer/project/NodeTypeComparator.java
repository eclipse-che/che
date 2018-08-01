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
package org.eclipse.che.ide.part.explorer.project;

import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.api.resources.Resource.FOLDER;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;

import java.util.Comparator;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Compares node by their type. By design folders should be on top, then files and finally synthetic
 * based nodes.
 *
 * @author Vlad Zhukovskiy
 */
public class NodeTypeComparator implements Comparator<Node> {

  private int getClassIndex(Node node) {
    if (node instanceof ResourceNode
        && (((ResourceNode) node).getData().getResourceType() == FOLDER
            || ((ResourceNode) node).getData().getResourceType() == PROJECT)) {
      return 1;
    }

    if (node instanceof ResourceNode && ((ResourceNode) node).getData().getResourceType() == FILE) {
      return 2;
    }

    if (node instanceof SyntheticNode<?>) {
      return 3;
    }

    return 0;
  }

  @Override
  public int compare(Node o1, Node o2) {
    int classIdx1 = getClassIndex(o1);
    int classIdx2 = getClassIndex(o2);

    if (classIdx1 != classIdx2) return classIdx1 - classIdx2;

    return 0;
  }
}
