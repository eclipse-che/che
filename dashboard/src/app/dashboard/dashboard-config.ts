/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {DashboardLastWorkspacesController} from './last-workspaces/last-workspaces.controller';
import {DashboardLastWorkspaces} from './last-workspaces/last-workspaces.directive';
import {DashboardPanel} from './dashboard-panel/dashboard-panel.directive';
import {CheWorkspace} from '../../components/api/workspace/che-workspace.factory';

export class DashboardConfig {

  constructor(register: che.IRegisterService) {

    // last workspaces
    register.controller('DashboardLastWorkspacesController', DashboardLastWorkspacesController);
    register.directive('dashboardLastWorkspaces', DashboardLastWorkspaces);

    // panel of last used entries
    register.directive('dashboardPanel', DashboardPanel);

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/', {
        title: 'Dashboard',
        templateUrl: 'app/dashboard/dashboard.html',
        resolve: {
          check: ['$q', '$location', 'cheWorkspace', ($q: ng.IQService, $location: ng.ILocationService, cheWorkspace: CheWorkspace) => {
            cheWorkspace.fetchWorkspaces().then(() => {
              if (cheWorkspace.getWorkspaces().length === 0) {
                $location.path('/create-workspace');
              }
            }, (error: any) => {
              if (error.status === 304 && cheWorkspace.getWorkspaces().length === 0) {
                $location.path('/create-workspace');
              }
            });
          }]
        }
      });
    }]);
  }
}


