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
 * Controller for team member item..
 *
 * @author Ann Shumilova
 */
export class MemberItemController {

  static $inject = ['$mdDialog', 'cheTeam', 'lodash', 'confirmDialogService'];

  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Controller for handling callback events. (Comes from directive's scope).
   */
  private callback: any;
  /**
   * Member to be displayed. (Comes from directive's scope).
   */
  private member: any;
  /**
   * Whether current member is owner of the team. (Comes from directive's scope).
   */
  private isOwner: boolean;

  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Actions that are not part of any role.
   */
  private otherActions: Array<string>;
  /**
   * Confirm dialog service.
   */
  private confirmDialogService: any;

  /**
   * Default constructor that is using resource injection
   */
  constructor($mdDialog: angular.material.IDialogService, cheTeam: che.api.ICheTeam, lodash: any, confirmDialogService: any) {
    this.$mdDialog = $mdDialog;
    this.cheTeam = cheTeam;
    this.lodash = lodash;
    this.confirmDialogService = confirmDialogService;

    this.otherActions = [];
  }

  /**
   * Call user permissions removal. Show the dialog
   * @param  event - the $event
   */
  removeMember(event: MouseEvent): void {
    let promise = this.confirmDialogService.showConfirmDialog('Remove member', 'Would you like to remove member  ' + this.member.email + ' ?', 'Delete');

    promise.then(() => {
      if (this.member.isPending) {
        this.callback.deleteInvitation(this.member);
      } else {
        this.callback.removePermissions(this.member);
      }
    });
  }

  /**
   * Handler edit member user's request.
   */
  editMember(): void {
    this.callback.editMember(this.member);
  }

  /**
   * Returns string with member roles.
   *
   * @returns {string} string format of roles array
   */
  getMemberRoles(): string {
    if (this.isOwner) {
      return 'Team Owner';
    }

    let roles = this.cheTeam.getRolesFromActions(this.member.permissions.actions);
    let titles = [];
    let processedActions = [];
    roles.forEach((role: any) => {
      titles.push(role.title);
      processedActions = processedActions.concat(role.actions);
    });

    this.otherActions = this.lodash.difference(this.member.permissions.actions, processedActions);
    return titles.join(', ');
  }

}

