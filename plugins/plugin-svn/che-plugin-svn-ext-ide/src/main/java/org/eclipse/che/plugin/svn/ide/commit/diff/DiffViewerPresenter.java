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
package org.eclipse.che.plugin.svn.ide.commit.diff;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;

/**
 * Presenter for the {@link DiffViewerView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class DiffViewerPresenter extends SubversionActionPresenter implements DiffViewerView.ActionDelegate {

    private DiffViewerView view;

    @Inject
    protected DiffViewerPresenter(AppContext appContext,
                                  SubversionOutputConsoleFactory consoleFactory,
                                  ProcessesPanelPresenter processesPanelPresenter,
                                  DiffViewerView view,
                                  StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors);
        this.view = view;
        this.view.setDelegate(this);
    }

    public void showDiff(String content) {
        view.showDiff(content);
        view.onShow();
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        view.onClose();
    }
}
