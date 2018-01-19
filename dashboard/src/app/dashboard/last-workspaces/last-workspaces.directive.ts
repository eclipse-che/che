/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
