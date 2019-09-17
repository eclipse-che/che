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
  workspaces: Array<che.IWorkspace> = [];
  isLoading: boolean = true;

  /**
   * Default constructor
   */
  constructor(cheWorkspace: CheWorkspace, cheNotification: CheNotification) {
    this.cheWorkspace = cheWorkspace;
    this.cheNotification = cheNotification;
  }

  $onInit(): void {
    this.loadData();
  }

  /**
   * Load workspaces
   */
  loadData(): void {
    this.workspaces = this.cheWorkspace.getWorkspaces();

    if (this.workspaces.length > 0) {
      this.isLoading = false;
      return;
    }

    let promise = this.cheWorkspace.fetchWorkspaces();

    promise.then((result) => {
      this.workspaces = result;
      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Update workspaces failed.');
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
