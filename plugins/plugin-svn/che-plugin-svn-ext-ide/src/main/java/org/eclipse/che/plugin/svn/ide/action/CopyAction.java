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
package org.eclipse.che.plugin.svn.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.copy.CopyPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.ResourceBasedNode;

/**
 * Extension of {@link SubversionAction} for implementing the "svn copy" (copy a file or directory) command.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class CopyAction extends SubversionAction {

    private       ProjectExplorerPresenter projectExplorerPresenter;
    private final CopyPresenter            presenter;

    @Inject
    public CopyAction(final AppContext appContext,
                      final ProjectExplorerPresenter projectExplorerPresenter,
                      final SubversionExtensionLocalizationConstants constants,
                      final SubversionExtensionResources resources,
                      final CopyPresenter presenter) {
        super(constants.copyTitle(), constants.copyDescription(), resources.copy(), appContext, constants, resources,
              projectExplorerPresenter);
        this.projectExplorerPresenter = projectExplorerPresenter;

        this.presenter = presenter;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e) {
        presenter.showCopy(getSelectedNode());
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isSelectionRequired() {
        return true;
    }

    private ResourceBasedNode<?> getSelectedNode() {
        Object selectedNode = projectExplorerPresenter.getSelection().getHeadElement();
        return selectedNode != null && selectedNode instanceof ResourceBasedNode ? (ResourceBasedNode)selectedNode : null;
    }
}
