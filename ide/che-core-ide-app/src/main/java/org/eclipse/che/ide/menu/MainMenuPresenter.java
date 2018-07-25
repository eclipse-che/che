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
package org.eclipse.che.ide.menu;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * Manages Main Menu Items, their runtime visibility and enabled state.
 *
 * @author Nikolay Zamosenchuk
 */
@Singleton
public class MainMenuPresenter implements Presenter, MainMenuView.ActionDelegate {

  private final MainMenuView view;

  /**
   * Main Menu Presenter requires View implementation
   *
   * @param view
   */
  @Inject
  public MainMenuPresenter(MainMenuView view) {
    this.view = view;
    this.view.setDelegate(this);
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }
}
