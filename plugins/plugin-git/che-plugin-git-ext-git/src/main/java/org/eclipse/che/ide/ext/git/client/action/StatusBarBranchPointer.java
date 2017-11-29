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
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
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

  public static final String GIT_CURRENT_HEAD_NAME = "git.current.head.name";

  private final AppContext appContext;
  private final GitResources resources;
  private final BranchPresenter branchPresenter;
  private final GitLocalizationConstant constant;
  private final HorizontalPanel panel;

  @Inject
  public StatusBarBranchPointer(
      AppContext appContext,
      GitResources resources,
      BranchPresenter branchPresenter,
      GitLocalizationConstant constant) {
    super();
    this.appContext = appContext;
    this.resources = resources;
    this.branchPresenter = branchPresenter;
    this.constant = constant;

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
    if (project != null && project.getAttributes().containsKey(GIT_CURRENT_HEAD_NAME)) {
      Label projectNameLabel = new Label(project.getName());
      projectNameLabel.ensureDebugId("statusBarProjectBranchRepositoryName");
      projectNameLabel.getElement().getStyle().setMarginLeft(5., Unit.PX);
      panel.add(projectNameLabel);

      SVGImage branchIcon = new SVGImage(resources.checkoutReference());
      branchIcon.getSvgElement().getStyle().setMarginLeft(5., Unit.PX);
      panel.add(branchIcon);

      Label headLabel = new Label(project.getAttribute(GIT_CURRENT_HEAD_NAME));
      headLabel.ensureDebugId("statusBarProjectBranchName");
      headLabel.setTitle(constant.branchesControlTitle());
      Style headLabelStyle = headLabel.getElement().getStyle();
      headLabelStyle.setCursor(Cursor.POINTER);
      headLabelStyle.setMarginLeft(5., Unit.PX);
      headLabel.addClickHandler(event -> branchPresenter.showBranches(project));
      panel.add(headLabel);
    }
  }
}
