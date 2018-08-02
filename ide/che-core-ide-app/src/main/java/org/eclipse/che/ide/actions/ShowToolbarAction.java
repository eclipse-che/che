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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ToggleAction;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.workspace.WorkspaceView;

/** Action to show or hide Toolbar. */
@Singleton
public class ShowToolbarAction extends ToggleAction {

  public static final String SHOW_TOOLBAR = "showToolbar";

  private final PreferencesManager preferencesManager;

  private final WorkspaceView workspaceView;

  @Inject
  public ShowToolbarAction(
      CoreLocalizationConstant localizationConstant,
      PreferencesManager preferencesManager,
      WorkspaceView workspaceView) {
    super(localizationConstant.actionShowToolbar());
    this.preferencesManager = preferencesManager;
    this.workspaceView = workspaceView;

    String showToolbar = preferencesManager.getValue(SHOW_TOOLBAR);
    workspaceView.showToolbar(Boolean.parseBoolean(showToolbar));
  }

  @Override
  public boolean isSelected(ActionEvent e) {
    return workspaceView.isToolbarVisible();
  }

  @Override
  public void setSelected(ActionEvent e, boolean state) {
    preferencesManager.setValue(SHOW_TOOLBAR, Boolean.toString(state));
    workspaceView.showToolbar(state);
    preferencesManager.flushPreferences();
  }
}
