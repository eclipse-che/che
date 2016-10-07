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
  $log: ng.ILogService;
  $filter: ng.IFilterService;
  $timeout: ng.ITimeoutService;
  $location: ng.ILocationService;
  $mdDialog: ng.material.IDialogService;

  loading: boolean;
  isLoading: boolean;
  isCreation: boolean;

  stackId: string;
  stackName: string;
  stackJson: string;
  invalidStack: string;

  stack: any;
  editorOptions: any;
  changesPromise: ng.IPromise;
  machinesViewStatus: any;

  cheStack: CheStack;
  cheNotification: CheNotification;

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
    if (this.isCreation) {
      this.stack = this.getNewStackTemplate();
    } else {
      this.stackId = $route.current.params.stackId;
      this.fetchStack();
    }
  }

  /**
   * Returns template for the new stack.
   *
   * @returns {{stack}} new stack template
   */
  getNewStackTemplate(): any {
    this.stackName = 'New Stack';
    let stack: any = {};
    stack.name = this.stackName;
    stack.description = '';
    stack.source = {};
    stack.source.origin = '';
    stack.source.type = '';
    stack.components = [];
    stack.tags = [];
    stack.workspaceConfig = {};
    stack.scope = 'advanced';
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
    if (this.stack.tags.includes(tag)) {
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
   * Update stack's editor json from stack.
   */
  updateJsonFromStack(): void {
    this.stackJson = angular.toJson(this.stack, true);
  }

  /**
   * Update stack from stack's editor json.
   */
  updateStackFromJson(): void {
    this.stack = angular.fromJson(this.stackJson);
    this.stackName = angular.copy(this.stack.name);
  }

  /**
   * Prepare data to be displayed.
   */
  prepareStackData(): void {
    if (!this.stack.tags) {
      this.stack.tags = [];
    }

    delete this.stack.links;
    this.stackName = angular.copy(this.stack.name);
    this.updateJsonFromStack();
  }

  /**
   * Updates stack info.
   * @param isFormValid {Boolean} true if form is valid
   */
  updateStack(isFormValid: boolean) {
    if (this.isCreation) {
      this.stack.name = this.stackName;
      this.updateJsonFromStack();
      return;
    }

    if (isFormValid === false || this.stack.name === this.stackName) {
      this.updateJsonFromStack();
      if (this.changesPromise) {
        this.$timeout.cancel(this.changesPromise);
      }
    } else {
      this.stack.name = this.stackName;
      this.saveStack();
    }
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
    if (this.changesPromise) {
      this.$timeout.cancel(this.changesPromise);
    }
    this.changesPromise = this.$timeout(() => {
      this.cheStack.updateStack(this.stack.id, this.stackJson).then(() => {
        this.cheNotification.showInfo('Stack is successfully updated.');
        this.isLoading = false;
        this.cheStack.fetchStacks();
      }, (error) => {
        this.isLoading = false;
        this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Update stack failed.');
        this.$log.error(error);
        this.stack = this.cheStack.getStackById(this.stackId);
        this.prepareStackData();
      });
    }, 1000);
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
        let message = 'Failed to delete <b>' + this.stack.name + '</b> stack.' + (error && error.message) ? error.message : "";
        this.cheNotification.showError(message);
      });
    });
  }

}
