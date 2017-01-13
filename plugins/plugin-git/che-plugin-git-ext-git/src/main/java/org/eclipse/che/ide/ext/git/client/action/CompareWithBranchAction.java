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
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.branchList.BranchListPresenter;

import static com.google.common.base.Preconditions.checkState;

/**
 * Action for comparing with branch.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CompareWithBranchAction extends GitAction {
    private final BranchListPresenter  presenter;

    @Inject
    public CompareWithBranchAction(BranchListPresenter presenter,
                                   AppContext appContext,
                                   GitLocalizationConstant locale) {
        super(locale.compareWithBranchTitle(), locale.compareWithBranchTitle(), null, appContext);
        this.presenter = presenter;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        final Project project = appContext.getRootProject();
        final Resource resource = appContext.getResource();

        checkState(project != null, "Null project occurred");

        presenter.showBranches(project, resource);
    }
}
