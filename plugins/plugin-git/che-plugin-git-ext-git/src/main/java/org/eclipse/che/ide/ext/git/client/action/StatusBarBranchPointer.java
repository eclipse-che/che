/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.action;

import com.google.common.annotations.Beta;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.branch.BranchPresenter;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Displays in status bar current project name with vcs branch if present.
 *
 * @author Vlad Zhukovskyi
 * @since 6.0.0
 */
@Singleton
@Beta
public class StatusBarBranchPointer extends BaseAction implements CustomComponentAction {

  private final AppContext appContext;
  private final GitResources resources;
  private final BranchPresenter branchPresenter;
  private final HorizontalPanel panel;

  @Inject
  public StatusBarBranchPointer(
      AppContext appContext, GitResources resources, BranchPresenter branchPresenter) {
    super();
    this.appContext = appContext;
    this.resources = resources;
    this.branchPresenter = branchPresenter;

    panel = new HorizontalPanel();
    panel.ensureDebugId("statusBarProjectBranchPointerPanel");
  }

  @Override
  public Widget createCustomComponent(Presentation presentation) {
    return panel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {}

  @Override
  public void update(ActionEvent e) {
    panel.clear();

    Project project = appContext.getRootProject();
    if (project == null) {
      return;
    }

    Label projectNameLabel = new Label(project.getName());
    projectNameLabel.ensureDebugId("statusBarProjectBranchRepositoryName");
    projectNameLabel.getElement().getStyle().setMarginLeft(5., Unit.PX);
    panel.add(projectNameLabel);

    String head = project.getAttribute("git.current.head.name");
    if (head == null) {
      return;
    }

    SVGImage branchIcon = new SVGImage(resources.checkoutReference());
    branchIcon.getSvgElement().getStyle().setMarginLeft(5., Unit.PX);
    Label headLabel = new Label(head);
    headLabel.ensureDebugId("statusBarProjectBranchName");
    Style headLabelStyle = headLabel.getElement().getStyle();
    headLabelStyle.setCursor(Cursor.POINTER);
    headLabelStyle.setMarginLeft(5., Unit.PX);
    headLabel.addClickHandler(event -> branchPresenter.showBranches(project));
    panel.add(branchIcon);
    panel.add(headLabel);
  }
}
