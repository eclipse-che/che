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
import {CheStack} from '../../../components/api/che-stack.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheProfile} from '../../../components/api/che-profile.factory';
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';

/**
 * @ngdoc controller
 * @name stacks.list.controller:ListStacksCtrl
 * @description This class is handling the controller for listing the stacks
 * @author Ann Shumilova
 */
export class ListStacksController {

  static $inject = ['cheStack', 'cheProfile', '$log', '$mdDialog', 'cheNotification', '$rootScope', 'lodash', '$q', 'confirmDialogService', '$scope', 'cheListHelperFactory'];

  cheStack: CheStack;
  cheNotification: CheNotification;
  $log: ng.ILogService;
  $mdDialog: ng.material.IDialogService;
  $q: ng.IQService;
  stackFilter: {
    name: string
  };
  stacks: Array<any>;
  stackOrderBy: string;
  userId: string;
  state: string;
  loading: boolean;
  profile: any;
  lodash: any;

  private confirmDialogService: ConfirmDialogService;
  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor that is using resource
   */
  constructor(cheStack: CheStack, cheProfile: CheProfile, $log: ng.ILogService, $mdDialog: ng.material.IDialogService, cheNotification: CheNotification, $rootScope: ng.IRootScopeService, lodash: any, $q: ng.IQService, confirmDialogService: ConfirmDialogService, $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.cheStack = cheStack;
    this.$log = $log;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;
    this.lodash = lodash;
    this.$q = $q;
    this.confirmDialogService = confirmDialogService;

    ($rootScope as any).showIDE = false;

    this.stackFilter = {name: ''};
    this.stackOrderBy = 'stack.name';

    const helperId = 'list-stacks';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.stacks = [];

    this.profile = cheProfile.getProfile();
    if (this.profile.userId) {
      this.userId = this.profile.userId;
      this.getStacks();
    } else {
      this.profile.$promise.then(() => {
        this.userId = this.profile.userId ? this.profile.userId : undefined;
        this.getStacks();
      }, () => {
        this.userId = undefined;
      });
    }
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter stacks names.
   */
  onSearchChanged(str: string): void {
    this.stackFilter.name = str;
    this.cheListHelper.applyFilter('name', this.stackFilter);
  }

  /**
   * Gets the list of stacks.
   *
   * @return {IPromise<any>}
   */
  getStacks(): ng.IPromise<any> {
    this.loading = true;

    let promise = this.cheStack.fetchStacks();
    return promise.then(() => {
      this.loading = false;
      this.stacks = this.cheStack.getStacks();
    }, (error: any) => {
      if (error.status === 304) {
        this.stacks = this.cheStack.getStacks();
      } else {
        this.state = 'error';
      }
      this.loading = false;
    }).finally(() => {
      const isStackSelectable = (stack: che.IStack) => {
        return stack.creator === this.profile.userId;
      };
      this.cheListHelper.setList(this.stacks, 'id', isStackSelectable);
    });
  }

  /**
   * Show dialog for imported stack.
   * @param $event {MouseEvent}
   */
  showSelectStackRecipeDialog($event: MouseEvent): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'BuildStackController',
      controllerAs: 'buildStackController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        callbackController: this
      },
      templateUrl: 'app/stacks/list-stacks/build-stack/build-stack.html'
    });
  }

  /**
   * Performs confirmation and deletion of pointed stack.
   *
   * @param stack {che.IStack} stack to delete
   */
  deleteStack(stack: che.IStack): void {
    let confirmationPromise: ng.IPromise<any> = this.confirmStacksDeletion(1, stack.name);
    confirmationPromise.then(() => {
      this.loading = true;
      this.cheStack.deleteStack(stack.id).then(() => {
        delete this.cheListHelper.itemsSelectionStatus[stack.id];
        this.cheNotification.showInfo('Stack ' + stack.name + ' has been successfully removed.');
        return this.getStacks();
      }, (error: any) => {
        this.loading = false;
        let message = 'Failed to delete ' + stack.name + 'stack.' + (error && error.message) ? error.message : '';
        this.cheNotification.showError(message);
      }).finally(() => {
        this.cheListHelper.updateBulkSelectionStatus();
      });
    });

  }

  /**
   * Make a copy of pointed stack with new name.
   *
   * @param stack {che.IStack} stack to make copy of
   */
  duplicateStack(stack: che.IStack): void {
    let newStack: any = angular.copy(stack);
    delete newStack.links;
    delete newStack.creator;
    delete newStack.id;
    newStack.name = this.generateStackName(stack.name + '-copy');
    this.loading = true;
    this.cheStack.createStack(newStack).then(() => {
      return this.getStacks();
    }, (error: any) => {
      this.loading = false;
      let message = 'Failed to create ' + newStack.name + 'stack.' + (error && error.message) ? error.message : '';
      this.cheNotification.showError(message);
    }).finally(() => {
      this.cheListHelper.updateBulkSelectionStatus();
    });
  }

  /**
   * Generate new stack name - based on the provided one and existing.
   *
   * @param name
   * @returns {string}
   */
  generateStackName(name: string): string {
    /* tslint:disable */
    name += '-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4));
    /* tslint:enable */
    if (this.stacks.length === 0) {
      return name;
    }
    let existingNames = this.lodash.pluck(this.stacks, 'name');
    if (existingNames.indexOf(name) < 0) {
      return name;
    }
    let generatedName = name;
    let counter = 1;
    while (existingNames.indexOf(generatedName) >= 0 && counter < 1000) {
      generatedName = name + counter++;
    }

    return generatedName;
  }

  /**
   * Delete all selected stacks.
   */
  deleteSelectedStacks(): void {
    const selectedStacks = this.cheListHelper.getSelectedItems(),
          selectedStackIds = selectedStacks.map((stack: che.IStack) => {
            return stack.id;
          });

    if (!selectedStackIds.length) {
      this.cheNotification.showError('No selected stacks.');
      return;
    }

    const confirmationPromise: ng.IPromise<any> = this.confirmStacksDeletion(selectedStackIds.length, '');
    confirmationPromise.then(() => {
      const deleteStackPromises = [];

      selectedStackIds.forEach((stackId: string) => {
        this.cheListHelper.itemsSelectionStatus[stackId] = false;
        deleteStackPromises.push(this.cheStack.deleteStack(stackId));
      });

      this.$q.all(deleteStackPromises).then(() => {
        this.cheNotification.showInfo('Selected stacks have been successfully removed.');
      }).catch(() => {
        this.cheNotification.showError('Failed to delete selected stack(s).');
      }).finally(() => {
        this.getStacks();
      });
    });
  }

  /**
   * Show confirm dialog for stacks deletion.
   *
   * @param numberToDelete{number} number of stacks to be deleted
   * @param stackName{string} name of stack to confirm (can be null)
   * @returns {ng.IPromise<any>}
   */
  confirmStacksDeletion(numberToDelete: number, stackName: string): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' stacks?';
    } else {
      content += stackName ? stackName + '?' : 'this selected stack?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove stacks', content, 'Delete');
  }

}
