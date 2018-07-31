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
package org.eclipse.che.ide.command.toolbar.controller;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.mvp.Presenter;

/** Manages visibility of Toolbar. */
@Singleton
public class ToolbarControllerPresenter implements Presenter, ToolbarControllerView.ActionDelegate {

  private ToolbarControllerView view;
  private ToolbarControllerMenu toolbarControllerMenu;

  @Inject
  public ToolbarControllerPresenter(
      ToolbarControllerView view, ToolbarControllerMenu toolbarControllerMenu) {
    this.view = view;
    this.toolbarControllerMenu = toolbarControllerMenu;
    view.setDelegate(this);
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  @Override
  public void showMenu(int mouseX, int mouseY) {
    toolbarControllerMenu.show(mouseX, mouseY);
  }
}
