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

/**
 * @ngdoc directive
 * @name dashboard.directive:DashboardLastWorkspaces
 * @description This class is handling the directive of the listing last opened workspaces in the dashboard
 * @author Oleksii Orel
 */
export class DashboardLastWorkspaces {


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/dashboard/last-workspaces/last-workspaces.html';

    this.controller = 'DashboardLastWorkspacesController';
    this.controllerAs = 'dashboardLastWorkspacesController';
    this.bindToController = true;
  }

}
