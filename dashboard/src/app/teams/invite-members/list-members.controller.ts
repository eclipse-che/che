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
import {CheTeamRoles} from '../../../components/api/che-team-roles';

/**
 * @ngdoc controller
 * @name teams.invite.members:ListMembersController
 * @description This class is handling the controller for the list of invited members.
 * @author Ann Shumilova
 */
export class ListMembersController {

  static $inject = ['$mdDialog', 'lodash', 'cheTeam'];

  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * No members selected.
   */
  private isNoSelected: boolean;
  /**
   * Bulk operation checked state.
   */
  private isBulkChecked: boolean;
  /**
   * Status of selected members.
   */
  private membersSelectedStatus: any;
  /**
   * Number of selected members.
   */
  private membersSelectedNumber: number;
  /**
   * Members order by value.
   */
  private membersOrderBy: string;
  /**
   * List of members to be invited.
   */
  private members: Array<any>;
  /**
   * Owner of the team (comes from scope).
   */
  private owner: string;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: angular.material.IDialogService, lodash: any, cheTeam: che.api.ICheTeam) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;
    this.cheTeam = cheTeam;

    this.isNoSelected = true;
    this.isBulkChecked = false;
    this.membersSelectedStatus = {};
    this.membersSelectedNumber = 0;
    this.membersOrderBy = 'email';
  }

  /**
   * Forms the list of members.
   */
  buildMembersList(): void {
    this.members.forEach((member: any) => {
      member.role = member.roles ? angular.toJson(member.roles[0]) : angular.toJson(CheTeamRoles.TEAM_MEMBER);
    });
  }

  /**
   * Returns developer role value.
   *
   * @returns {string} string of the developer role value
   */
  getDeveloperRoleValue(): string {
    return angular.toJson(CheTeamRoles.TEAM_MEMBER);
  }

  /**
   * Returns admin role value.
   *
   * @returns {string} string of the admin role value
   */
  getAdminRoleValue(): string {
    return angular.toJson(CheTeamRoles.TEAM_ADMIN);
  }

  /**
   * Handler for value changed in the list.
   * @param member
   */
  onValueChanged(member: any): void {
    member.roles = [angular.fromJson(member.role)];
  }

  /**
   * Update members selected status
   */
  updateSelectedStatus(): void {
    this.membersSelectedNumber = 0;
    this.isBulkChecked = !!this.members.length;
    this.members.forEach((member: any) => {
      if (this.membersSelectedStatus[member.email]) {
        this.membersSelectedNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllMembers();
      this.isBulkChecked = false;
      return;
    }
    this.selectAllMembers();
    this.isBulkChecked = true;
  }

  /**
   * Check all members in list.
   */
  selectAllMembers(): void {
    this.membersSelectedNumber = this.members.length;
    this.members.forEach((member: any) => {
      this.membersSelectedStatus[member.email] = true;
    });
  }

  /**
   * Uncheck all members in list
   */
  deselectAllMembers(): void {
    this.membersSelectedStatus = {};
    this.membersSelectedNumber = 0;
  }

  /**
   * Adds member to the list.
   *
   * @param user
   * @param roles
   */
  addMembers(users: Array<any>, roles: Array<any>): void {
    users.forEach((user: any) => {
      user.roles = roles;
      this.members.push(user);
    });
    this.buildMembersList();
  }

  /**
   * Shows dialog to add new member.
   *
   * @param $event
   */
  showAddDialog($event: MouseEvent): void {
    let members = this.members.concat([{email: this.owner}]);
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'MemberDialogController',
      controllerAs: 'memberDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        members: members,
        member: null,
        callbackController: this
      },
      templateUrl: 'app/teams/member-dialog/member-dialog.html'
    });
  }

  /**
   * Removes selected members.
   */
  removeSelectedMembers(): void {
    this.lodash.remove(this.members, (member: any) => {
      return this.membersSelectedStatus[member.email];
    });
    this.buildMembersList();
    this.deselectAllMembers();
    this.isBulkChecked = false;
  }
}
