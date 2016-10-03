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

/**
 * @ngdoc controller
 * @name workspace.details.controller:WorkspaceEnvironmentsController
 * @description This class is handling the controller for details of workspace : section environments
 * @author Oleksii Kurinnyi
 */
export class WorkspaceEnvironmentsController {
  cheEnvironmentRegistry;
  environmentManager;

  editorOptions;

  workspaceConfig;
  environment;
  environmentName: string;
  newEnvironmentName: string;
  recipeType: string;
  machines: Array<any>;
  machinesViewStatus;

  environmentOnChange;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope, $timeout, cheEnvironmentRegistry) {
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: false,
      readOnly: true,
      gutters: [],
      onLoad: (editor) => {
        $timeout(() => {
          editor.refresh();
        }, 1000);
      }
    };

    $scope.$watch(() => {
      return this.workspaceConfig.environments;
    }, () => {
      this.init();
    });
  }

  /**
   * Sets initial values
   */
  init() {
    this.newEnvironmentName = this.environmentName;
    this.environment = this.workspaceConfig.environments[this.environmentName];

    this.recipeType = this.environment.recipe.type;
    this.environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(this.recipeType);

    this.editorOptions.mode = this.environmentManager.editorMode;

    this.machines = this.environmentManager.getMachines(this.environment);

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
  isUnique(name) {
    return name === this.environmentName || !this.workspaceConfig.environments[name];
  }

  /**
   * Updates name of environment
   * @param isFormValid {boolean}
   */
  updateEnvironmentName(isFormValid) {
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

  changeMachineDev(machineName) {
    if (!machineName) {
      return;
    }

    // remove ws-agent from machine which is the dev machine now
    this.machines.forEach((machine) => {
      if (this.environmentManager.isDev(machine)) {
        this.environmentManager.setDev(machine, false);
      }
    });

    let machine = this.machines.find(machine => {
      return machine.name === machineName;
    });

    // add ws-agent to current machine agents list
    this.environmentManager.setDev(machine, true);

    let newEnvironment = this.environmentManager.getEnvironment(this.environment, this.machines);
    this.workspaceConfig.environments[this.environmentName] = newEnvironment;
    this.environment = newEnvironment;

    return this.doUpdateEnvironments().then(() => {
      this.init();
    });
  }

  /**
   * Callback which is called in order to update environment config
   * @returns {Promise}
   */
  updateEnvironmentConfig() {
    let newEnvironment = this.environmentManager.getEnvironment(this.environment, this.machines);
    this.workspaceConfig.environments[this.newEnvironmentName] = newEnvironment;
    this.environment = newEnvironment;
    return this.doUpdateEnvironments();
  }

  /**
   * Callback which is called in order to rename specified machine
   * @param oldName
   * @param newName
   * @returns {*}
   */
  updateMachineName(oldName, newName) {
    let newEnvironment = this.environmentManager.renameMachine(this.environment, oldName, newName);
    this.workspaceConfig.environments[this.newEnvironmentName] = newEnvironment;

    this.machinesViewStatus[this.newEnvironmentName][newName] = this.machinesViewStatus[this.newEnvironmentName][oldName];
    delete this.machinesViewStatus[this.newEnvironmentName][oldName];

    return this.doUpdateEnvironments().then(() => {
      this.init();
    })
  }

  /**
   * Callback which is called in order to delete specified machine
   * @param name
   * @returns {*}
   */
  deleteMachine(name) {
    let newEnvironment = this.environmentManager.deleteMachine(this.environment, name);
    this.workspaceConfig.environments[this.newEnvironmentName] = newEnvironment;
    return this.doUpdateEnvironments().then(() => {
      this.init();
    })
  }

  /**
   * Calls parent controller's callback to update environment
   * @returns {IPromise<TResult>|*|Promise.<TResult>}
   */
  doUpdateEnvironments() {
    return this.environmentOnChange();
  }

}
