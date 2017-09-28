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
import {CheProfile} from '../../../../components/api/che-profile.factory';

/**
 * This class is handling the controller for the add members popup
 * @author Oleksii Orel
 * @author Ann Shumilova
 */
export class AddMemberController {

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

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $mdDialog: angular.material.IDialogService, lodash: any, cheTeam: che.api.ICheTeam,
              chePermissions: che.api.IChePermissions, cheProfile: CheProfile) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;
    this.cheTeam = cheTeam;

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

      let user = this.cheProfile.getProfileById(userId);

      if (user) {
        this.formUserItem(user, permission);
      } else {
        let promise = this.cheProfile.fetchProfileById(userId).then(() => {
          this.formUserItem(this.cheProfile.getProfileById(userId), permission);
        });
        promises.push(promise);
      }
    }

    this.$q.all(promises).finally(() => {
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

    Object.keys(this.membersSelectedStatus).forEach((key: string) => {
      if (this.membersSelectedStatus[key] === true) {
        checkedUsers.push({userId: key, isTeamAdmin: this.isTeamAdmin(key)});
      }
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

  /**
   * Return <code>true</code> if all members in list are checked.
   * @returns {boolean}
   */
  isAllMembersSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * Returns <code>true</code> if all members in list are not checked.
   * @returns {boolean}
   */
  isNoMemberSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Make all members in list selected.
   */
  selectAllMembers(): void {
    this.members.forEach((member: any) => {
      this.membersSelectedStatus[member.userId] = true;
    });
  }

  /**
   * Make all members in list deselected.
   */
  deselectAllMembers(): void {
    this.members.forEach((member: any) => {
      this.membersSelectedStatus[member.userId] = false;
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllMembers();
      this.isBulkChecked = false;
    } else {
      this.selectAllMembers();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update members selected status.
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    Object.keys(this.membersSelectedStatus).forEach((key: string) => {
      if (this.membersSelectedStatus[key]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });

    if (this.isNoSelected) {
      this.isBulkChecked = false;
      return;
    }

    this.isBulkChecked = (this.isAllSelected && Object.keys(this.membersSelectedStatus).length === this.members.length);
  }
}
