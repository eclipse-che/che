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
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';
import {WorkspacesService} from '../../workspaces.service';

/**
 * @ngdoc controller
 * @name workspaces.list.controller:WorkspaceItemCtrl
 * @description This class is handling the controller for item of workspace list
 * @author Ann Shumilova
 */
export class WorkspaceItemCtrl {

  static $inject = ['$location', 'lodash', 'cheWorkspace', 'workspacesService'];

  $location: ng.ILocationService;
  lodash: any;
  cheWorkspace: CheWorkspace;
  workspacesService: WorkspacesService;

  workspace: che.IWorkspace;
  workspaceName: string;
  workspaceSupportIssues = '';

  /**
   * Default constructor that is using resource
   */
  constructor($location: ng.ILocationService,
              lodash: any,
              cheWorkspace: CheWorkspace,
              workspacesService: WorkspacesService) {
    this.$location = $location;
    this.lodash = lodash;
    this.cheWorkspace = cheWorkspace;
    this.workspacesService = workspacesService;
    this.workspaceName = this.cheWorkspace.getWorkspaceDataManager().getName(this.workspace);
  }

  /**
   * Returns `true` if supported.
   *
   * @returns {boolean}
   */
  get isSupported(): boolean {
    if (!this.workspacesService.isSupportedRecipeType(this.workspace)) {
      this.workspaceSupportIssues = 'Current infrastructure doesn\'t support this workspace recipe type.';

      return false;
    }
    if (!this.workspacesService.isSupportedVersion(this.workspace)) {
      this.workspaceSupportIssues = `This workspace is using old definition format which is not compatible anymore. 
          Please follow the documentation to update the definition of the workspace and benefits from the latest capabilities.`;

      return false;
    }
    if (!this.workspaceSupportIssues) {
      this.workspaceSupportIssues = '';
    }

    return true;
  }

  /**
   * Redirects to workspace details.
   * @param tab {string}
   */
  redirectToWorkspaceDetails(tab?: string): void {
    this.$location.path('/workspace/' + this.workspace.namespace + '/' + this.workspaceName).search({tab: tab ? tab : 'Overview'});
  }

  getDefaultEnvironment(workspace: che.IWorkspace): che.IWorkspaceEnvironment {
    let environments = workspace.config.environments;
    let envName = workspace.config.defaultEnv;
    let defaultEnvironment = environments[envName];
    return defaultEnvironment;
  }

  getMemoryLimit(workspace: che.IWorkspace): string {
    if (!workspace.config && workspace.devfile) {
      return '-';
    }

    let environment = this.getDefaultEnvironment(workspace);
    if (environment) {
      let limits = this.lodash.pluck(environment.machines, 'attributes.memoryLimitBytes');
      let total = 0;
      limits.forEach((limit: number) => {
        if (limit) {
          total += limit / (1024 * 1024);
        }
      });
      return (total > 0) ? Math.round(total) + ' MB' : '-';
    }

    return '-';
  }

  /**
   * Returns current status of workspace
   * @returns {String}
   */
  getWorkspaceStatus(): string {
    let workspace = this.cheWorkspace.getWorkspaceById(this.workspace.id);
    return workspace ? workspace.status : 'unknown';
  }
}
