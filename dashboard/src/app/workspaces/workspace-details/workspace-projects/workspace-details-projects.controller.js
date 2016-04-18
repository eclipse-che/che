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
    this.workspaceId = $route.current.params.workspaceId;

    let profilePreferences = cheAPI.getProfile().getPreferences();

    this.profileCreationDate = profilePreferences['che:created'];

    if (!this.cheWorkspace.getWorkspacesById().get(this.workspaceId)) {
      let promise = this.cheWorkspace.fetchWorkspaceDetails(this.workspaceId);
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
    this.workspace = this.cheWorkspace.getWorkspacesById().get(this.workspaceId);
    this.projects = this.workspace.config.projects;
  }

  widthGtSm() {
    return this.$mdMedia('gt-sm');
  }
}
