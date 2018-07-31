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
package org.eclipse.che.ide.ui.toolbar;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * Manages Toolbar items, changes item state and other.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
public class ToolbarPresenter implements Presenter, ToolbarView.ActionDelegate {

  private ToolbarView view;

  /**
   * Creates an instance of this presenter.
   *
   * @param view
   */
  @Inject
  public ToolbarPresenter(ToolbarView view) {
    this.view = view;
    this.view.setDelegate(this);
  }

  public void bindMainGroup(ActionGroup group) {
    view.setLeftActionGroup(group);
  }

  public void bindCenterGroup(ActionGroup group) {
    view.setCenterActionGroup(group);
  }

  public void bindRightGroup(ActionGroup group) {
    view.setRightActionGroup(group);
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }
}
