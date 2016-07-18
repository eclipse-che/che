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
 * @name dashboard.controller:DashboardLastProjectsController
 * @description This class is handling the controller of the last projects to display in the dashboard
 * @author Florent Benoit
 */
export class DashboardLastProjectsController {


  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor(cheWorkspace, lodash) {
    this.cheWorkspace = cheWorkspace;
    this.lodash = lodash;
    this.workspacesById = cheWorkspace.getWorkspacesById();

    this.projects = [];
    this.state = 'loading';

    // fetch workspaces when initializing
    let promise = cheWorkspace.fetchWorkspaces();

    promise.then(() => {
        this.buildProjectsList();
        this.state = 'OK';
      },
      (error) => {
        if (error.status === 304) {
          // ok
          this.buildProjectsList();
          this.state = 'OK';
          return;
        }
        this.state = 'error';
      });
  }

  buildProjectsList() {
    let workspaceProjects = this.cheWorkspace.getWorkspaceProjects();
    this.projects = this.lodash(workspaceProjects).chain().values().flatten().value();
  }

  getProjects() {
    return this.projects;
  }

  /**
   * Gets the workspace based on its ID
   * @param workspaceId
   * @returns {workspace|*}
   */
  getWorkspace(workspaceId) {
    return this.workspacesById.get(workspaceId);
  }
}
