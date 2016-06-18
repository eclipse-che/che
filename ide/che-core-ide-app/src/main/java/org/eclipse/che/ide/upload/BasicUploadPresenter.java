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
package org.eclipse.che.ide.upload;

import com.google.inject.Inject;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.ResourceBasedNode;

import java.util.List;

/**
 * Provides common functionality for upload classes
 *
 * @author  Alex Vengrovsk.
 */
public class BasicUploadPresenter {

    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public BasicUploadPresenter(ProjectExplorerPresenter projectExplorer) {
        this.projectExplorer = projectExplorer;
    }

    public ResourceBasedNode<?> getResourceBasedNode() {
        List<?> selection = projectExplorer.getSelection().getAllElements();
        //we should be sure that user selected single element to work with it
        if (selection == null || selection.size() != 1) {
            return null;
        }

        Object o = selection.get(0);

        if (o instanceof ResourceBasedNode<?>) {
            ResourceBasedNode<?> node = (ResourceBasedNode<?>)o;
            //it may be file node, so we should take parent node
            if (node.isLeaf() && isResourceAndStorableNode(node.getParent())) {
                return (ResourceBasedNode<?>)node.getParent();
            }

            return isResourceAndStorableNode(node) ? node : null;
        }

        return null;
    }

    protected String parseMessage(String message) {
        int startIndex = 0;
        int endIndex = message.indexOf("</pre>");

        if (message.contains("<pre>message:")) {
            startIndex = message.indexOf("<pre>message:") + "<pre>message:".length();
        } else if (message.contains("<pre>")) {
            startIndex = message.indexOf("<pre>") + "<pre>".length();
        }

        return (endIndex != -1) ? message.substring(startIndex, endIndex) : message.substring(startIndex);
    }

    private boolean isResourceAndStorableNode(@Nullable Node node) {
        return node != null && node instanceof ResourceBasedNode<?> && node instanceof HasStorablePath;
    }
}