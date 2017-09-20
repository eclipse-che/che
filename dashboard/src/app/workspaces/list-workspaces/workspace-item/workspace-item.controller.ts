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
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';

/**
 * @ngdoc controller
 * @name workspaces.list.controller:WorkspaceItemCtrl
 * @description This class is handling the controller for item of workspace list
 * @author Ann Shumilova
 */
export class WorkspaceItemCtrl {
  $location: ng.ILocationService;
  lodash: _.LoDashStatic;
  cheWorkspace: CheWorkspace;

  workspace: che.IWorkspace;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, lodash: _.LoDashStatic, cheWorkspace: CheWorkspace) {
    this.$location = $location;
    this.lodash = lodash;
    this.cheWorkspace = cheWorkspace;
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
    if (workspace.runtime && workspace.runtime.machines && workspace.runtime.machines.length > 0) {
      let limits = this.lodash.pluck(workspace.runtime.machines, 'config.limits.ram');
      let total = 0;
      limits.forEach((limit: number) => {
        total += limit;
      });
      return Math.round(total) + ' MB';
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
