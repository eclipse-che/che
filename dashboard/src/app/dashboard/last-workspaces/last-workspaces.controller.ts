/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';

/**
 * @ngdoc controller
 * @name dashboard.controller:DashboardLastWorkspacesController
 * @description This class is handling the controller of the last workspaces to display in the dashboard
 * @author Oleksii Orel
 */
export class DashboardLastWorkspacesController {

  static $inject = ['cheWorkspace', 'cheNotification'];

  cheWorkspace: CheWorkspace;
  cheNotification: CheNotification;
  workspaces: Array<che.IWorkspace>;
  isLoading: boolean;

  /**
   * Default constructor
   */
  constructor(cheWorkspace: CheWorkspace, cheNotification: CheNotification) {
    this.cheWorkspace = cheWorkspace;
    this.cheNotification = cheNotification;

    this.workspaces = cheWorkspace.getWorkspaces();

    if (this.workspaces.length === 0) {
      this.updateData();
    }
  }

  /**
   * Update workspaces
   */
  updateData(): void {
    this.isLoading = true;
    let promise = this.cheWorkspace.fetchWorkspaces();

    promise.then(() => {
      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        return;
      }
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Update workspaces failed.');
    });
  }

  /**
   * Returns workspaces
   * @returns {Array<che.IWorkspace>}
   */
  getWorkspaces(): Array<che.IWorkspace> {
    return this.workspaces;
  }
}
