/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

import {DashboardController} from './dashboard.controller';
import {DashboardLastWorkspacesController} from './last-workspaces/last-workspaces.controller';
import {DashboardLastWorkspaces} from './last-workspaces/last-workspaces.directive';
import {DashboardPanel} from './dashboard-panel/dashboard-panel.directive';

export class DashboardConfig {

  constructor(register) {

    // last workspaces
    register.controller('DashboardLastWorkspacesController', DashboardLastWorkspacesController);
    register.directive('dashboardLastWorkspaces', DashboardLastWorkspaces);

    // controller
    register.controller('DashboardController', DashboardController);

    // panel of last used entries
    register.directive('dashboardPanel', DashboardPanel);

    // config routes
    register.app.config(($routeProvider) => {
      $routeProvider.accessWhen('/', {
        title: 'Dashboard',
        templateUrl: 'app/dashboard/dashboard.html',
        controller: 'DashboardController',
        controllerAs: 'dashboardController',
        resolve: {
          check: ['$q', '$location', 'cheWorkspace', 'cheService', ($q, $location, cheWorkspace, cheService) => {
            cheWorkspace.fetchWorkspaces().then(() => {
              if (cheWorkspace.getWorkspaces().length === 0) {
                $location.path('/create-project');
              }
            }, (error: any) => {
              if (error.status === 304 && cheWorkspace.getWorkspaces().length === 0) {
                $location.path('/create-project');
              }
            });

            return cheService.fetchServices();
          }]
        }
      });
    })
    ;
  }
}


