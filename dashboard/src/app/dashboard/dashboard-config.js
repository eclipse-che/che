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

import {DashboardController} from './dashboard.controller';
import {DashboardLastWorkspacesController} from './last-workspaces/last-workspaces.controller';
import {DashboardLastWorkspaces} from './last-workspaces/last-workspaces.directive';
import {DashboardLastProjectsController} from './last-projects/last-projects.controller';
import {DashboardLastProjects} from './last-projects/last-projects.directive';

export class DashboardConfig {

  constructor(register) {

    // last workspaces
    register.controller('DashboardLastWorkspacesController', DashboardLastWorkspacesController);
    register.directive('dashboardLastWorkspaces', DashboardLastWorkspaces);

    // last projects
    register.controller('DashboardLastProjectsController', DashboardLastProjectsController);
    register.directive('dashboardLastProjects', DashboardLastProjects);

    // controller
    register.controller('DashboardController', DashboardController);

    // config routes
    register.app.config(($routeProvider) => {
      $routeProvider.accessWhen('/', {
        templateUrl: 'app/dashboard/dashboard.html',
        controller: 'DashboardController',
        controllerAs: 'dashboardController',
        resolve: {
          check: ['$q', 'cheService', 'cheAdminService', ($q, cheService, cheAdminService) => {
            var defer = $q.defer();
            cheService.fetchServices().then(() => {
              cheAdminService.fetchServices().then(() => {
                defer.resolve();
              }, () => {
                defer.resolve();
              });
            });
            return defer.promise;
          }]
        }
      });
    })
    ;
  }
}


