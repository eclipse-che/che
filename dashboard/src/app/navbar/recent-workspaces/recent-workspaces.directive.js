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
 * @ngdoc directive
 * @name navbar.directive:NavbarRecentWorkspaces
 * @description This class is handling the directive of the listing recent opened workspaces in the navbar
 * @author Oleksii Orel
 */
export class NavbarRecentWorkspaces {


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/navbar/recent-workspaces/recent-workspaces.html';

    this.controller = 'NavbarRecentWorkspacesCtrl';
    this.controllerAs = 'navbarRecentWorkspacesCtrl';
    this.bindToController = true;
  }

}
