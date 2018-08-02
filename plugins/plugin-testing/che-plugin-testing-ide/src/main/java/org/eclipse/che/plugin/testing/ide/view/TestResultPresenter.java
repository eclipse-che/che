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
package org.eclipse.che.plugin.testing.ide.view;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.plugin.testing.ide.TestLocalizationConstant;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.model.TestRootState;
import org.eclipse.che.plugin.testing.ide.model.TestStateEventsListener;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Presenter for the displaying the test results on the workspace.
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestResultPresenter extends BasePresenter implements TestResultView.ActionDelegate {
  private final WorkspaceAgent workspaceAgent;
  private final TestLocalizationConstant localizationConstant;
  private final TestResources resources;
  private final TestResultView view;

  @Inject
  public TestResultPresenter(
      WorkspaceAgent workspaceAgent,
      TestLocalizationConstant localizationConstant,
      TestResources resources,
      TestResultView view) {
    this.workspaceAgent = workspaceAgent;
    this.localizationConstant = localizationConstant;
    this.resources = resources;
    this.view = view;
    view.setDelegate(this);
  }

  @Override
  public String getTitle() {
    return localizationConstant.titleTestResultPresenter();
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public String getTitleToolTip() {
    return localizationConstant.titleTestResultPresenterToolTip();
  }

  @Override
  public SVGResource getTitleImage() {
    return resources.testIcon();
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  /** Activate Test results part and showing the test results. */
  public void handleResponse() {
    workspaceAgent.openPart(this, PartStackType.INFORMATION);
    workspaceAgent.setActivePart(this);
  }

  public TestRootState getRootState() {
    return view.getRootState();
  }

  public TestStateEventsListener getEventListener() {
    return view;
  }
}
