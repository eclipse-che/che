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

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.delete.DeleteRepositoryPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/** @author Andrey Plotnikov */
@Singleton
public class DeleteRepositoryAction extends GitAction {
    private final DeleteRepositoryPresenter presenter;
    private final AnalyticsEventLogger      eventLogger;
    private final DialogFactory             dialogFactory;
    private       GitLocalizationConstant   constant;

    @Inject
    public DeleteRepositoryAction(DeleteRepositoryPresenter presenter,
                                  AppContext appContext,
                                  GitResources resources,
                                  GitLocalizationConstant constant,
                                  AnalyticsEventLogger eventLogger,
                                  ProjectExplorerPresenter projectExplorer,
                                  DialogFactory dialogFactory) {
        super(constant.deleteControlTitle(), constant.deleteControlPrompt(), resources.deleteRepo(), appContext, projectExplorer);
        this.presenter = presenter;
        this.constant = constant;
        this.eventLogger = eventLogger;
        this.dialogFactory = dialogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        dialogFactory.createConfirmDialog(constant.deleteGitRepositoryTitle(),
                                          constant.deleteGitRepositoryQuestion(getActiveProject().getRootProject().getName()),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  presenter.deleteRepository();
                                              }
                                          }, null).show();
    }
}
