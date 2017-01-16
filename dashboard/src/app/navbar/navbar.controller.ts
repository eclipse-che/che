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
import {CheAPI} from '../../components/api/che-api.factory';

export class CheNavBarController {
  links = [{
    href: '#/create-workspace',
    name: 'New Workspace'
  }];
  menuItemUrl = {
    dashboard: '#/',
    workspaces: '#/workspaces',
    administration: '#/administration',
    // subsections
    plugins: '#/admin/plugins',
    account: '#/account',
    stacks: '#/stacks'
  };

  private $mdSidenav: ng.material.ISidenavService;
  private $scope: ng.IScope;
  private $window: ng.IWindowService;
  private $location: ng.ILocationService;
  private $route: ng.route.IRouteService;
  private cheAPI: CheAPI;
  private profile: che.IProfile;
  private email: string;

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($mdSidenav: ng.material.ISidenavService, $scope: ng.IScope, $location: ng.ILocationService, $route: ng.route.IRouteService, cheAPI: CheAPI, $window: ng.IWindowService) {
    this.$mdSidenav = $mdSidenav;
    this.$scope = $scope;
    this.$location = $location;
    this.$route = $route;
    this.cheAPI = cheAPI;
    this.$window = $window;

    this.profile = cheAPI.getProfile().getProfile();
    if (this.profile.email) {
      this.email = this.profile.email;
    } else {
      this.profile.$promise.then(() => {
        this.email = this.profile.email ? this.profile.email : 'N/A ';
      }, () => {
        this.email = 'N/A ';
      });
    }

    // highlight navbar menu item
    $scope.$on('$locationChangeStart', () => {
      let path = '#' + $location.path();
      $scope.$broadcast('navbar-selected:set', path);
    });

    cheAPI.getWorkspace().fetchWorkspaces();
  }

  reload(): void {
    this.$route.reload();
  }

  /**
   * Toggle the left menu
   */
  toggleLeftMenu(): void {
    this.$mdSidenav('left').toggle();
  }

  getWorkspacesNumber(): number {
    return this.cheAPI.getWorkspace().getWorkspaces().length;
  }

  openLinkInNewTab(url: string): void {
    this.$window.open(url, '_blank');
  }
}
