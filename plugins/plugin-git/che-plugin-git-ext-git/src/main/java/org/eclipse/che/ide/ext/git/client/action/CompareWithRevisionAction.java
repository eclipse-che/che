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
package org.eclipse.che.ide.ext.git.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.revisionsList.RevisionListPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;

import javax.validation.constraints.NotNull;

/**
 * Action for comparing with revision.
 *
 * @author Igor Vinokur
 */
@Singleton
public class CompareWithRevisionAction extends GitAction {
    private final RevisionListPresenter    presenter;
    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public CompareWithRevisionAction(RevisionListPresenter presenter,
                                     AppContext appContext,
                                     GitLocalizationConstant locale,
                                     ProjectExplorerPresenter projectExplorer) {
        super(locale.compareWithRevisionTitle(), locale.compareWithRevisionTitle(), appContext, projectExplorer);
        this.presenter = presenter;
        this.projectExplorer = projectExplorer;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.showRevisions();
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(getActiveProject() != null);
        event.getPresentation().setEnabled(isGitRepository() && compareSupported());
    }

    private boolean compareSupported() {
        Selection selection = projectExplorer.getSelection();
        return selection.isSingleSelection() && selection.getHeadElement() instanceof FileReferenceNode;
    }
}
