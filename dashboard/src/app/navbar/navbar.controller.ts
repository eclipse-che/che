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
import {CheAPI} from '../../components/api/che-api.factory';
import {CheKeycloak} from '../../components/api/che-keycloak.factory';
import {CheService} from '../../components/api/che-service.factory';

export class CheNavBarController {

  static $inject = ['$mdSidenav', '$scope', '$location', '$route', 'cheAPI', '$window', 'chePermissions', 'cheKeycloak', 'cheService'];

  menuItemUrl = {
    dashboard: '#/',
    workspaces: '#/workspaces',
    administration: '#/administration',
    // subsections
    plugins: '#/admin/plugins',
    factories: '#/factories',
    account: '#/account',
    stacks: '#/stacks',
    organizations: '#/organizations',
    usermanagement: '#/admin/usermanagement'
  };

  accountItems = [
    {
      name: 'Account',
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
  private organizations: Array<che.IOrganization>;
  private cheKeycloak: CheKeycloak;
  private cheService: CheService;
  private isPermissionServiceAvailable: boolean;
  private isKeycloackPresent: boolean;

  /**
   * Default constructor
   */
  constructor($mdSidenav: ng.material.ISidenavService,
              $scope: ng.IScope,
              $location: ng.ILocationService,
              $route: ng.route.IRouteService,
              cheAPI: CheAPI,
              $window: ng.IWindowService,
              chePermissions: che.api.IChePermissions,
              cheKeycloak: CheKeycloak,
              cheService: CheService) {
    this.$mdSidenav = $mdSidenav;
    this.$scope = $scope;
    this.$location = $location;
    this.$route = $route;
    this.cheAPI = cheAPI;
    this.$window = $window;
    this.chePermissions = chePermissions;
    this.cheKeycloak = cheKeycloak;
    this.cheService = cheService;

    this.isKeycloackPresent = this.cheKeycloak.isPresent();

    this.profile = cheAPI.getProfile().getProfile();

    this.userServices = this.chePermissions.getUserServices();

    // highlight navbar menu item
    $scope.$on('$locationChangeStart', () => {
      let path = '#' + $location.path();
      $scope.$broadcast('navbar-selected:set', path);
    });

    cheAPI.getWorkspace().fetchWorkspaces();
    cheAPI.getFactory().fetchFactories();

    this.isPermissionServiceAvailable = false;
    this.resolvePermissionServiceAvailability().then((isAvailable: boolean) => {
      this.isPermissionServiceAvailable = isAvailable;

      if (isAvailable) {
        if (this.chePermissions.getSystemPermissions()) {
          this.updateData();
        } else {
          this.chePermissions.fetchSystemPermissions().finally(() => {
            this.updateData();
          });
        }
      }
    });
  }

  /**
   * Resolves promise with <code>true</code> if Permissions service is available.
   *
   * @returns {ng.IPromise<boolean>}
   */
  resolvePermissionServiceAvailability(): ng.IPromise<boolean> {
    return this.cheService.fetchServices().then(() => {
      return this.cheService.isServiceAvailable(this.chePermissions.getPermissionsServicePath());
    });
  }

  /**
   * Update data.
   */
  updateData(): void {
    const organization = this.cheAPI.getOrganization();
    organization.fetchOrganizations().then(() => {
      this.organizations = organization.getOrganizations();
      const user = this.cheAPI.getUser().getUser();
      organization.fetchOrganizationByName(user.name).finally(() => {
        this.hasPersonalAccount = angular.isDefined(organization.getOrganizationByName(user.name));
      });
    });
  }

  reload(): void {
    this.$route.reload();
  }

  /**
   * Returns user nickname.
   * @return {string}
   */
  getUserName(): string {
    const {attributes, email} = this.profile;
    const fullName = this.cheAPI.getProfile().getFullName(attributes).trim();

    return fullName ? fullName : email;
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

  /**
   * Returns number of all organizations.
   *
   * @return {number}
   */
  getOrganizationsNumber(): number {
    if (!this.organizations) {
      return 0;
    }

    return this.organizations.length;
  }

  /**
   * Returns number of root organizations.
   *
   * @return {number}
   */
  getRootOrganizationsNumber(): number {
    if (!this.organizations) {
      return 0;
    }
    let rootOrganizations = this.organizations.filter((organization: any) => {
      return !organization.parent;
    });

    return rootOrganizations.length;
  }

  openLinkInNewTab(url: string): void {
    this.$window.open(url, '_blank');
  }

  /**
   * Opens user profile in new browser page.
   */
  gotoProfile(): void {
    this.$location.path('/account');
  }

  /**
   * Logout.
   */
  logout(): void {
    this.cheKeycloak.logout();
  }

}
