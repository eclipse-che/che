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
package org.eclipse.che.plugin.svn.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.lockunlock.LockUnlockPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Extension of {@link SubversionAction} for implementing the "svn lock" command.
 */
@Singleton
public class LockAction extends SubversionAction {

    private final LockUnlockPresenter presenter;

    @Inject
    public LockAction(final AppContext appContext,
                      final LockUnlockPresenter presenter,
                      final ProjectExplorerPresenter projectExplorerPresenter,
                      final SubversionExtensionLocalizationConstants constants,
                      final SubversionExtensionResources resources) {
        super(constants.lockTitle(), constants.lockDescription(), resources.lock(), appContext,
              constants, resources, projectExplorerPresenter);
        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        this.presenter.showLockDialog();
    }

    @Override
    protected boolean isSelectionRequired() {
        return false;
    }
}
