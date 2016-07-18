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
 * @name workspace.details.controller:WorkspaceDetailsProjectsCtrl
 * @description This class is handling the controller for details of workspace : section projects
 * @author Ann Shumilova
 */
export class WorkspaceDetailsProjectsCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($route, cheAPI, $mdMedia) {
    this.cheWorkspace = cheAPI.getWorkspace();
    this.$mdMedia = $mdMedia;
    this.namespace = $route.current.params.namespace;
    this.workspaceName = $route.current.params.workspaceName;
    this.workspaceKey = this.namespace + ":" + this.workspaceName;

    let preferences = cheAPI.getPreferences().getPreferences();

    this.profileCreationDate = preferences['che:created'];

    if (!this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName)) {
      let promise = this.cheWorkspace.fetchWorkspaceDetails(this.workspaceKey);
      promise.then(() => {
        this.updateProjectsData();
    }, (error) => {
      if (error.status === 304) {
        this.updateProjectsData();
      }
    });
    } else {
      this.updateProjectsData();
    }
  }

  updateProjectsData() {
    this.workspace = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
    this.projects = this.workspace.config.projects;
  }

  widthGtSm() {
    return this.$mdMedia('gt-sm');
  }
}
