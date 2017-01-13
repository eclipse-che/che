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

/**
 * @ngdoc controller
 * @name workspace.details.controller:WorkspaceMachineConfigController
 * @description This class is handling the controller for machine config
 * @author Oleksii Kurinnyi
 */
export class WorkspaceMachineConfigController {
  $mdDialog: ng.material.IDialogService;
  $q: ng.IQService;
  $timeout: ng.ITimeoutService;
  lodash: _.LoDashStatic;

  timeoutPromise;

  environmentManager;
  machine;
  machineConfig;
  machinesList: any[];
  machineName: string;
  newDev: boolean;
  newRam: number;

  machineDevOnChange;
  machineConfigOnChange;
  machineNameOnChange;
  machineOnDelete;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, $q: ng.IQService, $scope: ng.IScope, $timeout: ng.ITimeoutService, lodash: _.LoDashStatic) {
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.$timeout = $timeout;
    this.lodash = lodash;

    this.timeoutPromise = null;
    $scope.$on('$destroy', () => {
      if (this.timeoutPromise) {
        $timeout.cancel(this.timeoutPromise);
      }
    });

    this.init();
  }

  /**
   * Sets initial values
   */
  init(): void {
    this.machine = this.lodash.find(this.machinesList, (machine: any) => {
      return machine.name === this.machineName;
    });

    this.machineConfig = {
      source: this.environmentManager.getSource(this.machine),
      isDev: this.environmentManager.isDev(this.machine),
      memoryLimitBytes: this.environmentManager.getMemoryLimit(this.machine),
      servers: this.environmentManager.getServers(this.machine),
      agents: this.environmentManager.getAgents(this.machine),
      canEditEnvVariables: this.environmentManager.canEditEnvVariables(this.machine),
      envVariables: this.environmentManager.getEnvVariables(this.machine),
      canRenameMachine: this.environmentManager.canRenameMachine(this.machine),
      canDeleteMachine: this.environmentManager.canDeleteMachine(this.machine)
    };

    this.newDev = this.machineConfig.isDev;

    this.newRam = this.machineConfig.memoryLimitBytes;
  }

  /**
   * Modifies agents list in order to add or remove 'ws-agent'
   */
  enableDev(): void {
    if (this.machineConfig.isDev === this.newDev) {
      return;
    }

    this.machineDevOnChange({name: this.machineName});
  }

  /**
   * Updates amount of RAM for machine after a delay
   * @param isFormValid {boolean}
   */
  updateRam(isFormValid: boolean): void {
    this.$timeout.cancel(this.timeoutPromise);

    if (!isFormValid || this.machineConfig.memoryLimitBytes === this.newRam) {
      return;
    }

    this.timeoutPromise = this.$timeout(() => {
      this.environmentManager.setMemoryLimit(this.machine, this.newRam);

      this.doUpdateConfig();
    }, 1000);
  }

  /**
   * Callback which is called in order to update list of servers
   * @returns {ng.IPromise<any>}
   */
  updateServers(): ng.IPromise<any> {
    this.environmentManager.setServers(this.machine, this.machineConfig.servers);
    return this.doUpdateConfig();
  }

  /**
   * Callback which is called in order to update list of agents
   * @returns {Promise}
   */
  updateAgents(): ng.IPromise<any> {
    this.environmentManager.setAgents(this.machine, this.machineConfig.agents);
    return this.doUpdateConfig();
  }

  /**
   * Callback which is called in order to update list of environment variables
   * @returns {Promise}
   */
  updateEnvVariables(): void {
    this.environmentManager.setEnvVariables(this.machine, this.machineConfig.envVariables);
    this.doUpdateConfig();
    this.init();
  }

  /**
   * Calls parent controller's callback to update machine config
   * @returns {ng.IPromise<any>}
   */
  doUpdateConfig(): ng.IPromise<any> {
    return this.machineConfigOnChange();
  }

  /**
   * Show dialog to edit machine name
   * @param $event {MouseEvent}
   */
  showEditDialog($event: MouseEvent): void {
    let machinesNames = Object.keys(this.machinesList);

    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'EditMachineNameDialogController',
      controllerAs: 'editMachineNameDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        name: this.machineName,
        machinesNames: machinesNames,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/environments/machine-config/edit-machine-name-dialog/edit-machine-name-dialog.html'
    });
  }

  /**
   * Updates machine name
   * @param newMachineName {string} new machine name
   */
  updateMachineName(newMachineName: string): ng.IPromise<any> {
    if (this.machineName === newMachineName) {
      let defer = this.$q.defer();
      defer.resolve();
      return defer.promise;
    }

    return this.machineNameOnChange({
      oldName: this.machineName,
      newName: newMachineName
    });
  }

  /**
   * Deletes machine
   */
  deleteMachine(): void {
    this.showDeleteConfirmation().then(() => {
      this.machineOnDelete({
        name: this.machineName
      });
      this.init();
    });
  }

  /**
   * Show confirmation popup before machine to delete
   * @returns {ng.IPromise<any>}
   */
  showDeleteConfirmation(): ng.IPromise<any> {
    let confirmTitle = 'Would you like to delete this machine?';
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove machine')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }
}
