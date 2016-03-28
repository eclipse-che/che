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
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.history.HistoryPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.ui.FontAwesome;

/** @author Andrey Plotnikov */
@Singleton
public class HistoryAction extends GitAction {
    private final Provider<HistoryPresenter> presenterProvider;

    @Inject
    public HistoryAction(Provider<HistoryPresenter> presenterProvider,
                         AppContext appContext,
                         GitLocalizationConstant constant,
                         ProjectExplorerPresenter projectExplorer) {
        super(constant.historyControlTitle(), constant.historyControlPrompt(), FontAwesome.HISTORY, appContext, projectExplorer);
        this.presenterProvider = presenterProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        this.presenterProvider.get().showDialog();
    }
}
