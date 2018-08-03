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

/**
 * @ngdoc directive
 * @name dashboard.directive:DashboardLastWorkspaces
 * @description This class is handling the directive of the listing last opened workspaces in the dashboard
 * @author Oleksii Orel
 */
export class DashboardLastWorkspaces implements ng.IDirective {

  restrict = 'E';
  templateUrl = 'app/dashboard/last-workspaces/last-workspaces.html';

  controller = 'DashboardLastWorkspacesController';
  controllerAs = 'dashboardLastWorkspacesController';
  bindToController = true;

}
