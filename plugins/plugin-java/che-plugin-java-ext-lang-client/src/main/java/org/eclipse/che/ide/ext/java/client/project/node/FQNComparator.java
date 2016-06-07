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
package org.eclipse.che.ide.ext.java.client.project.node;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.part.explorer.project.FoldersOnTopFilter;

/**
 * @author Vlad Zhukovskiy
 */
public class FQNComparator extends FoldersOnTopFilter {

    @Override
    public int compare(Node o1, Node o2) {
        if (o1 instanceof PackageNode && o2 instanceof PackageNode) {
            return ((PackageNode)o1).getDisplayPackage().compareTo(((PackageNode)o2).getDisplayPackage());
        }

        return super.compare(o1, o2);
    }
}
