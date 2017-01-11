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

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.ui.Tooltip;
import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.RIGHT;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

/**
 * Action to provide Dashboard button onto toolbar.
 *
 * @author Oleksii Orel
 */
public class RedirectToDashboardAction extends Action implements CustomComponentAction,
        WorkspaceStartedEvent.Handler, WorkspaceStoppedEvent.Handler {

    private final DashboardLocalizationConstant constant;
    private final DashboardResources            resources;
    private final AppContext                    appContext;

    private Element                             arrow;

    @Inject
    public RedirectToDashboardAction(DashboardLocalizationConstant constant,
                                     DashboardResources resources,
                                     EventBus eventBus,
                                     AppContext appContext) {
        this.constant = constant;
        this.resources = resources;
        this.appContext = appContext;

        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        FlowPanel panel = new FlowPanel();
        panel.setWidth("24px");
        panel.setHeight("24px");

        arrow = DOM.createAnchor();
        arrow.setClassName(resources.dashboardCSS().dashboardArrow());
        arrow.setInnerHTML("<i class=\"fa fa-chevron-right\" />");
        panel.getElement().appendChild(arrow);

        arrow.setAttribute("href", constant.openDashboardUrlWorkspace(appContext.getWorkspace().getConfig().getName()));
        arrow.setAttribute("target", "_blank");

        Tooltip.create((elemental.dom.Element) arrow,
                BOTTOM,
                RIGHT,
                constant.openDashboardToolbarButtonTitle());

        return panel;
    }

    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
        if (arrow != null) {
            arrow.setAttribute("href", constant.openDashboardUrlWorkspace(event.getWorkspace().getConfig().getName()));
        }
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        if (arrow != null) {
            arrow.setAttribute("href", constant.openDashboardUrlWorkspaces());
        }
    }

}
