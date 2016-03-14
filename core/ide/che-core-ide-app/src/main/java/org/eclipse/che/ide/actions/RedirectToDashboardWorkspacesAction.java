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

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

/**
 * Open a new dashboard window with information about the workspaces
 *
 * @author Oleksii Orel
 */
public class RedirectToDashboardWorkspacesAction extends Action {

    private static final String REDIRECT_URL = "/dashboard/#/workspaces";

    private final AnalyticsEventLogger        eventLogger;

    @Inject
    public RedirectToDashboardWorkspacesAction(CoreLocalizationConstant localization,
                                               AnalyticsEventLogger eventLogger) {
        super(localization.actionRedirectToDashboardWorkspacesTitle(), localization.actionRedirectToDashboardWorkspacesDescription(), null, null);
        this.eventLogger = eventLogger;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        Window.open(REDIRECT_URL, "_blank", "");
    }
}
