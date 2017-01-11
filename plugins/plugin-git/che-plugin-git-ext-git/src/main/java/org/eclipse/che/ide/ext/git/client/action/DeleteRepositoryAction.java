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
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.delete.DeleteRepositoryPresenter;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;

import static com.google.common.base.Preconditions.checkState;

/** @author Andrey Plotnikov */
@Singleton
public class DeleteRepositoryAction extends GitAction {
    private final DeleteRepositoryPresenter presenter;
    private final DialogFactory             dialogFactory;
    private       GitLocalizationConstant   constant;

    @Inject
    public DeleteRepositoryAction(DeleteRepositoryPresenter presenter,
                                  AppContext appContext,
                                  GitResources resources,
                                  GitLocalizationConstant constant,
                                  DialogFactory dialogFactory) {
        super(constant.deleteControlTitle(), constant.deleteControlPrompt(), resources.deleteRepo(), appContext);
        this.presenter = presenter;
        this.constant = constant;
        this.dialogFactory = dialogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        final Project project = appContext.getRootProject();

        checkState(project != null, "Null project occurred");

        dialogFactory.createConfirmDialog(constant.deleteGitRepositoryTitle(),
                                          constant.deleteGitRepositoryQuestion(project.getName()),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  presenter.deleteRepository(project);
                                              }
                                          }, null).show();
    }
}
