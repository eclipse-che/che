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
  }

  /**
   * Returns `true` if default environment of workspace contains supported recipe type.
   *
   * @returns {boolean}
   */
  get isSupported(): boolean {
    return this.workspacesService.isSupported(this.workspace);
  }

  /**
   * Redirects to workspace details.
   * @param tab {string}
   */
  redirectToWorkspaceDetails(tab?: string): void {
    this.$location.path('/workspace/' + this.workspace.namespace + '/' + this.workspace.config.name).search({tab: tab ? tab : 'Overview'});
  }

  getDefaultEnvironment(workspace: che.IWorkspace): che.IWorkspaceEnvironment {
    let environments = workspace.config.environments;
    let envName = workspace.config.defaultEnv;
    let defaultEnvironment = environments[envName];
    return defaultEnvironment;
  }

  getMemoryLimit(workspace: che.IWorkspace): string {
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
