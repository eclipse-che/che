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
import {CheUser} from '../../../../components/api/che-user.factory';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {ConfirmDialogService} from '../../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheProfile} from '../../../../components/api/che-profile.factory';

interface IOrganizationMember extends che.IUser {
  permissions: che.IPermissions;
}

/**
 * @ngdoc controller
 * @name organization.details.members:ListOrganizationMembersController
 * @description This class is handling the controller for the list of organization's members.
 * @author Oleksii Orel
 */
export class ListOrganizationMembersController {

  static $inject = ['chePermissions', 'cheUser', 'cheProfile', 'cheOrganization', 'confirmDialogService', '$mdDialog',
'$q', 'cheNotification', 'lodash', '$location', 'organizationsPermissionService', '$scope', 'cheListHelperFactory', 'resourcesService', '$log'];

  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * User API interaction.
   */
  private cheUser: CheUser;
  /**
   * Organization API interaction.
   */
  private cheOrganization: che.api.ICheOrganization;
  /**
   * User profile API interaction.
   */
  private cheProfile: any;
  /**
   * Permissions API interaction.
   */
  private chePermissions: che.api.IChePermissions;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Notifications service.
   */
  private cheNotification: CheNotification;
  /**
   * Confirm dialog service.
   */
  private confirmDialogService: ConfirmDialogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Logging service.
   */
  private $log: ng.ILogService;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Organization's members list.
   */
  private members: Array<IOrganizationMember>;
  /**
   * Members list of parent organization (comes from directive's scope)
   */
  private parentOrganizationMembers: Array<che.IUser>;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Filter for members list.
   */
  private memberFilter: any;
  /**
   * Current organization (comes from directive's scope).
   */
  private organization: che.IOrganization;
  /**
   * Organization permission service.
   */
  private organizationsPermissionService: OrganizationsPermissionService;
  /**
   * Has update permission.
   */
  private hasUpdatePermission;
  /**
   * Selection and filtration helper
   */
  private cheListHelper: che.widget.ICheListHelper;
  /**
   * todo
   */
  private organizationActions: che.resource.ICheOrganizationActions;
  /**
   * todo
   */
  private organizationRoles: che.resource.ICheOrganizationRoles;

