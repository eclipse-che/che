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
import {OrganizationsPermissionService} from '../../organizations-permission.service';

/**
 * @ngdoc controller
 * @name organizations.list.Item.controller:OrganizationsItemController
 * @description This class is handling the controller for item of organizations list
 * @author Oleksii Orel
 */
export class OrganizationsItemController {

  static $inject = ['$location', 'cheOrganization', 'confirmDialogService', 'cheNotification', 'organizationsPermissionService', 'chePermissions', 'resourcesService'];

  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * User service.
   */
  private userServices: che.IUserServices;
  /**
   * Organization permission service.
   */
  private organizationsPermissionService: OrganizationsPermissionService;
  /**
   * Organization API interaction.
   */
  private cheOrganization: che.api.ICheOrganization;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Organization details (the value is set in directive attributes).
   */
  private organization: che.IOrganization;
  /**
   * Callback needed to react on organizations updation (the value is set in directive attributes).
   */
  private onUpdate: Function;
  /**
   * todo
   */
  private organizationActions: che.resource.ICheOrganizationActions;

  /**
   * Default constructor that is using resource injection
   */
  constructor($location: ng.ILocationService, cheOrganization: che.api.ICheOrganization, confirmDialogService: any, cheNotification: any,
      organizationsPermissionService: OrganizationsPermissionService, chePermissions: che.api.IChePermissions, resourcesService: che.service.IResourcesService) {
    this.$location = $location;
    this.confirmDialogService = confirmDialogService;
    this.cheOrganization = cheOrganization;
    this.cheNotification = cheNotification;
    this.organizationsPermissionService =  organizationsPermissionService;
    this.organizationActions = resourcesService.getOrganizationActions();

    this.userServices = chePermissions.getUserServices();
  }

  /**
   * returns true if current user has Delete permission
   * @returns {boolean}
   */
  hasDeletePermission(): boolean {
    if (!this.organization || (!this.organization.parent && !this.userServices.hasAdminUserService)) {
      return false;
    }
    return this.organizationsPermissionService.isUserAllowedTo(this.organizationActions.DELETE, this.organization.id);
  }

  /**
   * Gets all sub organizations.
   */
  getAllSubOrganizations(): Array<che.IOrganization> {
    let subOrganizationsTree = this.cheOrganization.getOrganizations().filter((organization: che.IOrganization) => {
      if (!organization.parent || this.organization.id === organization.id) {
        return false;
      }
      return organization.qualifiedName.indexOf(this.organization.qualifiedName + '/') === 0;
    });

    return subOrganizationsTree;
  }

  /**
   * Redirect to factory details.
   */
  redirectToOrganizationDetails(tab: string) {
      this.$location.path('/organization/' + this.organization.qualifiedName).search(!tab ? {} : {tab: tab});
  }

  /**
   * Removes organization after confirmation.
   */
  removeOrganization(): void {
    this.confirmRemoval().then(() => {
      this.cheOrganization.deleteOrganization(this.organization.id).then(() => {
        this.onUpdate();
      }, (error: any) => {
        let message = 'Failed to delete organization ' + this.organization.name;
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : message);
      });
    });
  }

  /**
   * Shows dialog to confirm the current organization removal.
   *
   * @returns {angular.IPromise<any>}
   */
  confirmRemoval(): ng.IPromise<any> {
    return this.confirmDialogService.showConfirmDialog('Delete organization',
      'Would you like to delete organization \'' + this.organization.name + '\'?', 'Delete');
  }
}
