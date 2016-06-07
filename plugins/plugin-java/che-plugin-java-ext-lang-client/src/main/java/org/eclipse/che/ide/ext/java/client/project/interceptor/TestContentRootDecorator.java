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
package org.eclipse.che.ide.ext.java.client.project.interceptor;

import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.data.tree.HasAttributes;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.java.client.project.node.SourceFolderNode;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.ContentRoot.TEST_SOURCE;
import static org.eclipse.che.ide.project.node.AbstractProjectBasedNode.CUSTOM_BACKGROUND_FILL;

/**
 * Decorates child of test content root with custom background color.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class TestContentRootDecorator implements NodeInterceptor {


    /** {@inheritDoc} */
    @Override
    public Promise<List<Node>> intercept(Node parent, List<Node> children) {
        if (parent instanceof SourceFolderNode && ((SourceFolderNode)parent).getContentRootType() == TEST_SOURCE) {
            addBackgroundAttribute(children);
        } else {
            SourceFolderNode sourceFolderNode = getSourceFolder(parent);
            if (sourceFolderNode != null && sourceFolderNode.getContentRootType() == TEST_SOURCE) {
                addBackgroundAttribute(children);
            }
        }

        return Promises.resolve(children);
    }

    /** {@inheritDoc} */
    @Override
    public int getPriority() {
        return MAX_PRIORITY;
    }

    private SourceFolderNode getSourceFolder(Node node) {
        Node parent = node.getParent();

        while (parent != null) {
            if (parent instanceof SourceFolderNode) {
                return (SourceFolderNode)parent;
            }

            parent = parent.getParent();
        }

        return  null;
    }

    private void addBackgroundAttribute(List<Node> nodes) {
        for (Node node : nodes) {
            if (node instanceof HasAttributes) {
                ((HasAttributes)node).getAttributes().put(CUSTOM_BACKGROUND_FILL,
                                                          singletonList(Style.theme.projectExplorerTestItemBackground()));
            }
        }
    }
}
