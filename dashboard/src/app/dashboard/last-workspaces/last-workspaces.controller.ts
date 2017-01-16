/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';
import {CheWorkspace} from '../../../components/api/che-workspace.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';

/**
 * @ngdoc controller
 * @name dashboard.controller:DashboardLastWorkspacesController
 * @description This class is handling the controller of the last workspaces to display in the dashboard
 * @author Oleksii Orel
 */
export class DashboardLastWorkspacesController {
  cheWorkspace: CheWorkspace;
  cheNotification: CheNotification;
  workspaces: Array<che.IWorkspace>;
  isLoading: boolean;

  /**
   * Default constructor
   * @ngInject for Dependency injection
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
