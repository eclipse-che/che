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
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheWorkspace, WorkspaceStatus} from '../../../../components/api/workspace/che-workspace.factory';

/**
 * @ngdoc controller
 * @name workspaces.list.controller:WorkspaceItemStatusController
 * @description This class is handling the controller for workspace status widget
 * @author Oleksii Orel
 */
export class WorkspaceStatusController {

  static $inject = ['$rootScope', 'cheWorkspace', 'cheNotification'];

  /**
   * Root scope service.
   */
  private $rootScope: ng.IRootScopeService;
  private cheNotification: CheNotification;
  private cheWorkspace: CheWorkspace;

  private isRequestPending: boolean;
  private workspaceId: string;

  /**
   * Default constructor that is using resource
   */
  constructor($rootScope: ng.IRootScopeService, cheWorkspace: CheWorkspace, cheNotification: CheNotification) {
    this.$rootScope = $rootScope;
    this.cheWorkspace = cheWorkspace;
    this.cheNotification = cheNotification;
  }

  /**
   * Change workspace status.
   */
  changeWorkspaceStatus(): void {
    if (this.isRequestPending || !this.workspaceId) {
      return;
    }
    const workspace = this.cheWorkspace.getWorkspaceById(this.workspaceId);
    if (!workspace) {
      return;
    }

    const status = this.getWorkspaceStatus();
    const isRunButton = status !== WorkspaceStatus.RUNNING && status !== WorkspaceStatus.STOPPING && status !== WorkspaceStatus.STARTING;
    const environment = workspace.config ? workspace.config.defaultEnv : null;

    if (isRunButton) {
      this.updateRecentWorkspace(this.workspaceId);
    }
    this.isRequestPending = true;
    this.cheWorkspace.fetchStatusChange(this.workspaceId, 'ERROR').then((data: any) => {
      this.cheNotification.showError(data.error);
    });
    const promise = isRunButton ? this.cheWorkspace.startWorkspace(this.workspaceId, environment) : this.cheWorkspace.stopWorkspace(this.workspaceId);
    promise.catch((error: any) => {
      this.cheNotification.showError(`${isRunButton ? 'Run' : 'Stop'} workspace error.`, error);
    }).finally(() => {
      this.isRequestPending = false;
    });
  }

  /**
   * Returns status of button.
   *
   * @returns {boolean} <code>true</code> if button disabled
   */
  isButtonDisabled(): boolean {
    const status = this.getWorkspaceStatus();
    return this.isRequestPending || status === WorkspaceStatus.STOPPING;
  }

  /**
   * Returns current status of workspace
   * @returns {number}
   */
  getWorkspaceStatus(): number {
    const workspace = this.cheWorkspace.getWorkspaceById(this.workspaceId);
    if (!workspace || !workspace.status) {
      return -1;
    }

    return WorkspaceStatus[workspace.status];
  }

  /**
   * Is show run button.
   *
   * @returns {boolean}
   */
  isShowRun(): boolean {
    const status = this.getWorkspaceStatus();
    return status !== WorkspaceStatus.RUNNING && status !== WorkspaceStatus.STOPPING && status !== WorkspaceStatus.STARTING;
  }

  /**
   * Emit event to move workspace immediately
   * to top of the recent workspaces list
   *
   * @param {string} workspaceId
   */
  updateRecentWorkspace(workspaceId: string): void {
    this.$rootScope.$broadcast('recent-workspace:set', workspaceId);
  }

}
