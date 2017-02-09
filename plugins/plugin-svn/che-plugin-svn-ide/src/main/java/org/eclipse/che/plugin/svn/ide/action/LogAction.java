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
package org.eclipse.che.plugin.svn.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.log.ShowLogPresenter;

/**
 * Extension of {@link SubversionAction} for implementing the "svn log" command.
 */
@Singleton
public class LogAction extends SubversionAction {

    private final ShowLogPresenter presenter;

    @Inject
    public LogAction(ShowLogPresenter presenter,
                     AppContext appContext,
                     SubversionExtensionLocalizationConstants constants,
                     SubversionExtensionResources resources) {
        super(constants.logTitle(), constants.logDescription(), resources.log(), appContext, constants, resources);

        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        presenter.showLog();
    }
}
