/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';
import {CheEnvironmentRegistry} from '../../../../../components/api/environment/che-environment-registry.factory';
import {EnvironmentManager} from '../../../../../components/api/environment/environment-manager';
import {IEnvironmentManagerMachine} from '../../../../../components/api/environment/environment-manager-machine';
import {StackValidationService} from '../../../../stacks/stack-details/stack-validation.service';
import {IComposeRecipe} from '../../../../../components/api/environment/compose-parser';


/**
 * @ngdoc controller
 * @name environments.controller:ditMachineDialogController
 * @description This class is handling the controller for a dialog box about adding a new machine.
 * @author Oleksii Orel
 */
export class EditMachineDialogController {
  errors: Array<string> = [];
  private $mdDialog: ng.material.IDialogService;
  private stackValidationService: StackValidationService;
  private machineRAM: number;
  private machineRecipeScript: string;
  private machine: IEnvironmentManagerMachine;
  private copyRecipe: Object;
  private cheEnvironmentRegistry: CheEnvironmentRegistry;
  private environmentManager: EnvironmentManager;
  private isAdd: boolean;
  private machineName: string;
  private usedMachinesNames: Array<string>;
  private environment: che.IWorkspaceEnvironment;
  private editorMode: string;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, cheEnvironmentRegistry: CheEnvironmentRegistry, stackValidationService: StackValidationService) {
    this.$mdDialog = $mdDialog;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.stackValidationService = stackValidationService;

    this.isAdd = angular.isUndefined(this.machineName);
    this.usedMachinesNames = Object.keys(this.environment.machines).filter((machineName: string) => {
      return this.isAdd || machineName !== this.machineName;
    });

    this.environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(this.environment.recipe.type);

    this.editorMode = this.isCompose() ? this.environmentManager.editorMode : 'application/json';

    if (this.isAdd) {
      if (!this.isCompose()) {
        // we can add a new machine in case with compose only
        this.cancel();
        return;
      }
      this.machine = this.environmentManager.createNewDefaultMachine(this.environment);
      this.machineName = angular.copy(this.machine.name);
    } else {
      this.machine = angular.copy(this.environmentManager.getMachines(this.environment).find((machine: IEnvironmentManagerMachine) => {
        return machine.name === this.machineName;
      }));
    }
    if (this.machine) {
      this.copyRecipe = angular.copy(this.machine.recipe);
      this._parseMachineRecipe();
      this.machineRAM = this.isCompose() && this.machine.recipe.mem_limit ? parseInt(this.machine.recipe.mem_limit, 10) : 0;
    }
  }

  /**
   * Update machine RAM.
   */
  updateMachineRAM(): void {
    if (!this.isCompose() || !this.machineRAM) {
      return;
    }
    this.machine.recipe.mem_limit = this.machineRAM;
    this._parseMachineRecipe();
  }

  /**
   * Return true if changed or add a new one.
   *
   * @returns {boolean}
   */
  isChange(): boolean {
    return this.isAdd || this.machineName !== this.machine.name || !angular.equals(this.machine.recipe, this.copyRecipe);
  }

  /**
   * Returns true if the environment's recipe type is compose.
   *
   * @returns {boolean}
   */
  isCompose(): boolean {
    return this.environmentManager.type === this.stackValidationService.COMPOSE;
  }

  /**
   * Check if recipe is valid.
   * @returns {che.IValidation}
   */
  isRecipeValid(): che.IValidation {
    const recipeValidation = this._stringifyMachineRecipe();
    if (!recipeValidation.isValid) {
      return recipeValidation;
    }
    let recipe: che.IRecipe;
    if (this.isCompose()) {
      const recipeServices = jsyaml.load(this.environment.recipe.content);
      recipeServices.services[this.machineName] = this.machine.recipe;
      recipe = angular.copy(this.environment.recipe);
      recipe.content = jsyaml.safeDump(recipeServices, {'indent': 1});
    } else {
      recipe = this.machine.recipe;
    }

    return this.stackValidationService.getRecipeValidation(recipe);
  }

  /**
   * Check if the machine name is unique.
   * @param name: string
   * @returns {boolean}
   */
  isUnique(name: string): boolean {
    return this.usedMachinesNames.indexOf(name) === -1;
  }

  /**
   * It will hide the dialog box.
   */
  cancel(): void {
    this.$mdDialog.cancel();
  }

  /**
   * Update machine.
   */
  updateMachine(): void {
    this._stringifyMachineRecipe();
    if (this.isCompose()) {
      if (this.isAdd) {
        this.environmentManager.addMachine(this.environment, this.machine);
      } else {
        const mem_limit = 'mem_limit';
        if (this.copyRecipe && this.copyRecipe[mem_limit] !== this.machine.recipe[mem_limit]) {
          const machine = this.environment.machines[this.machine.name];
          if (machine && machine.attributes && machine.attributes.memoryLimitBytes) {
            delete  machine.attributes.memoryLimitBytes;
          }
        }
        const recipe: IComposeRecipe = jsyaml.load(this.environment.recipe.content);
        recipe.services[this.machine.name] = this.machine.recipe;
        this.environment.recipe.content = jsyaml.safeDump(recipe, {'indent': 1});
      }
    } else {
      this.environment.recipe = this.machine.recipe;
    }
    if (!angular.equals(this.machineName, this.machine.name)) {
      const environment = this.environmentManager.renameMachine(this.environment, this.machine.name, this.machineName);
      const machines = this.environmentManager.getMachines(environment).map((machine: IEnvironmentManagerMachine) => {
        if (machine.name === this.machineName) {
          machine.recipe = this.machine.recipe;
          machine.attributes = this.machine.attributes;
        }
        return machine;
      });
      this.environment = this.environmentManager.getEnvironment(environment, machines);
    }

    this.$mdDialog.hide();
  }

  _parseMachineRecipe(): void {
    if (this.isCompose()) {
      this.machineRecipeScript = jsyaml.safeDump(this.machine.recipe, {'indent': 1});
    } else {
      this.machineRecipeScript = angular.toJson(this.machine.recipe, true);
    }
  }

  _stringifyMachineRecipe(): che.IValidation {
    try {
      if (this.isCompose()) {
        this.machine.recipe = jsyaml.load(this.machineRecipeScript);
        if (this.machine.recipe.mem_limit) {
          this.machineRAM = this.machine.recipe.mem_limit;
        }
      } else {
        this.machine.recipe = angular.fromJson(this.machineRecipeScript);
      }
      return {isValid: true, errors: []};
    } catch (error) {
      return {isValid: false, errors: [error.toString()]};
    }
  }
}
