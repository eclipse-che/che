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
  private isKubernetes: boolean;
  private machineName: string;
  private usedMachinesNames: Array<string>;
  private environment: che.IWorkspaceEnvironment;
  private previousStateEnvironment: che.IWorkspaceEnvironment;
  private currentStateEnvironment: che.IWorkspaceEnvironment;
  private originEnvironment: che.IWorkspaceEnvironment;
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
    if (!this.environment) {
      return;
    }
    this.originEnvironment = angular.copy(this.environment);
    this.deepFreeze(this.originEnvironment);
    this.previousStateEnvironment = angular.copy(this.originEnvironment);
    this.currentStateEnvironment = angular.copy(this.originEnvironment);
    this.isKubernetes = this.cheRecipeService.isKubernetes(this.currentStateEnvironment.recipe);
    this.usedMachinesNames = Object.keys(this.currentStateEnvironment.machines).filter((machineName: string) => {
      return this.isAdd || machineName !== this.machineName;
    });

    this.environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(cheRecipeService.getRecipeType(this.currentStateEnvironment.recipe));
    if (!this.environmentManager) {
      return;
    }
    this.isEditorReadOnly = CheRecipeTypes.getValues().indexOf(this.environmentManager.type) === -1;
    this.editorMode = this.environmentManager.editorMode;

    if (this.isAdd) {
      if (!cheRecipeService.isScalable(this.currentStateEnvironment.recipe)) {
        // we can add a new machine in case with scalable type of recipes only
        return;
      }
      this.machine = this.environmentManager.createMachine(this.currentStateEnvironment);
      this.currentStateEnvironment = this.environmentManager.addMachine(this.currentStateEnvironment, this.machine);
    } else {
      const machines = this.environmentManager.getMachines(this.currentStateEnvironment);
      this.machine = angular.copy(machines.find((machine: IEnvironmentManagerMachine) => {
        return machine.name === this.machineName;
      }));
    }

    if (!this.machine || !this.machine.recipe) {
      return;
    }
    this.machineName = this.environmentManager.getMachineName(this.machine);
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
    // update environment's machines
    const machines = this.environmentManager.getMachines(this.currentStateEnvironment).map((machine: IEnvironmentManagerMachine) => {
      return machine.name === this.machine.name ? this.machine : machine;
    });
    this.currentStateEnvironment = this.environmentManager.getEnvironment(this.currentStateEnvironment, machines);
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
    if (this.isKubernetes) {
      return !angular.equals(this.machine.recipe.spec, this.originMachine.recipe.spec);
    }
    return !angular.equals(this.machine.recipe, this.originMachine.recipe);
  }

  /**
   * Update machine's name if it change.
   * @param {string} name
   * @param {boolean} isValid
   */
  onNameChange(name: string, isValid: boolean): void {
    if (!isValid) {
      return;
    }
    const oldMachineName = this.isAdd ? this.machine.name : this.getFullName(this.machineName);
    const machineName = this.getFullName(name);
    const environment = this.environmentManager.renameMachine(this.currentStateEnvironment, oldMachineName, machineName);
    const machines = this.environmentManager.getMachines(environment);
    const machineIndex = machines.findIndex((machine: IEnvironmentManagerMachine) => {
      return machine.name === machineName;
    });
    if (machineIndex === -1) {
      return;
    }
    this.machine.recipe = machines[machineIndex].recipe;
    this.currentStateEnvironment = this.environmentManager.getEnvironment(environment, machines);
    this.stringifyMachineRecipe();
    this.machine.name = this.getFullName(name);
  }

  /**
   * Check if recipe is valid.
   * @returns {che.IValidation}
   */
  isRecipeValid(): che.IValidation {
    try {
      this.machine.recipe = this.environmentManager.parseMachineRecipe(this.machineRecipeScript);
      if (this.isKubernetes) {
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
    if (angular.isFunction(this.onChange) && !angular.equals(this.previousStateEnvironment, this.originEnvironment)) {
      this.onChange(angular.copy(this.originEnvironment));
    }
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
      this.onChange(this.currentStateEnvironment);
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
        this.machineName = newMachineName;
      }
      // checks memory limit changes
      this.checkMemoryLimitChanges();
      // update environment's machines
      const machines = this.environmentManager.getMachines(this.currentStateEnvironment).map((machine: IEnvironmentManagerMachine) => {
        return machine.name === this.machine.name ? this.machine : machine;
      });
      this.currentStateEnvironment = this.environmentManager.getEnvironment(this.currentStateEnvironment, machines);
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
    if (!this.cheRecipeService.isScalable(this.previousStateEnvironment.recipe)) {
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
    if (this.cheRecipeService.isOpenshift(this.currentStateEnvironment.recipe)) {
      // check critical changes for openshift
      if (this.isAdd) {
        this.currentStateEnvironment = angular.copy(this.previousStateEnvironment);
        this.currentStateEnvironment = this.environmentManager.addMachine(this.currentStateEnvironment, this.machine);
      } else {
        if (!angular.equals(this.getOpenshiftMachinePod(this.originMachine.recipe), this.getOpenshiftMachinePod(this.machine.recipe))) {
          this.currentStateEnvironment = angular.copy(this.previousStateEnvironment);
          this.currentStateEnvironment = this.environmentManager.deleteMachine(this.currentStateEnvironment, this.originMachine.name);
          this.currentStateEnvironment = this.environmentManager.addMachine(this.currentStateEnvironment, this.machine);
          const name = this.environmentManager.getMachineName(this.machine);
          if (!this.currentStateEnvironment.machines[this.getFullName(name)]) {
            this.machineName = name;
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
    if (!machineRecipe || this.cheRecipeService.isOpenshift(this.previousStateEnvironment.recipe) || !machineRecipe.metadata) {
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

  /**
   * Recursively freeze each property which is of type object.
   * @param {Object} object
   * @returns {Object}
   */
  private deepFreeze(object: Object): Object {
    Object.getOwnPropertyNames(object).forEach((name: string) => {
      if (name.startsWith('__')){
        return;
      }
      let value = object[name];
      object[name] = value && typeof value === 'object' ? this.deepFreeze(value) : value;
    });
    return Object.freeze(object);
  }
}
