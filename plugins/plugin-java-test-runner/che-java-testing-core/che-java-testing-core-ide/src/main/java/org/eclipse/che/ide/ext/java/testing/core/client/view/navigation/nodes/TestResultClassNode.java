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
package org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.List;
/**
 * Tree (root) node for display the failing tests.
 * @author Mirage Abeysekara
 */
public class TestResultClassNode extends AbstractTreeNode implements HasPresentation {

    private String className;
    private final JavaResources javaResources;
    private NodePresentation nodePresentation;

    @Inject
    public TestResultClassNode(JavaResources javaResources,
                               @Assisted String className) {
        this.className = className;
        this.javaResources = javaResources;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return Promises.resolve(children);
    }

    @Override
    public String getName() {
        return "Class: " + className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setInfoText("(" + className + ")");
        presentation.setPresentableText(className.substring(className.lastIndexOf(".") + 1));
        presentation.setPresentableIcon(javaResources.svgClassItem());
    }

    @Override
    public NodePresentation getPresentation(boolean update) {
        if (nodePresentation == null) {
            nodePresentation = new NodePresentation();
            updatePresentation(nodePresentation);
        }

        if (update) {
            updatePresentation(nodePresentation);
        }
        return nodePresentation;
    }
}
