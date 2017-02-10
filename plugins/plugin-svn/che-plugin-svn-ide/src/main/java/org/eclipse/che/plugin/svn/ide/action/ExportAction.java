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
import org.eclipse.che.plugin.svn.ide.export.ExportPresenter;

/**
 * Extension of {@link SubversionAction} for implementing the "svn export" command.
 */
@Singleton
public class ExportAction extends SubversionAction {

    private ExportPresenter presenter;

    @Inject
    public ExportAction(AppContext appContext,
                        SubversionExtensionLocalizationConstants constants,
                        SubversionExtensionResources resources,
                        ExportPresenter presenter) {
        super(constants.exportTitle(), constants.exportDescription(), resources.export(), appContext, constants, resources);
        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        presenter.showExport();
    }
}
