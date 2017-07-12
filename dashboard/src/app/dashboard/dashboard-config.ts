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

import {DashboardLastWorkspacesController} from './last-workspaces/last-workspaces.controller';
import {DashboardLastWorkspaces} from './last-workspaces/last-workspaces.directive';
import {DashboardPanel} from './dashboard-panel/dashboard-panel.directive';
import {CheService} from '../../components/api/che-service.factory';
import {CheWorkspace} from '../../components/api/che-workspace.factory';

export class DashboardConfig {

  constructor(register: che.IRegisterService) {

    // last workspaces
    register.controller('DashboardLastWorkspacesController', DashboardLastWorkspacesController);
    register.directive('dashboardLastWorkspaces', DashboardLastWorkspaces);

    // panel of last used entries
    register.directive('dashboardPanel', DashboardPanel);

    // config routes
    register.app.config(($routeProvider: ng.route.IRouteProvider) => {
      $routeProvider.accessWhen('/', {
        title: 'Dashboard',
        templateUrl: 'app/dashboard/dashboard.html',
        resolve: {
          check: ['$q', '$location', 'cheWorkspace', 'cheService', ($q: ng.IQService, $location: ng.ILocationService, cheWorkspace: CheWorkspace, cheService: CheService) => {
            cheWorkspace.fetchWorkspaces().then(() => {
              if (cheWorkspace.getWorkspaces().length === 0) {
                $location.path('/create-workspace');
              }
            }, (error: any) => {
              if (error.status === 304 && cheWorkspace.getWorkspaces().length === 0) {
                $location.path('/create-workspace');
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


