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
 * @name navbar.directive:NavbarRecentWorkspaces
 * @description This class is handling the directive of the listing recent opened workspaces in the navbar
 * @author Oleksii Kurinnyi
 */
export class NavbarRecentWorkspaces implements ng.IDirective {
  restrict: string;
  templateUrl: string;
  controller: string;
  controllerAs: string;
  bindToController: boolean;

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/navbar/recent-workspaces/recent-workspaces.html';

    this.controller = 'NavbarRecentWorkspacesController';
    this.controllerAs = 'navbarRecentWorkspacesController';
    this.bindToController = true;
  }

}
