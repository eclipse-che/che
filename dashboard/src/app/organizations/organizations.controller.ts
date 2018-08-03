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

const MAX_ITEMS = 12;

/**
 * @ngdoc controller
 * @name organizations.controller:OrganizationsController
 * @description This class is handling the controller for organizations
 * @author Oleksii Orel
 */
export class OrganizationsController {

  static $inject = ['cheOrganization', 'cheNotification', 'cheTeamEventsManager', '$scope', '$q', 'chePermissions', '$rootScope'];

  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Organization API interaction.
   */
  private cheOrganization: che.api.ICheOrganization;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Loading state of the page.
   */
  private isInfoLoading: boolean;
  /**
   * List of organizations.
   */
  private organizations: Array<any> = [];
  /**
   * Page info object.
   */
  private pageInfo: che.IPageInfo;
  /**
   * Has admin user service.
   */
  private hasAdminUserService: boolean;

  /**
   * Default constructor that is using resource
   */
  constructor(cheOrganization: che.api.ICheOrganization, cheNotification: any,
              cheTeamEventsManager: che.api.ICheTeamEventsManager, $scope: ng.IScope,
              $q: ng.IQService, chePermissions: che.api.IChePermissions, $rootScope: che.IRootScopeService) {
    this.cheOrganization = cheOrganization;
    this.cheNotification = cheNotification;
    this.$q = $q;

    (<any>$rootScope).showIDE = false;

    this.hasAdminUserService = chePermissions.getUserServices().hasAdminUserService;
    let refreshHandler = () => {
      this.fetchOrganizations();
    };
    cheTeamEventsManager.addDeleteHandler(refreshHandler);
    cheTeamEventsManager.addRenameHandler(refreshHandler);

    $scope.$on('$destroy', () => {
      cheTeamEventsManager.removeRenameHandler(refreshHandler);
      cheTeamEventsManager.removeDeleteHandler(refreshHandler);
    });
    this.fetchOrganizations();
  }

  /**
   * Fetches the list of root organizations.
   * @param pageKey {string}
   */
  fetchOrganizations(pageKey?: string): void {
    this.isInfoLoading = true;
    let promise: ng.IPromise<Array<che.IOrganization>>;
    if (angular.isDefined(pageKey)) {
      promise = this.cheOrganization.fetchOrganizationPageObjects(pageKey);
    } else {
      // todo remove admin's condition after adding query search to server side
      promise = this.cheOrganization.fetchOrganizations(!this.hasAdminUserService ? MAX_ITEMS : 30);
    }

    promise.then((userOrganizations: Array<che.IOrganization>) => {
      this.pageInfo = angular.copy(this.cheOrganization.getPageInfo());
      this._updateOrganizationList(userOrganizations);
    }, (error: any) => {
      let message = error.data && error.data.message ? error.data.message : 'Failed to retrieve organizations.';
      this.cheNotification.showError(message);
    }).finally(() => {
      this.isInfoLoading = false;
    });
  }

  _updateOrganizationList(organizations: Array<che.IOrganization>): void {
    // todo remove this admin's condition after adding query search to server side
    if (this.hasAdminUserService) {
      this.organizations = organizations.filter((organization: che.IOrganization) => {
        return !organization.parent;
      });
      return;
    }
    this.organizations = organizations;
  }

  /**
   * Gets the list of organizations.
   *
   * @returns {Array<che.IOrganization>}
   */
  getOrganizations(): Array<che.IOrganization> {
    return this.organizations;
  }
}
