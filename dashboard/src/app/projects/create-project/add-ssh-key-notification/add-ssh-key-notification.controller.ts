/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * This class is handling the controller for the secret key error notification
 * @author Oleksii Orel
 */
export class AddSecretKeyNotificationCtrl {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, $location, cheAPI, lodash) {
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.cheAPI = cheAPI;
    this.lodash = lodash;

    this.workspacesById = cheAPI.getWorkspace().getWorkspacesById();
    this.workspace = this.workspacesById.get(this.workspaceId);

    if (!this.workspace) {
      cheAPI.getWorkspace().fetchWorkspaces().then(() => {
        this.workspace = this.workspacesById.get(this.workspaceId);
      });
    }

  }

  /**
   * Redirect to IDE preferences.
   */
  redirectToConfig() {
    if (!this.workspace) {
      return false;
    }
    this.$location.path('ide/' + this.workspace.namespace + '/' + this.workspace.config.name).search({action: 'showPreferences'});
    this.$mdDialog.hide();
    return true;
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }
}
