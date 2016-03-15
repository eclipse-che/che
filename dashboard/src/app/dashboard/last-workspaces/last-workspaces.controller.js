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
 * @name dashboard.controller:DashboardLastWorkspacesController
 * @description This class is handling the controller of the last workspaces to display in the dashboard
 * @author Oleksii Orel
 */
export class DashboardLastWorkspacesController {


  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor(cheWorkspace) {
    this.cheWorkspace = cheWorkspace;

    this.state = 'loading';
    this.workspaces = cheWorkspace.getWorkspaces();

    // fetch workspaces when initializing
    let promise = cheWorkspace.fetchWorkspaces();

    promise.then(() => {
        this.state = 'OK';
      },
      (error) => {
        if (error.status === 304) {
          // ok
          this.state = 'OK';
          return;
        }
        this.state = 'error';
      });

  }

  getWorkspaces() {
    return this.workspaces;
  }


}
