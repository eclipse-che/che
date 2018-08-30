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
import {AdminsUserManagementCtrl} from '../user-management.controller';

/**
 * This class is handling the controller for the add user
 * @author Oleksii Orel
 */
export class AdminsAddUserController {

  static $inject = ['$mdDialog', 'cheUser', 'cheNotification', 'lodash', 'cheOrganization', 'chePermissions', 'resourcesService'];

  private $mdDialog: ng.material.IDialogService;
  private lodash: any;
  private cheNotification: any;
  private cheUser: any;
  private callbackController: AdminsUserManagementCtrl;
  private newUserName: string;
  private newUserEmail: string;
  private newUserPassword: string;
  private organizations: Array<string>;
  private organization: string;
  private cheOrganization: che.api.ICheOrganization;
  private chePermissions: che.api.IChePermissions;
  private organizationRoles: che.resource.ICheOrganizationRoles;

  /**
   * Default constructor.
   */
  constructor($mdDialog: ng.material.IDialogService,
              cheUser: any,
              cheNotification: any,
              lodash: any,
              cheOrganization: che.api.ICheOrganization,
              chePermissions: che.api.IChePermissions,
              resourcesService: che.service.IResourcesService) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;
    this.cheUser = cheUser;
    this.cheNotification = cheNotification;
    this.cheOrganization = cheOrganization;
    this.chePermissions = chePermissions;
    this.organizationRoles = resourcesService.getOrganizationRoles();

    this.organizations = [];

    this.cheOrganization.fetchOrganizations().then(() => {
      let organizations = this.cheOrganization.getOrganizations();
      let rootOrganizations = organizations.filter((organization: any) => {
        return !organization.parent;
      });
      this.organizations = lodash.pluck(rootOrganizations, 'name');
      if (this.organizations.length > 0) {
        this.organization = this.organizations[0];
      }
    });
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  abort(): void {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the add button of the dialog(create new user).
   */
  createUser(): void {
    let promise = this.cheUser.createUser(this.newUserName, this.newUserEmail, this.newUserPassword);

    promise.then((data: any) => {
      if (this.organization) {
        this.addUserToOrganization(data.id);
      } else {
        this.finish();
      }
    }, (error: any) => {
      this.cheNotification.showError(error.data.message ? error.data.message : 'Failed to create user.');
    });
  }

  /**
   * Finish user creation.
   */
  private finish(): void {
    this.$mdDialog.hide();
    this.callbackController.updateUsers();
    this.cheNotification.showInfo('User successfully created.');
  }

  /**
   * Adds user to chosen organization.
   *
   * @param userId
   */
  private addUserToOrganization(userId: string): void {
    let organizations = this.cheOrganization.getOrganizations();
    let organization = this.lodash.find(organizations, (organization: any) => {
      return organization.name === this.organization;
    });

    let actions = this.organizationRoles.MEMBER.actions;
    let permissions = {
      instanceId: organization.id,
      userId: userId,
      domainId: 'organization',
      actions: actions
    };
    this.chePermissions.storePermissions(permissions).then(() => {
      this.finish();
    }, (error: any) => {
      this.cheNotification.showError(error.data.message ? error.data.message : 'Failed to add user to organization' + this.organization + '.');
    });
  }
}
