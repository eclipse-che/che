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
import {CheStack} from '../../../../components/api/che-stack.factory';

/**
 * @ngdoc controller
 * @name workspaces.select-stack.controller:WorkspaceSelectStackController
 * @description This class is handling the controller for the 'select stack' widget.
 * @author Oleksii Orel
 */
export class WorkspaceSelectStackController {
  $timeout: ng.ITimeoutService;
  $scope: ng.IScope;
  lodash: any;

  stack: any;
  stacks: any[];
  readyToGoStack: any;
  stackLibraryUser: any;
  tabName: string;
  selectedTabIndex: number;

  recipeUrl: string;
  recipeScript: string;

  onTabChange: Function;
  onStackChange: Function;

  tabs: string[];

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout: ng.ITimeoutService, $scope: ng.IScope, lodash: any, cheStack: CheStack) {
    this.$timeout = $timeout;
    this.$scope = $scope;
    this.lodash = lodash;

    this.tabs = ['ready-to-go', 'stack-library', 'stack-import', 'stack-authoring'];
    this.setSelectedTab();

    this.stacks = cheStack.getStacks();
    if (this.stacks.length) {
      this.$scope.$emit('create-project-stacks:initialized');
    } else {
      cheStack.fetchStacks().then(() => {
        this.$scope.$emit('create-project-stacks:initialized');
      });
    }

    $scope.$on('event:selectStackId', (event: ng.IAngularEvent, data: any) => {
      event.stopPropagation();
      let findStack = this.lodash.find(this.stacks, (stack: any) => {
        return stack.id === data.stackId;
      });
      if (findStack) {
        if (data.tabName === 'ready-to-go') {
          this.readyToGoStack = findStack;
        } else if (data.tabName === 'stack-library') {
          this.stackLibraryUser = findStack;
        }
        if (this.tabName === data.tabName) {
          this.onStackSelect(findStack);
        }
      }
    });

    $scope.$watch(() => { return this.tabName; }, () => {
      if (!this.tabName) {
        return;
      }
      this.setSelectedTab();
    });
  }

  setSelectedTab(): void {
    this.selectedTabIndex = this.tabs.indexOf(this.tabName) !== -1
      ? this.tabs.indexOf(this.tabName)
      : 0;
  }

  /**
   * Callback when tab has been change
   * @param tabName {string} the select tab name
   */
  setStackTab(tabName: string): void {
    this.tabName = tabName;
    this.onTabChange({tabName: tabName});

    if (tabName === 'ready-to-go') {
      this.onStackSelect(this.readyToGoStack);
      this.recipeScript = null;
      this.recipeUrl = null;
      return;
    } else if (tabName === 'stack-library') {
      this.onStackSelect(this.stackLibraryUser);
      this.recipeScript = null;
      this.recipeUrl = null;
      return;
    } else {
      if (tabName === 'stack-import') {
        if (this.recipeUrl) {
          return;
        }
        this.recipeScript = null;
      } else if (tabName === 'stack-authoring') {
        if (this.recipeScript) {
          return;
        }
        this.recipeUrl = null;
      }
    }
    this.onStackSelect(null);
  }

  /**
   * Callback when stack has been select
   * @param stack
   */
  onStackSelect(stack: any): void {
    this.stack = angular.copy(stack);
    this.onStackChange({stack: this.stack});
  }
}
