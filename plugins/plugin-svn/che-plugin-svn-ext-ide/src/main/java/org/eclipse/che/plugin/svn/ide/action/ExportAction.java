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
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.export.ExportPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Extension of {@link SubversionAction} for implementing the "svn export" command.
 */
@Singleton
public class ExportAction extends SubversionAction {

    private ProjectExplorerPresenter projectExplorerPresenter;
    private ExportPresenter          presenter;

    @Inject
    public ExportAction(final AppContext appContext,
                        final ProjectExplorerPresenter projectExplorerPresenter,
                        final SubversionExtensionLocalizationConstants constants,
                        final SubversionExtensionResources resources,
                        final ExportPresenter presenter) {
        super(constants.exportTitle(), constants.exportDescription(), resources.export(), appContext,
              constants, resources, projectExplorerPresenter);
        this.projectExplorerPresenter = projectExplorerPresenter;
        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        presenter.showExport(getSelectedNode());
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isSelectionRequired() {
        return true;
    }

    private HasStorablePath getSelectedNode() {
        Object selectedNode = projectExplorerPresenter.getSelection().getHeadElement();
        return selectedNode != null && selectedNode instanceof HasStorablePath ? (HasStorablePath)selectedNode : null;
    }
}
