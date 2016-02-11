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
import org.eclipse.che.ide.ext.git.client.add.AddToIndexPresenter;
import org.eclipse.che.ide.ui.FontAwesome;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/** @author Andrey Plotnikov */
@Singleton
public class AddToIndexAction extends GitAction {
    private final AddToIndexPresenter  presenter;
    private final AnalyticsEventLogger eventLogger;

    @Inject
    public AddToIndexAction(AddToIndexPresenter presenter,
                            AppContext appContext,
                            GitLocalizationConstant constant,
                            AnalyticsEventLogger eventLogger,
                            ProjectExplorerPresenter projectExplorer) {
        super(constant.addToIndexTitle(), constant.addToIndexTitle(), FontAwesome.PLUS, appContext, projectExplorer);
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
