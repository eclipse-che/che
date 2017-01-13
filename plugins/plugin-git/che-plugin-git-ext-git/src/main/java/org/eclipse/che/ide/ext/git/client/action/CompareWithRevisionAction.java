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
package org.eclipse.che.ide.ext.git.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.revisionsList.RevisionListPresenter;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.resources.Resource.FILE;

/**
 * Action for comparing with revision.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CompareWithRevisionAction extends GitAction {
    private final RevisionListPresenter    presenter;

    @Inject
    public CompareWithRevisionAction(RevisionListPresenter presenter,
                                     AppContext appContext,
                                     GitLocalizationConstant locale) {
        super(locale.compareWithRevisionTitle(), locale.compareWithRevisionTitle(), null, appContext);
        this.presenter = presenter;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        final Project project = appContext.getRootProject();
        final Resource resource = appContext.getResource();

        checkState(project != null, "Null project occurred");
        checkState(resource instanceof File, "Invalid file occurred");

        presenter.showRevisions(project, (File)resource);
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        super.updateInPerspective(event);

        final Resource resource = appContext.getResource();

        event.getPresentation().setEnabled(resource != null && resource.getResourceType() == FILE);
    }
}
