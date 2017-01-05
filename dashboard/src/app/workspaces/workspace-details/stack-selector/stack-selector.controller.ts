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
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';
import {StackSelectorScope} from './stack-selector-scope.enum';

/**
 * @ngdoc controller
 * @name workspaces.stack-selector.controller:StackSelector
 * @description This class is handling the controller of stack selector.
 * @author Oleksii Kurinnyi
 */

export class StackSelectorController {
  $filter: ng.IFilterService;
  $log: ng.ILogService;
  lodash: _.LoDashStatic;
  cheStack: CheStack;
  cheEnvironmentRegistry: CheEnvironmentRegistry;

  scope: Object = StackSelectorScope;
  showFilters: boolean = false;

  // filter by
  selectedScope: number = StackSelectorScope.ALL;
  selectedTags: string[] = [];
  searchString: string;
  stackOrderBy: string = 'name';

  stacks: che.IStack[] = [];
  stacksFiltered: che.IStack[] = [];
  hideStackList: boolean;
  environmentManagers: {
    [recipeType: string]: EnvironmentManager
  } = {};

  selectedStackId: string;
  stackIconLinks: {
    [stackId: string]: string
  } = {};
  stackMachines: {
    [stackId: string]: Array<{[machineProp: string]: string|number}>
  } = {};

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($filter: ng.IFilterService, $log: ng.ILogService, lodash: _.LoDashStatic, cheStack: CheStack, cheEnvironmentRegistry: CheEnvironmentRegistry) {
    this.$filter = $filter;
    this.$log = $log;
    this.lodash = lodash;
    this.cheStack = cheStack;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;

    this.fetchStacks().finally(() => {
      this.buildFilteredList();
    });
  }

  /**
   * Get list of available stacks.
   */
  fetchStacks(): ng.IPromise<any> {
    return this.cheStack.fetchStacks().then(() => {
      this.stacks = angular.copy(this.cheStack.getStacks());
      this.updateStacks();
    }, (error: any) => {
      if (error.status === 304) {
        this.stacks = angular.copy(this.cheStack.getStacks());
        this.updateStacks();
      } else {
        this.$log.error(error);
      }
    });
  }

  /**
   * For each stack get machines and for each machine cast memory limit to GB.
   * Get stack icons.
   */
  updateStacks(): void {
    this.stacks.forEach((stack: che.IStack) => {
      // get icon link
      let findLink = this.lodash.find(stack.links, (link: che.IStackLink) => {
        return link.rel === 'get icon link';
      });
      if (findLink) {
        this.stackIconLinks[stack.id] = findLink.href;
      }

      // get machines memory limits
      let defaultEnv = stack.workspaceConfig.defaultEnv,
          environment = stack.workspaceConfig.environments[defaultEnv],
          environmentManager = this.getEnvironmentManager(environment.recipe.type),
          machines = environmentManager.getMachines(environment);
      this.stackMachines[stack.id] = [];
      machines.forEach((machine: any) => {
        this.stackMachines[stack.id].push({
          name: machine.name,
          memoryLimitBytes: environmentManager.getMemoryLimit(machine)
        });
      });
    });
  }

  /**
   * Returns environment manager specified by recipe type.
   *
   * @param recipeType {string} recipe type
   * @return {EnvironmentManager}
   */
  getEnvironmentManager(recipeType: string): EnvironmentManager {
    if (!this.environmentManagers[recipeType]) {
      this.environmentManagers[recipeType] = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
    }

    return this.environmentManagers[recipeType];
  }

  /**
   * Set specified stack ID as selected.
   *
   * @param stackId {string} stack ID
   */
  selectStack(stackId: string): void {
    this.selectedStackId = stackId;
  }

  /**
   * Callback on search query has been changed.
   *
   * @param searchString {string}
   */
  searchChanged(searchString: string): void {
    this.searchString = searchString;
    this.buildFilteredList();
  }

  /**
   * Callback on scope has been changed.
   */
  scopeChanged(): void {
    this.buildFilteredList();
  }

  /**
   * Rebuild list of filtered and sorted stacks. Set selected stack if it's needed.
   */
  buildFilteredList(): void {
    this.stacksFiltered.length = 0;
    this.stacksFiltered = this.$filter('stackScopeFilter')(this.stacks, this.selectedScope, this.stackMachines);
    this.stacksFiltered = this.$filter('stackSearchFilter')(this.stacksFiltered, this.searchString);
    this.stacksFiltered = this.$filter('orderBy')(this.stacksFiltered, this.stackOrderBy);

    if (this.stacksFiltered.length === 0) {
      return;
    }

    // check if selected stack is shown or not
    let needSelectStack = this.lodash.every(this.stacksFiltered, (stack: che.IStack) => {
      return stack.id !== this.selectedStackId;
    });
    if (needSelectStack) {
      this.selectStack(this.stacksFiltered[0].id);
    }
  }

}
