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
package org.eclipse.che.ide.menu;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * Manages status panel group items, their runtime visibility and enabled state.
 *
 * @author Oleksii Orel
 */
@Singleton
public class StatusPanelGroupPresenter implements Presenter, StatusPanelGroupView.ActionDelegate {

  private final StatusPanelGroupView view;

  /**
   * Bottom Menu Presenter requires View implementation
   *
   * @param view
   */
  @Inject
  public StatusPanelGroupPresenter(StatusPanelGroupView view) {
    this.view = view;
    this.view.setDelegate(this);
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }
}
