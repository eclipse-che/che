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
package org.eclipse.che.ide.actions;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

/**
 * Open a new dashboard window with information about the projects
 *
 * @author Oleksii Orel
 */
public class RedirectToDashboardProjectsAction extends Action {

    private static final String REDIRECT_URL = "/dashboard/#/projects";

    @Inject
    public RedirectToDashboardProjectsAction(CoreLocalizationConstant localization) {
        super(localization.actionRedirectToDashboardProjectsTitle(), localization.actionRedirectToDashboardProjectsDescription(), null, null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        Window.open(REDIRECT_URL, "_blank", "");
    }
}
