/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';

/**
 * @ngdoc controller
 * @name workspaces.list.controller:WorkspaceItemStatusController
 * @description This class is handling the controller for workspace status widget
 * @author Oleksii Orel
 */
export class WorkspaceStatusController {
  /**
   * Root scope service.
   */
  private $rootScope: ng.IRootScopeService;
  private cheNotification: CheNotification;
  private cheWorkspace: CheWorkspace;

  private isLoading: boolean;
  private workspace: che.IWorkspace;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($rootScope: ng.IRootScopeService,
              cheNotification: CheNotification,
              cheWorkspace: CheWorkspace) {
    this.$rootScope = $rootScope;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;

    this.isLoading = false;
  }

  startWorkspace(): void {
    let status = this.getWorkspaceStatus();
    if (this.isLoading || !this.workspace || !this.workspace.config || !(status === 'STOPPED' || status === 'ERROR')) {
      return;
    }

    this.updateRecentWorkspace(this.workspace.id);

    this.isLoading = true;
    let promise = this.cheWorkspace.startWorkspace(this.workspace.id, this.workspace.config.defaultEnv);

    promise.then(() => {
      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError('Run workspace error.', error);
    });
  }

  stopWorkspace(): void {
    let status = this.getWorkspaceStatus();
    if (this.isLoading || !this.workspace || (status !== 'RUNNING' && status !== 'STARTING')) {
      return;
    }

    this.isLoading = true;
    let promise = this.cheWorkspace.stopWorkspace(this.workspace.id, false);

    promise.then(() => {
      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError('Stop workspace error.', error);
    });
  }

  /**
   * Returns current status of workspace
   * @returns {String}
   */
  getWorkspaceStatus(): string {
    let workspace = this.cheWorkspace.getWorkspaceById(this.workspace.id);
    return workspace ? workspace.status : 'unknown';
  }

  /**
   * Is show run button.
   *
   * @returns {boolean}
   */
  isShowRun(): boolean {
    let status = this.getWorkspaceStatus();
    return status !== 'RUNNING' && status !== 'STOPPING' && status !== 'SNAPSHOTTING' && status !== 'STARTING';
  }

  /**
   * Is stop button disabled.
   *
   * @returns {boolean}
   */
  isStopDisabled(): boolean {
    let status = this.getWorkspaceStatus();
    return status === 'STOPPING' || status === 'SNAPSHOTTING'
  }

  /**
   * Emit event to move workspace immediately
   * to top of the recent workspaces list
   *
   * @param {string} workspaceId
   */
  updateRecentWorkspace(workspaceId: string): any {
    this.$rootScope.$broadcast('recent-workspace:set', workspaceId);
  }

}
