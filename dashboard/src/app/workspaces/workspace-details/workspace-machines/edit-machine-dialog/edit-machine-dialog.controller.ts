/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheEnvironmentRegistry} from '../../../../../components/api/environment/che-environment-registry.factory';
import {EnvironmentManager} from '../../../../../components/api/environment/environment-manager';
import {IEnvironmentManagerMachine} from '../../../../../components/api/environment/environment-manager-machine';
import {CheRecipeService} from '../../che-recipe.service';
import {IPodItem} from '../../../../../components/api/environment/kubernetes-machine-recipe-parser';
import {CheRecipeTypes} from '../../../../../components/api/recipe/che-recipe-types';


/**
 * @ngdoc controller
 * @name environments.controller:ditMachineDialogController
 * @description This class is handling the controller for a dialog box about adding a new machine.
 * @author Oleksii Orel
 */
export class EditMachineDialogController {

  static $inject = ['$mdDialog', 'cheEnvironmentRegistry', 'cheRecipeService'];

  errors: Array<string> = [];
  private $mdDialog: ng.material.IDialogService;
  private $log: ng.ILogService;
  private machineRAM: number;
  private machineRecipeScript: string;
  private machine: IEnvironmentManagerMachine;
  private originMachine: IEnvironmentManagerMachine;
  private cheEnvironmentRegistry: CheEnvironmentRegistry;
  private environmentManager: EnvironmentManager;
  private isAdd: boolean;
  private machineName: string;
  private usedMachinesNames: Array<string>;
  private environment: che.IWorkspaceEnvironment;
  private copyEnvironment: che.IWorkspaceEnvironment;
  private editorMode: string;
  private isEditorReadOnly: boolean;

