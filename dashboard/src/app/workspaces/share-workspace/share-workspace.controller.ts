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
import {CheUser} from '../../../components/api/che-user.factory';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';

interface ISharedWorkspaceUser extends che.IUser {
  permissions: any;
}

/**
 * Controller for workspace sharing with other users.
 *
 * @ngdoc controller
 * @name workspace.details.controller:ShareWorkspaceController
 * @description This class is handling the controller sharing workspace
 * @author Ann Shumilova
 */
export class ShareWorkspaceController {

  static $inject = ['cheWorkspace', 'cheUser', 'chePermissions', 'cheNotification', '$mdDialog', '$document', '$mdConstant', '$route', '$q', 'lodash',
   'confirmDialogService', 'cheTeam', '$log', '$scope', 'cheListHelperFactory'];

  /**
   * Workspace API interaction.
   */
  private cheWorkspace: any;
  /**
   * User API interaction.
   */
  private cheUser: CheUser;
  /**
   * Permissions API interaction.
   */
  private chePermissions: che.api.IChePermissions;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: CheNotification;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: ng.material.IDialogService;
  /**
   * Wrapper for browser's window.document object
   */
  private $document: ng.IDocumentService;
  /**
   * Angular promise service.
   */
  private $q: ng.IQService;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Confirm dialog service.
   */
  private confirmDialogService: ConfirmDialogService;
  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * Log service.
   */
  private $log: ng.ILogService;

  private personalAccount: any;
  private separators: number[];
  private users: Array<ISharedWorkspaceUser>;
  private emails: string[];
  private existingUsers: Map<string, string>;
  private notExistingUsers: string[];
  private isLoading: boolean;
  private actions: string[];
  private workspaceDomain: string;
  private namespace: string;
  private workspaceName: string;
  private workspace: che.IWorkspace;
  private userOrderBy: string;
  private userFilter: {
    email: string;
  };
  private noPermissionsError: boolean;
  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor that is using resource
   */
  constructor(cheWorkspace: CheWorkspace,
              cheUser: CheUser,
              chePermissions: che.api.IChePermissions,
              cheNotification: CheNotification,
              $mdDialog: ng.material.IDialogService,
              $document: ng.IDocumentService,
              $mdConstant: any,
              $route: ng.route.IRouteService,
              $q: ng.IQService,
              lodash: any,
              confirmDialogService: ConfirmDialogService,
              cheTeam: che.api.ICheTeam,
              $log: ng.ILogService,
              $scope: ng.IScope,
              cheListHelperFactory: che.widget.ICheListHelperFactory) {

    this.cheWorkspace = cheWorkspace;
    this.cheUser = cheUser;
    this.chePermissions = chePermissions;
    this.cheNotification = cheNotification;
    this.$mdDialog = $mdDialog;
    this.$document = $document;
    this.$q = $q;
    this.lodash = lodash;
    this.confirmDialogService = confirmDialogService;
    this.cheTeam = cheTeam;
    this.$log = $log;

    // email values separators:
    this.separators = [$mdConstant.KEY_CODE.ENTER, $mdConstant.KEY_CODE.COMMA, $mdConstant.KEY_CODE.SPACE];
    // users that have permissions in current workspace:
    this.users = [];
    // entered emails to share workspace with:
    this.emails = [];
    // filtered entered emails - users that really are registered
    this.existingUsers = new Map();
    // filtered entered emails - users that are not registered
    this.notExistingUsers = [];

    this.isLoading = false;
    // temp solution with defined actions, better to provide list of available for user to choose:
    this.actions = ['read', 'use', 'run', 'configure'];
    this.workspaceDomain = 'workspace';

    this.namespace = $route.current.params.namespace;
    this.workspaceName = $route.current.params.workspaceName;

    this.workspace = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
    if (this.workspace) {
      this.refreshWorkspacePermissions();
    } else {
      this.isLoading = true;
      let promise = this.cheWorkspace.fetchWorkspaces();

      promise.then(() => {
        this.isLoading = false;
        this.workspace = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
        this.refreshWorkspacePermissions();
      }, (error: any) => {
        this.isLoading = false;
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to update workspace data.');
      });
    }

    this.userOrderBy = 'email';
    this.userFilter = {email: ''};

    const helperId = 'share-workspace';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.fetchPersonalAccount();
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter user emails.
   */
  onSearchChanged(str: string): void {
    this.userFilter.email = str;
    this.cheListHelper.applyFilter('email', this.userFilter);
  }


  /**
   * Fetches account ID.
   *
   * @return {IPromise<any>}
   */
  fetchPersonalAccount(): ng.IPromise<any> {
    return this.cheTeam.fetchTeams().then(() => {
      this.personalAccount = this.cheTeam.getPersonalAccount();
    }, (error: any) => {
      if (error.status === 304) {
        this.personalAccount = this.cheTeam.getPersonalAccount();
      }
    });
  }

  /**
   * Refresh the workspace permissions list.
   */
  refreshWorkspacePermissions(): void {
    this.isLoading = true;
    this.noPermissionsError = true;

    if (!this.workspace) {
      return;
    }

    this.chePermissions.fetchWorkspacePermissions(this.workspace.id).then(() => {
      this.isLoading = false;
      this.formUserList();
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.formUserList();
      } else if (error.status === 403) {
        this.noPermissionsError = false;
      } else {
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to retrieve workspace permissions.');
      }
    });
  }

