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
import { CheDashboardConfigurationService } from '../../components/branding/che-dashboard-configuration.service';

type ConfigurableMenu = { [key in che.ConfigurableMenuItem ]: string };

const CONFIGURABLE_MENU: ConfigurableMenu = {
  administration: '#/administration',
  factories: '#/factories',
  getstarted: '#/getstarted',
  organizations: '#/organizations',
  stacks: '#/stacks',
};

export const MENU_ITEM = angular.extend({
  account: '#/account',
  dashboard: '#/',
  usermanagement: '#/admin/usermanagement',
  workspaces: '#/workspaces',
}, CONFIGURABLE_MENU);

export class CheNavBarController {

  static $inject = [
    '$location',
    '$scope',
    'cheAPI',
    'cheDashboardConfigurationService',
    'cheKeycloak',
    'chePermissions',
    'cheService',
  ];

  menuItemUrl = MENU_ITEM;

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

  private $location: ng.ILocationService;
  private $scope: ng.IScope;
  private cheAPI: CheAPI;
  private cheDashboardConfigurationService: CheDashboardConfigurationService;
  private cheKeycloak: CheKeycloak;
  private chePermissions: che.api.IChePermissions;
  private cheService: CheService;

  private profile: che.IProfile;
  private userServices: che.IUserServices;
  private hasPersonalAccount: boolean;
  private organizations: Array<che.IOrganization>;
  private isPermissionServiceAvailable: boolean;
  private isKeycloackPresent: boolean;

  private workspacesNumber: number;
  private pageFactories: Array<che.IFactory> = [];

  /**
   * Default constructor
   */
  constructor(
    $location: ng.ILocationService,
    $scope: ng.IScope,
    cheAPI: CheAPI,
    cheDashboardConfigurationService: CheDashboardConfigurationService,
    cheKeycloak: CheKeycloak,
    chePermissions: che.api.IChePermissions,
    cheService: CheService,
  ) {
    this.$location = $location;
    this.$scope = $scope;
    this.cheAPI = cheAPI;
    this.cheDashboardConfigurationService = cheDashboardConfigurationService;
    this.cheKeycloak = cheKeycloak;
    this.chePermissions = chePermissions;
    this.cheService = cheService;

    const handler = (workspaces: Array<che.IWorkspace>) => {
      this.workspacesNumber = workspaces.length;
    };
    this.cheAPI.getWorkspace().addListener('onChangeWorkspaces', handler);

    $scope.$on('$destroy', () => {
      this.cheAPI.getWorkspace().removeListener('onChangeWorkspaces', handler);
    });
  }

  $onInit(): void {
    this.isKeycloackPresent = this.cheKeycloak.isPresent();
    this.profile = this.cheAPI.getProfile().getProfile();
    this.userServices = this.chePermissions.getUserServices();

    // highlight navbar menu item
    this.$scope.$on('$locationChangeStart', () => {
      let path = '#' + this.$location.path();
      this.$scope.$broadcast('navbar-selected:set', path);
    });

    this.cheAPI.getWorkspace().fetchWorkspaces().then((workspaces: Array<che.IWorkspace>) => {
      this.workspacesNumber = workspaces.length;
    });

    this.cheAPI.getFactory().fetchFactories().then(() => {
      this.pageFactories = this.cheAPI.getFactory().getPageFactories();
    });

    this.isPermissionServiceAvailable = false;
    this.resolvePermissionServiceAvailability().then((isAvailable: boolean) => {
      this.isPermissionServiceAvailable = isAvailable;
      if (isAvailable) {
        if (this.chePermissions.getSystemPermissions()) {
          this.updateData();
        } else {
          this.chePermissions.fetchSystemPermissions()
            .catch(() => {
              // fetch unhandled rejection
            })
            .finally(() => {
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
      organization.fetchOrganizationByName(user.name)
        .catch(() => {
          // fetch unhandled rejection
        })
        .finally(() => {
          this.hasPersonalAccount = angular.isDefined(organization.getOrganizationByName(user.name));
        });
    });
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
   * Returns number of factories.
   * @return {number}
   */
  getFactoriesNumber(): number {
    return this.pageFactories.length;
  }

  /**
   * Returns number of all organizations.
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

  showMenuItem(menuItem: che.ConfigurableMenuItem | string): boolean {
    return this.cheDashboardConfigurationService.allowedMenuItem(menuItem);
  }

  /**
   * Opens user profile in new browser page.
   */
  private gotoProfile(): void {
    this.$location.path('/account');
  }

  /**
   * Logout.
   */
  private logout(): void {
    this.cheKeycloak.logout();
  }

}
