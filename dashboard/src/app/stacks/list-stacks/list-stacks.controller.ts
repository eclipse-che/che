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
import {CheStack} from "../../../components/api/che-stack.factory";
import {CheNotification} from "../../../components/notification/che-notification.factory";
import {CheProfile} from "../../../components/api/che-profile.factory";

/**
 * @ngdoc controller
 * @name stacks.list.controller:ListStacksCtrl
 * @description This class is handling the controller for listing the stacks
 * @author Ann Shumilova
 */
export class ListStacksController {
  cheStack: CheStack;
  cheNotification: CheNotification;
  $log: ng.ILogService;
  $mdDialog: ng.material.IDialogService;
  $q: ng.IQService;
  stackFilter: Object;
  stackSelectionState: Object;
  stacks: Array<any>;
  stackOrderBy: string;
  userId: string;
  state: string;
  isAllSelected: boolean;
  isNoSelected: boolean;
  loading: boolean;
  profile: any;
  lodash: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheStack: CheStack, cheProfile: CheProfile, $log: ng.ILogService, $mdDialog: ng.material.IDialogService, cheNotification: CheNotification, $rootScope: ng.IRootScopeService, lodash: any, $q: ng.IQService) {
    this.cheStack = cheStack;
    this.$log = $log;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;
    this.lodash = lodash;
    this.$q = $q;

    ($rootScope as any).showIDE = false;

    this.stackFilter = {name: ''};
    this.stackOrderBy = 'stack.name';

    this.stackSelectionState = {};
    this.isAllSelected = false;
    this.isNoSelected = true;

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
   * Gets the list of stacks.
   */
  getStacks(): void {
    this.loading = true;
    this.updateSelectionState();

    let promise = this.cheStack.fetchStacks();
    promise.then(() => {
        this.loading = false;
        this.stacks = this.cheStack.getStacks();
      },
      (error) => {
        if (error.status === 304) {
          this.stacks = this.cheStack.getStacks();
        }
        this.state = 'error';
        this.loading = false;
      });
  }

  /**
   * Performs confirmation and deletion of pointed stack.
   *
   * @param stack stack to delete
   */
  deleteStack(stack: any): void {
    let confirmationPromise: ng.IPromise<any> = this.confirmStacksDeletion(1, stack.name);
    confirmationPromise.then(() => {
      this.loading = true;
      this.cheStack.deleteStack(stack.id).then(() => {
        delete this.stackSelectionState[stack.id];
        this.cheNotification.showInfo('Stack ' + stack.name + ' has been successfully removed.');
        this.getStacks();
      }, (error) => {
        this.loading = false;
        let message = 'Failed to delete ' + stack.name + 'stack.' + (error && error.message) ? error.message : "";
        this.cheNotification.showError(message);
      });
    });

  }

  /**
   * Make a copy of pointed stack with new name.
   *
   * @param stack stack to make copy of
   */
  duplicateStack(stack: any): void {
    let newStack: any = angular.copy(stack);
    delete newStack.links;
    delete newStack.creator;
    delete newStack.id;
    newStack.name = this.generateStackName(stack.name + ' - Copy');
    this.loading = true;
    this.cheStack.createStack(newStack).then(() => {
      this.getStacks();
    }, (error) => {
      this.loading = false;
      let message = 'Failed to create ' + newStack.name + 'stack.' + (error && error.message) ? error.message : "";
      this.cheNotification.showError(message);
    });
  }

  /**
   * Generate new stack name - based on the provided one and existing.
   *
   * @param name
   * @returns {string}
   */
  generateStackName(name: string): string {
    if (this.stacks.length === 0) {
      return name;
    }
    let existingNames = this.lodash.pluck(this.stacks, 'name');

    if (existingNames.indexOf(name) < 0) {
      return name;
    }

    let generatedName = name;
    let counter = 1;
    while (existingNames.indexOf(generatedName) >= 0) {
      generatedName = name + counter++;
    }
    return generatedName;
  }

  /**
   * Returns whether all stacks are selected.
   *
   * @returns {boolean} <code>true</code> if all stacks are selected
   */
  isAllStacksSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * Returns whether there are no selected stacks.
   *
   * @returns {boolean} <code>true</code> if no stacks are selected
   */
  isNoStacksSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Make all own stacks selected.
   */
  selectAllStacks(): void {
    this.stackSelectionState = {};
    this.stacks.forEach((stack) => {
      if (stack.creator === this.userId) {
        this.stackSelectionState[stack.id] = true;
      }
    });
  }

  /**
   * Make all stacks deselected.
   */
  deselectAllStacks(): void {
    this.stacks.forEach((stack) => {
      if (this.stackSelectionState[stack.id]) {
        this.stackSelectionState[stack.id] = false;
      }
    });
  }

  /**
   * Change the state of the stacks selection,
   */
  changeSelectionState(): void {
    if (this.isAllSelected) {
      this.deselectAllStacks();
    } else {
      this.selectAllStacks();
    }
    this.updateSelectionState();
  }

  /**
   * Update stack selection state.
   */
  updateSelectionState(): void {
    this.isNoSelected = true;
    let keys: Array<string> = Object.keys(this.stackSelectionState);
    this.isAllSelected = keys.length > 0;
    keys.forEach((key) => {
      if (this.stackSelectionState[key]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });
  }

  /**
   * Delete all selected stacks.
   */
  deleteSelectedStacks(): void {
    let selectedStackIds: Array<any> = [];


    Object.keys(this.stackSelectionState).forEach((key) => {
      if (this.stackSelectionState[key]) {
        selectedStackIds.push(key);
      }
    });

    if (!selectedStackIds.length) {
      this.cheNotification.showError('No selected stacks.');
      return;
    }

    let confirmationPromise: ng.IPromise<any> = this.confirmStacksDeletion(selectedStackIds.length, '');
    confirmationPromise.then(() => {
      let deleteStackPromises = [];

      selectedStackIds.forEach((stackId) => {
        this.stackSelectionState[stackId] = false;
        deleteStackPromises.push(this.cheStack.deleteStack(stackId));
      });

      this.$q.all(deleteStackPromises).then(() => {
        this.cheNotification.showInfo('Selected stacks have been successfully removed.');
      })
        .catch(() => {
          this.cheNotification.showError('Failed to delete selected stack(s).');
        })
        .finally(() => {
          this.getStacks();
      });
    });
  }

  /**
   * Show confirm dialog for stacks deletion.
   *
   * @param numberToDelete number of stacks to be deleted
   * @param stackName name of stack to confirm (can be null)
   * @returns {*}
   */
  confirmStacksDeletion(numberToDelete: number, stackName: string): ng.IPromise<any> {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' stacks?';
    } else {
      confirmTitle += stackName ? stackName + '?' : 'this selected stack?';
    }
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove stacks')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }

}
