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
package org.eclipse.che.ide.ext.dashboard.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_LEFT_MAIN_MENU;

/**
 * Extension that adds redirect to Dashboard button to the main menu.
 *
 * @author Oleksii Orel
 */
@Singleton
@Extension(title = "Dashboard", version = "4.0.0")
public class DashboardExtension {

    @Inject
    public DashboardExtension(ActionManager actionManager,
                              RedirectToDashboardAction redirectToDashboardAction,
                              DashboardResources dashboardResources) {
        actionManager.registerAction("redirectToDashboardAction", redirectToDashboardAction);
        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_LEFT_MAIN_MENU);
        mainToolbarGroup.add(redirectToDashboardAction, Constraints.FIRST);

        dashboardResources.dashboardCSS().ensureInjected();
    }
}
