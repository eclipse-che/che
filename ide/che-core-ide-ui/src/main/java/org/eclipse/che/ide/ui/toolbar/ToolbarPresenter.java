/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
