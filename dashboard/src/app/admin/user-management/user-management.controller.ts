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
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';

const MAX_ITEMS = 12;

/**
 * This class is handling the controller for the admins user management
 * @author Oleksii Orel
 */
export class AdminsUserManagementCtrl {

  static $inject = ['$q', '$rootScope', '$log', '$mdDialog', 'cheUser', '$location', 'cheNotification', 'confirmDialogService', 'cheOrganization', '$scope', 'cheListHelperFactory'];

  $q: ng.IQService;
  $log: ng.ILogService;
  $mdDialog: ng.material.IDialogService;
  $location: ng.ILocationService;
  cheUser: CheUser;
  cheNotification: CheNotification;
  pagesInfo: any;
  users: Array<che.IUser>;
  usersMap: Map<string, che.IUser>;
  userFilter: {name: string};
  userOrderBy: string;
  isLoading: boolean;

  private confirmDialogService: ConfirmDialogService;
  private cheOrganization: che.api.ICheOrganization;
  private userOrganizationCount: {[userId: string]: number} = {};
  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor.
   */
  constructor($q: ng.IQService,
              $rootScope: che.IRootScopeService,
              $log: ng.ILogService,
              $mdDialog: ng.material.IDialogService,
              cheUser: CheUser,
              $location: ng.ILocationService,
              cheNotification: CheNotification,
              confirmDialogService: ConfirmDialogService,
              cheOrganization: che.api.ICheOrganization,
              $scope: ng.IScope,
              cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.$q = $q;
    this.$log = $log;
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.cheUser = cheUser;
    this.cheOrganization = cheOrganization;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;

    $rootScope.showIDE = false;

    this.isLoading = false;

    this.users = [];
    this.usersMap = this.cheUser.getUsersMap();

    this.userOrderBy = 'name';
    this.userFilter = {name: ''};

    const helperId = 'user-management';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    if (this.usersMap && this.usersMap.size > 1) {
      this.updateUsers();
    } else {
      this.isLoading = true;
      this.cheUser.fetchUsers(MAX_ITEMS, 0).then(() => {
        this.isLoading = false;
        this.updateUsers();
      }, (error: any) => {
        this.isLoading = false;
        if (error && error.status !== 304) {
          this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to retrieve the list of users.');
        }
      });
    }

    this.pagesInfo = this.cheUser.getPagesInfo();
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter user names.
   */
  onSearchChanged(str: string): void {
    this.userFilter.name = str;
    this.cheListHelper.applyFilter('name', this.userFilter);
  }

  /**
   * Redirect to user details
   * @param userId {string}
   * @param tab {string}
   */
  redirectToUserDetails(userId: string, tab?: string): void {
    this.$location.path('/admin/userdetails/' + userId).search(!tab ? {} : {tab: tab});
  }

  /**
   * Update user's organizations count
   * @param userId {string}
   */
  updateUserOrganizationsCount(userId: string): void {
    this.cheOrganization.fetchUserOrganizations(userId, 1).then((userOrganizations: Array<any>) => {
      if (!angular.isArray(userOrganizations) || userOrganizations.length === 0) {
        return;
      }
      this.userOrganizationCount[userId] = this.cheOrganization.getUserOrganizationPageInfo(userId).countPages;
    });
  }

  /**
   * User clicked on the - action to remove the user. Show the dialog
   * @param  event {MouseEvent} - the $event
   * @param user {any} - the selected user
   */
  removeUser(event: MouseEvent, user: any): void {
    let content = 'Are you sure you want to remove \'' + user.email + '\'?';
    let promise = this.confirmDialogService.showConfirmDialog('Remove user', content, 'Delete', 'Cancel');

    promise.then(() => {
      this.isLoading = true;
      let promise = this.cheUser.deleteUserById(user.id);
      promise.then(() => {
        this.isLoading = false;
        this.updateUsers();
      }, (error: any) => {
        this.isLoading = false;
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Delete user failed.');
      });
    });
  }

  /**
   * Delete all selected users
   */
  deleteSelectedUsers(): void {
    const selectedUsers = this.cheListHelper.getSelectedItems(),
          selectedUserIds = selectedUsers.map((user: che.IUser) => {
            return user.id;
          });

    const queueLength = selectedUserIds.length;
    if (!queueLength) {
      this.cheNotification.showError('No such user.');
      return;
    }

    const confirmationPromise = this.showDeleteUsersConfirmation(queueLength);
    confirmationPromise.then(() => {
      const numberToDelete = queueLength;
      const deleteUserPromises = [];
      let isError = false;
      let currentUserId;

      selectedUserIds.forEach((userId: string) => {
        currentUserId = userId;
        this.cheListHelper.itemsSelectionStatus[userId] = false;

        let promise = this.cheUser.deleteUserById(userId);
        promise.catch((error: any) => {
          isError = true;
          this.$log.error('Cannot delete user: ', error);
        });
        deleteUserPromises.push(promise);
      });

      this.$q.all(deleteUserPromises).finally(() => {
        this.isLoading = true;

        const promise = this.cheUser.fetchUsersPage(this.pagesInfo.currentPageNumber);
        promise.then(() => {
          this.isLoading = false;
          this.updateUsers();
        }, (error: any) => {
          this.isLoading = false;
          this.$log.error(error);
        });

        if (isError) {
          this.cheNotification.showError('Delete failed.');
        } else {
          if (numberToDelete === 1) {
            this.cheNotification.showInfo('Selected user has been removed.');
          } else {
            this.cheNotification.showInfo('Selected users have been removed.');
          }
        }
      });
    });
  }

  /**
   * Show confirmation popup before delete
   * @param numberToDelete {number}
   * @returns {angular.IPromise<any>}
   */
  showDeleteUsersConfirmation(numberToDelete: number): angular.IPromise<any> {
    let content = 'Are you sure you want to remove ' + numberToDelete + ' selected ';
    if (numberToDelete > 1) {
      content += 'users?';
    } else {
      content += 'user?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove users', content, 'Delete', 'Cancel');
  }

  /**
   * Update users array
   */
  updateUsers(): void {
    // update users array
    this.users.length = 0;
    this.usersMap.forEach((user: any) => {
      this.users.push(user);
    });

    this.cheListHelper.setList(this.users, 'id');
  }

  /**
   * Ask for loading the users page in asynchronous way
   * @param pageKey {string} - the key of page
   */
  fetchUsersPage(pageKey: string): void {
    this.isLoading = true;
    let promise = this.cheUser.fetchUsersPage(pageKey);

    promise.then(() => {
      this.isLoading = false;
      this.updateUsers();
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.updateUsers();
      } else {
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Update information failed.');
      }
    });
  }

  /**
   * Returns true if the next page is exist.
   * @returns {boolean}
   */
  hasNextPage(): boolean {
    return this.pagesInfo.currentPageNumber < this.pagesInfo.countOfPages;
  }

  /**
   * Returns true if the previous page is exist.
   * @returns {boolean}
   */
  hasPreviousPage(): boolean {
    return this.pagesInfo.currentPageNumber > 1;
  }

  /**
   * Returns true if we have more then one page.
   * @returns {boolean}
   */
  isPagination(): boolean {
    return this.pagesInfo.countOfPages > 1;
  }

  /**
   * Add a new user. Show the dialog
   * @param  event {MouseEvent} - the $event
   */
  showAddUserDialog(event: MouseEvent): void {
    this.$mdDialog.show({
      targetEvent: event,
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'AdminsAddUserController',
      controllerAs: 'adminsAddUserController',
      locals: {callbackController: this},
      templateUrl: 'app/admin/user-management/add-user/add-user.html'
    });
  }
}
