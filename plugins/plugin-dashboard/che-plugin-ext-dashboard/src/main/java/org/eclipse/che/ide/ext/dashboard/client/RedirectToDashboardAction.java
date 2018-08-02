/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.dashboard.client;

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.LEFT;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.ui.Tooltip;

/**
 * Action to provide Dashboard button onto toolbar.
 *
 * @author Oleksii Orel
 */
public class RedirectToDashboardAction extends BaseAction
    implements CustomComponentAction, WorkspaceRunningEvent.Handler, WorkspaceStoppedEvent.Handler {

  private final DashboardLocalizationConstant constant;
  private final DashboardResources resources;
  private final AppContext appContext;

  private Element arrow;
  private Tooltip tooltip;

  @Inject
  public RedirectToDashboardAction(
      DashboardLocalizationConstant constant,
      DashboardResources resources,
      EventBus eventBus,
      AppContext appContext) {
    this.constant = constant;
    this.resources = resources;
    this.appContext = appContext;

    eventBus.addHandler(WorkspaceRunningEvent.TYPE, this);
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {}

  @Override
  public Widget createCustomComponent(Presentation presentation) {
    FlowPanel panel = new FlowPanel();
    panel.setWidth("24px");
    panel.setHeight("24px");

    /** Show button Expanded by default if IDE is loaded in frame. */
    if (isInFrame()) {
      arrow = DOM.createDiv();
      arrow.setClassName(resources.dashboardCSS().dashboardArrow());
      panel.getElement().appendChild(arrow);

      tooltip =
          Tooltip.create(
              (elemental.dom.Element) arrow,
              BOTTOM,
              LEFT,
              constant.hideDashboardNavBarToolbarButtonTitle());

      showExpanded();

      DOM.setEventListener(
          arrow,
          new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
              onArrowClicked();
            }
          });
      DOM.sinkEvents(arrow, Event.ONMOUSEDOWN);
    } else {
      arrow = DOM.createAnchor();
      arrow.setClassName(resources.dashboardCSS().dashboardArrow());
      panel.getElement().appendChild(arrow);

      tooltip =
          Tooltip.create(
              (elemental.dom.Element) arrow,
              BOTTOM,
              LEFT,
              constant.openDashboardToolbarButtonTitle());

      showCollapsed();

      arrow.setAttribute(
          "href",
          constant.openDashboardUrlWorkspace(appContext.getWorkspace().getConfig().getName()));
      arrow.setAttribute("target", "_blank");
    }

    return panel;
  }

  @Override
  public void onWorkspaceRunning(WorkspaceRunningEvent event) {
    if (arrow != null) {
      arrow.setAttribute(
          "href",
          constant.openDashboardUrlWorkspace(appContext.getWorkspace().getConfig().getName()));
    }
  }

  @Override
  public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
    if (arrow != null) {
      arrow.setAttribute("href", constant.openDashboardUrlWorkspaces());
    }
  }

  /**
   * Determines whether the IDE is loaded inside frame.
   *
   * @return <b>true</b> if IDE is loaded in frame
   */
  private native boolean isInFrame() /*-{
        if ($wnd == $wnd.parent) {
            return false;
        }

        return true;
    }-*/;

  /** Makes arrow left-oriented. Dashboard navigation bar should be visible. */
  private native void showExpanded() /*-{
        var elem = this.@org.eclipse.che.ide.ext.dashboard.client.RedirectToDashboardAction::arrow;
        if (!elem) {
            return;
        }

        elem.innerHTML = "<i class=\"fa fa-chevron-left\" />";
        elem.expanded = true;

        var tooltip = this.@org.eclipse.che.ide.ext.dashboard.client.RedirectToDashboardAction::tooltip;
        if (!tooltip) {
            return;
        }

        var constant = this.@org.eclipse.che.ide.ext.dashboard.client.RedirectToDashboardAction::constant;
        var message = constant.@org.eclipse.che.ide.ext.dashboard.client.DashboardLocalizationConstant::hideDashboardNavBarToolbarButtonTitle()();
        tooltip.@org.eclipse.che.ide.ui.Tooltip::setTitle(*)(message);
    }-*/;

  /** Makes arrow right-oriented. Dashboard navigation bar should be hidden. */
  private native void showCollapsed() /*-{
        var elem = this.@org.eclipse.che.ide.ext.dashboard.client.RedirectToDashboardAction::arrow;
        if (!elem) {
            return;
        }

        elem.innerHTML = "<i class=\"fa fa-chevron-right\" />";
        elem.expanded = false;

        var tooltip = this.@org.eclipse.che.ide.ext.dashboard.client.RedirectToDashboardAction::tooltip;
        if (!tooltip) {
            return;
        }

        var constant = this.@org.eclipse.che.ide.ext.dashboard.client.RedirectToDashboardAction::constant;
        var message = constant.@org.eclipse.che.ide.ext.dashboard.client.DashboardLocalizationConstant::showDashboardNavBarToolbarButtonTitle()();
        tooltip.@org.eclipse.che.ide.ui.Tooltip::setTitle(*)(message);
    }-*/;

  /** Handles clicking on the arrow. */
  private native void onArrowClicked() /*-{
        var elem = this.@org.eclipse.che.ide.ext.dashboard.client.RedirectToDashboardAction::arrow;
        if (!elem) {
            return;
        }

        if (elem.expanded) {
            $wnd.parent.postMessage("hide-navbar", "*");
            this.@org.eclipse.che.ide.ext.dashboard.client.RedirectToDashboardAction::showCollapsed()();
        } else {
            $wnd.parent.postMessage("show-navbar", "*");
            this.@org.eclipse.che.ide.ext.dashboard.client.RedirectToDashboardAction::showExpanded()();
        }
    }-*/;
}
