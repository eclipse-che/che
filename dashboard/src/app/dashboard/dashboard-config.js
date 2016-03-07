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

import {DashboardCtrl} from './dashboard.controller';
import {DashboardLastProjectsCtrl} from './last-projects/last-projects.controller';
import {DashboardLastProjects} from './last-projects/last-projects.directive';

export class DashboardConfig {

  constructor(register) {

    // last projects
    register.controller('DashboardLastProjectsCtrl', DashboardLastProjectsCtrl);
    register.directive('dashboardLastProjects', DashboardLastProjects);

    // controller
    register.controller('DashboardCtrl', DashboardCtrl);

    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/', {
        templateUrl: 'app/dashboard/dashboard.html',
        controller: 'DashboardCtrl',
        controllerAs: 'dashboardCtrl',
        resolve: {
          check: ['$q', 'cheService', 'cheAdminService', function ($q, cheService, cheAdminService) {
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


