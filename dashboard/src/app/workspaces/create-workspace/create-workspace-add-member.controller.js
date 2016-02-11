/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc controller
 * @name workspaces.create.workspace.add.member.controller:CreteWorkspaceAddMemberCtrl
 * @description This class is handling the controller for adding members to new workspace
 * @author Ann Shumilova
 */
export class CreateWorkspaceAddMemberCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($route, cheAPI, $mdDialog, $mdMedia, $q, cheNotification) {
    this.cheAPI = cheAPI;
    this.$mdDialog = $mdDialog;
    this.$mdMedia = $mdMedia;
    this.$q = $q;
    this.cheNotification = cheNotification;
  }

  widthGtSm() {
    return this.$mdMedia('gt-sm');
  }

  /**
   * User clicked on the + button to add a new member. Show the dialog
   * @param $event
   */
  showAddMemberDialog($event) {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'WorkspaceDetailsMembersDialogAddCtrl',
      controllerAs: 'workspaceDetailsMembersDialogAddCtrl',
      bindToController: true,
      clickOutsideToClose: true,
      locals: { callbackController: this},
      templateUrl: 'app/workspaces/workspace-details/members/workspace-details-members-dialog-add.html'
    });
  }

  /**
   * Callback by the dialog that is prompting for users to add
   * @param userEmailToAdd the user to add
   * @param permissionsToSet the roles to add
   */
  callbackMemberAdd(email, roles) {
    let isAlreadyAdded = this.isAlreadyAdded(email);
    if (isAlreadyAdded) {
      this.cheNotification.showInfo('You already have ' + email + ' in the list.');
      return;
    }

    let user = this.cheAPI.getUser().getUserByAlias(email);
    if (user) {
      this.addMember(user, roles);
    } else {
      let promiseGetUserByEmail = this.cheAPI.getUser().fetchUserByAlias(email);
      promiseGetUserByEmail.then(() => {
        user = this.cheAPI.getUser().getUserByAlias(email);
        if (user) {
          this.addMember(user, roles);
        } else {
          this.cheNotification.showError('User with email: ' + email + ' not found.');
        }
      }, (error) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Failed to add member ' + email + '.');
      });
    }
  }

  isAlreadyAdded(email) {
    if (!this.members || this.members.length === 0) {
      return false;
    }

    let found = false;
    this.members.forEach((member) => {
      if (member.email === email) {
        found = true;
      }
    });
    return found;
  }

  addMember(user, roles) {
    user.roles = roles;
    user.role = this.cheAPI.getUser().getDisplayRole(roles);
    user.name = '';
    this.members.push(user);
  }

  removeMember(event, member) {
    let index = this.members.indexOf(member);
    this.members.splice(index, 1);
  }
}
