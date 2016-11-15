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
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';

/**
 * @ngdoc controller
 * @name workspace.details.controller:WorkspaceEnvironmentsController
 * @description This class is handling the controller for details of workspace : section environments
 * @author Oleksii Kurinnyi
 */

const MIN_WORKSPACE_RAM: number = Math.pow(1024, 3);
const MAX_WORKSPACE_RAM: number = 100 * MIN_WORKSPACE_RAM;
const DEFAULT_WORKSPACE_RAM: number = 2 * MIN_WORKSPACE_RAM;

export class WorkspaceEnvironmentsController {
  cheEnvironmentRegistry: CheEnvironmentRegistry;
  environmentManager: EnvironmentManager;
  $mdDialog: ng.material.IDialogService;

  editorOptions: {
    lineWrapping: boolean,
    lineNumbers: boolean,
    readOnly: boolean,
    mode?: string,
    gutters: any[],
    onLoad: Function
  };

  stackId: string;
  workspaceConfig: che.IWorkspaceConfig;
  environment: any;
  environmentName: string;
  newEnvironmentName: string;
  recipe: any;
  machines: any[];
  machinesViewStatus: any;
  devMachineName: string;

  environmentOnChange: Function;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope: ng.IScope, $timeout: ng.ITimeoutService, $mdDialog: ng.material.IDialogService, cheEnvironmentRegistry: CheEnvironmentRegistry) {
    this.$mdDialog = $mdDialog;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: false,
      readOnly: true,
      gutters: [],
      onLoad: (editor: any) => {
        $timeout(() => {
          editor.refresh();
        }, 1000);
      }
    };

    $scope.$watch(() => {
      return (this.workspaceConfig && this.workspaceConfig.environments);
    }, () => {
      if (this.workspaceConfig &&
        this.workspaceConfig.environments &&
        this.workspaceConfig.environments[this.environmentName]) {
        this.init();
      }
    });
  }

  /**
   * Sets initial values
   */
  init(): void {
    this.newEnvironmentName = this.environmentName;
    this.environment = this.workspaceConfig.environments[this.environmentName];

    this.recipe = this.environment.recipe;
    if (!this.recipe || !(this.recipe.content || this.recipe.location)) {
      this.machines = [];
      delete this.devMachineName;
      delete this.machinesViewStatus[this.environmentName];
      return;
    }

    this.environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(this.recipe.type);

    this.editorOptions.mode = this.environmentManager.editorMode;

    this.machines = this.environmentManager.getMachines(this.environment);
    this.devMachineName = this.getDevMachineName();

    if (!this.machinesViewStatus[this.environmentName]) {
      this.machinesViewStatus[this.environmentName] = {};
    }
  }

  /**
   * Returns true if environment name is unique
   *
   * @param name {string} environment name to validate
   * @returns {boolean}
   */
  isUnique(name: string): boolean {
    return name === this.environmentName || !this.workspaceConfig.environments || !this.workspaceConfig.environments[name];
  }

  /**
   * Updates name of environment
   * @param isFormValid {boolean}
   */
  updateEnvironmentName(isFormValid: boolean): void {
    if (!isFormValid || this.newEnvironmentName === this.environmentName) {
      return;
    }

    this.workspaceConfig.environments[this.newEnvironmentName] = this.environment;
    delete this.workspaceConfig.environments[this.environmentName];

    if (this.workspaceConfig.defaultEnv === this.environmentName) {
      this.workspaceConfig.defaultEnv = this.newEnvironmentName;
    }

    this.machinesViewStatus[this.newEnvironmentName] = this.machinesViewStatus[this.environmentName];

    this.environmentName = this.newEnvironmentName;

    this.doUpdateEnvironments();
  }

  /**
   * Returns name of machine which includes 'ws-agent'
   *
   * @returns {string} name of dev machine
   */
  getDevMachineName(): string {
    let devMachine: any = this.machines.find((machine: any) => {
      return this.environmentManager.isDev(machine);
    });

    return devMachine ? devMachine.name : '';
  }

  /**
   * Add 'ws-agent' to list of agents of specified machine and remove it from lists of agents of other machines.
   *
   * @param machineName
   * @returns {ng.IPromise<any>}
   */
  changeMachineDev(machineName: string): ng.IPromise<any> {
    if (!machineName) {
      return;
    }

    // remove ws-agent from machine which is the dev machine now
    this.machines.forEach((machine: any) => {
      if (this.environmentManager.isDev(machine)) {
        this.environmentManager.setDev(machine, false);
      }
    });

    let machine = this.machines.find((machine: any) => {
      return machine.name === machineName;
    });

    // add ws-agent to current machine agents list
    this.environmentManager.setDev(machine, true);

    let newEnvironment = this.environmentManager.getEnvironment(this.environment, this.machines);
    this.workspaceConfig.environments[this.environmentName] = newEnvironment;
    this.environment = newEnvironment;

    this.doUpdateEnvironments();
    this.init();
  }

  /**
   * Callback which is called in order to update environment config
   *
   * @returns {ng.IPromise<any>}
   */
  updateEnvironmentConfig(): ng.IPromise<any> {
    let newEnvironment = this.environmentManager.getEnvironment(this.environment, this.machines);
    this.workspaceConfig.environments[this.newEnvironmentName] = newEnvironment;
    this.environment = newEnvironment;
    return this.doUpdateEnvironments();
  }

  /**
   * Callback which is called in order to rename specified machine
   * @param oldName
   * @param newName
   *
   * @returns {ng.IPromise<any>}
   */
  updateMachineName(oldName: string, newName: string): void {
    let newEnvironment = this.environmentManager.renameMachine(this.environment, oldName, newName);
    this.workspaceConfig.environments[this.newEnvironmentName] = newEnvironment;

    this.machinesViewStatus[this.newEnvironmentName][newName] = this.machinesViewStatus[this.newEnvironmentName][oldName];
    delete this.machinesViewStatus[this.newEnvironmentName][oldName];

    this.doUpdateEnvironments();
    this.init();
  }

  /**
   * Callback which is called in order to delete specified machine
   *
   * @param name
   */
  deleteMachine(name: string): void {
    let newEnvironment = this.environmentManager.deleteMachine(this.environment, name);
    this.workspaceConfig.environments[this.newEnvironmentName] = newEnvironment;

    this.doUpdateEnvironments();
    this.init();
  }

  /**
   * Callback when stack has been changed.
   *
   * @param config {object} workspace config
   * @param stackId {string}
   */
  changeWorkspaceStack(config: any, stackId: string): void {
    this.stackId = stackId;
    this.workspaceConfig = config;

    if (!this.environmentName || this.environmentName !== config.defaultEnv) {
      this.environmentName = config.defaultEnv;
      this.newEnvironmentName = this.environmentName;
    }

    // for compose recipe
    // check if there are machines without memory limit
    let environment = this.workspaceConfig.environments[this.environmentName];
    if (environment.recipe && environment.recipe.type === 'compose') {
      let recipeType = environment.recipe.type,
        environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
      let machines: any = environmentManager.getMachines(environment);
      machines.forEach((machine: any) => {
        if (!machine.attributes.memoryLimitBytes || machine.attributes.memoryLimitBytes < MIN_WORKSPACE_RAM || machine.attributes.memoryLimitBytes > MAX_WORKSPACE_RAM) {
          environmentManager.setMemoryLimit(machine, DEFAULT_WORKSPACE_RAM);
        }
      });

      // if recipe contains only one machine
      // then this is the dev machine
      if (machines.length === 1) {
        environmentManager.setDev(machines[0], true);
      }

      this.workspaceConfig.environments[this.environmentName] = environmentManager.getEnvironment(environment, machines);
    }

    this.doUpdateEnvironments();
  }

  /**
   * Calls parent controller's callback to update environment
   *
   * @returns {ng.IPromise<any>}
   */
  doUpdateEnvironments(): ng.IPromise<any> {
    return this.environmentOnChange();
  }

  /**
   * Show dialog to add a new machine to config
   * @param $event
   */
  showAddMachineDialog($event: MouseEvent): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'AddMachineDialogController',
      controllerAs: 'addMachineDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        environmentKey: this.environmentName,
        environments: this.workspaceConfig.environments,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/environments/add-machine-dialog/add-machine-dialog.html'
    });
  }

  /**
   * Sets environments
   * @param environments: {[envName: string]: any}
   */
  setEnvironments(environments: {[envName: string]: any}) {
    this.workspaceConfig.environments = environments;
    this.doUpdateEnvironments();
    this.init();
  }
}
