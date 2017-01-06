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
package org.eclipse.che.ide.search.selectpath;

import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Interceptor for showing only folder nodes.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class FolderNodeInterceptor implements NodeInterceptor {
    @Override
    public Promise<List<Node>> intercept(Node parent, List<Node> children) {
        List<Node> nodes = new ArrayList<>();

        for (Node child : children) {
            if (!child.isLeaf()) {
                nodes.add(child);
            }
        }

        return Promises.resolve(nodes);
    }

    @Override
    public int getPriority() {
        return NORM_PRIORITY;
    }
}
