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
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheRecipeService} from '../che-recipe.service';

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

  static $inject = ['$q', '$scope', '$timeout', '$mdDialog', 'cheEnvironmentRegistry', '$log', 'cheNotification', 'cheRecipeService'];

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
  workspaceRuntime: any;
  workspaceConfig: che.IWorkspaceConfig;
  environment: any;
  environmentName: string;
  newEnvironmentName: string;
  recipe: any;
  machines: any[];
  machinesViewStatus: any;
  devMachineName: string;

  environmentOnChange: Function;

  private $q: ng.IQService;
  /**
   * Logging service.
   */
  private $log: ng.ILogService;
  /**
   * Notification factory.
   */
  private cheNotification: CheNotification;
  /**
   * Environment recipe service.
   */
  private cheRecipeService: CheRecipeService;

  /**
   * Default constructor that is using resource injection
   */
  constructor($q: ng.IQService, $scope: ng.IScope, $timeout: ng.ITimeoutService, $mdDialog: ng.material.IDialogService,
    cheEnvironmentRegistry: CheEnvironmentRegistry, $log: ng.ILogService, cheNotification: CheNotification, cheRecipeService: CheRecipeService) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.$log = $log;
    this.cheNotification = cheNotification;
    this.cheRecipeService = cheRecipeService;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: false,
      readOnly: true,
      gutters: [],
      onLoad: (editor: any) => {
        $timeout(() => {
          editor.refresh();
        }, 500);
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
      this.devMachineName = null;
      delete this.machinesViewStatus[this.environmentName];
      return;
    }

    const recipeType = this.recipe.type;
    this.environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
    if (!this.environmentManager) {
      const errorMessage = `Unsupported recipe type '${recipeType}'`;
      this.$log.error(errorMessage);
      this.cheNotification.showError(errorMessage);
      return;
    }

    this.editorOptions.mode = this.environmentManager.editorMode;

    this.machines = this.environmentManager.getMachines(this.environment, this.workspaceRuntime);
    this.devMachineName = this.getDevMachineName();

    if (!this.machinesViewStatus[this.environmentName]) {
      this.machinesViewStatus[this.environmentName] = {};
    }
  }

  /**
   * Returns true if the recipe type is scalable.
   *
   * @returns {boolean}
   */
  isMultiMachine(): boolean {
    return this.cheRecipeService.isScalable(this.environment.recipe);
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
    if (!environment) {
      return;
    }

    if (environment.recipe && environment.recipe.type === 'compose') {
      let recipeType = environment.recipe.type,
        environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
      let machines: any = environmentManager.getMachines(environment);
      machines.forEach((machine: any) => {
        if (!machine.attributes.memoryLimitBytes || machine.attributes.memoryLimitBytes < MIN_WORKSPACE_RAM || machine.attributes.memoryLimitBytes > MAX_WORKSPACE_RAM) {
          environmentManager.setMemoryLimit(machine, DEFAULT_WORKSPACE_RAM);
        }
      });

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
   * Shows dialog to add a new one machine in to environment.
   */
  addMachine(): void {
    this.$mdDialog.show({
      controller: 'EditMachineDialogController',
      controllerAs: 'editMachineDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        environment: this.workspaceConfig.environments[this.environmentName],
        onChange: (environment: che.IWorkspaceEnvironment) => {
          this.workspaceConfig.environments[this.environmentName] = environment;
          this.setEnvironments(this.workspaceConfig.environments);
        }
      },
      templateUrl: 'app/workspaces/workspace-details/workspace-machines/edit-machine-dialog/edit-machine-dialog.html'
    });
  }

  /**
   * Gets location URL
   *
   * @returns {string}
   */
  getLocationUrl(): string {
    let url: string = '';
    if (this.environment && this.environment.recipe && this.environment.recipe.location && /^https?:\/\//i.test(this.environment.recipe.location)) {
      url = this.environment.recipe.location;
    }
    return url;
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
