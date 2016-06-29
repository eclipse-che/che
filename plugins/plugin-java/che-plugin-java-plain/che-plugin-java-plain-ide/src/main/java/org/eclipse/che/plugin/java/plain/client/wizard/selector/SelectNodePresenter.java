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
package org.eclipse.che.plugin.java.plain.client.wizard.selector;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

import java.util.Collections;
import java.util.List;

/**
 * Presenter for choosing source directory.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectNodePresenter implements SelectNodeView.ActionDelegate {

    private final SelectNodeView           view;
    private final ProjectExplorerPresenter projectExplorerPresenter;
    private       SelectionDelegate        selectionDelegate;

    @Inject
    public SelectNodePresenter(SelectNodeView view, ProjectExplorerPresenter projectExplorerPresenter) {
        this.view = view;
        this.projectExplorerPresenter = projectExplorerPresenter;
        this.view.setDelegate(this);
    }

    /**
     * Show tree of the project.
     *
     * @param projectName
     */
    public void show(SelectionDelegate selectionDelegate, String projectName) {
        this.selectionDelegate = selectionDelegate;

        for (Node node : projectExplorerPresenter.getRootNodes()) {
            if (node.getName().equals(projectName)) {
                view.setStructure(Collections.singletonList(node));
                break;
            }
        }

        view.show();
    }

    /** {@inheritDoc} */
    @Override
    public void setSelectedNode(List<Node> selectedNodes) {
        selectionDelegate.onNodeSelected(selectedNodes);
    }
}
