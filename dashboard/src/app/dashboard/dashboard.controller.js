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
 * @name dashboard.controller:DashboardController
 * @description This class is handling the controller of the dashboard
 * @author Florent Benoit
 */
export class DashboardController {


  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($rootScope, cheWorkspace, $location) {
    'ngInject';
    $rootScope.showIDE = false;
    this.cheWorkspace = cheWorkspace;
    this.$location = $location;

    cheWorkspace.fetchWorkspaces().then(() => {
        this.checkWorkspaces();
      },
      (error) => {
        if (error.status === 304) {
          this.checkWorkspaces();
          return;
        }
      });
  }

  checkWorkspaces() {
    //If there are any workspaces - redirect to create workspace with project page:
    if (this.cheWorkspace.getWorkspaces() && this.cheWorkspace.getWorkspaces().length === 0) {
      this.$location.path('/create-project');
    }
  }
}
