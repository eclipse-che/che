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
import {CheStack} from '../../../../components/api/che-stack.factory';
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';

/**
 * @ngdoc controller
 * @name workspaces.select-stack.controller:WorkspaceSelectStackController
 * @description This class is handling the controller for the 'select stack' widget.
 * @author Oleksii Orel
 */
export class WorkspaceSelectStackController {

  static $inject = ['$log', '$timeout', '$scope', 'lodash', 'cheStack', 'cheEnvironmentRegistry'];

  $log: ng.ILogService;
  $timeout: ng.ITimeoutService;
  $scope: ng.IScope;
  lodash: any;
  cheStack: CheStack;
  cheEnvironmentRegistry: CheEnvironmentRegistry;

  stacks: any[];
  readyToGoStack: any;
  stackLibraryUser: any;
  tabName: string;
  selectedTabIndex: number;

  recipeUrl: string;
  recipeScript: string;
  recipeFormat: string;

  workspaceStackOnChange: Function;
  environmentName: string;
  workspaceName: string;
  workspaceImportedRecipe: {
    type: string,
    content: string,
    location: string
  };

  tabs: string[];

  /**
   * Default constructor that is using resource
   */
  constructor($log: ng.ILogService, $timeout: ng.ITimeoutService, $scope: ng.IScope, lodash: any, cheStack: CheStack, cheEnvironmentRegistry: CheEnvironmentRegistry) {
    this.$log = $log;
    this.$timeout = $timeout;
    this.$scope = $scope;
    this.lodash = lodash;
    this.cheStack = cheStack;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;

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
          this.selectStack(findStack);
        }
      }
    });

    $scope.$watch(() => { return this.tabName; }, () => {
      if (!this.tabName) {
        return;
      }
      this.setSelectedTab();
    });

    $scope.$watch(() => { return this.workspaceImportedRecipe; }, () => {
      if (!this.workspaceImportedRecipe) {
        return;
      }
      this.initStackSelecter();
    }, true);
  }

  /**
   * Initialize stack selector widget.
   */
  initStackSelecter(): void {
    if (!this.workspaceImportedRecipe) {
      return;
    }

    let type = this.workspaceImportedRecipe.type || 'compose';
    if (type === 'dockerimage') {
      type = 'dockerfile';
      this.recipeScript = 'FROM ' + this.workspaceImportedRecipe.content;
      this.tabName = 'stack-authoring';
    } else  if (angular.isDefined(this.workspaceImportedRecipe.location)) {
      this.tabName = 'stack-import';
      this.recipeUrl = this.workspaceImportedRecipe.location;
    } else if (angular.isDefined(this.workspaceImportedRecipe.content)) {
      this.tabName = 'stack-authoring';
      this.recipeScript = this.workspaceImportedRecipe.content;
    }
    this.recipeFormat = type;

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
    if (tabName === 'ready-to-go') {
      this.selectStack(this.readyToGoStack);
      this.recipeScript = '';
      this.recipeUrl = '';
    } else if (tabName === 'stack-library') {
      this.selectStack(this.stackLibraryUser);
      this.recipeScript = '';
      this.recipeUrl = '';
    } else {
      if (tabName === 'stack-import') {
        this.recipeScript = '';
      } else if (tabName === 'stack-authoring') {
        this.recipeUrl = '';
      }
    }
    this.recipeFormat = '';
  }

  /**
   * Set current stack as selected
   * @param {che.IStack} stack
   */
  selectStack(stack: che.IStack): void {
    if (!stack) {
      return;
    }
    let workspaceConfig = angular.copy(stack.workspaceConfig);

    if (this.environmentName && this.environmentName !== workspaceConfig.defaultEnv) {
      workspaceConfig.environments[this.environmentName] = workspaceConfig.environments[workspaceConfig.defaultEnv];
      delete workspaceConfig.environments[workspaceConfig.defaultEnv];
      workspaceConfig.defaultEnv = this.environmentName;
    }
    if (this.workspaceName && this.workspaceName !== workspaceConfig.name) {
      workspaceConfig.name = this.workspaceName;
    }

    this.workspaceStackOnChange({config: workspaceConfig, stackId: stack ? stack.id : ''});
  }

  /**
   * Callback when stack import URL is changed or recipe content is changed.
   *
   * @param {string} tabName
   * @param {string} recipeFormat
   * @param {string} recipeUrl
   * @param {string} recipeScript
   */
  onRecipeChange(tabName: string, recipeFormat: string, recipeUrl: string, recipeScript: string): void {
    let stackTemplate  = this.cheStack.getStackTemplate(),
        defEnvName     = stackTemplate.workspaceConfig.defaultEnv,
        defEnvironment = stackTemplate.workspaceConfig.environments[defEnvName],
        recipe         = defEnvironment.recipe;

    if (tabName === 'stack-import') {
      if (this.recipeFormat === recipeFormat && this.recipeUrl === recipeUrl) {
        return;
      }
      recipe.location = recipeUrl;
      delete recipe.content;
    } else {
      if (this.recipeFormat === recipeFormat && this.recipeScript === recipeScript) {
        return;
      }
      recipe.content = recipeScript;
      delete recipe.location;

      // clean machines list in config
      if (recipeFormat === 'compose') {
        defEnvironment.machines = {};
      }
    }
    recipe.type = recipeFormat;
    recipe.contentType = recipeFormat === 'compose' ? 'application/x-yaml' : 'text/x-dockerfile';

    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipe.type);

    let machines    = environmentManager.getMachines(defEnvironment),
        environment = environmentManager.getEnvironment(defEnvironment, machines);
    stackTemplate.workspaceConfig.environments[defEnvName] = environment;

    this.selectStack(stackTemplate);
  }

}
