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
package org.eclipse.che.ide.ext.dashboard.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;

/**
 * Action to provide Dashboard button onto toolbar.
 *
 * @author Oleksii Orel
 */
public class RedirectToDashboardAction extends Action implements CustomComponentAction {
    private final AnalyticsEventLogger          eventLogger;
    private final DashboardLocalizationConstant constant;
    private final DashboardResources            resources;

    @Inject
    public RedirectToDashboardAction(DashboardLocalizationConstant constant,
                                     DashboardResources resources,
                                     AnalyticsEventLogger eventLogger) {
        this.constant = constant;
        this.resources = resources;
        this.eventLogger = eventLogger;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        final Anchor dashboardButton = new Anchor();
        final Element tooltipContainer = DOM.createDiv();
        final Element tooltipElement = DOM.createSpan();

        dashboardButton.ensureDebugId("dashboard-toolbar-button");
        dashboardButton.addStyleName(resources.dashboardCSS().dashboardButton());
        dashboardButton.setHref(constant.openDashboardRedirectUrl());
        dashboardButton.getElement().setAttribute("target", "_blank");
        dashboardButton.getElement().insertFirst(resources.dashboardButtonBackground().getSvg().getElement());
        dashboardButton.getElement().appendChild(resources.dashboardButtonIcon().getSvg().getElement());
        tooltipElement.setInnerText(constant.openDashboardToolbarButtonTitle());
        tooltipContainer.appendChild(tooltipElement);
        tooltipContainer.setClassName(resources.dashboardCSS().tooltip());
        dashboardButton.getElement().appendChild(tooltipContainer);

        return dashboardButton;
    }

}
