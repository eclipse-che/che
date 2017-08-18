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
package org.eclipse.che.ide.workspace.create;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * Provides methods which allow to set up special parameters for creating user workspaces.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(CreateWorkspaceViewImpl.class)
interface CreateWorkspaceView extends View<CreateWorkspaceView.ActionDelegate> {

  /** Shows dialog window to set up creating workspace. */
  void show();

  /** Hides dialog window. */
  void hide();

  /** Returns name of workspace from special place on view. */
  String getWorkspaceName();

  /**
   * Sets name for workspace in special place on view
   *
   * @param name name which will be set
   */
  void setWorkspaceName(String name);

  /**
   * Shows error message for workspace name.
   *
   * @param error error message which will be shown
   */
  void showValidationNameError(String error);

  interface ActionDelegate {
    /** Performs some actions when user clicks on create workspace button. */
    void onCreateButtonClicked();

    /** Performs some actions when user change name of workspace. */
    void onNameChanged();
  }
}
