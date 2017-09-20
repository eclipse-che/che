/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheAPI} from '../../components/api/che-api.factory';
import {CheKeycloak, keycloakUserInfo} from '../../components/api/che-keycloak.factory';

export class CheNavBarController {
  private menuItemUrl = {
    dashboard: '#/',
    workspaces: '#/workspaces',
    administration: '#/administration',
    // subsections
    plugins: '#/admin/plugins',
    factories: '#/factories',
    account: '#/account',
    stacks: '#/stacks',
    usermanagement: '#/admin/usermanagement'
  };

  accountItems = [
    {
      name: 'Go to Profile',
      onclick: () => {
        this.gotoProfile();
      }
    },
    {
      name: 'Logout',
      onclick: () => {
        this.logout();
      }
    }
  ];

  private $mdSidenav: ng.material.ISidenavService;
  private $scope: ng.IScope;
  private $window: ng.IWindowService;
  private $location: ng.ILocationService;
  private $route: ng.route.IRouteService;
  private cheAPI: CheAPI;
  private profile: che.IProfile;
  private chePermissions: che.api.IChePermissions;
  private userServices: che.IUserServices;
  private hasPersonalAccount: boolean;
  private cheKeycloak: CheKeycloak;
  private userInfo: keycloakUserInfo;

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($mdSidenav: ng.material.ISidenavService,
              $scope: ng.IScope,
              $location: ng.ILocationService,
              $route: ng.route.IRouteService,
              cheAPI: CheAPI,
              $window: ng.IWindowService,
              chePermissions: che.api.IChePermissions,
              cheKeycloak: CheKeycloak) {
    this.$mdSidenav = $mdSidenav;
    this.$scope = $scope;
    this.$location = $location;
    this.$route = $route;
    this.cheAPI = cheAPI;
    this.$window = $window;
    this.chePermissions = chePermissions;
    this.cheKeycloak = cheKeycloak;
    this.userInfo = null;

    this.profile = cheAPI.getProfile().getProfile();

    this.userServices = this.chePermissions.getUserServices();

    // highlight navbar menu item
    $scope.$on('$locationChangeStart', () => {
      let path = '#' + $location.path();
      $scope.$broadcast('navbar-selected:set', path);
    });

    cheAPI.getWorkspace().fetchWorkspaces();
    cheAPI.getFactory().fetchFactories();

    if (this.cheKeycloak.isPresent()) {
      this.cheKeycloak.fetchUserInfo().then((userInfo: keycloakUserInfo) => {
        this.userInfo = userInfo;
      });
    }
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

  /**
   * Returns number of workspaces.
   *
   * @return {number}
   */
  getWorkspacesNumber(): number {
    return this.cheAPI.getWorkspace().getWorkspaces().length;
  }

  /**
   * Returns number of factories.
   *
   * @return {number}
   */
  getFactoriesNumber(): number {
    return this.cheAPI.getFactory().getPageFactories().length;
  }

  openLinkInNewTab(url: string): void {
    this.$window.open(url, '_blank');
  }

  /**
   * Returns <code>true</code> if Keycloak is present.
   *
   * @returns {boolean}
   */
  isKeycloakPresent(): boolean {
    return this.cheKeycloak.isPresent();
  }

  /**
   * Opens user profile in new browser page.
   */
  gotoProfile(): void {
    const url = this.cheKeycloak.getProfileUrl();
    this.$window.open(url);
  }

  /**
   * Logout.
   */
  logout(): void {
    this.cheKeycloak.logout();
  }

}
