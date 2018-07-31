/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.workspace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implements {@link WorkspaceView}
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
@Singleton
public class WorkspaceViewImpl extends LayoutPanel implements WorkspaceView {

  interface WorkspaceViewUiBinder extends UiBinder<Widget, WorkspaceViewImpl> {}

  private static WorkspaceViewUiBinder uiBinder = GWT.create(WorkspaceViewUiBinder.class);

  @UiField SimpleLayoutPanel perspectivePanel;

  @UiField DockLayoutPanel ideMainDockPanel;

  @UiField DockLayoutPanel topMenuLayoutPanel;

  @UiField SimplePanel menuPanel;

  @UiField SimplePanel toolbarPanel, noToolbarPanel;

  @UiField SimplePanel statusPanel;

  ActionDelegate delegate;

  private boolean toolbar = true;

  /** Create view. */
  @Inject
  protected WorkspaceViewImpl() {
    add(uiBinder.createAndBindUi(this));
    getElement().setId("codenvyIdeWorkspaceViewImpl");
  }

  @Override
  public void showToolbar(boolean show) {
    toolbar = show;

    ideMainDockPanel.setWidgetHidden(toolbarPanel, !show);
    ideMainDockPanel.setWidgetHidden(noToolbarPanel, show);
  }

  @Override
  public boolean isToolbarVisible() {
    return toolbar;
  }

  /** {@inheritDoc} */
  @Override
  public AcceptsOneWidget getMenuPanel() {
    return menuPanel;
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public AcceptsOneWidget getPerspectivePanel() {
    return perspectivePanel;
  }

  /** {@inheritDoc} */
  @Override
  public AcceptsOneWidget getToolbarPanel() {
    return toolbarPanel;
  }

  @Override
  public AcceptsOneWidget getStatusPanel() {
    return statusPanel;
  }
}
