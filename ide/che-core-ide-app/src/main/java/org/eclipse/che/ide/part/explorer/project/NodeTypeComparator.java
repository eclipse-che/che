/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.explorer.project;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;

import java.util.Comparator;

import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.api.resources.Resource.FOLDER;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;

/**
 * Compares node by their type.
 * By design folders should be on top, then files and finally synthetic based nodes.
 *
 * @author Vlad Zhukovskiy
 */
public class NodeTypeComparator implements Comparator<Node> {

    private int getClassIndex(Node node) {
        if (node instanceof ResourceNode && (((ResourceNode)node).getData().getResourceType() == FOLDER
                                             || ((ResourceNode)node).getData().getResourceType() == PROJECT)) {
            return 1;
        }

        if (node instanceof ResourceNode && ((ResourceNode)node).getData().getResourceType() == FILE) {
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
