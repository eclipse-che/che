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
import org.eclipse.che.plugin.svn.ide.update.UpdatePresenter;

/**
 * Extension of {@link SubversionAction} for implementing the "svn update" command (For the latest revision).
 */
@Singleton
public class UpdateAction extends SubversionAction {

    private final UpdatePresenter presenter;

    @Inject
    public UpdateAction(AppContext appContext,
                        SubversionExtensionLocalizationConstants constants,
                        SubversionExtensionResources resources,
                        UpdatePresenter presenter) {
        super(constants.updateTitle(), constants.updateDescription(), resources.update(), appContext, constants, resources);

        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        presenter.showUpdate();
    }
}
