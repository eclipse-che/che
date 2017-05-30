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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

import java.util.List;

/**
 * Sets "Go Into" mode on node which is supports that mode.
 *
 * @author Vlad Zhukovskiy
 * @see Node#supportGoInto()
 */
@Singleton
public class GoIntoAction extends ProjectAction {

    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public GoIntoAction(ProjectExplorerPresenter projectExplorer) {
        super("Go into");
        this.projectExplorer = projectExplorer;
    }

    /** {@inheritDoc} */
    @Override
    protected void updateProjectAction(ActionEvent e) {
        List<?> selection = projectExplorer.getSelection().getAllElements();

        e.getPresentation().setEnabledAndVisible(!projectExplorer.isGoIntoActivated()
                                                 && selection.size() == 1
                                                 && isNodeSupportGoInto(selection.get(0)));
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        List<?> selection = projectExplorer.getSelection().getAllElements();

        if (selection.isEmpty() || selection.size() > 1) {
            throw new IllegalArgumentException("Node isn't selected");
        }

        Object node = selection.get(0);

        if (isNodeSupportGoInto(node)) {
            projectExplorer.goInto((Node)node);
        }
    }

    private boolean isNodeSupportGoInto(Object node) {
        return node instanceof Node && ((Node)node).supportGoInto();
    }
}
