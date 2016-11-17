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
import {WorkspaceEnvironmentsController} from '../environments.controller';

/**
 * @ngdoc controller
 * @name environments.controller:AddMachineDialogController
 * @description This class is handling the controller for a dialog box about adding a new machine.
 * @author Oleksii Orel
 */
export class AddMachineDialogController {
  $mdDialog: ng.material.IDialogService;
  $timeout: ng.ITimeoutService;
  callbackController: WorkspaceEnvironmentsController;
  machineRAM: number;
  machineName: string;
  environmentKey: string;
  machineRecipeScript: string;
  usedMachinesName: Array<string>;
  machine: Object;
  machineRecipe: Object;
  editorOptions: {
    lineWrapping: boolean,
    lineNumbers: boolean,
    matchBrackets: boolean,
    mode: string,
    onLoad: Function };
  environments: che.IWorkspaceEnvironments;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, $timeout: ng.ITimeoutService) {
    this.$mdDialog = $mdDialog;
    this.$timeout = $timeout;

    this.usedMachinesName = [];
    angular.forEach(this.environments, (environment: any) => {
      angular.forEach(environment.machines, (machine: Object, machineKey: string) => {
        this.usedMachinesName.push(machineKey);
      });
    });

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: true,
      matchBrackets: true,
      mode: 'compose',
      onLoad: (editor: any) => {
        this.$timeout(() => {
          editor.refresh();
        }, 500);
      }
    };

    this.setDefaultData();
  }

  /**
   * Set default data.
   */
  setDefaultData(): void {
    this.machine = {
      'agents': [
        'org.eclipse.che.terminal', 'org.eclipse.che.ssh'
      ],
      'servers': {},
      'attributes': {}
    };
    this.machineRecipeScript = 'new-machine:\n  image: codenvy/ubuntu_jdk8\n  mem_limit: 2147483648\n';
    this.updateMachineRecipe();
    if (this.isUnique(this.machineName)) {
      return;
    }
    for (let pos: number = 1; pos < 1000; pos++) {
      if (this.isUnique(this.machineName + pos.toString())) {
        this.machineName += pos.toString();
        break;
      }
    }
    this.updateMachineName(true);
  }

  /**
   * Update machine recipe.
   */
  updateMachineRecipe(): void {
    this.machineRecipe = jsyaml.load(this.machineRecipeScript);
    if (!this.machineRecipe) {
      return;
    }
    let keys: Array<string> = Object.keys(this.machineRecipe);
    if (keys.length === 0) {
      return;
    }
    this.machineName = keys[0];
    this.machineRAM = this.machineRecipe[keys[0]].mem_limit;
  }

  /**
   * Update machine RAM.
   */
  updateMachineRAM() {
    this.machineRecipe[Object.keys(this.machineRecipe)[0]].mem_limit = this.machineRAM;
    this.machineRecipeScript = jsyaml.safeDump(this.machineRecipe, {'indent': 1});
  }

  /**
   * Update machine name.
   * @param isValid: boolean
   */
  updateMachineName(isValid: boolean): void {
    if (!isValid) {
      return;
    }
    this.machineRecipe = jsyaml.load(this.machineRecipeScript);
    if (!this.machineRecipe) {
      return;
    }
    let machines: Array<string> = Object.keys(this.machineRecipe);
    if (machines.length > 0 && machines[0] !== this.machineName) {
      this.machineRecipe[this.machineName] = angular.copy(this.machineRecipe[machines[0]]);
      delete this.machineRecipe[machines[0]];
    }
    this.machineRecipeScript = jsyaml.safeDump(this.machineRecipe, {'indent': 1});
  }

  /**
   * Check if recipe is valid.
   * @returns {boolean}
   */
  isRecipeValid(): boolean {
    if (!this.machineRecipe) {
      return false;
    }
    let machines: Array<string> = Object.keys(this.machineRecipe);
    if (!machines || machines.length !== 1) {
      return false;
    }
    let environment: any = this.environments[this.environmentKey];
    if (!environment.recipe || environment.recipe.type !== 'compose') {
      return false;
    }
    return this.machineRecipe[machines[0]].image || this.machineRecipe[machines[0]].build;
  }

  /**
   * Check if the machine name is unique.
   * @param name: string
   * @returns {boolean}
   */
  isUnique(name: string): boolean {
    return this.usedMachinesName.indexOf(name) === -1;
  }

  /**
   * It will hide the dialog box.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Add a new machine.
   */
  addMachine(): void {
    let environments: {[envName: string]: any} = angular.copy(this.environments);
    let environment: any = environments[this.environmentKey];
    let machines: any = environment.machines;
    machines[this.machineName] = this.machine;
    let recipeServices: any = jsyaml.load(environment.recipe.content);
    angular.extend(recipeServices.services, this.machineRecipe);
    environment.recipe.content = jsyaml.safeDump(recipeServices, {'indent': 1});
    this.callbackController.setEnvironments(environments);
    this.hide();
  }
}
