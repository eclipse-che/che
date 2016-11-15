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
 * @ngdoc controller
 * @name workspaces.list.controller:WorkspaceItemCtrl
 * @description This class is handling the controller for item of workspace list
 * @author Ann Shumilova
 */
export class WorkspaceItemCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location, lodash, cheWorkspace) {
    this.$location = $location;
    this.lodash = lodash;
    this.cheWorkspace = cheWorkspace;
  }

  redirectToWorkspaceDetails() {
    this.$location.path('/workspace/' + this.workspace.namespace +'/' + this.workspace.config.name);
  }

  getDefaultEnvironment(workspace) {
    let environments = workspace.config.environments;
    let envName = workspace.config.defaultEnv;
    let defaultEnvironment = environments[envName];
    return defaultEnvironment;
  }

  getMemoryLimit(workspace) {
    if (workspace.runtime && workspace.runtime.machines && workspace.runtime.machines.length > 0) {
      let limits = this.lodash.pluck(workspace.runtime.machines, 'config.limits.ram');
      let total = 0;
      limits.forEach((limit) => {
        total += limit;
      });
      return Math.round(total) + ' MB';
    }

    let environment = this.getDefaultEnvironment(workspace);
    if (environment) {
      let limits = this.lodash.pluck(environment.machines, 'attributes.memoryLimitBytes');
      let total = 0;
      limits.forEach((limit) => {
        if (limit) {
          total += limit / (1024*1024);
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
  getWorkspaceStatus() {
    let workspace = this.cheWorkspace.getWorkspaceById(this.workspace.id);
    return workspace ? workspace.status : 'unknown';
  }
}
