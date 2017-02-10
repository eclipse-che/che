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

/**
 * Extension of {@link SubversionAction} for implementing the "svn diff" (create patch) command.
 */
@Singleton
public class CreatePatchAction extends SubversionAction {

    @Inject
    public CreatePatchAction(AppContext appContext,
                             SubversionExtensionLocalizationConstants constants,
                             SubversionExtensionResources resources) {
        super(constants.createPatchTitle(), constants.createPatchDescription(), resources.createPatch(), appContext, constants, resources);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }
}