  /**
   * Combines permissions and users data in one list.
   */
  formUserList(): void {
    const permissions = this.chePermissions.getWorkspacePermissions(this.workspace.id);
    this.users = [];

    const promises: Array<ng.IPromise<any>> = [];

    permissions.forEach((permission: any) => {
      const userId = permission.userId;
      const user = this.cheUser.getUserFromId(userId);
      if (user) {
        this.formUserItem(user, permission);
      } else {
        const promise = this.cheUser.fetchUserId(userId).then(() => {
          this.formUserItem(this.cheUser.getUserFromId(userId), permission);
        });
        promises.push(promise);
      }
    });

    this.$q.all(promises).finally(() => {
      this.cheListHelper.setList(this.users, 'id');
    });
  }

  /**
   * Forms item to display with permissions and user data.
   *
   * @param user user data
   * @param permissions permissions data
   */
  formUserItem(user: any, permissions: any): void {
    let userItem = angular.copy(user);
    userItem.permissions = permissions;
    this.users.push(userItem);
  }

  shareWorkspace(users: Array<{userId: string, isTeamAdmin: boolean}>): Array<ng.IPromise<any>> {
    let permissionPromises = [];

    users.forEach((user: {userId: string, isTeamAdmin: boolean}) => {
      permissionPromises.push(this.storeWorkspacePermissions(user.userId, user.isTeamAdmin));
    });

    this.$q.all(permissionPromises).then(() => {
      this.refreshWorkspacePermissions();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to share workspace.');
    });

    return permissionPromises;
  }

  /**
   * Removes all selected users permissions for current workspace
   */
  deleteSelectedWorkspaceMembers(): void {
    const selectedUsers: Array<ISharedWorkspaceUser> = this.cheListHelper.getSelectedItems();

    const queueLength = selectedUsers.length;
    if (!queueLength) {
      this.cheNotification.showError('No such workspace member.');
      return;
    }

    let confirmationPromise = this.showDeleteConfirmation(queueLength);
    confirmationPromise.then(() => {
      const numberToDelete = queueLength;
      const deleteUserPromises = [];
      let isError = false;
      let currentUserEmail;

      selectedUsers.forEach((user: ISharedWorkspaceUser) => {
        currentUserEmail = user.email;
        this.cheListHelper.itemsSelectionStatus[user.id] = false;

        const promise = this.chePermissions.removeWorkspacePermissions(user.permissions.instanceId, user.id);
        promise.catch((error: any) => {
          isError = true;
          this.$log.error('Cannot delete permissions: ', error);
        });
        deleteUserPromises.push(promise);
      });

      this.$q.all(deleteUserPromises).finally(() => {
        this.refreshWorkspacePermissions();
        if (isError) {
          this.cheNotification.showError('Delete failed.');
        } else {
          if (numberToDelete === 1) {
            this.cheNotification.showInfo(currentUserEmail + 'has been removed.');
          } else {
            this.cheNotification.showInfo('Selected numbers have been removed.');
          }
        }
      });
    });
  }

