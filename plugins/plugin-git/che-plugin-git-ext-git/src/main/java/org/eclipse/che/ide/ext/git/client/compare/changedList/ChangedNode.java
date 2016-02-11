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
package org.eclipse.che.ide.ext.git.client.compare.changedList;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.project.node.AbstractTreeNode;
import org.eclipse.che.ide.api.project.node.HasAction;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * Node Element used for setting it to TreeNodeStorage and viewing changed files.
 *
 * @author Igor Vinokur
 */
public class ChangedNode extends AbstractTreeNode implements HasPresentation, HasAction {

    private String name;
    private String state;

    private NodePresentation nodePresentation;

    /**
     * Create instance of ChangedNode.
     *
     * @param name
     *         name of the file that represents this node with its full path
     * @param state
     *         state of the file that represents this node
     */
    public ChangedNode(String name, String state) {
        this.name = name;
        this.state = state;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return Promises.resolve(Collections.<Node>emptyList());
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Represents state of the file, 'A' if added 'D' if deleted 'M' if modified, etc.
     *
     * @return state state of the node
     */
    public String getState() {
        return state;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setPresentableText(name);

        if (state.startsWith("M")) {
            presentation.setPresentableTextCss("color: DodgerBlue ;");
        } else if (state.startsWith("D")) {
            presentation.setPresentableTextCss("color: red;");
        } else if (state.startsWith("A")) {
            presentation.setPresentableTextCss("color: green;");
        } else if (state.startsWith("C")) {
            presentation.setPresentableTextCss("color: purple;");
        }
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

    @Override
    public void actionPerformed() {

    }
}
