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
import org.eclipse.che.ide.ext.git.client.pull.PullPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/** @author Andrey Plotnikov */
@Singleton
public class PullAction extends GitAction {
    private final PullPresenter        presenter;
    private final AnalyticsEventLogger eventLogger;

    @Inject
    public PullAction(PullPresenter presenter,
                      AppContext appContext,
                      GitResources resources,
                      GitLocalizationConstant constant,
                      AnalyticsEventLogger eventLogger,
                      ProjectExplorerPresenter projectExplorer) {
        super(constant.pullControlTitle(), constant.pullControlPrompt(), resources.pull(), appContext, projectExplorer);
        this.presenter = presenter;
        this.eventLogger = eventLogger;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        presenter.showDialog();
    }
}
