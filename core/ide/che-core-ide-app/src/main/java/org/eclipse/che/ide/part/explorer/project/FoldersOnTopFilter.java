/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.explorer.project;

import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.SyntheticBasedNode;

import java.util.Comparator;

/**
 * @author Vlad Zhukovskiy
 */
public class FoldersOnTopFilter implements Comparator<Node> {

    private int getClassIndex(Node node) {
        if (node instanceof FolderReferenceNode) {
            return 1;
        }

        if (node instanceof FileReferenceNode) {
            return 2;
        }

        if (node instanceof SyntheticBasedNode<?>) {
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