  /**
   * Default constructor that is using resource
   */
  constructor(chePermissions: che.api.IChePermissions, cheUser: CheUser, cheProfile: CheProfile, cheOrganization: che.api.ICheOrganization,
              confirmDialogService: ConfirmDialogService, $mdDialog: angular.material.IDialogService, $q: ng.IQService, cheNotification: CheNotification,
              lodash: any, $location: ng.ILocationService, organizationsPermissionService: OrganizationsPermissionService,
              $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory, resourcesService: che.service.IResourcesService, $log: ng.ILogService) {
    this.chePermissions = chePermissions;
    this.cheProfile = cheProfile;
    this.cheUser = cheUser;
    this.cheOrganization = cheOrganization;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.$location = $location;
    this.lodash = lodash;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;
    this.organizationsPermissionService = organizationsPermissionService;
    this.organizationActions = resourcesService.getOrganizationActions();
    this.organizationRoles = resourcesService.getOrganizationRoles();
    this.$log = $log;

    this.members = [];

    this.memberFilter = {name: ''};
    const helperId = 'list-organization-members';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.formMemberList();
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter organization members.
   */
  onSearchChanged(str: string): void {
    this.memberFilter.name = str;
    this.cheListHelper.applyFilter('name', this.memberFilter);
  }


  /**
   * Fetches the list of organization members.
   */
  fetchMembers(): void {
    if (!this.organization || !this.organization.id) {
      return;
    }
    this.isLoading = true;
    this.chePermissions.fetchOrganizationPermissions(this.organization.id).then(() => {
      this.formMemberList();
    }, (error: any) => {
      let errorMessage = error && error.data && error.data.message ? error.data.message : 'Failed to retrieve organization permissions.';
      this.cheNotification.showError(errorMessage);
    }).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Combines permissions and users data in one list.
   */
  formMemberList(): void {
    this.members = [];

    const permissions = this.chePermissions.getOrganizationPermissions(this.organization.id);

    const promises = permissions.map((permission: che.IPermissions) => {
      const userId = permission.userId;

      if (this.cheUser.getUserFromId(userId)) {
        this.formMemberItem(this.cheUser.getUserFromId(userId), permission);
        return this.$q.when();
      }

      return this.cheUser.fetchUserId(userId).then(() => {
        this.formMemberItem(this.cheUser.getUserFromId(userId), permission);
      }, (error: any) => {
        this.$log.error(`Failed to fetch user by ID with error ${error}`);
      });
    });

    this.$q.all(promises).finally(() => {
      this.cheListHelper.setList(this.members, 'email');
    });

    this.hasUpdatePermission = this.organizationsPermissionService.isUserAllowedTo(this.organizationActions.UPDATE.toString(), this.organization.id);
  }

  /**
   * Forms item to display with permissions and user data.
   *
   * @param userInfo {che.IUser} user's profile
   * @param permissions {che.IPermissions} data
   */
  formMemberItem(userInfo: che.IUser, permissions: che.IPermissions): void {
    const member = angular.copy(userInfo) as IOrganizationMember;
    member.permissions = permissions;
    this.members.push(member);
  }

  /**
   * Selects which dialog should be shown.
   */
  selectAddMemberDialog() {
    if (this.organization.parent) {
      this.showMembersListDialog();
    } else {
      this.showMemberDialog(null);
    }
  }

  /**
   * Shows dialog for adding new member to the organization.
   */
  showMemberDialog(member: che.IMember): void {
    this.$mdDialog.show({
      controller: 'OrganizationMemberDialogController',
      controllerAs: 'organizationMemberDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        members: this.members,
        callbackController: this,
        member: angular.copy(member),
        parentOrganizationId: this.organization.parent,
        parentOrganizationMembers: this.parentOrganizationMembers
      },
      templateUrl: 'app/organizations/organization-details/organization-member-dialog/organization-member-dialog.html'
    });
  }

  /**
   * Shows dialog to select members from list to a sub-organization.
   *
   */
  showMembersListDialog(): void {
    this.$mdDialog.show({
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'OrganizationSelectMembersDialogController',
      controllerAs: 'organizationSelectMembersDialogController',
      locals: {
        callbackController: this,
        parentOrganizationMembers: this.parentOrganizationMembers,
        members: this.members
      },
      templateUrl: 'app/organizations/organization-details/organization-select-members-dialog/organization-select-members-dialog.html'
    });
  }

  /**
   * Add new members to the organization.
   *
   * @param {Array<che.IMember>} members members to be added
   * @param {string} role member role
   */
  addMembers(members: Array<che.IMember>, role: string): void {
    let promises = [];
    let unregistered = [];

    members.forEach((member: che.IMember) => {
      if (member.id) {
        let actions = this.organizationRoles[role].actions;
        let permissions = {
          instanceId: this.organization.id,
          userId: member.id,
          domainId: 'organization',
          actions: actions
        };
        let promise = this.chePermissions.storePermissions(permissions);
        promises.push(promise);
      } else {
        unregistered.push(member.email);
      }
    });

    this.isLoading = true;
    this.$q.all(promises).then(() => {
      this.fetchMembers();
    }).finally(() => {
      this.isLoading = false;
      if (unregistered.length > 0) {
        this.cheNotification.showError('User' + (unregistered.length > 1 ? 's ' : ' ') + unregistered.join(', ') + (unregistered.length > 1 ? ' are' : ' is') + ' not registered in the system.');
      }
    });
  }

  /**
   * Perform edit member permissions.
   *
   * @param member
   */
  editMember(member: che.IMember): void {
    this.showMemberDialog(member);
  }

  /**
   * Performs member's permissions update.
   *
   * @param member member to update permissions
   */
  updateMember(member: che.IMember): void {
    if (member.permissions.actions.length > 0) {
      this.storePermissions(member.permissions);
    } else {
      this.removePermissions(member);
    }
  }

  /**
   * Stores provided permissions.
   *
   * @param permissions {che.IPermissions}
   */
  storePermissions(permissions: che.IPermissions): void {
    this.isLoading = true;
    this.chePermissions.storePermissions(permissions).then(() => {
      this.fetchMembers();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Set user permissions failed.');
    });
  }

  /**
   * Remove all selected members.
   */
  removeSelectedMembers(): void {
    const selectedMembers = this.cheListHelper.getSelectedItems(),
          selectedMemberIds = selectedMembers.map((member: che.IMember) => {
            return member.id;
          });

    if (!selectedMemberIds.length) {
      this.cheNotification.showError('No such developers.');
      return;
    }

    const confirmationPromise = this.showDeleteMembersConfirmation(selectedMemberIds.length);
    confirmationPromise.then(() => {
      const removeMembersPromises = [];
      let removalError;
      let isCurrentUser = false;
      for (let i = 0; i < selectedMemberIds.length; i++) {
        const id = selectedMemberIds[i];
        this.cheListHelper.itemsSelectionStatus[id] = false;
        if (id === this.cheUser.getUser().id) {
          isCurrentUser = true;
        }
        const promise = this.chePermissions.removeOrganizationPermissions(this.organization.id, id);
        promise.catch((error: any) => {
          removalError = error;
        });
        removeMembersPromises.push(promise);
      }

      this.$q.all(removeMembersPromises).finally(() => {
        if (isCurrentUser) {
          this.processCurrentUserRemoval();
        } else {
          this.fetchMembers();
        }

        if (removalError) {
          this.cheNotification.showError(removalError.data && removalError.data.message ? removalError.data.message : 'User removal failed.');
        }
      });
    });
  }

  /**
   * Call user permissions removal. Show the dialog
   * @param member
   */
  removeMember(member: che.IMember): void {
    let promise = this.confirmDialogService.showConfirmDialog('Remove member', 'Would you like to remove member  ' + member.email + ' ?', 'Delete');

    promise.then(() => {
      this.removePermissions(member);
    });
  }

  /**
   * Returns true if the member is owner for current organization.
   * @param member
   *
   * @returns {boolean}
   */
  isOwner(member: che.IMember): boolean {
    if (!this.organization || !member) {
      return false;
    }

    return this.organization.qualifiedName.split('/')[0] === member.name;
  }

  /**
   * Returns string with member roles.
   * @param member
   *
   * @returns {string} string format of roles array
   */
  getMemberRoles(member: che.IMember): string {
    if (!member) {
      return '';
    }
    if (this.isOwner(member)) {
      return 'Organization Owner';
    }
    let roles = this.cheOrganization.getRolesFromActions(member.permissions.actions);
    let titles = [];
    let processedActions = [];
    roles.forEach((role: any) => {
      titles.push(role.title);
      processedActions = processedActions.concat(role.actions);
    });

    return titles.join(', ');
  }

  /**
   * Returns string with member other actions.
   * @param member
   *
   * @returns {string} string format of roles array
   */
  getOtherActions(member: che.IMember): string {
    if (!member) {
      return '';
    }
    let roles = this.cheOrganization.getRolesFromActions(member.permissions.actions);
    let processedActions = [];
    roles.forEach((role: any) => {
      processedActions = processedActions.concat(role.actions);
    });

    return this.lodash.difference(member.permissions.actions, processedActions).join(', ');
  }

  /**
   * Process the removal of current user from organization.
   */
  processCurrentUserRemoval(): void {
    this.$location.path('/organizations');
  }

  /**
   * Removes user permissions for current organization
   *
   * @param member {che.IMember}
   */
  removePermissions(member: che.IMember): void {
    this.isLoading = true;
    this.chePermissions.removeOrganizationPermissions(member.permissions.instanceId, member.id).then(() => {
      if (member.id === this.cheUser.getUser().id) {
        this.processCurrentUserRemoval();
      } else {
        this.fetchMembers();
      }
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to remove user ' + member.email + ' permissions.');
    });
  }

  /**
   * Show confirmation popup before members removal
   * @param numberToDelete {number}
   * @returns {ng.IPromise<any>}
   */
  showDeleteMembersConfirmation(numberToDelete: number): ng.IPromise<any> {
    let confirmTitle = 'Would you like to remove ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' members?';
    } else {
      confirmTitle += 'the selected member?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove members', confirmTitle, 'Delete');
  }
}
