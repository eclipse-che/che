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
import org.eclipse.che.plugin.svn.ide.sw.SwitchPresenter;

/**
 * Extension of {@link SubversionAction} for implementing the "svn switch" command.
 */
@Singleton
public class SwitchAction extends SubversionAction {

    private final SwitchPresenter presenter;

    @Inject
    public SwitchAction(SwitchPresenter presenter,
                        AppContext appContext,
                        SubversionExtensionLocalizationConstants constants,
                        SubversionExtensionResources resources) {
        super(constants.switchTitle(), constants.switchDescription(), resources.switchLocation(), appContext, constants, resources);
        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        presenter.showWindow();
    }
}
