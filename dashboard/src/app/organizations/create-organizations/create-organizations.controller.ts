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
 * @ngdoc controller
 * @name organizations.create.controller:CreateOrganizationController
 * @description This class is handling the controller for the new organization creation.
 * @author Oleksii Orel
 */
export class CreateOrganizationController {

  static $inject = ['cheOrganization', 'chePermissions', 'cheUser', 'cheNotification', '$location', '$q', '$log', '$rootScope', 'initData'];

  /**
   * Organization API interaction.
   */
  private cheOrganization: che.api.ICheOrganization;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Permissions API interaction.
   */
  private chePermissions: che.api.IChePermissions;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Log service.
   */
  private $log: ng.ILogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Current organization's name.
   */
  private organizationName: string;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * The list of users to invite.
   */
  private members: Array<che.IMember>;
  /**
   * Parent organization name.
   */
  private parentQualifiedName: string;
  /**
   * Parent organization id.
   */
  private parentOrganizationId: string;
  /**
   * List of members of parent organization.
   */
  private parentOrganizationMembers: Array<che.IUser>;

  /**
   * Default constructor
   */
  constructor(cheOrganization: che.api.ICheOrganization, chePermissions: che.api.IChePermissions, cheUser: any, cheNotification: any,
              $location: ng.ILocationService, $q: ng.IQService, $log: ng.ILogService, $rootScope: che.IRootScopeService,
              initData: any) {
    this.cheOrganization = cheOrganization;
    this.chePermissions = chePermissions;
    this.cheUser = cheUser;
    this.cheNotification = cheNotification;
    this.$location = $location;
    this.$q = $q;
    this.$log = $log;
    $rootScope.showIDE = false;

    this.organizationName = '';
    this.isLoading = false;
    this.members = [];

    // injected by route provider
    this.parentQualifiedName = initData.parentQualifiedName;
    this.parentOrganizationId = initData.parentOrganizationId;
    this.parentOrganizationMembers = initData.parentOrganizationMembers;
  }

  /**
   * Check if the name is unique.
   * @param name
   * @returns {boolean}
   */
  isUniqueName(name: string): boolean {
    let organizations = this.cheOrganization.getOrganizations();
    let account = this.parentQualifiedName ? this.parentQualifiedName + '/' : '';
    if (!organizations.length) {
      return true;
    } else {
      for (let i = 0; i < organizations.length; i++) {
        if (organizations[i].qualifiedName === account + name) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Performs new organization creation.
   */
  createOrganization(): void {
    this.isLoading = true;
    this.cheOrganization.createOrganization(this.organizationName, this.parentOrganizationId).then((organization: che.IOrganization) => {
      this.addPermissions(organization, this.members);
      this.cheOrganization.fetchOrganizations();
    }, (error: any) => {
      this.isLoading = false;
      let message = error.data && error.data.message ? error.data.message : 'Failed to create organization ' + this.organizationName + '.';
      this.cheNotification.showError(message);
    });
  }

  /**
   * Add permissions for members in pointed organization.
   *
   * @param organization {che.IOrganization} organization
   * @param members members to be added to organization
   */
  addPermissions(organization: che.IOrganization, members: Array<any>) {
    let promises = [];
    members.forEach((member: che.IMember) => {
      if (member.id && member.id !== this.cheUser.getUser().id) {
        let actions = this.cheOrganization.getActionsFromRoles(member.roles);
        let permissions = {
          instanceId: organization.id,
          userId: member.id,
          domainId: 'organization',
          actions: actions
        };

        let promise = this.chePermissions.storePermissions(permissions);
        promises.push(promise);
      }
    });

    this.$q.all(promises).then(() => {
      this.isLoading = false;
      this.$location.path('/organization/' + organization.qualifiedName);
    }, (error: any) => {
      this.isLoading = false;
      let message = error.data && error.data.message ? error.data.message : 'Failed to create organization ' + this.organizationName + '.';
      this.cheNotification.showError(message);
    });
  }
}
