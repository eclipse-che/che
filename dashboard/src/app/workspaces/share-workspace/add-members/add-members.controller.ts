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
import {CheProfile} from '../../../../components/api/che-profile.factory';
import {CheUser} from '../../../../components/api/che-user.factory';

/**
 * This class is handling the controller for the add members popup
 * @author Oleksii Orel
 * @author Ann Shumilova
 */
export class AddMemberController {

  static $inject = ['$q', '$mdDialog', 'lodash', 'cheTeam', 'chePermissions', 'cheProfile', 'cheUser', '$log', '$scope', 'cheListHelperFactory'];

  private $mdDialog: angular.material.IDialogService;
  private $q: ng.IQService;
  private lodash: any;
  private chePermissions: che.api.IChePermissions;
  private cheProfile: CheProfile;
  /**
   * Workspace namespace (is set from outside).
   */
  private namespace: string;
  /**
   * Callback handler (is set from outside).
   */
  private callbackController: any;
  /**
   * The list of users, that already have permissions in the workspace (is set from outside).
   */
  private users: Array<any>;
  private team: any;
  private isLoading: boolean;
  private members: Array<any>;

  /**
   * Selected status of members in list.
   */
  private membersSelectedStatus: any;
  /**
   * Bulk operation state.
   */
  private isBulkChecked: boolean;
  /**
   * No selected members state.
   */
  private isNoSelected: boolean;
  /**
   * All selected members state.
   */
  private isAllSelected: boolean;

  private cheTeam: che.api.ICheTeam;

  private cheUser: CheUser;

  private $log: ng.ILogService;

  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor.
   */
  constructor($q: ng.IQService, $mdDialog: angular.material.IDialogService, lodash: any, cheTeam: che.api.ICheTeam,
              chePermissions: che.api.IChePermissions, cheProfile: CheProfile, cheUser: CheUser, $log: ng.ILogService,
              $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;
    this.cheTeam = cheTeam;
    this.cheUser = cheUser;
    this.$log = $log;

    this.chePermissions = chePermissions;
    this.cheProfile = cheProfile;

    this.team = cheTeam.getTeamByName(this.namespace);
    this.membersSelectedStatus = {};
    this.isBulkChecked = false;
    this.isNoSelected = true;
    this.isAllSelected = true;

    if (this.team) {
     this.fetchTeamMembers();
    }

    const helperId = 'add-members';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });
  }

  fetchTeamMembers(): void {
    this.isLoading = true;
    this.chePermissions.fetchOrganizationPermissions(this.team.id).then(() => {
      this.formMemberList();
    }, (error: any) => {
      if (error.status === 304) {
        this.formMemberList();
      } else {
        this.isLoading = false;
      }
    });
  }

  /**
   * Combines permissions and users data in one list.
   */
  formMemberList(): void {
    let permissions = this.chePermissions.getOrganizationPermissions(this.team.id);
    let existingMembers = this.lodash.pluck(this.users, 'id');
    this.members = [];
    let promises = [];

    for (let i = 0; i < permissions.length; i++) {
      let permission = permissions[i];
      let userId = permission.userId;
      if (existingMembers.indexOf(userId) >= 0) {
        continue;
      }

      if (this.cheUser.getUserFromId(userId)) {
        this.formUserItem(this.cheUser.getUserFromId(userId), permission);
        continue;
      }

      const promise = this.cheUser.fetchUserId(userId).then(() => {
        this.formUserItem(this.cheUser.getUserFromId(userId), permission);
      }, (error: any) => {
        this.$log.log(`Failed to fetch user by ID with error ${error}`);
      });
      promises.push(promise);
    }

    this.$q.all(promises).finally(() => {
      this.cheListHelper.setList(this.members, 'email');
      this.isLoading = false;
    });
  }

  /**
   * Forms item to display with permissions and user data.
   *
   * @param user user data
   * @param permissions permissions data
   */
  formUserItem(user: any, permissions: any): void {
    user.name = this.cheProfile.getFullName(user.attributes);
    let userItem = angular.copy(user);
    userItem.userId = user.id;
    userItem.permissions = permissions;
    this.members.push(userItem);
  }


  /**
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the share button of the dialog.
   */
  shareWorkspace() {
    let checkedUsers = [];

    this.cheListHelper.getSelectedItems().forEach((member: any) => {
      checkedUsers.push({userId: member.userId, isTeamAdmin: this.isTeamAdmin(member.userId)});
    });

    let permissionPromises = this.callbackController.shareWorkspace(checkedUsers);

    this.$q.all(permissionPromises).then(() => {
      this.$mdDialog.hide();
    });
  }

  /**
   * Returns true if user is team administrator.
   *
   * @param {string} userId user ID
   * @return {boolean}
   */
  isTeamAdmin(userId: string): boolean {
    let member = this.members.find((_member: any) => {
      return _member.userId === userId;
    });

    if (!member || !member.permissions) {
      return false;
    }

    let roles = this.cheTeam.getRolesFromActions(member.permissions.actions);
    if (!roles || roles.length === 0) {
      return false;
    }

    return roles.some((role: any) => {
      return /admin/i.test(role.title);
    });
  }

}
