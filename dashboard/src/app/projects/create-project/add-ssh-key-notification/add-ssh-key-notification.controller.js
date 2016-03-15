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
 * This class is handling the controller for the add secret key
 * @author Oleksii Orel
 */
export class AddSecretKeyNotificationCtrl {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, cheAPI, lodash) {
    this.$mdDialog = $mdDialog;
    this.cheAPI = cheAPI;
    this.lodash = lodash;

    this.configURL = '#/workspaces';

    // fetch workspaces when initializing
    let promise = cheAPI.getWorkspace().fetchWorkspaces();
    promise.then(() => {
      this.updateConfigURL(this.workspaceId);
    }, (error) => {
      if (error.status === 304) {
        this.updateConfigURL(this.workspaceId);
      }
    });

  }

  /**
   * Update config url.
   * @param workspaceId - the ID of the current workspace
   */
  updateConfigURL(workspaceId) {
    let workspaces = this.cheAPI.getWorkspace().getWorkspaces();

    let findWorkspace = this.lodash.find(workspaces, (workspace) => {
      return workspace.id === workspaceId;
    });

    if (findWorkspace) {
      let findLink = this.lodash.find(findWorkspace.links, (link) => {
        return link.rel === 'ide url';
      });
      if (findLink) {
        this.configURL = findLink.href + '?action=showPreferences';
      }
    }
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }
}