  /**
   * Environment recipe service.
   */
  private cheRecipeService: CheRecipeService;
  /**
   * Callback which is called when workspace is changed.
   */
  private onChange: (environment: che.IWorkspaceEnvironment) => void;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService,
              cheEnvironmentRegistry: CheEnvironmentRegistry,
              cheRecipeService: CheRecipeService) {
    this.$mdDialog = $mdDialog;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.cheRecipeService = cheRecipeService;

    this.isAdd = angular.isUndefined(this.machineName);
    this.copyEnvironment = angular.copy(this.environment);
    if (!this.copyEnvironment) {
      return;
    }
    this.usedMachinesNames = Object.keys(this.copyEnvironment.machines).filter((machineName: string) => {
      return this.isAdd || machineName !== this.machineName;
    });

    this.environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(cheRecipeService.getRecipeType(this.copyEnvironment.recipe));
    if (!this.environmentManager) {
      return;
    }
    this.isEditorReadOnly = CheRecipeTypes.getValues().indexOf(this.environmentManager.type) === -1;
    this.editorMode = this.environmentManager.editorMode;

    if (this.isAdd) {
      if (!cheRecipeService.isScalable(this.copyEnvironment.recipe)) {
        // we can add a new machine in case with scalable type of recipes only
        return;
      }
      this.machine = this.environmentManager.createMachine(this.copyEnvironment);
      this.copyEnvironment = this.environmentManager.addMachine(this.copyEnvironment, this.machine);
    } else {
      const machines = this.environmentManager.getMachines(this.copyEnvironment);
      this.machine = angular.copy(machines.find((machine: IEnvironmentManagerMachine) => {
        return machine.name === this.machineName;
      }));
    }

    if (!this.machine || !this.machine.recipe) {
      return;
    }
    this.machineName = angular.copy(this.environmentManager.getMachineName(this.machine));
    this.machineRAM = this.environmentManager.getMemoryLimit(this.machine);
    // update memory limit
    this.environmentManager.setMemoryLimit(this.machine, this.machineRAM);
    this.originMachine = angular.copy(this.machine);
    if (!this.updateMachineRAM()) {
      this.stringifyMachineRecipe();
    }
  }

  /**
   * Updates machine RAM.
   */
  updateMachineRAM(): void {
    if (!this.machineRAM) {
      return;
    }
    this.environmentManager.setMemoryLimit(this.machine, this.machineRAM);
    this.stringifyMachineRecipe();
  }

  /**
   * Returns true if changed or add a new one.
   * @returns {boolean}
   */
  isChange(): boolean {
    if (this.isAdd) {
      return true;
    }
    const machineName = this.environmentManager.getMachineName(this.originMachine);
    if (this.machineName !== machineName) {
      return true;
    }
    if (this.machineRAM !== this.environmentManager.getMemoryLimit(this.originMachine)) {
      return true;
    }
    return !angular.equals(this.machine.recipe, this.originMachine.recipe);
  }

  /**
   * Update machine's name if it change.
   * @param {string} name
   */
  onNameChange(name: string): void {
    this.machineName = name;
    const machineName = this.getFullName(name);
    const oldEnvironment = this.isAdd ? this.copyEnvironment : this.environment;
    const oldMachineName = this.isAdd ? this.machine.name : this.originMachine.name;
    const environment = this.environmentManager.renameMachine(oldEnvironment, oldMachineName, machineName);
    const machines = this.environmentManager.getMachines(environment);
    const machineIndex = machines.findIndex((machine: IEnvironmentManagerMachine) => {
      return machine.name === machineName;
    });
    if (machineIndex === -1) {
      return;
    }
    this.machine.recipe = machines[machineIndex].recipe;
    this.copyEnvironment = this.environmentManager.getEnvironment(environment, machines);
    this.stringifyMachineRecipe();
  }

  /**
   * Check if recipe is valid.
   * @returns {che.IValidation}
   */
  isRecipeValid(): che.IValidation {
    try {
      this.machine.recipe = this.environmentManager.parseMachineRecipe(this.machineRecipeScript);
      if (this.cheRecipeService.isOpenshift(this.copyEnvironment.recipe)) {
        const newPod = this.machine.recipe.metadata.name;
        const oldPod = this.originMachine.recipe.metadata.name;
        if (newPod !== oldPod && this.usedMachinesNames.map((name: string) => {
            return name.split(/\//)[0];
          }).indexOf(newPod) !== -1) {
          return {isValid: false, errors: [`Pod's name is required.`]};
        }
      }
      return {isValid: true, errors: []};
    } catch (error) {
      return {isValid: false, errors: [error.toString()]};
    }
  }

  /**
   * Check if the machine name is unique.
   * @param {string} name
   * @returns {boolean}
   */
  isUnique(name: string): boolean {
    return this.usedMachinesNames.indexOf(this.getFullName(name)) === -1;
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
    if (!this.isRecipeValid().isValid) {
      return;
    }
    if (angular.isFunction(this.onChange)) {
      this.onChange(this.copyEnvironment);
    }
    this.$mdDialog.hide();
  }

  /**
   * Parse machine recipe.
   */
  parseMachineRecipe(): void {
    try {
      this.machine.recipe = this.environmentManager.parseMachineRecipe(this.machineRecipeScript);
      // checks critical recipe changes
      this.checkCriticalRecipeChanges();
      // checks machine name changes
      const newMachineName = this.environmentManager.getMachineName(this.machine);
      if (this.machineName !== newMachineName) {
        this.onNameChange(newMachineName);
      }
      // checks memory limit changes
      this.checkMemoryLimitChanges();
      // update environment's machines
      const machines = this.environmentManager.getMachines(this.copyEnvironment).map((machine: IEnvironmentManagerMachine) => {
        return machine.name === this.machine.name ? this.machine : machine;
      });
      this.copyEnvironment = this.environmentManager.getEnvironment(this.copyEnvironment, machines);
    } catch (e) {
      this.$log.error('Cannot stringify machine\'s recipe, error: ', e);
    }
  }

  /**
   * Stringify machine recipe.
   */
  private stringifyMachineRecipe(): void {
    try {
      this.machineRecipeScript = this.environmentManager.stringifyRecipe(this.machine.recipe);
    } catch (e) {
      this.$log.error('Cannot parse machine\'s recipe, error: ', e);
    }
  }

  /**
   * Gets full name.
   * @param {string} name
   * @returns {string}
   */
  private getFullName(name: string): string {
    if (!this.originMachine.name) {
      return name;
    }
    const oldName = this.environmentManager.getMachineName(this.originMachine);
    return this.originMachine.name.replace(new RegExp(oldName + '$'), name);
  }

  /**
   * Checks memory limit changes.
   */
  private checkMemoryLimitChanges(): void {
    // check recipe RAM limit
    if (!this.cheRecipeService.isScalable(this.environment.recipe)) {
      this.machineRAM = this.environmentManager.getMemoryLimit(this.machine);
    } else {
      const copyMachine = angular.copy(this.machine);
      if (copyMachine.attributes && copyMachine.attributes.memoryLimitBytes) {
        delete copyMachine.attributes.memoryLimitBytes;
        const machineRAM = this.environmentManager.getMemoryLimit(copyMachine);
        if (machineRAM > 0) {
          this.machineRAM = machineRAM;
        }
      }
    }
    // update RAM limit
    this.environmentManager.setMemoryLimit(this.machine, this.machineRAM);
  }

  /**
   * Checks critical recipe changes.
   */
  private checkCriticalRecipeChanges(): void {
    if (this.cheRecipeService.isOpenshift(this.copyEnvironment.recipe)) {
      // check critical changes for openshift
      if (this.isAdd) {
        this.copyEnvironment = angular.copy(this.environment);
        this.copyEnvironment = this.environmentManager.addMachine(this.copyEnvironment, this.machine);
      } else {
        if (!angular.equals(this.getOpenshiftMachinePod(this.originMachine.recipe), this.getOpenshiftMachinePod(this.machine.recipe))) {
          this.copyEnvironment = angular.copy(this.environment);
          this.copyEnvironment = this.environmentManager.deleteMachine(this.copyEnvironment, this.originMachine.name);
          this.copyEnvironment = this.environmentManager.addMachine(this.copyEnvironment, this.machine);
          const name = this.environmentManager.getMachineName(this.machine);
          if (!this.copyEnvironment.machines[this.getFullName(name)]) {
            this.onNameChange(name);
          }
        }
      }
    }
  }

  /**
   * Gets empty pod from openshift machine recipe.
   * @param {IPodItem} machineRecipe
   * @returns {IPodItem}
   */
  private getOpenshiftMachinePod(machineRecipe: IPodItem): IPodItem {
    if (!machineRecipe || this.cheRecipeService.isOpenshift(this.environment.recipe) || !machineRecipe.metadata) {
      return machineRecipe;
    }
    const pod = angular.copy(machineRecipe);
    delete pod.spec;
    if (!angular.isArray(machineRecipe.metadata.annotations)) {
      return pod;
    }
    // remove container's name annotations
    Object.keys(machineRecipe.metadata.annotations).forEach((annotation: string) => {
      if (annotation.startsWith('org.eclipse.che.container')) {
        delete pod.metadata.annotations[annotation];
      }
    });
    return pod;
  }
}
