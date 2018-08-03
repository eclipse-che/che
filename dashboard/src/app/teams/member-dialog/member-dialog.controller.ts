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
 * @name teams.member:MemberDialogController
 * @description This class is handling the controller for adding/editing members dialog.
 * @author Ann Shumilova
 */
export class MemberDialogController {

  static $inject = ['$mdDialog', 'cheTeam', 'cheUser', '$q', 'lodash'];

  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
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
   * Processing state of adding member.
   */
  private isProcessing: boolean;
  /**
   * Set of user roles info.
   */
  private roles: Array<any>;
  /**
   * Already added emails.
   */
  private emails: Array<string>;
  /**
   * Existing members.
   */
  private members: Array<any>;
  /**
   * Entered email address.
   */
  private email: string;
  /**
   * Controller that will handle callbacks.
   */
  private callbackController: any;

  /**
   * Member to be displayed, may be <code>null</code> if add new member is needed. (Comes from outside)
   */
  private member: any;
  /**
   * Role to be used, may be <code>null</code> if role is needed to be set. (Comes from outside)
   */
  private role: any;
  /**
   * Choosen role for user.
   */
  private newRole: any;
  /**
   * Dialog window title.
   */
  private title: string;
  /**
   * Title of operation button (Save or Add)
   */
  private buttonTitle: string;
  /**
   * Email validation error message.
   */
  private emailError: string;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: angular.material.IDialogService, cheTeam: che.api.ICheTeam, cheUser: any, $q: ng.IQService, lodash: any) {
    this.$mdDialog = $mdDialog;
    this.cheTeam = cheTeam;
    this.cheUser = cheUser;
    this.$q = $q;
    this.lodash = lodash;

    this.isProcessing = false;

    this.emails = [];
    this.members.forEach((member: any) => {
      this.emails.push(member.email);
    });

    // role is set, need to add only user with this role:
    if (this.role) {
      this.email = '';
      this.title = 'Add new ' + this.role.title.toLowerCase();
      this.buttonTitle = 'Add';
      return;
    }

    this.roles = CheTeamRoles.getValues();
    if (this.member) {
      this.title = 'Edit ' + this.member.name + ' roles';
      this.buttonTitle = 'Save';
      this.email = this.member.email;
      let roles = this.cheTeam.getRolesFromActions(this.member.permissions.actions);
      this.newRole = (roles && roles.length > 0) ? angular.toJson(roles[0]) : angular.toJson(CheTeamRoles.TEAM_MEMBER);
    } else {
      this.email = '';
      this.title = 'Invite member to collaborate';
      this.buttonTitle = 'Add';
      this.newRole = angular.toJson(CheTeamRoles.TEAM_MEMBER);
    }
  }

  /**
   * Hides the add member dialog.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Checks whether entered email valid and is unique.
   *
   * @param value value with email(s) to check
   * @returns {boolean} true if pointed email(s) are valid and not in the list yet
   */
  isValidEmail(value: string): boolean {
    // return this.emails.indexOf(email) < 0;
    let emails = value.replace(/ /g, ',').split(',');
    for (let i = 0; i < emails.length; i++) {
      let email = emails[i];
      let emailRe = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
      if (!emailRe.test(email)) {
        this.emailError = email + ' is invalid email address.';
        return false;
      }

      if (this.emails.indexOf(email) >= 0) {
        this.emailError = 'User with email ' + email + ' is already invited.';
        return false;
      }
    }
    return true;
  }

  /**
   * Adds new member.
   */
  addMembers(): void {
    let userRoles =  this.role ? [this.role] : [angular.fromJson(this.newRole)];


    let emails = this.email.replace(/ /g, ',').split(',');
    // form the list of emails without duplicates and empty values:
    let resultEmails = emails.reduce((array: Array<string>, element: string) => {
      if (array.indexOf(element) < 0 && element.length > 0) {
        array.push(element);
      }
      return array;
    }, []);

    let promises = [];
    let users = [];
    resultEmails.forEach((email: string) => {
      promises.push(this.processUser(email, users));
    });

    this.$q.all(promises).then(() => {
      this.finishAdding(users, userRoles);
    });
  }

  processUser(email: string, users : Array<any>): ng.IPromise<any> {
    let deferred = this.$q.defer();
    let user = this.cheUser.getUserByAlias(email);
    if (user) {
      users.push(user);
      deferred.resolve();
    } else {
      user = {};
      user.email = email;
      this.isProcessing = true;
      this.cheUser.fetchUserByAlias(email).then(() => {
        users.push(this.cheUser.getUserByAlias(email));
        deferred.resolve();
      }, (error: any) => {
        users.push(user);
        deferred.resolve();
      });
    }
    return deferred.promise;
  }

  /**
   * Handle edit member user's action.
   */
  editMember(): void {
    this.member.permissions.actions = this.getCurrentActions();
    this.callbackController.updateMember(this.member);
    this.hide();
  }

  /**
   * Returns the actions of current chosen roles.
   */
  getCurrentActions(): Array<string> {
    let userRoles = this.role ? [this.role] : [angular.fromJson(this.newRole)];
    let processedActions = [];
    this.roles.forEach((role: any) => {
      processedActions = processedActions.concat(role.actions);
    });


    let actions = this.member ? this.member.permissions.actions : [];
    let otherActions = this.lodash.difference(actions, processedActions);

    return this.lodash.uniq(this.cheTeam.getActionsFromRoles(userRoles).concat(otherActions));
  }

  /**
   * Finish adding user state.
   *
   * @param users users to be added
   * @param roles user's roles
   */
  finishAdding(users: Array<any>, roles: any): void {
    this.isProcessing = false;
    this.callbackController.addMembers(users, roles);
    this.hide();
  }

}
