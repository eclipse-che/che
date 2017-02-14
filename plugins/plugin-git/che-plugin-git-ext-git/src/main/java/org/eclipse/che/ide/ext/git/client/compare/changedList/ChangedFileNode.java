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
package org.eclipse.che.ide.ext.git.client.compare.changedList;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.ext.git.client.compare.changedList.ChangedListView.ActionDelegate;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
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
public class ChangedFileNode extends AbstractTreeNode implements HasPresentation, HasAction {

    private NodePresentation nodePresentation;

    private final String         pathName;
    private final Status         status;
    private final NodesResources nodesResources;
    private final ActionDelegate actionDelegate;
    private final boolean        viewPath;

    /**
     * Create instance of ChangedFileNode.
     *
     * @param pathName
     *         name of the file that represents this node with its full path
     * @param status
     *         git status of the file that represents this node
     * @param nodesResources
     *         resources that contain icons
     * @param actionDelegate
     *         sends delegated events from the view
     * @param viewPath
     *         <code>true</code> if it is needed to view file name with its full path,
     *         and <code>false</code> if it is needed to view only name of the file
     */
    public ChangedFileNode(String pathName,
                           Status status,
                           NodesResources nodesResources,
                           ActionDelegate actionDelegate,
                           boolean viewPath) {
        this.pathName = pathName;
        this.status = status;
        this.nodesResources = nodesResources;
        this.actionDelegate = actionDelegate;
        this.viewPath = viewPath;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return Promises.resolve(Collections.<Node>emptyList());
    }

    @Override
    public String getName() {
        return pathName;
    }

    /**
     * Git status of the file.
     *
     * @return Git status of the file
     */
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        String name = Path.valueOf(pathName).lastSegment();
        presentation.setPresentableText(viewPath ? name : pathName);
        presentation.setPresentableIcon(nodesResources.file());

        switch (status) {
            case MODIFIED:
                presentation.setPresentableTextCss("color: DodgerBlue ;");
                return;
            case DELETED:
                presentation.setPresentableTextCss("color: red;");
                return;
            case ADDED:
                presentation.setPresentableTextCss("color: green;");
                return;
            case COPIED:
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
       actionDelegate.onFileNodeDoubleClicked();
    }
}
