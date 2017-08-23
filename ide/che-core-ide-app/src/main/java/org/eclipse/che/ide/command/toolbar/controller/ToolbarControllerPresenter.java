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
