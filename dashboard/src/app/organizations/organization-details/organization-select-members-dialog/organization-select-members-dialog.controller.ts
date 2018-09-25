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
import {ListOrganizationInviteMembersController} from '../organization-invite-members/list-organization-invite-members.controller';

interface IOrganizationMember extends che.IMember {
  fullName?: string;
}

/**
 * This class is handling the controller for the add organization's members dialog.
 *
 * @author Oleksii Kurinnyi
 */
export class OrganizationSelectMembersDialogController {

  static $inject = ['$q', '$mdDialog', 'lodash', 'cheProfile', 'cheUser', 'resourcesService'];

  /**
   * User profile API interaction.
   */
  private cheProfile: any;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Callback handler (is set from outside).
   */
  private callbackController: ListOrganizationInviteMembersController;
  /**
   * The list of users, that already are members of this organization (is set from outside).
   */
  private members: Array<che.IMember>;
  /**
   * The list of users, that are members of parent organization (is set from outside).
   */
  private parentOrganizationMembers: Array<che.IUser>;
  /**
   * The list of users, that are available to be added
   */
  private availableUsers: Array<IOrganizationMember>;
  /**
   * Current user.
   */
  private user: che.IUser;
  /**
   * Selected status of members in list.
   */
  private userSelectedStatus: any;
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
  /**
   * True when loading resources.
   */
  private isLoading: boolean;
  /**
   *
   */
  private organizationRoles: che.resource.ICheOrganizationRoles;

  /**
   * Default constructor.
   */
  constructor($q: ng.IQService, $mdDialog: angular.material.IDialogService, lodash: any, cheProfile: any, cheUser: any, resourcesService: che.service.IResourcesService) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;
    this.cheProfile = cheProfile;
    this.cheUser = cheUser;
    this.organizationRoles = resourcesService.getOrganizationRoles();

    this.isLoading = false;

    this.userSelectedStatus = {};
    this.isBulkChecked = false;
    this.isNoSelected = true;
    this.isAllSelected = false;

    this.user = this.cheUser.getUser();

    this.formUsersAvailableList();
  }

  /**
   * Builds list of users that are available to be added.
   */
  formUsersAvailableList(): void {
    const existingMembers = this.members.reduce((map: {[id: string]: che.IMember}, member: che.IMember) => {
      map[member.id] = member;
      return map;
    }, {});
    this.availableUsers = <any>this.parentOrganizationMembers.filter((parentOrganizationMember: che.IUser) => {
      return !existingMembers[parentOrganizationMember.id] && parentOrganizationMember.id !== this.user.id;
    });

    if (!this.availableUsers.length) {
      return ;
    }

    const userProfilesPromises = [];

    this.isLoading = true;

    this.availableUsers.forEach((user: IOrganizationMember) => {
      const profile = this.cheProfile.getProfileById(user.id);
      if (profile) {
        user.fullName = this.cheProfile.getFullName(profile.attributes);
      } else {
        const promise = this.cheProfile.fetchProfileById(user.id).then(() => {
          const profile = this.cheProfile.getProfileById(user.id);
          user.fullName = this.cheProfile.getFullName(profile.attributes);
        });
        userProfilesPromises.push(promise);
      }
    });

    this.$q.all(userProfilesPromises).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  hide() {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the "Add" button of the dialog.
   */
  addMembers() {
    const checkedUsers = this.availableUsers.reduce((usersToAdd: Array<che.IMember>, member: IOrganizationMember) => {
      if (this.userSelectedStatus[member.id]) {
        usersToAdd.push(member);
      }
      return usersToAdd;
    }, []);

    this.callbackController.addMembers(checkedUsers, this.organizationRoles.MEMBER.name);
    this.$mdDialog.hide();
  }

  /**
   * Return <code>true</code> if all users in list are checked.
   * @returns {boolean}
   */
  isAllUsersSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * Returns <code>true</code> if all users in list are not checked.
   * @returns {boolean}
   */
  isNoUsersSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Make all users in list selected.
   */
  selectAllUsers(): void {
    this.availableUsers.forEach((user: IOrganizationMember) => {
      this.userSelectedStatus[user.id] = true;
    });
  }

  /**
   * Make all users in list deselected.
   */
  deselectAllUsers(): void {
    this.availableUsers.forEach((user: IOrganizationMember) => {
      this.userSelectedStatus[user.id] = false;
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllUsers();
      this.isBulkChecked = false;
    } else {
      this.selectAllUsers();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Set selected status for user.
   *
   * @param {string} id user ID
   * @param {boolean} isSelected
   */
  setSelectedStatus(id: string, isSelected: boolean) {
    this.userSelectedStatus[id] = isSelected;
    this.updateSelectedStatus();
  }

  /**
   * Update members selected status.
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    Object.keys(this.userSelectedStatus).forEach((key: string) => {
      if (this.userSelectedStatus[key]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });

    if (this.isNoSelected) {
      this.isBulkChecked = false;
      return;
    }

    this.isBulkChecked = (this.isAllSelected && Object.keys(this.userSelectedStatus).length === this.availableUsers.length);
  }
}

