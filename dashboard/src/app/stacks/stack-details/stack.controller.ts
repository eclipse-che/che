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

/**
 * Controller for stack management - creation or edit.
 *
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class StackController {
  GENERAL_SCOPE: string = 'general';
  ADVANCED_SCOPE: string = 'advanced';
  $log: ng.ILogService;
  $filter: ng.IFilterService;
  $timeout: ng.ITimeoutService;
  $location: ng.ILocationService;
  $mdDialog: ng.material.IDialogService;
  cheStack: CheStack;
  cheNotification: CheNotification;
  loading: boolean;
  isLoading: boolean;
  isCreation: boolean;
  isStackChange: boolean;
  stackId: string;
  stackName: string;
  stackJson: string;
  invalidStack: string;
  stackTags: Array<string>;
  stack: any;
  copyStack: any;
  editorOptions: any;
  machinesViewStatus: any;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($route, $location, $log, $filter, cheStack, $mdDialog, cheNotification, $timeout) {
    this.$location = $location;
    this.$log = $log;
    this.$filter = $filter;
    this.cheStack = cheStack;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;
    this.$timeout = $timeout;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: false,
      matchBrackets: true,
      mode: 'application/json',
      onLoad: (editor) => {
        this.$timeout(() => {
          editor.refresh();
        }, 1000);
      }
    };

    this.machinesViewStatus = {};
    //Checking creation mode:
    this.isCreation = $route.current.params.stackId === 'create';
    this.copyStack = {};
    this.stackTags = [];

    if(!this.cheStack.getStacks().length) {
      this.loading = true;
      this.cheStack.fetchStacks().finally(()=>{
        this.loading = false;
      });
    }

    if (this.isCreation) {
      this.stack = this.getNewStackTemplate();
      this.stackTags = angular.copy(this.stack.tags);
      this.stackName = angular.copy(this.stack.name);
    } else {
      this.stackId = $route.current.params.stackId;
      this.fetchStack();
    }
  }

  /**
   * Check if the name is unique.
   * @param name
   * @returns {boolean}
   */
  isUniqueName(name: string): boolean {
    if(this.copyStack && this.copyStack.name === name) {
      return true;
    }
    return this.cheStack.isUniqueName(name);
  }

  /**
   * Cancels stack's changes
   */
  cancelStackChanges() {
    if (!this.copyStack) {
      return;
    }
    if (this.isCreation) {
      this.$location.path('/stacks');
    }
    this.stack = angular.copy(this.copyStack);
    this.stackName = angular.copy(this.stack.name);

    if (this.stack.tags && this.stack.tags.isArray) {
      this.stackTags = angular.copy(this.stack.tags);
    } else {
      this.stackTags = [];
    }

    this.stackTags = this.stack.tags ? angular.copy(this.stack.tags) : [];
    this.updateJsonFromStack();
  }

  /**
   * Returns template for the new stack.
   *
   * @returns {{stack}} new stack template
   */
  getNewStackTemplate(): any {
    let stack: any = this.cheStack.getStackTemplate();
    this.stackName = stack.name;
    return stack;
  }

  /**
   * Fetch the stack details.
   */
  fetchStack(): void {
    this.loading = true;
    this.stack = this.cheStack.getStackById(this.stackId);

    if (this.stack) {
      this.loading = false;
      this.prepareStackData();
      return;
    }

    this.cheStack.fetchStack(this.stackId).then((stack) => {
      this.stack = stack;
      this.loading = false;
      this.prepareStackData();
    }, (error) => {
      if (error.status === 304) {
        this.loading = false;
        this.stack = this.cheStack.getStackById(this.stackId);
        this.prepareStackData();
      } else {
        this.$log.error(error);
        this.loading = false;
        this.invalidStack = error.statusText + error.status;
      }
    });
  }

  /**
   * Handle stack's tag adding.
   *
   * @param tag {string} stack's tag
   * @returns {string} tag if it is unique one, otherwise null
   */
  handleTagAdding(tag: string): string {
    //Prevents mentioning same tags twice:
    if (this.stackTags.indexOf(tag) > -1) {
      return null;
    }

    return tag;
  }

  /**
   * Reset stack's tags.
   */
  resetTags(): void {
    if (!this.stack || !this.stack.tags) {
      return;
    }
    this.stack.tags.length = 0;
    this.updateJsonFromStack();
  }

  /**
   * Updates stack name info.
   * @param isFormValid {Boolean} true if form is valid
   */
  updateStackName(isFormValid: boolean) {
    if (isFormValid === false) {
      return;
    }
    this.stack.name = this.stackName;
    this.updateJsonFromStack();
  }

  /**
   * Updates stack tags info.
   */
  updateStackTags() {
    if (!this.stack.tags) {
      this.stack.tags = [];
    }
    this.stack.tags = angular.copy(this.stackTags);
    this.updateJsonFromStack();
  }

  /**
   * Update stack's editor json from stack.
   */
  updateJsonFromStack(): void {
    this.isStackChange = !angular.equals(this.stack, this.copyStack);
    this.stackJson = angular.toJson(this.stack, true);
  }

  /**
   * Update stack from stack's editor json.
   */
  updateStackFromJson(): void {
    let stack: any;
    try {
      stack = angular.fromJson(this.stackJson);
    } catch (e) {
      this.isStackChange = false;
      return;
    }
    this.isStackChange = !angular.equals(stack, this.copyStack);
    if (this.isStackChange) {
      this.stack = stack;
      this.stackTags = !stack.tags ? [] : angular.copy(stack.tags);
      this.stackName = !stack.name ? '' : angular.copy(stack.name);
    }
  }

  /**
   * Prepare data to be displayed.
   */
  prepareStackData(): void {
    if (!this.stack.tags) {
      this.stack.tags = [];
    }
    this.stackTags = angular.copy(this.stack.tags);

    delete this.stack.links;
    this.stackName = angular.copy(this.stack.name);
    this.copyStack = angular.copy(this.stack);
    this.updateJsonFromStack();
  }

  /**
   * Saves stack configuration - creates new one or updates existing.
   */
  saveStack(): void {
    this.updateJsonFromStack();
    if (this.isCreation) {
      this.createStack();
      return;
    }
    this.cheStack.updateStack(this.stack.id, this.stackJson).then((stack) => {
      this.cheNotification.showInfo('Stack is successfully updated.');
      this.isLoading = false;
      this.stack = stack;
      this.prepareStackData();
    }, (error) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Update stack failed.');
      this.$log.error(error);
      this.stack = this.cheStack.getStackById(this.stackId);
      this.cancelStackChanges();
    });
  }

  /**
   * Creates new stack.
   */
  createStack(): void {
    this.cheStack.createStack(this.stackJson).then((stack) => {
      this.stack = stack;
      this.isLoading = false;
      this.cheStack.fetchStacks();
      this.prepareStackData();
      this.isCreation = false;
    }, (error) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Creation stack failed.');
      this.$log.error(error);
    });
  }

  /**
   * Deletes current stack if user confirms.
   */
  deleteStack(): void {
    let confirmTitle = 'Would you like to delete ' + this.stack.name + '?';

    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove stack')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    this.$mdDialog.show(confirm).then(() => {
      this.loading = true;
      this.cheStack.deleteStack(this.stack.id).then(() => {
        this.cheNotification.showInfo('Stack <b>' + this.stack.name + '</b> has been successfully removed.');
        this.$location.path('/stacks');
      }, (error) => {
        this.loading = false;
        let message = 'Failed to delete <b>' + this.stack.name + '</b> stack.' + (error && error.message) ? error.message : '';
        this.cheNotification.showError(message);
      });
    });
  }

}