  /**
   * Show confirmation popup before delete
   * @param numberToDelete {number}
   * @returns {ng.IPromise<any>}
   */
  showDeleteConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' members?';
    } else {
      content += 'this selected member?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove members', content, 'Delete');
  }

  /**
   * Show workspace sharing dialog.
   *
   * @param {MouseEvent} $event the event
   */
  showShareWorkspaceDialog($event: MouseEvent): void {
    if (this.isPersonalTeam()) {
      let canShare = (this.personalAccount.qualifiedName === this.namespace);
      this.showAddDevelopersDialog($event, canShare);
    } else {
      if (this.cheUser.getUser().name === this.namespace) {
        this.showAddDevelopersDialog($event, true);
      } else {
        this.showAddMembersDialog($event);
      }
    }
  }

  /**
   * Returns true if team is personal.
   *
   * @return {boolean}
   */
  isPersonalTeam(): boolean {
    if (!this.personalAccount) {
      return false;
    }

    return this.users.some((user: any) => {
      return user.name === this.namespace;
    });
  }

  /**
   * Add new workspace member to share a personal workspace. Show the dialog
   * @param {MouseEvent} $event - the $event
   * @param {boolean} canShare - true if user can share this workspace
   */
  showAddDevelopersDialog($event: MouseEvent, canShare: boolean): void {
    let parentEl = angular.element(this.$document.find('body'));

    this.$mdDialog.show({
      targetEvent: $event,
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'AddDeveloperController',
      controllerAs: 'addDeveloperController',
      locals: {
        callbackController: this,
        canShare: canShare,
        existingUsers: this.existingUsers
      },
      parent: parentEl,
      templateUrl: 'app/workspaces/share-workspace/add-developers/add-developers.html'
    });
  }

  /**
   * Add new workspace member. Show the dialog
   * @param {MouseEvent} $event - the $event
   */
  showAddMembersDialog($event: MouseEvent): void {
    let parentEl = angular.element(this.$document.find('body'));

    this.$mdDialog.show({
      targetEvent: $event,
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'AddMemberController',
      controllerAs: 'addMemberController',
      locals: {
        callbackController: this,
        namespace: this.namespace,
        users: this.users
      },
      parent: parentEl,
      templateUrl: 'app/workspaces/share-workspace/add-members/add-members.html'
    });
  }

  /**
   * Returns the list of the not registered users emails as string.
   *
   * @returns {string} string with wrong emails coma separated
   */
  getNotExistingEmails(): string {
    return this.notExistingUsers.join(', ');
  }

  /**
   * Stores user permissions of the current workspace.
   *
   * @param {string} userId user ID
   * @param {boolean} isTeamAdmin
   * @returns {ng.IPromise} promise with store permissions request
   */
  storeWorkspacePermissions(userId: string, isTeamAdmin: boolean): ng.IPromise<any> {

    let permission: any = {};
    permission.userId = userId;
    permission.domainId = this.workspaceDomain;
    permission.instanceId = this.workspace.id;
    permission.actions = this.actions;
    if (isTeamAdmin) {
      permission.actions.push('setPermissions');
    }

    return this.chePermissions.storePermissions(permission);
  }

  /**
   * Removes user permissions for current workspace
   *
   * @param {Object} user user
   */
  removePermissions(user: any): void {
    this.isLoading = true;
    this.chePermissions.removeWorkspacePermissions(user.permissions.instanceId, user.id).then(() => {
      this.refreshWorkspacePermissions();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to remove user ' + user.email + ' permissions.');
    });
  }

  /**
   * Handle user email adding.
   *
   * @param {string} email user's email
   * @returns {string|null}
   */
  handleUserAdding(email: string): string {
    // prevents mentioning same users twice:
    if (this.existingUsers.has(email)) {
      return null;
    }

    // displays user name instead of email
    if (this.cheUser.getUserByAlias(email)) {
      let user = this.cheUser.getUserByAlias(email);
      this.existingUsers.set(email, user.id);
      return email;
    }

    this.cheUser.fetchUserByAlias(email).then(() => {
      let user = this.cheUser.getUserByAlias(email);
      this.existingUsers.set(email, user.id);
    }, (error: any) => {
      this.notExistingUsers.push(email);
    });

    return email;
  }

  /**
   * Removes removed email from the list of registered users.
   *
   * @param {string} email email to remove
   */
  onRemoveEmail(email: string): void {
    this.existingUsers.delete(email);

    this.lodash.remove(this.notExistingUsers, (data: any) => {
      return email === data;
    });
  }

  /**
   * Returns user's name by email
   *
   * @param {string} email user email
   * @returns {user.name|*} user's name
   */
  getUserName(email: string): string {
    let user = this.cheUser.getUserByAlias(email);
    return user ? user.name : email;
  }

  /**
   * Checks user exists in the system (is reqistered).
   *
   * @param {string} email user's email to be checked
   * @returns {boolean}
   */
  isUserExists(email: string): boolean {
    return (this.cheUser.getUserByAlias(email) !== undefined);
  }

}
